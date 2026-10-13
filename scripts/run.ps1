$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$classesDir = Join-Path $projectRoot 'build\classes'
$driverJar = Join-Path $projectRoot 'lib\mysql-connector-j-8.4.0.jar'

if (-not (Test-Path $classesDir)) {
    throw "Compiled classes not found at $classesDir. Run build.ps1 before starting the server."
}

if (-not (Test-Path $driverJar)) {
    throw "MySQL Connector/J JAR not found at $driverJar. Download it to the lib folder first."
}

$env:DB_HOST = if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' }
$env:DB_PORT = if ($env:DB_PORT) { $env:DB_PORT } else { '3306' }
$env:DB_NAME = if ($env:DB_NAME) { $env:DB_NAME } else { 'whats_in_my_kitchen' }
$env:DB_USER = if ($env:DB_USER) { $env:DB_USER } else { 'root' }
$env:DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { '' }
$env:DB_USE_SSL = if ($env:DB_USE_SSL) { $env:DB_USE_SSL } else { 'false' }
$env:PORT = if ($env:PORT) { $env:PORT } else { '8080' }

Write-Host 'Starting What’sInMyKitchen server...'
java --add-modules jdk.httpserver -cp "$classesDir;$driverJar" com.whatsinmykitchen.Main
