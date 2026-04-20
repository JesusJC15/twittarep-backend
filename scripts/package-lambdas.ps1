$ErrorActionPreference = "Stop"

if (-not $env:JAVA_21_HOME) {
    Write-Error "Set JAVA_21_HOME to your JDK 21 installation path."
}

$env:JAVA_HOME = $env:JAVA_21_HOME
mvn -pl microservices/shared-kernel,microservices/posts-service,microservices/feed-service,microservices/user-service -am package -DskipTests
