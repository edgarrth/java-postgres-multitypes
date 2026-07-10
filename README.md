# Axiz Payment Processing PostgreSQL PoC

Microservicio REST de demostración para una fintech de pagos. La PoC implementa **DDD táctico** con **arquitectura hexagonal**, **Java 25**, **Spring Boot 4.1.0**, **Spring MVC**, **JDBC**, **Flyway** y **PostgreSQL + pgvector**.

El objetivo es demostrar, en un único microservicio, seis capacidades de PostgreSQL aplicadas a payment processing:

| Capacidad PostgreSQL | Tabla dedicada | Caso de uso fintech |
|---|---|---|
| `tsvector` | `payment_search_documents` | Búsqueda full-text de pagos, conciliaciones, canales y descripciones operativas. |
| `pgvector` | `payment_semantic_rules` | Búsqueda semántica de reglas de enrutamiento o decisión de pagos. |
| PostgreSQL como cache | `payment_cache_entries` | Cache temporal de decisiones de riesgo, tasas o datos de comercios. |
| `jsonb` | `payment_profiles` | Perfil flexible de comercio/cliente con atributos variables. |
| Eventos / outbox | `payment_outbox_events` | Persistencia transaccional de eventos de dominio para publicación posterior. |
| `bytea` | `payment_digital_certificates` | Almacenamiento de certificados o llaves públicas PEM de procesadores de pago. |

## Decisiones técnicas principales

- **Java 25** definido en `pom.xml` con `maven.compiler.release=25`.
- **Spring Boot 4.1.0** como versión base.
- **Spring MVC** mediante `spring-boot-starter-webmvc`, evitando el starter clásico `spring-boot-starter-web`.
- **JDBC + JdbcTemplate**, porque permite usar capacidades nativas de PostgreSQL (`tsvector`, `vector`, `jsonb`, `bytea`) sin forzar un ORM donde no aporta valor en la PoC.
- **Flyway como único mecanismo de schema + seed data**. No hay scripts duplicados de inicialización en Docker.
- **Infraestructura mínima**: solo PostgreSQL con pgvector. No se agrega broker porque el caso de eventos se implementa con patrón outbox en PostgreSQL.

## Estructura del proyecto

```text
axiz-payment-processing-poc/
├── README.md
├── pom.xml
├── datasets/
│   ├── flyway/
│   │   ├── V1__create_payment_processing_schema.sql
│   │   └── V2__seed_payment_processing_data.sql
│   ├── sample-payment-certificate.pem
│   └── sample-payment-profile.json
├── infraestructura/
│   ├── docker-compose.yml
│   └── http/
│       └── payment-processing-api.http
├── scripts/
│   └── maven-with-docker.sh
└── src/
    ├── main/
    │   ├── java/pe/axiz/paymentprocessing/
    │   │   ├── PaymentProcessingApplication.java
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   ├── port/in/
    │   │   │   ├── port/out/
    │   │   │   └── exception/
    │   │   ├── application/service/
    │   │   └── infrastructure/adapter/
    │   │       ├── in/rest/
    │   │       └── out/persistence/
    │   └── resources/application.yml
    └── test/java/pe/axiz/paymentprocessing/application/service/
```

## Diagrama de arquitectura

```mermaid
flowchart LR
    Client[Cliente REST / HTTP file] --> Rest[REST Controllers]
    Rest --> InPorts[Puertos de entrada / Use cases]
    InPorts --> App[Servicios de aplicación]
    App --> Domain[Modelo de dominio]
    App --> OutPorts[Puertos de salida]
    OutPorts --> Jdbc[Adaptadores JDBC]
    Jdbc --> Pg[(PostgreSQL + pgvector)]

    subgraph Hexagonal Architecture
        Rest
        InPorts
        App
        Domain
        OutPorts
        Jdbc
    end

    subgraph PostgreSQL capabilities
        Pg --> Tsvector[tsvector]
        Pg --> Vector[pgvector]
        Pg --> Cache[unlogged cache table]
        Pg --> Jsonb[jsonb]
        Pg --> Events[outbox events]
        Pg --> Bytea[bytea PEM]
    end
```

## Estructura DDD + Hexagonal

### Dominio

Ubicación: `src/main/java/pe/axiz/paymentprocessing/domain`

Contiene el lenguaje del negocio y no depende de Spring:

