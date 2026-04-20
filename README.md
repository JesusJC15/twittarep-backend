# TwittaRep Backend

Proyecto backend simple para la tarea: posts de hasta 140 caracteres, feed publico, Auth0 y una version serverless en AWS.

## Estructura

- `backend/`: backend Spring Boot principal.
- `microservices/`: `posts-service`, `feed-service` y `user-service`.
- `infra/`: plantilla AWS SAM.
- `scripts/`: comandos de ayuda.

## Endpoints del backend

- `GET /api/posts`: publico.
- `GET /api/stream`: publico.
- `POST /api/posts`: requiere `write:posts`.
- `GET /api/me`: requiere `read:profile`.
- Swagger UI: `/swagger-ui/index.html`.

## Idea simple de implementacion

- `backend/` resuelve todo lo principal en Spring Boot.
- `posts-service` y `feed-service` leen la misma coleccion `posts` en MongoDB Atlas.
- `user-service` solo devuelve la informacion del usuario desde el JWT.
- Asi se cumplen los 3 microservicios sin tener que montar llamadas internas entre servicios.

## Variables necesarias

Completar `.env.example` con:

- `AUTH0_ISSUER_URI`
- `AUTH0_AUDIENCE`
- `MONGODB_URI`
- `FRONTEND_LOCAL_ORIGIN`
- `FRONTEND_S3_ORIGIN`

## Correr local

```powershell
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME
mvn -pl backend spring-boot:run
```

Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Probar

```powershell
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME
mvn test
```

## Empaquetar lambdas

```powershell
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME
mvn package -DskipTests
```

## Auth0

Crear:

- una SPA para el frontend.
- una API con audience propio.
- scopes: `read:posts`, `write:posts`, `read:profile`.

## Pendiente afuera de este repo

- poner los valores reales de Auth0 y MongoDB Atlas.
- desplegar el backend.
- instalar `sam` para desplegar la parte serverless.
