# HU2-HU6 Paso a Paso Detallado (AWS)

Documento de respaldo para presentacion. Incluye:

- HU2: RDS (base relacional) + persistencia del microservicio.
- HU3: Despliegue API (ECR + ECS + ALB + API Gateway).
- HU4: Cognito + JWT en API Gateway.
- HU5: Variables de entorno con Parameter Store y Secrets Manager.
- HU6: Logs y alarmas CloudWatch.

---

## 1) Arquitectura final

1. Cliente -> API Gateway HTTP API.
2. API Gateway -> ALB (HTTP integration).
3. ALB -> ECS Fargate service (container API Spring Boot).
4. ECS -> RDS MySQL.
5. Cognito valida JWT en API Gateway.
6. Variables sensibles salen de SSM/Secrets, no de valores quemados.

---

## 2) HU2 - Aprovisionar base relacional en RDS

## 2.1 Lo que se creo

1. RDS MySQL instance: `database-2` (region `us-east-2`).
2. Endpoint usado por la API:
   - `database-2.cvyimkqigl9v.us-east-2.rds.amazonaws.com`
3. Esquema y usuario de aplicacion definidos en script:
   - Archivo: `infra/hu2/rds-init.sql`

Contenido:

```sql
CREATE DATABASE IF NOT EXISTS practice_service;

CREATE USER IF NOT EXISTS 'practice_user'@'%' IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';
GRANT ALL PRIVILEGES ON practice_service.* TO 'practice_user'@'%';

FLUSH PRIVILEGES;

      #si es por MySql
USE practice_service;
SELECT identification_number, name, email
FROM persons
WHERE identification_number = 'TU_ID';

CloudShell
mysql -h database-2.cvyimkqigl9v.us-east-2.rds.amazonaws.com -P 3306 -u admin -p
```

Correr proyecvto con variable en prod

$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://database-2.cvyimkqigl9v.us-east-2.rds.amazonaws.com:3306/practice_service?createDatabaseIfNotExist=true&useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="YPGuPYVdkvHQKHHoqsjs"

echo $env:DB_URL
.\gradlew.bat bootRun


## 2.2 Requisito "accesible desde cualquier parte"

Se habilito acceso externo para pruebas (usuario desde IP publica y/o opcion public access en RDS), y luego se dejo conectividad de runtime por SG entre ECS y RDS:

- Regla correcta para runtime:
  - RDS SG inbound `3306` desde SG de ECS service.
- En el trabajo hubo tambien una regla por IP para pruebas locales.

## 2.3 Persistencia desde endpoints HU1

Endpoints que persisten/consultan:

- `POST /api/v1/personas/guardarpersona`
- `GET /api/v1/personas/consultarpersona/{identificationNumber}`

Validacion funcional:

1. POST crea/actualiza en tabla `persons`.
2. GET devuelve lo guardado.
3. Se valido tambien por ALB y por API Gateway.

---

## 3) Escaneo del proyecto: perfiles y properties

Archivos detectados:

- `src/main/resources/application.properties`
- `src/main/resources/application-prod.properties`
- `src/main/resources/application-test.properties`

## 3.1 Como funciona cada uno

1. `application.properties` (default/base)
   - Se usa siempre como base.
   - Si no hay perfil activo, queda este.
   - Tiene defaults para H2 en memoria.
   - Tambien define rutas API (`/api/v1/personas/...`) y docs (`/v3/api-docs`).

2. `application-prod.properties` (perfil prod)
   - Se activa con `SPRING_PROFILES_ACTIVE=prod`.
   - Fuerza driver/dialect MySQL.
   - Lee `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` desde variables de entorno.

3. `application-test.properties` (tests)
   - Usado por pruebas.
   - DB H2 de test.

## 3.2 Cuando usar cada uno

1. Local rapido sin RDS:
   - No setear profile (usa defaults H2).

2. Local contra RDS:
   - Setear profile prod y variables DB:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://database-2.cvyimkqigl9v.us-east-2.rds.amazonaws.com:3306/practice_service?createDatabaseIfNotExist=true&useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="<password>"
.\gradlew.bat bootRun
```

3. En ECS:
   - `SPRING_PROFILES_ACTIVE=prod`
   - DB vars via SSM/Secrets (HU5), no hardcode.

---

## 4) HU3 - Despliegue API en AWS

## 4.1 Verificacion de herramientas

```powershell
aws --version
docker --version
java --version
.\gradlew.bat -v
```

## 4.2 Build local (referencia)

```powershell
./gradlew.bat clean test
docker build -t practice-aws-api:v1 .
docker run --rm -p 8081:8081 practice-aws-api:v1
```

## 4.3 AWS CLI + variables

```powershell
aws configure
aws sts get-caller-identity

$env:AWS_REGION="us-east-2"
$env:ACCOUNT_ID="917714910836"
$env:ECR_REPO="practice-aws-api"
$env:IMAGE_TAG="v1"
$env:CLUSTER_NAME="practice-aws-cluster"
$env:SERVICE_NAME="practice-aws-service"
$env:TASK_FAMILY="practice-aws-task"
```

## 4.4 ECR

```powershell
aws ecr create-repository --repository-name $env:ECR_REPO --region $env:AWS_REGION
aws ecr describe-repositories --repository-names $env:ECR_REPO --region $env:AWS_REGION
```

## 4.5 Publicar imagen (ruta usada en la practica)

Por restricciones locales de Docker/virtualizacion y problemas de cache local de Gradle, se hizo build remoto con CodeBuild usando ZIP en S3.

Comandos usados para artefacto:

```powershell
$BUCKET="practice-aws-artifacts-917714910836-us-east-2"
aws s3 mb "s3://$BUCKET" --region us-east-2