- `SearchDocument`: documento operacional de pagos indexado con `tsvector`.
- `SemanticRule`: regla de ruteo/decisión con embedding en `pgvector`.
- `CacheEntry`: entrada temporal de cache basada en PostgreSQL.
- `PaymentProfile`: perfil flexible de comercio/cliente usando `jsonb`.
- `PaymentOutboxEvent`: evento de dominio persistido como outbox.
- `DigitalCertificate`: certificado/llave pública PEM almacenado como `bytea`.

### Puertos de entrada

Ubicación: `domain/port/in`

Definen los casos de uso consumidos por la capa REST:

- `PaymentSearchUseCase`
- `SemanticRoutingUseCase`
- `PaymentCacheUseCase`
- `PaymentProfileUseCase`
- `PaymentEventUseCase`
- `DigitalCertificateUseCase`

### Servicios de aplicación

Ubicación: `application/service`

Implementan los casos de uso, validaciones de aplicación y coordinación con los puertos de salida:

- `PaymentSearchService`
- `SemanticRoutingService`
- `PaymentCacheService`
- `PaymentProfileService`
- `PaymentEventService`
- `DigitalCertificateService`
- `DeterministicEmbeddingService`

`DeterministicEmbeddingService` crea embeddings determinísticos de 6 dimensiones con SHA-256. En producción, este componente se reemplazaría por un modelo real o servicio de embeddings, manteniendo intacto el puerto/repositorio.

### Adaptadores de entrada REST

Ubicación: `infrastructure/adapter/in/rest`

Exponen endpoints REST, DTOs, validaciones de request y manejo uniforme de errores.

### Adaptadores de salida PostgreSQL

Ubicación: `infrastructure/adapter/out/persistence`

Implementan los repositorios con `JdbcTemplate` y SQL nativo para aprovechar funcionalidades específicas de PostgreSQL.

## Infraestructura mínima

La carpeta `infraestructura` contiene solo lo necesario para probar el concepto:

```text
infraestructura/
├── docker-compose.yml
└── http/payment-processing-api.http
```

El `docker-compose.yml` levanta únicamente:

- PostgreSQL 16 con extensión pgvector preinstalada mediante imagen `pgvector/pgvector:0.8.5-pg16`.

No se agrega Kafka, RabbitMQ, Redis ni herramientas adicionales porque no son necesarias para validar las seis capacidades solicitadas.

## Datasets y precarga de datos

La carpeta `datasets` contiene:

```text
datasets/
├── flyway/
│   ├── V1__create_payment_processing_schema.sql
│   └── V2__seed_payment_processing_data.sql
├── sample-payment-certificate.pem
└── sample-payment-profile.json
```

La precarga se ejecuta automáticamente con Flyway al iniciar el microservicio:

- `V1__create_payment_processing_schema.sql`: crea extensión `vector`, tablas e índices.
- `V2__seed_payment_processing_data.sql`: inserta datos demo para las seis funcionalidades.

En `pom.xml`, la carpeta `datasets/flyway` se copia al classpath como `db/migration`, por eso Spring Boot/Flyway la detecta sin duplicar scripts.

## Cómo levantar la base de datos

Desde la raíz del proyecto:

```bash
cd infraestructura
docker compose up -d
```

Validar estado:

```bash
docker compose ps
```

Conectarse manualmente:

```bash
docker exec -it axiz-payment-postgres psql -U axiz -d axiz_payments
```

## Cómo compilar

Con Maven local y JDK 25:

```bash
mvn clean verify
```

Alternativa sin instalar Maven/JDK local, usando Docker:

```bash
./scripts/maven-with-docker.sh clean verify
```

Ese script usa la imagen `maven:3.9.11-eclipse-temurin-25`.

## Cómo ejecutar el microservicio

Primero levantar PostgreSQL:

```bash
cd infraestructura
docker compose up -d
cd ..
```

Luego ejecutar la aplicación:

```bash
mvn spring-boot:run
```

O con Docker para Maven/JDK:

```bash
./scripts/maven-with-docker.sh spring-boot:run
```

