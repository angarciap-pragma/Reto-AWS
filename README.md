# HU1 + HU2 - API Personas, SonarLint y persistencia relacional

## Estado
- HU1 lista.
- HU2 implementada en codigo (persistencia relacional via JPA).
- Endpoints implementados:
  - `POST /api/v1/personas/guardarpersona`
  - `GET /api/v1/personas/consultarpersona/{identificationNumber}`
- Datos por persona:
  - `identificationNumber`
  - `name`
  - `email`

## SonarLint (IntelliJ)
1. Instalar plugin `SonarLint`.
2. Abrir el proyecto y ejecutar `Analyze -> Inspect Code` o revisar problemas en tiempo real.
3. Corregir issues y validar con:
   - `./gradlew test`

## Pruebas automatizadas incluidas
- Guardar y consultar persona correctamente.
- Validacion de payload invalido.
- Validacion de `identificationNumber` en path.
- Conflicto cuando la persona ya existe (`409`).
- No encontrado cuando la persona no existe (`404`).

## Ejemplos curl
```bash
curl -i -X POST http://localhost:8080/api/v1/personas/guardarpersona \
  -H "Content-Type: application/json" \
  -d '{"identificationNumber":"1001","name":"Andrea Garcia","email":"andrea@demo.com"}'
```

```bash
curl -i http://localhost:8080/api/v1/personas/consultarpersona/1001
```

## HU2 - RDS (MySQL) y persistencia
La aplicacion ahora usa `spring-boot-starter-data-jpa` y guarda personas en tabla relacional `persons`.

### Configuracion para RDS
1. Crear RDS MySQL con acceso publico habilitado.
2. Permitir en el Security Group el puerto `3306` desde tu IP publica (o CIDR requerido).
3. Conectarte con el usuario master de RDS y ejecutar el script:
   - `infra/hu2/rds-init.sql`
4. Levantar el microservicio con perfil `prod` y variables:

```bash
set SPRING_PROFILES_ACTIVE=prod
set DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/practice_service?useSSL=true&requireSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set DB_USERNAME=practice_user
set DB_PASSWORD=<TU_PASSWORD>
./gradlew bootRun
```

### Notas
- En `prod`, la app usa MySQL (`application-prod.properties`).
- En local/test se usa H2 por defecto.
