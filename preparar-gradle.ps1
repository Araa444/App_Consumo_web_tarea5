$ErrorActionPreference = "Stop"
$gradleVersion = "8.9"
$gradleCache = Join-Path $env:TEMP "aradirectorio-gradle-8.9"
$gradleZip = Join-Path $gradleCache "gradle.zip"
New-Item -ItemType Directory -Force -Path $gradleCache | Out-Null
$gradleExe = Join-Path $gradleCache "gradle-8.9\bin\gradle.bat"
if (!(Test-Path $gradleExe)) {
    Invoke-WebRequest "https://services.gradle.org/distributions/gradle-8.9-bin.zip" -OutFile $gradleZip
    Expand-Archive $gradleZip -DestinationPath $gradleCache -Force
}
& $gradleExe wrapper --gradle-version $gradleVersion --distribution-type bin
if ($LASTEXITCODE -ne 0) { throw "No se pudo generar el wrapper. Revisa JDK 17 y SDK en Android Studio." }
Write-Host "Wrapper listo. Vuelve a sincronizar el proyecto en Android Studio."
