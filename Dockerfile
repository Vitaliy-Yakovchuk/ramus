# syntax=docker/dockerfile:1.7-labs
#
# Збірка Ramus у контейнері.
#
#   docker build --target artifacts --output out .   # тільки JAR у ./out
#   docker build --target test .                     # прогнати тести
#   docker build -t ramus .                          # образ, готовий до запуску GUI
#
# Деталі й запуск GUI: docs/DOCKER.md

ARG JDK_IMAGE=eclipse-temurin:21-jdk-noble
ARG JRE_IMAGE=eclipse-temurin:21-jre-noble

# ---------------------------------------------------------------------------
# build — компіляція та shadowJar
# ---------------------------------------------------------------------------
FROM ${JDK_IMAGE} AS build

# GRADLE_USER_HOME усередині образу: завантажені залежності лишаються в шарі,
# тож наступні збірки не ходять у мережу, поки не змінились build-скрипти.
ENV GRADLE_USER_HOME=/opt/gradle-home \
    GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.console=plain -Djava.awt.headless=true"

WORKDIR /src

# Шар 1: сам Gradle. Версія прибита wrapper'ом, а не образом.
COPY gradlew ./
COPY gradle gradle
RUN ./gradlew --version

# Шар 2: залежності. Копіюємо лише build-скрипти — шар інвалідується
# тільки коли реально змінився опис збірки, а не код.
COPY --parents settings.gradle build.gradle */build.gradle ./
COPY gui-qualifier/libs gui-qualifier/libs
RUN ./gradlew :local-client:dependencies --configuration runtimeClasspath

# Шар 3: код.
COPY . .
RUN ./gradlew :local-client:shadowJar

# ---------------------------------------------------------------------------
# test — юніт-тести в headless-режимі
#   docker build --target test .
# ---------------------------------------------------------------------------
FROM build AS test
# -PskipGolden — еталонні знімки діаграм звірюються попіксельно й залежать від
# шрифтів машини, на якій знято еталон. Див. docs/DOCKER.md, розділ «Тести».
RUN ./gradlew test -PskipGolden

# ---------------------------------------------------------------------------
# artifacts — вивантаження JAR на хост без запуску контейнера
#   docker build --target artifacts --output out .
# ---------------------------------------------------------------------------
FROM scratch AS artifacts
COPY --from=build /src/local-client/build/libs/ramus.jar /

# ---------------------------------------------------------------------------
# runtime — запуск GUI (потрібен проброшений X11, див. docs/DOCKER.md)
# ---------------------------------------------------------------------------
FROM ${JRE_IMAGE} AS runtime

# Мінімум для java.desktop: Xlib, шрифти. Без них Swing падає на старті.
RUN apt-get update \
    && apt-get install --no-install-recommends -y \
        libxext6 libxrender1 libxtst6 libxi6 libxft2 \
        fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# UID 1000 — щоб файли в примонтованому /work належали користувачу хоста.
# В noble цей UID уже зайнятий службовим користувачем ubuntu, звільняємо його.
RUN userdel --remove ubuntu 2>/dev/null || true; \
    useradd --create-home --uid 1000 ramus
COPY --from=build /src/local-client/build/libs/ramus.jar /opt/ramus/ramus.jar

# Робочий каталог для .rsf-проєктів; монтуйте сюди свій: -v "$PWD:/work"
RUN install -d -o ramus -g ramus /work

USER ramus
WORKDIR /work

ENTRYPOINT ["java", "-jar", "/opt/ramus/ramus.jar"]
