# Architecture Overview

## Backend principal

- Spring Boot 3 + Java 21.
- MongoDB Atlas for posts.
- Auth0 as OAuth2 Resource Server provider.
- Swagger UI for documented REST API.

## Serverless

- API Gateway HTTP API.
- AWS Lambda functions:
  - posts-service
  - feed-service
  - user-service
- Shared Auth0 JWT audience and issuer.
- MongoDB Atlas as persistent store for posts-service and feed-service.

## Security flow

1. Frontend authenticates with Auth0 SPA.
2. Frontend requests an access token for the backend audience.
3. Public endpoints are called without token.
4. Protected endpoints send `Authorization: Bearer <token>`.
5. El backend valida JWT directamente y la version Lambda usa API Gateway con JWT authorizer.
6. Para simplificar la entrega, `posts-service` y `feed-service` leen la misma coleccion de posts.
