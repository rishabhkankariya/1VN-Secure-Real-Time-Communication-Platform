# 1VN Windows Installer Builder
# Invokes Inno Setup (ISCC.exe) to create 1VN-Setup.exe
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = (Get-Item "$scriptDir\..").FullName
$issFile = "$rootDir\installer\1vn-setup.iss"
$releaseDir = "$rootDir\release"

Write-Host "=================================================="
Write-Host "   1VN - Compiling Windows Setup Installer       "
Write-Host "=================================================="

# Ensure release directory exists
if (-not (Test-Path $releaseDir)) {
    New-Item -ItemType Directory -Force $releaseDir | Out-Null
}

# Locate ISCC.exe
$isccCandidates = @(
    "iscc.exe",
    "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe",
    "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe",
    "$env:ProgramFiles\Inno Setup 6\ISCC.exe"
)

$isccPath = $null
foreach ($candidate in $isccCandidates) {
    if (Get-Command $candidate -ErrorAction SilentlyContinue) {
        $isccPath = (Get-Command $candidate).Source
        break
    } elseif (Test-Path $candidate) {
        $isccPath = $candidate
        break
    }
}

if (-not $isccPath) {
    throw "Inno Setup compiler (ISCC.exe) not found. Please install Inno Setup 6."
}

Write-Host "Using Inno Setup compiler: $isccPath"
Write-Host "Compiling installer script: $issFile"

& $isccPath "$issFile"

if ($LASTEXITCODE -ne 0) {
    throw "ISCC compilation failed with code $LASTEXITCODE"
}

$setupExe = "$releaseDir\1VN-Setup.exe"
if (Test-Path $setupExe) {
    $size = (Get-Item $setupExe).Length
    Write-Host "=================================================="
    Write-Host "Installer created successfully!"
    Write-Host "Output: $setupExe"
    Write-Host "File Size: $([Math]::Round($size / 1MB, 2)) MB"
    Write-Host "=================================================="
} else {
    throw "Expected installer output not found: $setupExe"
}
