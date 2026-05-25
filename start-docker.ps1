Write-Host "1. Принудительное удаление всех папок target..." -ForegroundColor Cyan
Get-ChildItem -Directory -Recurse -Filter "target" | Remove-Item -Recurse -Force

Write-Host "2. Сборка толстых .jar файлов для Докера..." -ForegroundColor Cyan
.\mvnw clean package -DskipTests

Write-Host "3. Поднятие всей инфраструктуры (RabbitMQ + REST + Audit)..." -ForegroundColor Green
docker compose up -d --build

Write-Host "4. Вывод логов..." -ForegroundColor Yellow
docker compose logs -f