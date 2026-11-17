param([string]$JMeter = 'jmeter', [int]$Users = 20, [int]$Loops = 10)
$ErrorActionPreference = 'Stop'
if ($Users -lt 1 -or $Loops -lt 1) { throw 'Users 和 Loops 必须大于0' }
$root = Split-Path $PSScriptRoot -Parent
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$output = Join-Path $root "jmeter/results/$stamp"
New-Item -ItemType Directory -Force $output | Out-Null
& $JMeter -n -t (Join-Path $root 'jmeter/scripts/query_test_plan.jmx') "-Jusers=$Users" "-Jloops=$Loops" `
    -l (Join-Path $output 'samples.jtl') -j (Join-Path $output 'jmeter.log') -e -o (Join-Path $output 'report')
if ($LASTEXITCODE -ne 0) { throw "JMeter失败：$LASTEXITCODE" }
Write-Host "报告：$output/report/index.html"
