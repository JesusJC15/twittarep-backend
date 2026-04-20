# Microservices

Servicios serverless para la fase AWS Lambda:

- `posts-service`: dueño de publicaciones.
- `feed-service`: agrega el stream público consultando al servicio de posts.
- `user-service`: expone `/api/me` desde claims de Auth0.
- `shared-kernel`: DTOs y utilidades comunes.
