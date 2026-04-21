# TwittaRep - Aplicación Segura Tipo Twitter con Microservicios y Auth0

Una aplicación web simplificada estilo Twitter que permite a usuarios autenticados crear posts cortos (máximo 140 caracteres) en un feed público único. El proyecto comienza como un monolito en Spring Boot, evoluciona a microservicios sin servidor en AWS Lambda, y está completamente asegurado con Auth0.

## Descripción General

**TwittaRep** es una plataforma de microblogging que demuestra una arquitectura moderna de aplicaciones web:

- **Frontend**: Aplicación de página única (SPA) en React + Vite
- **Backend**: Monolito Spring Boot (fase inicial) + Microservicios AWS Lambda (fase final)
- **Seguridad**: OAuth2 con Auth0 como proveedor de identidad
- **Base de Datos**: MongoDB Atlas para persistencia
- **Hosting**: S3 para el frontend, API Gateway + Lambda para el backend

### Características Principales

* Autenticación y autorización con Auth0  
* Posts de máximo 140 caracteres  
* Feed público de todos los posts  
* Endpoints protegidos con JWT  
* Documentación Swagger/OpenAPI  
* Microservicios sin servidor (AWS Lambda)  
* Validación y manejo de errores  
* Tests automatizados  

---

## Arquitectura

### Fase 1: Monolito Spring Boot (Desarrollo)

```mermaid id="fase1mono"
flowchart TD
    A[Frontend (React + Vite)\nlocalhost:5173 / S3] -->|HTTPS + JWT| B[Auth0]

    B -->|Validación de Token| C[Spring Boot Monolito\nlocalhost:8080]

    C --> D[Controllers\nUsuarios / Posts]
    C --> E[Services\nLógica de negocio]
    C --> F[Repositories\nAcceso a datos]

    F --> G[(MongoDB Atlas\nusers / posts)]

    style C fill:#ccffcc
    style G fill:#ffcccc
```

### Fase 2: Microservicios Serverless (Producción)


```mermaid id="fase2micro"
flowchart TD
    A[Frontend (S3)\ntwittarep.s3.amazonaws.com]

    A --> B[API Gateway]
    A --> C[Auth0 Authorizer]

    B --> D[UserService (Lambda)]
    B --> E[PostsService (Lambda)]
    B --> F[FeedService (Lambda)]
    B --> G[Otros servicios...]

    D --> H[(MongoDB Atlas)]
    E --> H
    F --> H

    style B fill:#cce5ff
    style D fill:#ccffcc
    style E fill:#ccffcc
    style F fill:#ccffcc
    style H fill:#ffcccc
```

### Endpoints de la API

#### Endpoints Públicos (sin autenticación)

```
GET    /api/posts               - Obtener posts paginados
GET    /api/stream              - Obtener feed público
GET    /api/posts/{id}          - Obtener un post específico
```

#### Endpoints Protegidos (requieren JWT válido)

```
POST   /api/posts               - Crear un nuevo post
DELETE /api/posts/{id}          - Eliminar un post (solo autor)
GET    /api/me                  - Obtener info del usuario autenticado
GET    /api/feed                - Obtener feed personalizado
GET    /api/users/{id}          - Obtener perfil de usuario
```

---

## Estructura del Proyecto

