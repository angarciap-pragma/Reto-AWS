# HU1 - API Personas + SonarLint

## Estado
- HU1 lista.
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
