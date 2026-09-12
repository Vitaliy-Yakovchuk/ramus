#!/usr/bin/env bash
#
# Збирає Ramus.dmg — самодостатній образ із вбудованою Java.
#
#   ./packaging/macos/build-dmg.sh
#
# Скрипт перевіряє оточення перед збіркою, а не після: jpackage падає
# маловимовними помилками, і виявити бракуючий JDK за п'ять секунд краще, ніж
# за п'ять хвилин. Вся робота лишається за Gradle (:local-client:macDmg), тут
# лише те, чого build.gradle не бачить — вибір JDK, архітектура й підпис.
#
# Ключі:
#   --jdk PATH        конкретний JDK замість знайденого автоматично
#   --no-jlink        покласти в образ весь JDK, а не зменшений jlink-runtime
#   --locales LIST    мови, дані яких лишити в runtime (типово en,uk,ru)
#   --keep-quarantine не знімати карантин із зібраного DMG
#   -h, --help        ця довідка

set -euo pipefail

readonly MIN_JDK=21

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/../.." && pwd)"
readonly script_dir repo_root

jdk=""
use_jlink=true
locales=""
keep_quarantine=false

die() {
    printf '\n%s\n' "Помилка: $*" >&2
    exit 1
}

note() {
    printf '%s\n' "$*"
}

# Довідка — це сам заголовок файлу: два описи розходяться вже на другій правці.
usage() {
    awk 'NR > 2 { if (!/^#/) exit; sub(/^# ?/, ""); print }' "${BASH_SOURCE[0]}"
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --jdk)
            [[ $# -ge 2 ]] || die "--jdk потребує шляху"
            jdk="$2"
            shift 2
            ;;
        --no-jlink)
            use_jlink=false
            shift
            ;;
        --locales)
            [[ $# -ge 2 ]] || die "--locales потребує переліку, наприклад en,uk,ru"
            locales="$2"
            shift 2
            ;;
        --keep-quarantine)
            keep_quarantine=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            die "невідомий ключ «$1» (--help покаже перелік)"
            ;;
    esac
done

# --- оточення ---------------------------------------------------------------

[[ "$(uname -s)" == "Darwin" ]] ||
    die "DMG збирається лише на macOS: jpackage, iconutil і sips існують тільки тут.
Без Mac під рукою скористайтеся GitHub Actions — .github/workflows/macos-dmg.yml
збирає образи для обох архітектур."

host_arch="$(uname -m)"

# Порядок пошуку: явний ключ, змінна оточення, java_home, JAVA_HOME. Останній
# — навмисно останній: у ньому часто лежить JRE або старий JDK, успадкований
# від чужого інсталятора.
if [[ -z "${jdk}" ]]; then
    jdk="${PACKAGING_JAVA_HOME:-}"
fi
if [[ -z "${jdk}" && -x /usr/libexec/java_home ]]; then
    jdk="$(/usr/libexec/java_home -v "${MIN_JDK}+" 2>/dev/null || true)"
fi
if [[ -z "${jdk}" ]]; then
    jdk="${JAVA_HOME:-}"
fi

[[ -n "${jdk}" ]] ||
    die "не знайдено JDK ${MIN_JDK} або новіший.
Встановіть його (наприклад: brew install --cask temurin@${MIN_JDK}) або вкажіть явно:
    $0 --jdk /Library/Java/JavaVirtualMachines/temurin-${MIN_JDK}.jdk/Contents/Home"

[[ -x "${jdk}/bin/java" ]] || die "у «${jdk}» немає bin/java"

for tool in jpackage jlink jdeps; do
    [[ -x "${jdk}/bin/${tool}" ]] ||
        die "у «${jdk}» немає bin/${tool} — це JRE, а не повний JDK"
done

jdk_version="$("${jdk}/bin/java" -XshowSettings:properties -version 2>&1 |
    awk -F'= ' '/java\.specification\.version/ { print $2; exit }')"
[[ -n "${jdk_version}" ]] || die "не вдалося визначити версію JDK у «${jdk}»"
(( ${jdk_version%%.*} >= MIN_JDK )) ||
    die "потрібен JDK ${MIN_JDK} або новіший, а в «${jdk}» — ${jdk_version}"

