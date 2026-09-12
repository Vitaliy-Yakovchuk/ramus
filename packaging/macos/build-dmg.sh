#!/usr/bin/env bash

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
    printf '\n%s\n' "Error: $*" >&2
    exit 1
}

note() {
    printf '%s\n' "$*"
}

usage() {
    cat <<'USAGE'
Builds Ramus.dmg — a self-contained image with Java inside.

    ./packaging/macos/build-dmg.sh

The build itself is :local-client:macDmg; this script adds what build.gradle
cannot see — the choice of JDK, the architecture, and the signature.

Options:
    --jdk PATH        use this JDK instead of the one found automatically
    --no-jlink        bundle the whole JDK instead of a trimmed jlink runtime
    --locales LIST    locale data to keep in the runtime (default en,uk,ru)
    --keep-quarantine leave the quarantine flag on the built DMG
    -h, --help        this help
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --jdk)
            [[ $# -ge 2 ]] || die "--jdk needs a path"
            jdk="$2"
            shift 2
            ;;
        --no-jlink)
            use_jlink=false
            shift
            ;;
        --locales)
            [[ $# -ge 2 ]] || die "--locales needs a list, for example en,uk,ru"
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
            die "unknown option \"$1\" (--help lists them)"
            ;;
    esac
done

[[ "$(uname -s)" == "Darwin" ]] ||
    die "a DMG can only be built on macOS: jpackage, iconutil and sips exist nowhere else.
With no Mac at hand use GitHub Actions — .github/workflows/macos-dmg.yml builds
the images for both architectures."

host_arch="$(uname -m)"

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
    die "no JDK ${MIN_JDK} or newer found.
Install one (for example: brew install --cask temurin@${MIN_JDK}) or name it:
    $0 --jdk /Library/Java/JavaVirtualMachines/temurin-${MIN_JDK}.jdk/Contents/Home"

[[ -x "${jdk}/bin/java" ]] || die "\"${jdk}\" has no bin/java"

for tool in jpackage jlink jdeps; do
    [[ -x "${jdk}/bin/${tool}" ]] ||
        die "\"${jdk}\" has no bin/${tool} — that is a JRE, not a full JDK"
done

jdk_version="$("${jdk}/bin/java" -XshowSettings:properties -version 2>&1 |
    awk -F'= ' '/java\.specification\.version/ { print $2; exit }')"
[[ -n "${jdk_version}" ]] || die "could not read the JDK version in \"${jdk}\""
(( ${jdk_version%%.*} >= MIN_JDK )) ||
    die "JDK ${MIN_JDK} or newer is required, and \"${jdk}\" is ${jdk_version}"

jdk_arch="$("${jdk}/bin/java" -XshowSettings:properties -version 2>&1 |
    awk -F'= ' '/os\.arch/ { print $2; exit }')"
case "${jdk_arch}" in
    aarch64) dmg_arch="arm64" ;;
    x86_64)  dmg_arch="x86_64" ;;
    *)       dmg_arch="${jdk_arch}" ;;
esac

if [[ "${host_arch}" == "arm64" && "${dmg_arch}" != "arm64" ]]; then
    note "Warning: the JDK is built for ${jdk_arch}, so the DMG will be for ${dmg_arch}."
    note "         Install an arm64 build of the JDK for a native image."
fi

note "JDK:           ${jdk} (${jdk_version}, ${jdk_arch})"
note "Architecture:  ${dmg_arch}"
note "Runtime:       $([[ "${use_jlink}" == true ]] && echo "jlink${locales:+, locales: ${locales}}" || echo "the full JDK")"
note ""

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
[[ -n "${dmg}" ]] || die "Gradle finished, but no DMG appeared in dest/macos"
dmg="$(cd "$(dirname "${dmg}")" && pwd)/$(basename "${dmg}")"

signature="unknown"
mount_point="$(mktemp -d)"
if hdiutil attach "${dmg}" -nobrowse -readonly -mountpoint "${mount_point}" >/dev/null 2>&1; then
    app="${mount_point}/Ramus.app"
    if [[ -d "${app}" ]]; then
        if codesign --verify --deep --strict "${app}" >/dev/null 2>&1; then
            signature="$(codesign -dv "${app}" 2>&1 |
                awk -F'=' '/^Authority/ { print $2; exit }')"
            [[ -n "${signature}" ]] || signature="ad-hoc"
        else
            signature="none"
        fi
    else
        signature="the image holds no Ramus.app"
    fi
    hdiutil detach "${mount_point}" >/dev/null 2>&1 || true
fi
rmdir "${mount_point}" 2>/dev/null || true

if [[ "${keep_quarantine}" == false ]]; then
    xattr -d com.apple.quarantine "${dmg}" 2>/dev/null || true
fi

size="$(du -h "${dmg}" | cut -f1)"

cat <<EOF

Done: ${dmg}
Size: ${size}, architecture: ${dmg_arch}, signature: ${signature}

To install, open the DMG and drag Ramus into Applications. There is no Java to
install — it is inside.

If the image travels to another Mac (email, a website, GitHub Releases),
Gatekeeper there will report that Ramus is damaged — not because it is, but
because the application carries no Apple Developer ID. What the person
installing it can do:

    xattr -dr com.apple.quarantine /Applications/Ramus.app

or open it once through the context menu: right-click Ramus, choose Open, then
Open again in the dialog.

EOF