Variables soportadas:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/axiz_payments` |
| `DB_USERNAME` | `axiz` |
| `DB_PASSWORD` | `axiz_secret` |
| `SERVER_PORT` | `8080` |

## Endpoints en orden de ejecución recomendado

| Orden | Capacidad | Método | Endpoint | Descripción funcional | Descripción técnica |
|---:|---|---|---|---|---|
| 1 | `tsvector` | `POST` | `/api/v1/payment-search-documents` | Registra un documento operacional de pago para búsqueda. | Inserta en `payment_search_documents`; la columna `search_vector` se calcula automáticamente como `tsvector`. |
| 2 | `tsvector` | `GET` | `/api/v1/payment-search-documents?query=pago qr comercio` | Busca pagos o conciliaciones por texto. | Usa `websearch_to_tsquery('spanish', ?)` y ranking con `ts_rank_cd`. |
| 3 | `pgvector` | `POST` | `/api/v1/payment-semantic-rules` | Registra una regla de ruteo semántico. | Genera embedding determinístico de 6 dimensiones e inserta en columna `vector(6)`. |
| 4 | `pgvector` | `GET` | `/api/v1/payment-semantic-rules/nearest?text=pagos tarjeta&limit=3` | Encuentra reglas parecidas al texto enviado. | Usa operador de distancia coseno `<=>` sobre `pgvector`. |
| 5 | Cache | `PUT` | `/api/v1/payment-cache/{cacheKey}` | Guarda una decisión temporal de riesgo/tasa/comercio. | Hace upsert en tabla `UNLOGGED payment_cache_entries` con TTL. |
| 6 | Cache | `GET` | `/api/v1/payment-cache/{cacheKey}` | Consulta una entrada temporal. | Lee por PK y valida expiración por `expires_at`. |
| 7 | Cache | `DELETE` | `/api/v1/payment-cache/expired` | Elimina entradas vencidas. | Ejecuta limpieza física de registros expirados. |
| 8 | `jsonb` | `POST` | `/api/v1/payment-profiles` | Registra perfil flexible de comercio/cliente. | Inserta `attributes` como `jsonb`. |
| 9 | `jsonb` | `GET` | `/api/v1/payment-profiles?attributeName=riskLevel&value=LOW` | Busca perfiles por atributo variable. | Consulta `attributes ->> ? = ?` e índice GIN para consultas JSONB. |
| 10 | Eventos | `POST` | `/api/v1/payment-events` | Registra un evento de dominio de pagos. | Inserta en `payment_outbox_events` con estado `PENDING`. |
| 11 | Eventos | `GET` | `/api/v1/payment-events?status=PENDING` | Lista eventos pendientes de publicación. | Filtra por estado e índice `(status, created_at)`. |
| 12 | Eventos | `PATCH` | `/api/v1/payment-events/{id}/published` | Marca un evento como publicado. | Actualiza `status='PUBLISHED'` y `published_at=now()`. |
| 13 | `bytea` | `POST` | `/api/v1/payment-certificates` | Guarda certificado/llave PEM de procesador. | Decodifica Base64 y persiste bytes en `bytea`; calcula fingerprint SHA-256. |
| 14 | `bytea` | `GET` | `/api/v1/payment-certificates` | Lista metadatos de certificados. | Retorna alias, owner, algoritmo, fingerprint y tamaño. |
| 15 | `bytea` | `GET` | `/api/v1/payment-certificates/{alias}?includePem=true` | Consulta certificado por alias. | Lee `bytea` y opcionalmente retorna PEM como texto. |

## Request/Response rápido

También puedes usar `infraestructura/http/payment-processing-api.http` desde IntelliJ IDEA, VS Code REST Client o cualquier cliente compatible.

### 1. Insertar documento `tsvector`

Request:

```http
POST /api/v1/payment-search-documents
Content-Type: application/json

{
  "paymentReference": "PAY-QR-9001",
  "channel": "QR",
  "description": "Pago QR aprobado para comercio veterinaria con liquidación inmediata"
}
```

Response esperado:

```json
{
  "id": "uuid",
  "paymentReference": "PAY-QR-9001",
  "channel": "QR",
  "description": "Pago QR aprobado para comercio veterinaria con liquidación inmediata",
  "createdAt": "2026-07-09T..."
}
```

### 2. Buscar con `tsvector`

```http
GET /api/v1/payment-search-documents?query=pago qr comercio
```

Resultado: lista de documentos más relevantes según ranking full-text.

### 3. Insertar regla `pgvector`

```http
POST /api/v1/payment-semantic-rules
Content-Type: application/json

{
  "ruleCode": "ROUTE-CARD-LOW-RISK",
  "title": "Ruta tarjeta bajo riesgo",
  "description": "Procesar pagos con tarjeta de bajo monto usando adquirente principal y autorización inmediata"
}
```

Resultado: regla creada con embedding determinístico.

### 4. Buscar reglas cercanas con `pgvector`

```http
GET /api/v1/payment-semantic-rules/nearest?text=pagos con tarjeta de monto bajo&limit=3
```

Resultado: lista ordenada por menor distancia semántica.

### 5. Insertar cache

```http
PUT /api/v1/payment-cache/merchant-risk:MRC-2001
Content-Type: application/json

