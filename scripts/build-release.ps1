# 1VN Automated End-to-End Release Pipeline
# Packages Maven modules, builds runtime, generates native launcher, and compiles Windows installer
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = (Get-Item "$scriptDir\..").FullName
$releaseDir = "$rootDir\release"

Write-Host "=========================================================="
Write-Host "       1VN v1.0.0 - Full Production Release Build         "
Write-Host "=========================================================="

# 1. Package custom Java 25 runtime and application JARs
Write-Host "`n>>> [Step 1/3] Assembling Java 25 Runtime and Dependencies..."
& "$scriptDir\package-runtime.ps1"

# 2. Generate native 1VN.exe launcher
Write-Host "`n>>> [Step 2/3] Generating Native Windows Executable (1VN.exe)..."
& "$scriptDir\build-launcher.ps1"

# 3. Compile Windows Installer (1VN-Setup.exe)
Write-Host "`n>>> [Step 3/3] Compiling Windows Setup Installer with Inno Setup..."
& "$scriptDir\build-installer.ps1"

# 4. Create Portable ZIP distribution
Write-Host "`n>>> Creating Portable ZIP Archive..."
$portableZip = "$releaseDir\1VN-v1.0.0-windows-portable.zip"
if (Test-Path $portableZip) {
    Remove-Item -Force $portableZip
}
Compress-Archive -Path "$rootDir\dist\1VN\*" -DestinationPath $portableZip -CompressionLevel Optimal

Write-Host "`n=========================================================="
Write-Host "          1VN v1.0 Release Build Complete!                "
Write-Host "=========================================================="
Write-Host "Artifacts:"
Get-ChildItem -Path $releaseDir | Select-Object Name, @{Name="Size (MB)"; Expression={[Math]::Round($_.Length / 1MB, 2)}}, LastWriteTime | Format-Table -AutoSize
