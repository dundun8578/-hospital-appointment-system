$ErrorActionPreference = 'Stop'
$checks = @(
    @{ Name = '医院列表'; Url = 'http://localhost:8083/hospital/list' },
    @{ Name = '医生详情'; Url = 'http://localhost:8083/doctor/1' },
    @{ Name = '医生详情缓存重读'; Url = 'http://localhost:8083/doctor/1' },
    @{ Name = '排班列表'; Url = 'http://localhost:8083/schedule/doctor/1' }
)
foreach ($check in $checks) {
    $response = Invoke-RestMethod $check.Url -TimeoutSec 10
    if ($response.code -ne 200) { throw "$($check.Name)失败：$($response.message)" }
    Write-Host "PASS $($check.Name)"
}
foreach ($port in @(8082, 8083, 8084, 8085)) {
    if (-not (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)) {
        throw "服务端口 $port 未监听"
    }
    Write-Host "PASS 端口 $port"
}
Write-Host '只读检查通过；预约、支付与消息链路需单独验收。'