Compress-Archive -Path .\src,.\gradle,.\gradlew,.\gradlew.bat,.\build.gradle,.\settings.gradle,.\Dockerfile,.\buildspec.yml -DestinationPath source.zip -Force
aws s3 cp .\source.zip "s3://$BUCKET/practice_aws_service/source.zip" --region us-east-2
aws s3 ls "s3://$BUCKET/practice_aws_service/"

# re-subida alternativa usada
tar -a -c -f source.zip src gradle gradlew gradlew.bat build.gradle settings.gradle Dockerfile buildspec.yml
aws s3 cp .\source.zip "s3://$BUCKET/practice_aws_service/source.zip" --region us-east-2
```

## 4.6 ECS task definition y service

Registro:

```powershell
aws ecs register-task-definition --cli-input-json file://infra/hu3/ecs-taskdef.json --region us-east-2
aws ecs describe-task-definition --task-definition practice-aws-task --region us-east-2
```

## 4.7 ALB + Target Group

Configuracion final:

1. ALB: `practice-aws-alb`, internet-facing, listener `HTTP:80`.
2. Target group: `practice-aws-tg`, IP target, port `8081`, health `/v3/api-docs`.
3. ECS service asociado al TG.
4. SG ALB con inbound `80` desde `0.0.0.0/0`.
5. SG ECS con inbound `8081` desde SG ALB.

## 4.8 API Gateway

API HTTP con stage `$default` y rutas:

- `POST /api/v1/personas/guardarpersona`
- `GET /api/v1/personas/consultarpersona/{identificationNumber}`

Integracion al ALB (base URI sin path fijo):

- `http://practice-aws-alb-390142950.us-east-2.elb.amazonaws.com`

---

## 5) HU4 - Cognito + JWT

## 5.1 User pool y clientes

Se uso User Pool (no Identity Pool).  
Se creo authorizer JWT en API Gateway y se adjunto a las 2 rutas.

Authorizer:

- Identity source: `$request.header.Authorization`
- Issuer: `https://cognito-idp.us-east-2.amazonaws.com/<USER_POOL_ID>`
- Audience: `<APP_CLIENT_ID>`

## 5.2 Comando usado para token (con SECRET_HASH)

```powershell
aws cognito-idp initiate-auth `
  --region us-east-2 `
  --client-id 450e6n98bksd3rr2fr4bod822n `
  --auth-flow USER_PASSWORD_AUTH `
  --auth-parameters USERNAME=angarp.dev@gmail.com,PASSWORD=ThomyLuan1826+,SECRET_HASH=$secretHash
```

Uso:

- Header: `Authorization: Bearer <IdToken>`

Validacion:

1. Sin token -> 401.
2. Token invalido -> 401.
3. Token valido -> acceso.

---

## 6) HU5 - Variables de entorno con SSM + Secrets

## 6.1 Parametros creados

Parameter Store:

- `/practice/api/DB_URL`
- `/practice/api/DB_USERNAME`

Secrets Manager:

- `practice/api/DB_PASSWORD`

## 6.2 Permisos IAM

En `ecsTaskExecutionRole` se agrego inline policy para:

- `ssm:GetParameter`, `ssm:GetParameters`
- `secretsmanager:GetSecretValue`

## 6.3 Task definition actualizada

Archivo: `infra/hu3/ecs-taskdef.json`

Estado final:

```json
"environment": [
  { "name": "SPRING_PROFILES_ACTIVE", "value": "prod" }
],
"secrets": [
  { "name": "DB_URL", "valueFrom": "/practice/api/DB_URL" },
  { "name": "DB_USERNAME", "valueFrom": "/practice/api/DB_USERNAME" },
  { "name": "DB_PASSWORD", "valueFrom": "practice/api/DB_PASSWORD" }
]
```

Comando usado:

```powershell
aws ecs register-task-definition --cli-input-json file://infra/hu3/ecs-taskdef.json --region us-east-2
```

---

## 7) HU6 - Logs y alertas

## 7.1 Logs

Log group en uso:

- `/ecs/practice-aws-api`

## 7.2 Alarmas (2 minimas)

1. ALB HTTP errors:
   - `HTTPCode_Target_4XX_Count` (o 5XX si aparece disponible)
   - threshold `>= 1`
2. ECS capacity:
   - `CPUUtilization`
   - threshold `>= 70`

Notificacion:

- SNS topic `alerts-practice-api`.

---

## 8) Troubleshooting relevante (resumen ejecutivo)

1. Docker local bloqueado por virtualizacion.
2. Gradle local con lock/caches en Windows.
3. `ecsTaskExecutionRole` inexistente o mal trust.
4. Log group faltante en CloudWatch.
5. Imagen/tag ECR mismatch (`v1` vs `V1`).
6. RDS connectivity timeout por reglas SG.
7. API Gateway 404 por rutas/integracion mal adjuntas.
8. Cognito challenges y alineacion app-client/audience.

---

## 9) Estado funcional esperado final

1. ECS service `practice-aws-service` activo con `1 running`.
2. Target group con `1 healthy`.
3. API responde por ALB y por API Gateway.
4. Rutas protegidas con JWT (HU4).
5. DB vars via SSM/Secrets (HU5).
6. Logs y 2 alarmas en CloudWatch (HU6).