jdk_arch="$("${jdk}/bin/java" -XshowSettings:properties -version 2>&1 |
    awk -F'= ' '/os\.arch/ { print $2; exit }')"
case "${jdk_arch}" in
    aarch64) dmg_arch="arm64" ;;
    x86_64)  dmg_arch="x86_64" ;;
    *)       dmg_arch="${jdk_arch}" ;;
esac

# Образ успадковує архітектуру JDK, а не машини: JDK для Intel під Rosetta
# збере DMG, який на Apple silicon працюватиме через емуляцію, і мовчки.
if [[ "${host_arch}" == "arm64" && "${dmg_arch}" != "arm64" ]]; then
    note "Увага: JDK зібрано під ${jdk_arch}, тож DMG вийде для ${dmg_arch}."
    note "      Для рідного образу поставте arm64-збірку JDK."
fi

note "JDK:          ${jdk} (${jdk_version}, ${jdk_arch})"
note "Архітектура:  ${dmg_arch}"
note "Runtime:      $([[ "${use_jlink}" == true ]] && echo "jlink${locales:+, мови: ${locales}}" || echo "повний JDK")"
note ""

# --- збірка -----------------------------------------------------------------

args=(":local-client:macDmg" "-PpackagingJavaHome=${jdk}")
[[ "${use_jlink}" == true ]] || args+=("-PpackagingUseJlink=false")
[[ -z "${locales}" ]] || args+=("-PpackagingLocales=${locales}")

cd "${repo_root}"
./gradlew "${args[@]}"

dmg=""
for candidate in dest/macos/*.dmg; do
    [[ -e "${candidate}" ]] || continue
    if [[ -z "${dmg}" || "${candidate}" -nt "${dmg}" ]]; then
        dmg="${candidate}"
    fi
done
[[ -n "${dmg}" ]] || die "Gradle відпрацював, але DMG у dest/macos не з'явився"
dmg="$(cd "$(dirname "${dmg}")" && pwd)/$(basename "${dmg}")"

# --- підпис -----------------------------------------------------------------

# Без сертифіката Developer ID jpackage підписує застосунок ad-hoc. Цього
# досить, щоб він запускався на Apple silicon, але не досить для Gatekeeper на
# чужій машині — тому нижче й друкується інструкція про карантин.
signature="невідомо"
mount_point="$(mktemp -d)"
if hdiutil attach "${dmg}" -nobrowse -readonly -mountpoint "${mount_point}" >/dev/null 2>&1; then
    app="${mount_point}/Ramus.app"
    if [[ -d "${app}" ]]; then
        if codesign --verify --deep --strict "${app}" >/dev/null 2>&1; then
            signature="$(codesign -dv "${app}" 2>&1 |
                awk -F'=' '/^Authority/ { print $2; exit }')"
            [[ -n "${signature}" ]] || signature="ad-hoc"
        else
            signature="немає"
        fi
    else
        signature="у образі немає Ramus.app"
    fi
    hdiutil detach "${mount_point}" >/dev/null 2>&1 || true
fi
rmdir "${mount_point}" 2>/dev/null || true

# Файл, щойно створений локально, карантину не має; прапорець лишається на
# тому, що завантажують. Знімаємо про всяк випадок, щоб свіжозібраний образ
# точно відкривався на цій машині.
if [[ "${keep_quarantine}" == false ]]; then
    xattr -d com.apple.quarantine "${dmg}" 2>/dev/null || true
fi

# --- підсумок ---------------------------------------------------------------

size="$(du -h "${dmg}" | cut -f1)"

cat <<EOF

Готово: ${dmg}
Розмір: ${size}, архітектура: ${dmg_arch}, підпис: ${signature}

Встановлення: відкрити DMG подвійним клацанням і перетягнути Ramus до
«Програм». Java встановлювати не треба — вона всередині.

Якщо образ передаватимуть на інший Mac (пошта, сайт, GitHub Releases),
Gatekeeper там скаже «Ramus пошкоджено» — не тому, що пошкоджено, а тому,
що застосунок не має сертифіката Apple Developer ID. Що робити тому, хто
встановлює:

    xattr -dr com.apple.quarantine /Applications/Ramus.app

або перший запуск через контекстне меню: правою кнопкою на Ramus → «Відкрити»
→ «Відкрити» у діалозі.

EOF
