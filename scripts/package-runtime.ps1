# 1VN Desktop Runtime Packager
# Builds custom minimal Java 25 JRE via jlink and bundles application jars
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = (Get-Item "$scriptDir\..").FullName
$distDir = "$rootDir\dist\1VN"

Write-Host "=================================================="
Write-Host "   1VN - Packaging Windows Java Runtime          "
Write-Host "=================================================="
Write-Host "Project Root: $rootDir"
Write-Host "Distribution Target: $distDir"

# Clean dist directory
if (Test-Path $distDir) {
    Write-Host "Cleaning previous dist directory..."
    Remove-Item -Recurse -Force $distDir
}
New-Item -ItemType Directory -Force "$distDir\app\libs" | Out-Null
New-Item -ItemType Directory -Force "$distDir\data" | Out-Null

# 1. Build Maven modules
Write-Host "Building Maven project..."
Set-Location $rootDir
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    throw "Maven packaging failed"
}

# 2. Copy application JARs
Write-Host "Copying application JARs to app directory..."
Copy-Item "$rootDir\client\target\client-1.0-SNAPSHOT.jar" "$distDir\app\" -Force
Copy-Item "$rootDir\server\target\server-1.0-SNAPSHOT.jar" "$distDir\app\libs\" -Force
Copy-Item "$rootDir\client\target\libs\*" "$distDir\app\libs\" -Force

# 3. Build customized minimal Java runtime via jlink
Write-Host "Assembling minimal Java 25 runtime via jlink..."
$jlinkModules = @(
    "java.base",
    "java.desktop",
    "java.sql",
    "java.naming",
    "java.management",
    "java.xml",
    "jdk.unsupported"
) -join ","

& jlink `
    --add-modules $jlinkModules `
    --output "$distDir\runtime" `
    --strip-debug `
    --no-header-files `
    --no-man-pages `
    --compress=zip-6

if ($LASTEXITCODE -ne 0) {
    throw "jlink failed to create runtime"
}

Write-Host "Java runtime bundled successfully at: $distDir\runtime"
Write-Host "Runtime size:" (Get-ChildItem -Recurse "$distDir\runtime" | Measure-Object -Property Length -Sum).Sum "bytes"
Write-Host "1VN Desktop runtime package complete!"