```
twittarep-backend/                    # Repositorio Backend
├── backend/                          # Monolito Spring Boot
│   ├── src/
│   │   ├── main/java/com/twittarep/
│   │   │   ├── config/              # Configuración (Security, OpenAPI)
│   │   │   ├── controller/          # RestControllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── model/               # Documentos MongoDB
│   │   │   ├── repository/          # Acceso a datos
│   │   │   ├── service/             # Lógica de negocio
│   │   │   ├── security/            # Validadores JWT
│   │   │   └── exception/           # Manejo de excepciones
│   │   ├── resources/
│   │   │   └── application.yml      # Configuración
│   │   └── test/                    # Tests unitarios e integración
│   └── pom.xml
│
├── microservices/                   # Microservicios separados
│   ├── user-service/               # Servicio de usuarios
│   ├── posts-service/              # Servicio de posts
│   ├── feed-service/               # Servicio de feed
│   └── shared-kernel/              # Código compartido
│
├── lambdas/                         # Funciones AWS Lambda
│   ├── UserServiceLambda.java
│   ├── PostsServiceLambda.java
│   ├── FeedServiceLambda.java
│   └── LambdaUtils.java
│
├── infra/                           # Infraestructura AWS
│   └── template.yaml               # SAM Template
│
├── docs/                            # Documentación
│   ├── architecture.md
│   └── openapi/
│
├── scripts/                         # Scripts de ayuda
│   ├── run-backend.ps1
│   ├── test-all.ps1
│   └── package-lambdas.ps1
│
├── docker-compose.yml              # Orquestación local
├── pom.xml                         # POM padre
└── README.md

twittarep-frontend/                  # Repositorio Frontend
├── src/
│   ├── main.jsx                     # Entrada React
│   ├── App.jsx                      # Componente principal
│   ├── api.js                       # Cliente HTTP
│   └── styles.css                   # Estilos
├── index.html                       # HTML base
├── vite.config.js                   # Configuración Vite
├── package.json
├── .env.example                     # Variables de ambiente
└── README.md
```

---

## Comenzando

### Requisitos Previos

Antes de empezar, asegúrate de tener instalados:

```
- Java 21 (JDK)
  Descargar desde: https://www.oracle.com/java/technologies/downloads/
  
- Maven 3.8+
  Descargar desde: https://maven.apache.org/download.cgi
  
- Node.js 18+ y npm
  Descargar desde: https://nodejs.org/
  
- Docker y Docker Compose (opcional, para MongoDB local)
  Descargar desde: https://www.docker.com/products/docker-desktop
  
- Git
  Descargar desde: https://git-scm.com/
```

### Verificar Instalaciones

```powershell
java -version              # Debe mostrar Java 21
mvn -version              # Debe mostrar Maven 3.8+
node --version            # Debe mostrar Node 18+
npm --version
docker --version          # Opcional
```

---

## 📝 Instalación y Configuración

### 1. Clonar Repositorios

```powershell
# Backend
git clone https://github.com/JesusJC15/twittarep-backend
cd twittarep-backend

# Frontend (en otra carpeta)
git clone https://github.com/JesusJC15/twittarep-frontend
cd twittarep-frontend
```

### 2. Configurar Variables de Ambiente - Backend

**Crear archivo `.env` en la raíz del proyecto backend** (basado en `.env.example`):

```properties
# Auth0 Configuration
AUTH0_DOMAIN=twittarep.us.auth0.com
AUTH0_ISSUER_URI=https://${AUTH0_DOMAIN}/
AUTH0_AUDIENCE=https://api.twittarep.com

# MongoDB Atlas
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/twittarep?retryWrites=true&w=majority

# CORS - Orígenes permitidos
FRONTEND_LOCAL_ORIGIN=http://localhost:5173
FRONTEND_S3_ORIGIN=https://twittarep.s3.amazonaws.com

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

**Configurar Java Home en PowerShell**:

```powershell
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME
```

Alternativa: Establecer permanentemente en variables de sistema (Windows).

### 3. Configurar Variables de Ambiente - Frontend

**Crear archivo `.env` en `twittarep-frontend/`** (basado en `.env.example`):

```env
# Auth0
VITE_AUTH0_DOMAIN=twittarep.us.auth0.com
VITE_AUTH0_CLIENT_ID=SVJypYNX9sizwLCZ6ttnTlI1NUVMH66g
VITE_AUTH0_AUDIENCE=https://api.twittarep.com

# Backend API
VITE_API_BASE_URL=http://localhost:8080
```

### 4. Instalar Dependencias

**Backend** (Maven):

```powershell
# En la raíz del proyecto backend
mvn clean install -DskipTests
```

**Frontend** (npm):

```powershell
# En la raíz del proyecto frontend
npm install
```

---

## Ejecutar Localmente

### Opción A: Ejecución Manual

#### 1. Iniciar el Backend Spring Boot

```powershell
# Desde la raíz del proyecto backend
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME

