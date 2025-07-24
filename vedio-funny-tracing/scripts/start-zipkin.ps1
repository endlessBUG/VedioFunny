# 获取脚本路径
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$libPath = Join-Path $scriptPath ".." "lib"
$zipkinJar = Join-Path $libPath "zipkin.jar"

# 检查Zipkin jar是否存在
if (-not (Test-Path $zipkinJar)) {
    Write-Host "Zipkin jar not found. Downloading..."
    & "$scriptPath\download-zipkin.ps1"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to download Zipkin"
        exit 1
    }
}

# 检查Java是否安装
try {
    $javaVersion = java -version 2>&1
    Write-Host "Found Java:"
    Write-Host $javaVersion
} catch {
    Write-Error "Java is not installed or not in PATH"
    exit 1
}

# 设置Zipkin环境变量
$env:STORAGE_TYPE = "mem"  # 使用内存存储
$env:QUERY_PORT = "9411"   # 设置查询端口

# 启动Zipkin
Write-Host "Starting Zipkin server..."
Write-Host "Storage: Memory"
Write-Host "Port: 9411"
Write-Host "UI will be available at: http://localhost:9411"

try {
    java -jar $zipkinJar
} catch {
    Write-Error "Failed to start Zipkin: $_"
    exit 1
}
