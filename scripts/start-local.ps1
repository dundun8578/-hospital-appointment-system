param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $JavaHome) { throw '请设置 JAVA_HOME 为 JDK 17，或传入 -JavaHome。' }
$java = Join-Path $JavaHome 'bin/java.exe'
if (-not (Test-Path $java)) { throw "Java 不存在：$java" }
Set-Location $projectRoot
$logDirectory = Join-Path $projectRoot 'logs'
New-Item -ItemType Directory -Force $logDirectory | Out-Null
foreach ($entry in @(@('user',8082), @('doctor',8083), @('appointment',8084), @('order',8085))) {
    $module = $entry[0]
    $port = [int]$entry[1]
    if (Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue) {
        Write-Host "$module 端口 $port 已占用，跳过启动。"
        continue
    }
    $jar = Join-Path $projectRoot "hospital-$module/target/hospital-$module-1.0.0-exec.jar"
    if (-not (Test-Path $jar)) { throw "请先运行 mvn package：$jar" }
    $process = Start-Process -FilePath $java -ArgumentList @('-jar', $jar) -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $logDirectory "$module.log") `
        -RedirectStandardError (Join-Path $logDirectory "$module.err")
    Write-Host "$module 已发起启动，PID=$($process.Id)，端口=$port"
}
Write-Host '启动结果请查看 logs；本脚本不初始化数据库或更改 Docker 容器。'