mvn -pl backend spring-boot:run
```

El backend estará disponible en: http://localhost:8080

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Actuator Health**: http://localhost:8080/actuator/health

#### 2. Iniciar el Frontend React

```powershell
# Desde la carpeta twittarep-frontend
npm run dev
```

El frontend estará disponible en: http://localhost:5173

### Opción B: Usar Docker Compose (Todo en Contenedores)

```powershell
# En la raíz del proyecto backend
docker-compose up -d
```

Esto inicia:
- MongoDB local en puerto 27017
- Backend Spring Boot en puerto 8080
- Frontend React en puerto 5173

---

## Testing

### Ejecutar Todos los Tests

```powershell
# Backend - desde la raíz del proyecto
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME

mvn test
```

### Ejecutar Tests de un Módulo Específico

```powershell
# Solo backend
mvn -pl backend test

# Solo posts-service
mvn -pl microservices/posts-service test
```

### Tests Incluidos

#### Pruebas Unitarias

```
backend/src/test/java/com/twittarep/backend/
├── service/
│   ├── PostServiceTest.java        # Lógica de creación de posts
│   └── CurrentUserServiceTest.java # Información del usuario
└── controller/
    ├── PostControllerTest.java     # Endpoints de posts
    └── UserControllerTest.java     # Endpoints de usuario
```

#### Pruebas de Integración

- Validación de JWT con Auth0
- Persistencia en MongoDB
- Endpoints REST completos
- Validación de scopes

### Ejemplos de Pruebas

```powershell
# Obtener token de prueba
# 1. Ir a: https://twittarep.us.auth0.com/authorize?...
# 2. Copiar el access token
# 3. Usar en los ejemplos:

# Obtener feed público (sin autenticación)
curl -X GET "http://localhost:8080/api/stream?page=1&size=20"

# Obtener información del usuario (requiere autenticación)
curl -X GET "http://localhost:8080/api/me" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Crear un post (requiere autenticación)
curl -X POST "http://localhost:8080/api/posts" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content": "Mi primer tweet en TwittaRep!"}'
```

---

## Configuración de Auth0

### Paso 1: Crear Tenant en Auth0

1. Ir a https://auth0.com/ y crear una cuenta
2. Crear un nuevo tenant (ej: `twittarep.us.auth0.com`)

### Paso 2: Crear SPA para el Frontend

1. En Auth0 Dashboard → **Applications → Create Application**
2. **Name**: `TwittaRep Frontend`
3. **Application Type**: Single Page Application
4. **Technology**: React
5. En **Settings**:
   - **Allowed Callback URLs**: `http://localhost:5173/`
   - **Allowed Logout URLs**: `http://localhost:5173/`
   - **Allowed Web Origins**: `http://localhost:5173`
   - **CORS Allowed Origins**: `http://localhost:5173`
6. Guardar `Client ID` en el `.env` del frontend

### Paso 3: Crear API para el Backend

1. En Auth0 Dashboard → **APIs → Create API**
2. **Name**: `TwittaRep API`
3. **Identifier (Audience)**: `https://api.twittarep.com`
4. **Signing Algorithm**: RS256

### Paso 4: Crear Scopes

En la API creada → **Permissions**, agregar:

```
- read:posts     (Leer posts)
- write:posts    (Crear posts)
- read:profile   (Leer perfil)
```

### Paso 5: Crear M2M Application (para testing local)

1. **Applications → Create Application**
2. **Type**: Machine to Machine
3. **Name**: `TwittaRep Test Client`
4. **Authorized API**: Seleccionar la API `TwittaRep API`
5. En **Permissions**, agregar los 3 scopes anteriores
6. Guardar el `Client ID` y `Client Secret`

### Paso 6: Asociar Scopes al Frontend

En la SPA → **Settings → Advanced → Grant Types**:

Activar:
- Authorization Code
- Refresh Token
- Implicit

---

## Deployment en AWS

### Prerrequisitos para AWS

```powershell
# Instalar AWS CLI
pip install awscli

# Instalar SAM CLI
pip install aws-sam-cli

# Configurar credenciales
aws configure
```

### Desplegar Backend como Lambda

#### 1. Empaquetar las Lambdas

```powershell
# En la raíz del proyecto backend
$env:JAVA_21_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:JAVA_HOME=$env:JAVA_21_HOME

mvn package -DskipTests
```

