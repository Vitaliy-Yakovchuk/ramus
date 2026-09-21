# syntax=docker/dockerfile:1.7-labs
#
# Building Ramus in a container.
#
#   docker build --target artifacts --output out .   # the JAR alone, into ./out
#   docker build --target test .                     # run the tests
#   docker build -t ramus .                          # image ready to run the GUI
#
# Running the GUI needs X11 forwarded (see README, Building / Docker).

ARG JDK_IMAGE=eclipse-temurin:21-jdk-noble
ARG JRE_IMAGE=eclipse-temurin:21-jre-noble

# ---------------------------------------------------------------------------
# build - compile and shadowJar
# ---------------------------------------------------------------------------
FROM ${JDK_IMAGE} AS build

# GRADLE_USER_HOME inside the image: downloaded dependencies stay in the layer,
# so later builds go to the network only when the build scripts change.
ENV GRADLE_USER_HOME=/opt/gradle-home \
    GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.console=plain -Djava.awt.headless=true"

WORKDIR /src

# Layer 1: Gradle itself. The wrapper pins the version, not the image.
COPY gradlew ./
COPY gradle gradle
RUN ./gradlew --version

# Layer 2: dependencies. Only the build scripts are copied, so the layer is
# invalidated when the build description changes, not when the code does.
COPY --parents settings.gradle build.gradle */build.gradle ./
COPY gui-qualifier/libs gui-qualifier/libs
RUN ./gradlew :local-client:dependencies --configuration runtimeClasspath

# Layer 3: the code.
COPY . .
RUN ./gradlew :local-client:shadowJar

# ---------------------------------------------------------------------------
# test - unit tests, headless
#   docker build --target test .
# ---------------------------------------------------------------------------
FROM build AS test
RUN ./gradlew test

# ---------------------------------------------------------------------------
# artifacts - copy the JAR out to the host without running the container
#   docker build --target artifacts --output out .
# ---------------------------------------------------------------------------
FROM scratch AS artifacts
COPY --from=build /src/local-client/build/libs/ramus.jar /

# ---------------------------------------------------------------------------
# runtime - run the GUI (needs X11 forwarded)
# ---------------------------------------------------------------------------
FROM ${JRE_IMAGE} AS runtime

# The minimum java.desktop needs: Xlib and fonts. Without them Swing dies on
# startup.
RUN apt-get update \
    && apt-get install --no-install-recommends -y \
        libxext6 libxrender1 libxtst6 libxi6 libxft2 \
        fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# UID 1000 so that files in a mounted /work belong to the host user. In noble
# that UID is taken by the service user ubuntu, so it is freed first.
RUN userdel --remove ubuntu 2>/dev/null || true; \
    useradd --create-home --uid 1000 ramus
COPY --from=build /src/local-client/build/libs/ramus.jar /opt/ramus/ramus.jar

# Working directory for .rsf projects; mount your own: -v "$PWD:/work"
RUN install -d -o ramus -g ramus /work

USER ramus
WORKDIR /work

ENTRYPOINT ["java", "-jar", "/opt/ramus/ramus.jar"]
