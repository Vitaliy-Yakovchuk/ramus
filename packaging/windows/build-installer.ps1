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
The installer can only be built on Windows: jpackage packages for the system it
runs on and no other. With no Windows at hand use GitHub Actions —
.github/workflows/windows-installer.yml builds the package and verifies it.
'@
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')

if (-not $Jdk) { $Jdk = $env:PACKAGING_JAVA_HOME }
if (-not $Jdk) { $Jdk = $env:JAVA_HOME }
if (-not $Jdk) {
    Fail "no JDK $MinJdk or newer found. Install one (for example: winget install EclipseAdoptium.Temurin.$MinJdk.JDK) or name it: -Jdk C:\path\to\jdk"
}

$javaExe = Join-Path $Jdk 'bin\java.exe'
if (-not (Test-Path $javaExe)) { Fail "`"$Jdk`" has no bin\java.exe" }

foreach ($tool in @('jpackage.exe', 'jlink.exe', 'jdeps.exe')) {
    if (-not (Test-Path (Join-Path $Jdk "bin\$tool"))) {
        Fail "`"$Jdk`" has no bin\$tool — that is a JRE, not a full JDK"
    }
}

$settings = & $javaExe -XshowSettings:properties -version 2>&1
$version = ($settings | Select-String 'java\.specification\.version' |
    Select-Object -First 1) -replace '.*=\s*', ''
if (-not $version) { Fail "could not read the JDK version in `"$Jdk`"" }
if ([int]($version -split '\.')[0] -lt $MinJdk) {
    Fail "JDK $MinJdk or newer is required, and `"$Jdk`" is $version"
}

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
WiX Toolset 3 not found — without it jpackage builds neither msi nor exe. JDK 21
drives the third WiX line; support for WiX 4 and 5 arrived only in JDK 24.
Install it one of these ways:
    choco install wixtoolset --version=3.14.1
    winget install WiXToolset.WiXToolset
or download it from https://github.com/wixtoolset/wix3/releases
'@
    }
}

Write-Host "JDK:       $Jdk ($version)"
Write-Host "Type:      $Type"
Write-Host "Runtime:   $(if ($NoJlink) { 'the full JDK' } else { 'jlink' })"
Write-Host ''

$gradleArgs = @(':local-client:winInstaller', "-PpackagingJavaHome=$Jdk", "-PpackagingWinType=$Type")
if ($NoJlink) { $gradleArgs += '-PpackagingUseJlink=false' }
if ($Locales) { $gradleArgs += "-PpackagingLocales=$Locales" }

Push-Location $repoRoot
try {
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { Fail "Gradle exited with code $LASTEXITCODE" }

    $package = Get-ChildItem "dest\windows\*.$Type" |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $package) { Fail "Gradle finished, but no package appeared in dest\windows" }
} finally {
    Pop-Location
}

$sizeMb = [math]::Round($package.Length / 1MB, 1)

Write-Host ''
Write-Host "Done: $($package.FullName)"
Write-Host "Size: $sizeMb MB"
Write-Host ''
Write-Host @'
To install, double-click it. Java is not needed — it is inside. The application
installs into the user profile, so no administrator rights are required, a
shortcut appears in the Start menu, and project files (.ramus and .rsf) open on
double-click.

The next version carrying the same upgrade identifier replaces this one instead
of sitting beside it.

The package is not signed, so SmartScreen on another machine will show "Windows
protected your PC": More info -> Run anyway. Only a code signing certificate
removes that.
'@