#### 2. Desplegar con SAM

```powershell
# Validar template
sam validate --template infra/template.yaml

# Build
sam build

# Deploy (primera vez)
sam deploy --guided

# Deploy (subsecuentes)
sam deploy
```

#### 3. Desplegar Frontend en S3

```powershell
# Build el frontend
cd ../twittarep-frontend
npm run build

# Crear bucket S3 (si no existe)
aws s3 mb s3://twittarep-prod --region us-east-1

# Subir archivos
aws s3 sync dist/ s3://twittarep-prod --delete

# Habilitar sitio web estático
aws s3 website s3://twittarep-prod \
  --index-document index.html \
  --error-document index.html

# Hacer público
aws s3api put-bucket-policy --bucket twittarep-prod \
  --policy file://bucket-policy.json
```

**bucket-policy.json**:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::twittarep-prod/*"
    }
  ]
}
```

---

## Seguridad

### Implementación de Seguridad

#### Backend (Spring Boot)

```java
// SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Públicos
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stream").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Protegidos
                .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                .requestMatchers("/api/me").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        return http.build();
    }
}
```

#### Frontend (React)

```javascript
// App.jsx
import { Auth0Provider } from '@auth0/auth0-react';

function App() {
  return (
    <Auth0Provider
      domain={import.meta.env.VITE_AUTH0_DOMAIN}
      clientId={import.meta.env.VITE_AUTH0_CLIENT_ID}
      authorizationParams={{
        audience: import.meta.env.VITE_AUTH0_AUDIENCE,
        scope: "read:posts write:posts read:profile",
        redirect_uri: window.location.origin,
      }}
    >
      {/* Aplicación */}
    </Auth0Provider>
  );
}
```

### Mejores Prácticas

- **Nunca commit credenciales**: Usar `.env` local y variables de ambiente  
- **HTTPS en producción**: Usar certificados SSL/TLS  
- **CORS configurado**: Solo orígenes permitidos  
- **Validación JWT**: Verificar issuer, audience y firmas  
- **Rate limiting**: Implementar en API Gateway  
- **Logs y monitoreo**: CloudWatch para Lambda, Application Insights para Spring Boot  

---

## Documentación de API

### Swagger/OpenAPI

La documentación interactiva está disponible en:

**Local**: http://localhost:8080/swagger-ui/index.html

### Ejemplos de Requests

#### 1. Obtener Feed Público

```bash
curl -X GET "http://localhost:8080/api/stream?page=1&size=20" \
  -H "Content-Type: application/json"
```

**Response**:

```json
{
  "page": 1,
  "pageSize": 20,
  "totalCount": 150,
  "posts": [
    {
      "id": "post_001",
      "userId": "user_123",
      "username": "juan_perez",
      "content": "Aprendiendo microservicios 🚀",
      "createdAt": "2026-04-20T10:30:00Z",
      "updatedAt": "2026-04-20T10:30:00Z"
    }
  ]
}
```

#### 2. Crear un Post (Autenticado)

```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"content": "¡Hola TwittaRep!"}'
```

**Response** (201 Created):

```json
{
  "id": "post_002",
  "userId": "user_456",
  "username": "maria_torres",
  "content": "¡Hola TwittaRep!",
  "createdAt": "2026-04-20T11:00:00Z",
  "updatedAt": "2026-04-20T11:00:00Z"
}
```

#### 3. Obtener Información del Usuario

```bash
curl -X GET "http://localhost:8080/api/me" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

**Response**:

```json
{
  "id": "auth0|user_456",
  "email": "maria@example.com",
  "username": "maria_torres",
  "name": "María Torres"
}
```

---

## Autores

- **Jesús Jauregui** [JesusJC15](https://github.com/JesusJC15)
- **Natalia Espitia Espinel** [Natalia-Espitia](https://github.com/Natalia-Espitia)
- **Mayerlly Suárez Correa** [mayerllyyo](https://github.com/mayerllyyo)

### Demostración en Video

Mira la demostración completa del proyecto:

[Video Demo TwittaRep](https://drive.google.com/file/d/18kRCqXmyN_Q8NCOpiaiAjaGQVIKnPAAE/view?usp=sharing)
