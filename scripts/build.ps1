$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src\main\java'
$outputDir = Join-Path $projectRoot 'build\classes'
$driverJar = Join-Path $projectRoot 'lib\mysql-connector-j-8.4.0.jar'

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'Java JDK 17 or newer is not installed or not on PATH. Install the JDK and rerun this script.'
}

if (-not (Test-Path $driverJar)) {
    throw "MySQL Connector/J JAR not found at $driverJar. Download it from https://dev.mysql.com/downloads/connector/j/ and place it in the lib folder."
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$javaFiles = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java | Select-Object -ExpandProperty FullName

javac --add-modules jdk.httpserver -cp $driverJar -d $outputDir $javaFiles

if ($LASTEXITCODE -ne 0) {
    throw 'Java compilation failed. Check the error messages above and fix the source files.'
}

Write-Host 'Build finished successfully.'
Write-Host "Classes are in: $outputDir"
