Write-Host "1. Остановка и удаление всех контейнеров и сетей проекта..." -ForegroundColor Red
docker compose down

Write-Host "2. Очистка скомпилированных файлов Maven..." -ForegroundColor Cyan
.\mvnw clean

Write-Host "3. Принудительное удаление остатков target..." -ForegroundColor Cyan
Get-ChildItem -Directory -Recurse -Filter "target" | Remove-Item -Recurse -Force

Write-Host "Готово! Среда полностью очищена." -ForegroundColor Green