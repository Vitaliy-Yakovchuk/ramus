# Збірка Ramus у Docker

Коротка відповідь: **так, збирати можна й варто** — збірка герметична, не залежить
від JDK на машині розробника. **Запускати** GUI в контейнері теж можна, але це
вже компроміс: Swing потребує проброшеного X11.

Усе описане нижче перевірено на Fedora 44, Docker 29.6, BuildKit.

---

## 1. Що є в репозиторії

| Файл            | Призначення                                                       |
| --------------- | ----------------------------------------------------------------- |
| `Dockerfile`    | багатостадійна збірка: `build` → `test` / `artifacts` / `runtime` |
| `.dockerignore` | звужує контекст із ~208 МБ до ~27 МБ                              |

Docker у проєкті вже використовувався й раніше: `dest/build-installer --docker`
збирає Windows-інсталятор через образ `cdrx/nsis`. Цей Dockerfile робить те саме
для основної збірки.

---

## 2. Три сценарії

### Отримати JAR, нічого не встановлюючи

```bash
docker build --target artifacts --output out .
# out/ramus.jar — 26 МБ, запускається будь-де: java -jar out/ramus.jar
```

Стадія `artifacts` побудована на `FROM scratch`, тож `--output` кладе на хост
рівно один файл, без образу й без запуску контейнера.

### Прогнати тести

```bash
docker build --target test .
```

### Зібрати образ із застосунком

```bash
docker build -t ramus .
```

Результат ~318 МБ: `eclipse-temurin:21-jre` + Xlib + шрифти + `ramus.jar`.

---

## 3. Запуск GUI

Найпростіший варіант — X11-сокет хоста плюс cookie авторизації. Просто
змонтувати `/tmp/.X11-unix` недостатньо: X-сервер відкине з'єднання
(`Authorization required, but no authorization protocol specified`).

```bash
# cookie з «розслабленим» полем сімʼї — інакше hostname контейнера не збігається
XAUTH=$(mktemp)
xauth nlist "$DISPLAY" | sed -e 's/^..../ffff/' | xauth -f "$XAUTH" nmerge -
chmod 644 "$XAUTH"

docker run --rm \
  -e DISPLAY="$DISPLAY" \
  -e XAUTHORITY=/tmp/.xauth \
  -v /tmp/.X11-unix:/tmp/.X11-unix:ro \
  -v "$XAUTH:/tmp/.xauth:ro" \
  -v "$PWD:/work" \
  ramus
```

Проєкти беруться з `/work`, тому поточний каталог хоста монтується саме туди.
Користувач у контейнері має UID 1000, отже створені файли належать вам.

Під Wayland (GNOME, KDE) працює через XWayland — `DISPLAY` при цьому вказує на
неї, окремих дій не потрібно.

Варіант без доступу до X-сервера хоста — додати в образ `xvfb` і VNC. Для
розробки це зайве: `./gradlew runLocal` на хості швидший і не має цих проблем.

---

## 4. Чому Dockerfile саме такий

**Три шари замість одного `COPY . .`.** Шари впорядковані за частотою змін:
wrapper → build-скрипти → код. Правка `.java` не інвалідує шар із
завантаженими залежностями, і повторна збірка не ходить у мережу.

```dockerfile
COPY gradlew ./ ; COPY gradle gradle       # дистрибутив Gradle
COPY --parents settings.gradle build.gradle */build.gradle ./
RUN ./gradlew :local-client:dependencies   # ~180 МБ залежностей
COPY . .                                   # код
```

`COPY --parents` (синтаксис `dockerfile:1.7-labs`) переносить `*/build.gradle`
28 модулів зі збереженням структури каталогів. Без нього довелось би виписувати
28 окремих `COPY`.

**`GRADLE_USER_HOME` всередині образу, а не `--mount=type=cache`.** Кеш-маунти
швидші локально, але не переживають CI, де кожен запуск — чиста машина.
Залежності в шарі образу кешуються звичайним layer cache, тобто скрізь однаково.

**Версію Gradle задає wrapper, а не базовий образ.** Тому базовий образ —
`eclipse-temurin:21-jdk`, а не `gradle:8.5`; єдине джерело правди про версію
складача лишається в репозиторії (`gradle/wrapper/gradle-wrapper.properties`).

**`-Dorg.gradle.daemon=false`.** Демон у контейнері, який живе рівно один
`RUN`, — марна пам'ять і зайва секунда на форк.

**Стадії `test` й `artifacts` окремі від `build`.** Тести не блокують видачу
JAR, а `artifacts` не тягне в результат ні JDK, ні кеш Gradle.

**Runtime не від root.** `USER ramus` з UID 1000 — і безпечніше, і файли в
змонтованому `/work` не стають root-овими.

**Базові образи закріплені в `ARG`.** Змінити JDK для експерименту:

```bash
docker build --build-arg JDK_IMAGE=eclipse-temurin:17-jdk-noble .
```

Для повністю відтворюваної збірки в релізному пайплайні варто прибити образи по
дайджесту (`eclipse-temurin:21-jdk-noble@sha256:…`), а не по тегу.

---

## 5. Тести: чому `-PskipGolden`

Стадія `test` запускає `./gradlew test -PskipGolden`.

`DiagramGoldenTest` малює кожну діаграму зразків у PNG і звіряє відбиток із
еталоном **попіксельно** (`TOLERANCE = 0.0`). Відбиток залежить від шрифту, у
який fontconfig розкладає логічний `Dialog`:

| Середовище                      | `Dialog` →                              |
| ------------------------------- | --------------------------------------- |
| Fedora 44 (хост)                | `NotoSans[wght].ttf`, variable, v132055 |
| `temurin:21-jdk` (Ubuntu noble) | `DejaVuSans.ttf`                        |
| те саме + `fonts-noto-core`     | `NotoSans-Regular.ttf`, static, v131334 |

Навіть після встановлення Noto в контейнер різниця лишається (0.04–3.06 при
допуску 0.00) — статичний і variable-накреслення тієї ж гарнітури різних версій
дають різні гліфи.

Це обмеження самого тесту, а не Docker: він однаково зламається на будь-якій
машині з іншим набором шрифтів. Зробити його переносним можна, поклавши
конкретний `.ttf` у ресурси тесту й малюючи саме ним замість логічного `Dialog`.
Поки цього немає, у контейнері знімки вимикаються, а на машині розробника
`./gradlew :storage-test:test` перевіряє їх як і раніше.

---

## 6. Чого Docker тут не вирішує

- **macOS DMG** (`:local-client:macDmg`) — `jpackage`, `sips`, `iconutil`
  працюють лише на macOS. Ці задачі й самі кидають `GradleException` на іншій ОС.
- **Windows-інсталятор** — уже має свій шлях через `dest/build-installer --docker`.
- **Розробка** — цикл «правка → перезапуск» у контейнері повільніший за
  `./gradlew runLocal` на хості. Контейнер тут для CI, релізів і збірки на
  чужій машині, не для щоденної роботи.
