<#
.SYNOPSIS
    Збирає інсталятор Ramus для Windows — із вбудованою Java.

.DESCRIPTION
    Скрипт лише перевіряє оточення й кличе Gradle: уся логіка пакування — в
    :local-client:winInstaller, спільна з macOS-збіркою. Перевірки тут тому,
    що jpackage без WiX або з JRE замість JDK падає повідомленнями, за якими
    причину не видно.

.PARAMETER Jdk
    Конкретний JDK замість знайденого автоматично.

.PARAMETER Type
    msi (типово) або exe.

.PARAMETER NoJlink
    Покласти в пакунок увесь JDK, а не зменшений jlink-runtime.

.PARAMETER Locales
    Мови, дані яких лишити в runtime (типово en,uk,ru).

.EXAMPLE
    .\packaging\windows\build-installer.ps1
#>

[CmdletBinding()]
param(
    [string] $Jdk,
    [ValidateSet('msi', 'exe')]
    [string] $Type = 'msi',
    [switch] $NoJlink,
    [string] $Locales
)

$ErrorActionPreference = 'Stop'
$MinJdk = 21

function Fail([string] $message) {
    Write-Host ''
    Write-Error $message
    exit 1
}

if (-not $IsWindows -and $PSVersionTable.PSEdition -eq 'Core') {
    Fail @'
Інсталятор збирається лише на Windows: jpackage вміє пакувати тільки під ту
систему, у якій він запущений. Без Windows під рукою скористайтеся GitHub
Actions — .github/workflows/windows-installer.yml збирає й перевіряє пакунок.
'@
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')

# --- JDK --------------------------------------------------------------------

if (-not $Jdk) { $Jdk = $env:PACKAGING_JAVA_HOME }
if (-not $Jdk) { $Jdk = $env:JAVA_HOME }
if (-not $Jdk) {
    Fail "не знайдено JDK $MinJdk або новіший. Встановіть його (наприклад: winget install EclipseAdoptium.Temurin.$MinJdk.JDK) або вкажіть явно: -Jdk C:\path\to\jdk"
}

$javaExe = Join-Path $Jdk 'bin\java.exe'
if (-not (Test-Path $javaExe)) { Fail "у «$Jdk» немає bin\java.exe" }

foreach ($tool in @('jpackage.exe', 'jlink.exe', 'jdeps.exe')) {
    if (-not (Test-Path (Join-Path $Jdk "bin\$tool"))) {
        Fail "у «$Jdk» немає bin\$tool — це JRE, а не повний JDK"
    }
}

$settings = & $javaExe -XshowSettings:properties -version 2>&1
$version = ($settings | Select-String 'java\.specification\.version' |
    Select-Object -First 1) -replace '.*=\s*', ''
if (-not $version) { Fail "не вдалося визначити версію JDK у «$Jdk»" }
if ([int]($version -split '\.')[0] -lt $MinJdk) {
    Fail "потрібен JDK $MinJdk або новіший, а в «$Jdk» — $version"
}

# --- WiX --------------------------------------------------------------------

# jpackage не збирає ні msi, ні exe без WiX, і повідомляє про це туманно.
# JDK 21 працює з третьою гілкою WiX; підтримку WiX 4/5 додали лише в JDK 24.
$wix = Get-Command 'candle.exe' -ErrorAction SilentlyContinue
if (-not $wix) {
    $candidates = @(
        "${env:ProgramFiles(x86)}\WiX Toolset v3.14\bin\candle.exe",
        "${env:ProgramFiles(x86)}\WiX Toolset v3.11\bin\candle.exe",
        "${env:ProgramFiles}\WiX Toolset v3.14\bin\candle.exe"
    )
    $found = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
    if ($found) {
        $env:PATH = "$(Split-Path $found);$env:PATH"
    } else {
        Fail @'
не знайдено WiX Toolset 3 — без нього jpackage не збере ні msi, ні exe.
Встановіть його одним із способів:
    choco install wixtoolset --version=3.14.1
    winget install WiXToolset.WiXToolset
або завантажте з https://github.com/wixtoolset/wix3/releases
'@
    }
}

# --- збірка -----------------------------------------------------------------

Write-Host "JDK:       $Jdk ($version)"
Write-Host "Тип:       $Type"
Write-Host "Runtime:   $(if ($NoJlink) { 'повний JDK' } else { 'jlink' })"
Write-Host ''

$gradleArgs = @(':local-client:winInstaller', "-PpackagingJavaHome=$Jdk", "-PpackagingWinType=$Type")
if ($NoJlink) { $gradleArgs += '-PpackagingUseJlink=false' }
if ($Locales) { $gradleArgs += "-PpackagingLocales=$Locales" }

Push-Location $repoRoot
try {
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { Fail "Gradle завершився з кодом $LASTEXITCODE" }

    $package = Get-ChildItem "dest\windows\*.$Type" |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $package) { Fail "Gradle відпрацював, але пакунка в dest\windows не з'явилось" }
} finally {
    Pop-Location
}

$sizeMb = [math]::Round($package.Length / 1MB, 1)

Write-Host ''
Write-Host "Готово: $($package.FullName)"
Write-Host "Розмір: $sizeMb МБ"
Write-Host ''
Write-Host @'
Встановлення: подвійне клацання. Java не потрібна — вона всередині. Програма
ставиться у профіль користувача, тож права адміністратора не потрібні, у меню
«Пуск» з'являється ярлик, а файли проєктів (.ramus і .rsf) відкриваються
подвійним клацанням.

Наступна версія з тим самим ідентифікатором оновлення замінить цю, а не стане
поруч.

Пакунок не підписано сертифікатом, тому на чужій машині SmartScreen покаже
«Windows protected your PC»: «More info» → «Run anyway». Це знімається лише
сертифікатом для підпису коду.
'@
