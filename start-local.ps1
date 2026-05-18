Write-Host "1. Поднятие RabbitMQ в фоновом режиме..." -ForegroundColor Cyan
docker compose up -d kickstarter-rabbitmq

Write-Host "2. Очистка старых билдов..." -ForegroundColor Cyan
.\mvnw clean

Write-Host "3. Сборка контрактов и бэкенда..." -ForegroundColor Cyan
.\mvnw install -DskipTests

Write-Host "4. Запуск Audit Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit -Command title AuditService; .\mvnw -pl kickstarter-audit-service spring-boot:run -DskipTests"

Write-Host "5. Запуск KickStarter REST API..." -ForegroundColor Green
.\mvnw -pl kickstarter-rest spring-boot:run -DskipTests