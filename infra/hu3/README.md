# HU3 - Despliegue API en AWS (ECR + ECS + ALB + API Gateway)

## Variables (PowerShell)
```powershell
$env:AWS_REGION="us-east-2"
$env:ACCOUNT_ID="<TU_ACCOUNT_ID>"
$env:ECR_REPO="practice-aws-api"
$env:IMAGE_TAG="v1"
$env:CLUSTER_NAME="practice-aws-cluster"
$env:SERVICE_NAME="practice-aws-service"
$env:TASK_FAMILY="practice-aws-task"
```

## 1) Construir imagen Docker
```powershell
./gradlew.bat clean test

docker build -t ${env:ECR_REPO}:${env:IMAGE_TAG} .
```

## 2) Publicar imagen en ECR
```powershell
aws ecr create-repository --repository-name $env:ECR_REPO --region $env:AWS_REGION

aws ecr get-login-password --region $env:AWS_REGION |
  docker login --username AWS --password-stdin "$($env:ACCOUNT_ID).dkr.ecr.$($env:AWS_REGION).amazonaws.com"

docker tag ${env:ECR_REPO}:${env:IMAGE_TAG} "$($env:ACCOUNT_ID).dkr.ecr.$($env:AWS_REGION).amazonaws.com/$($env:ECR_REPO):$($env:IMAGE_TAG)"

docker push "$($env:ACCOUNT_ID).dkr.ecr.$($env:AWS_REGION).amazonaws.com/$($env:ECR_REPO):$($env:IMAGE_TAG)"
```

## 3) Aprovisionar ECS (Fargate)
1. Crear `ECS Cluster` llamado `practice-aws-cluster`.
2. Crear `Task execution role` (`ecsTaskExecutionRole`) con política `AmazonECSTaskExecutionRolePolicy`.
3. Crear CloudWatch log group:
```powershell
aws logs create-log-group --log-group-name /ecs/practice-aws-api --region $env:AWS_REGION
```
4. Editar `infra/hu3/ecs-taskdef.json` y reemplazar placeholders:
- `<ACCOUNT_ID>`
- `<REGION>`
- `<IMAGE_TAG>`
- `<RDS_ENDPOINT>`
- `<DB_PASSWORD>`

5. Registrar task definition:
```powershell
aws ecs register-task-definition --cli-input-json file://infra/hu3/ecs-taskdef.json --region $env:AWS_REGION
```

## 4) Crear ALB y desplegar servicio ECS
1. Crear Security Groups:
- `sg-alb`: inbound `80` desde `0.0.0.0/0`.
- `sg-ecs`: inbound `8081` solo desde `sg-alb`.
- En RDS SG: inbound `3306` solo desde `sg-ecs`.

2. Crear Application Load Balancer (internet-facing).
3. Crear Target Group tipo `IP`, puerto `8081`, health check path `/v3/api-docs`.
4. Crear ECS Service (Fargate):
- Cluster: `practice-aws-cluster`
- Task: `practice-aws-task`
- Desired tasks: `1`
- Network: subnets públicas o privadas con NAT
- SG del servicio: `sg-ecs`
- Attach al target group del ALB

## 5) Configurar API Gateway (2 endpoints)
Usa `HTTP API`.

1. Crear HTTP API.
2. Integration type: `HTTP`, URL base al ALB:
`http://<ALB_DNS>`
3. Crear rutas:
- `POST /api/v1/personas/guardarpersona`
- `GET /api/v1/personas/consultarpersona/{identificationNumber}`
4. Deployment stage: `prod`.

## 6) Validar persistencia en RDS
1. POST:
```bash
curl -i -X POST "https://<API_ID>.execute-api.<REGION>.amazonaws.com/prod/api/v1/personas/guardarpersona" \
  -H "Content-Type: application/json" \
  -d '{"identificationNumber":"1001","name":"Andrea Garcia","email":"andrea@demo.com"}'
```
2. GET:
```bash
curl -i "https://<API_ID>.execute-api.<REGION>.amazonaws.com/prod/api/v1/personas/consultarpersona/1001"
```
3. Validar en RDS:
```sql
SELECT identification_number, name, email FROM practice_service.persons WHERE identification_number='1001';
```

## 7) Checklist HU3
- Dockerfile funcional
- Imagen publicada en ECR
- ECS + Service activos
- ALB enruta al servicio
- API Gateway expone ambos endpoints
- Datos persisten en RDS
- RDS no expuesto a internet; solo SG de ECS
