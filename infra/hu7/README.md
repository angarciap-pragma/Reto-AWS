# HU7 - Lambda API Usuarios (Serverless Framework)

Implementacion de la HU7 en dos tecnologias:

1. `serverless-node`: Node.js
2. `serverless-java`: Java

Cada tecnologia incluye:

- 1 API Gateway HTTP API
- 4 Lambda functions (GET, POST, PUT, DELETE)
- datos de usuarios en memoria (quemados)

---

## Prerrequisitos

1. AWS CLI configurado (`aws configure`).
2. Serverless Framework instalado:
   ```bash
   npm i -g serverless
   ```
3. Region sugerida:
   - `us-east-2`

---

## A) Despliegue Node.js

Carpeta:
- `infra/hu7/serverless-node`

Comandos:

```bash
cd infra/hu7/serverless-node
serverless deploy --stage dev --region us-east-2
```

Endpoints (base):
- `GET    /usuarios/{id}`
- `POST   /usuarios`
- `PUT    /usuarios/{id}`
- `DELETE /usuarios/{id}`

---

## B) Despliegue Java

Carpeta:
- `infra/hu7/serverless-java`

Build JAR:

```bash
cd infra/hu7/serverless-java
./gradlew clean shadowJar
```

Deploy:

```bash
serverless deploy --stage dev --region us-east-2
```

Endpoints (base):
- `GET    /usuarios/{id}`
- `POST   /usuarios`
- `PUT    /usuarios/{id}`
- `DELETE /usuarios/{id}`

---

## Pruebas rapidas (ejemplos)

POST

```bash
curl -X POST "<BASE_URL>/usuarios" \
  -H "Content-Type: application/json" \
  -d '{"id":"2001","nombre":"Andrea","email":"andrea@demo.com"}'
```

GET

```bash
curl "<BASE_URL>/usuarios/2001"
```

PUT

```bash
curl -X PUT "<BASE_URL>/usuarios/2001" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Andrea Garcia","email":"andrea.garcia@demo.com"}'
```

DELETE

```bash
curl -X DELETE "<BASE_URL>/usuarios/2001"
```

