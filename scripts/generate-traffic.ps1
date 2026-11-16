param([int]$Seconds = 30, [string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
if ($Seconds -lt 1 -or $Seconds -gt 120) { throw "Seconds must be between 1 and 120." }
$until = (Get-Date).AddSeconds($Seconds); $count = 0
Write-Host "Generating traffic for $Seconds seconds. Watch Netdata and Actuator metrics."
while ((Get-Date) -lt $until) {
  Invoke-WebRequest -UseBasicParsing "$BaseUrl/api/hello" | Out-Null
  if (($count % 5) -eq 0) { Invoke-WebRequest -UseBasicParsing "$BaseUrl/api/slow?delayMs=500" | Out-Null }
  $count++
}
Write-Host "Sent $count normal requests plus periodic slow requests."
