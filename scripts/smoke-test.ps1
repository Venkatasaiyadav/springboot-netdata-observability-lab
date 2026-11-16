param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
Write-Host "Application: $BaseUrl/api/hello"
Write-Host "Actuator:    $BaseUrl/actuator/health"
Write-Host "Netdata:     http://localhost:19999"
Invoke-RestMethod "$BaseUrl/actuator/health" | ConvertTo-Json
Invoke-RestMethod "$BaseUrl/api/hello" | ConvertTo-Json
Invoke-RestMethod "$BaseUrl/api/health-check" | ConvertTo-Json
Write-Host "Smoke test passed."
