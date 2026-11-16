param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
function Invoke-LabStep([string]$Title, [string]$Url, [string]$Observe) {
  Write-Host "`n=== $Title ===" -ForegroundColor Cyan
  Write-Host "Observe: $Observe" -ForegroundColor Yellow
  Invoke-WebRequest -UseBasicParsing $Url | Select-Object StatusCode, Content | Format-List
  Start-Sleep -Seconds 3
}
Write-Host "Open http://localhost:19999 before starting."
Invoke-LabStep "CPU spike" "$BaseUrl/api/cpu?seconds=10" "Netdata container CPU chart; process.cpu.usage in /actuator/prometheus."
Invoke-LabStep "Memory spike" "$BaseUrl/api/memory?mb=100&seconds=10" "Container memory chart; jvm_memory_used_bytes."
Invoke-LabStep "Slow request" "$BaseUrl/api/slow?delayMs=3000" "HTTP request duration metrics such as http_server_requests_seconds."
Write-Host "`n=== Intentional error ===" -ForegroundColor Cyan
try { Invoke-WebRequest -UseBasicParsing "$BaseUrl/api/error" | Out-Null } catch { Write-Host "Expected HTTP 500 received." }
Write-Host "Inspect logs with: docker compose logs app --tail 50"
