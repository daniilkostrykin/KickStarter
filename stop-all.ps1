Write-Host "1. Stopping and removing all containers and networks..." -ForegroundColor Red
docker-compose down

Write-Host "2. Cleaning Maven compiled files..." -ForegroundColor Cyan
.\mvnw clean

Write-Host "3. Force removing remaining target folders..." -ForegroundColor Cyan
Get-ChildItem -Directory -Recurse -Filter "target" | Remove-Item -Recurse -Force

Write-Host "Done! Environment is completely clean." -ForegroundColor Green