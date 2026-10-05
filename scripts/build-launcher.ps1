# 1VN Desktop Launcher Generator
# Generates native 1VN.exe using JDK jpackage and debug batch launcher
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = (Get-Item "$scriptDir\..").FullName
$distDir = "$rootDir\dist\1VN"
$tmpPackageDir = "$rootDir\dist\jp_tmp"

Write-Host "=================================================="
Write-Host "   1VN - Generating Native Application Launcher  "
Write-Host "=================================================="

if (-not (Test-Path "$distDir\runtime")) {
    Write-Host "Runtime not found. Packaging runtime first..."
    & "$scriptDir\package-runtime.ps1"
}

# 1. Generate native 1VN.exe via jpackage
Write-Host "Invoking JDK 25 jpackage for native Windows launcher..."
if (Test-Path $tmpPackageDir) {
    Remove-Item -Recurse -Force $tmpPackageDir
}

& jpackage `
    --type app-image `
    --dest "$tmpPackageDir" `
    --name "1VN" `
    --app-version "1.0.0" `
    --input "$distDir\app" `
    --main-jar "client-1.0-SNAPSHOT.jar" `
    --main-class "com.onevn.client.DesktopLauncher" `
    --runtime-image "$distDir\runtime"

if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

# Copy generated 1VN.exe to dist root
$generatedExe = "$tmpPackageDir\1VN\1VN.exe"
if (Test-Path $generatedExe) {
    Copy-Item $generatedExe "$distDir\1VN.exe" -Force
    Write-Host "Native launcher copied to: $distDir\1VN.exe"
} else {
    throw "Generated executable not found at $generatedExe"
}

# Clean temporary jpackage output directory
Remove-Item -Recurse -Force $tmpPackageDir

# 2. Create debug batch launcher for developer troubleshooting
$debugBat = @"
@echo off
setlocal
title 1VN Chat - Debug Console
cd /d "%~dp0"
echo ==================================================
echo   1VN Secure Real-Time Communication Platform     
echo                  Console Debug Mode               
echo ==================================================
echo Starting embedded application runtime...
runtime\bin\java.exe -cp "app\client-1.0-SNAPSHOT.jar;app\libs\*" com.onevn.client.DesktopLauncher %*
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%.
    pause
)
"@
Set-Content -Path "$distDir\1VN-debug.bat" -Value $debugBat -Encoding ASCII

Write-Host "Debug batch runner created at: $distDir\1VN-debug.bat"
Write-Host "One-click launcher generation complete!"
