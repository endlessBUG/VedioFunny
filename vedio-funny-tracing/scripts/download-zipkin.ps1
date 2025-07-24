# ����TLS 1.2
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

# ����Zipkin�汾������URL
$zipkinVersion = "2.24.3"
$zipkinUrl = "https://repo1.maven.org/maven2/io/zipkin/zipkin-server/$zipkinVersion/zipkin-server-$zipkinVersion-exec.jar"

# ����libĿ¼
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$libPath = Join-Path $scriptPath ".." "lib"
if (-not (Test-Path $libPath)) {
    New-Item -ItemType Directory -Force -Path $libPath | Out-Null
    Write-Host "Created lib directory at: $libPath"
}

$zipkinJar = Join-Path $libPath "zipkin.jar"

# ����Zipkin
Write-Host "Downloading Zipkin $zipkinVersion..."
Write-Host "From: $zipkinUrl"
Write-Host "To: $zipkinJar"

try {
    Invoke-WebRequest -Uri $zipkinUrl -OutFile $zipkinJar
    Write-Host "Successfully downloaded Zipkin to: $zipkinJar"
} catch {
    Write-Error "Failed to download Zipkin: $_"
    exit 1
}
