Write-Host "1. Поднятие RabbitMQ..." -ForegroundColor Cyan
docker-compose up -d

Write-Host "2. Принудительное удаление всех папок target..." -ForegroundColor Cyan
Get-ChildItem -Directory -Recurse -Filter "target" | Remove-Item -Recurse -Force

Write-Host "3. Сборка контрактов и бэкенда..." -ForegroundColor Cyan
.\mvnw install -DskipTests

Write-Host "4. Запуск KickStarter REST API..." -ForegroundColor Green
.\mvnw -pl kickstarter-rest spring-boot:run -DskipTests