{
  "ttlSeconds": 3600,
  "value": {
    "merchantId": "MRC-2001",
    "riskLevel": "LOW",
    "decision": "ALLOW"
  }
}
```

### 6. Consultar cache

```http
GET /api/v1/payment-cache/merchant-risk:MRC-2001
```

### 7. Insertar perfil `jsonb`

```http
POST /api/v1/payment-profiles
Content-Type: application/json

{
  "profileCode": "PROFILE-MERCHANT-NEW",
  "documentNumber": "20445566778",
  "attributes": {
    "customerSegment": "SME",
    "merchantCategory": "retail",
    "riskLevel": "MEDIUM",
    "settlementMode": "T1"
  }
}
```

### 8. Consultar perfil `jsonb`

```http
GET /api/v1/payment-profiles?attributeName=riskLevel&value=LOW
```

### 9. Crear evento outbox

```http
POST /api/v1/payment-events
Content-Type: application/json

{
  "aggregateId": "PAY-QR-9001",
  "eventType": "PaymentAuthorized",
  "payload": {
    "paymentReference": "PAY-QR-9001",
    "amount": 150.90,
    "currency": "PEN",
    "channel": "QR"
  }
}
```

### 10. Guardar certificado PEM en `bytea`

```http
POST /api/v1/payment-certificates
Content-Type: application/json

{
  "alias": "processor-secondary-public-key",
  "owner": "Secondary Payment Processor",
  "algorithm": "RSA",
  "base64Pem": "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZ3d0RRWUpLb1pJaHZjTkFRRUJCUUFEU3dBd1NBSkJBTHYybkRlbW9Pbmx5Rm9yQXhpelBvY05vdFJlYWxLZXkKOVFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg=="
}
```

## Cómo testear

### Tests automatizados

Ejecutar:

```bash
mvn test
```

O con Docker:

```bash
./scripts/maven-with-docker.sh test
```

Tests incluidos:

| Test | Qué valida |
|---|---|
| `DeterministicEmbeddingServiceTest` | Verifica que el embedding sea estable y de 6 dimensiones. |
| `PaymentCacheServiceTest` | Verifica escritura/lectura de cache y error cuando no existe. |
| `DigitalCertificateServiceTest` | Verifica cálculo de fingerprint SHA-256 y almacenamiento del contenido PEM como bytes. |

### Tests manuales end-to-end

1. Levanta PostgreSQL con Docker Compose.
2. Ejecuta la aplicación.
3. Abre `infraestructura/http/payment-processing-api.http`.
4. Ejecuta los requests en orden.

Qué se prueba en cada grupo:

- Requests 1-2: inserción y búsqueda full-text con `tsvector`.
- Requests 3-4: inserción y búsqueda semántica con `pgvector`.
- Requests 5-6: escritura y lectura de cache con TTL.
- Requests 7-8: escritura y consulta flexible con `jsonb`.
- Requests 9-10: creación y lectura de eventos outbox.
- Requests 11-12: persistencia y consulta de PEM en `bytea`.

## Consultas SQL útiles

Ver tablas:

```sql
\dt
```

Revisar documentos full-text:

```sql
select payment_reference, channel, description, search_vector
from payment_search_documents;
```

Revisar embeddings:

```sql
select rule_code, embedding
from payment_semantic_rules;
```

Revisar cache:

```sql
select cache_key, cache_value, expires_at
from payment_cache_entries;
```

Revisar perfiles JSONB:

```sql
select profile_code, attributes
from payment_profiles
where attributes ->> 'riskLevel' = 'LOW';
```

Revisar eventos:

```sql
select aggregate_id, event_type, status, payload
from payment_outbox_events;
```

Revisar certificados:

```sql
select alias, owner, algorithm, fingerprint_sha256, octet_length(pem_content) as size_bytes
from payment_digital_certificates;
```

## Notas productivas y futuras mejoras

Para mantener la PoC enfocada, no se agregaron componentes innecesarios. En una implementación productiva se recomendaría agregar, según necesidad real:

- Publicador real de outbox hacia Kafka, RabbitMQ, SQS/SNS o EventBridge.
- Servicio real de embeddings para `pgvector`.
- Seguridad OAuth2/JWT, mTLS o integración CIAM.
- Observabilidad con OpenTelemetry.
- Testcontainers para pruebas de integración con PostgreSQL real.
- Separación de secretos con Vault, AWS Secrets Manager o equivalente.
