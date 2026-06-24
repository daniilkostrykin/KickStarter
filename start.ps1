Write-Host "1. Starting RabbitMQ..." -ForegroundColor Cyan
docker-compose up -d

Write-Host "2. Force removing all target folders..." -ForegroundColor Cyan
Get-ChildItem -Directory -Recurse -Filter "target" | Remove-Item -Recurse -Force

Write-Host "3. Building contracts, generating gRPC, and compiling backend..." -ForegroundColor Cyan
.\mvnw clean install -DskipTests

Write-Host "4. Starting microservices in separate windows..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw -pl kickstarter-grpc-server spring-boot:run -DskipTests"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw -pl kickstarter-audit-service spring-boot:run -DskipTests"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw -pl kickstarter-enrichment-client spring-boot:run -DskipTests"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw -pl kickstarter-notification-service spring-boot:run -DskipTests"

Start-Sleep -Seconds 5

Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw -pl kickstarter-rest spring-boot:run -DskipTests"