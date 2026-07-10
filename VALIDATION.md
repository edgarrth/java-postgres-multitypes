# Validación de generación

Fecha de validación en sandbox: 2026-07-09.

## Validaciones realizadas en este entorno

Este sandbox no tiene Maven, Docker ni JDK 25 instalados, por lo que no puede ejecutar una compilación completa de Spring Boot 4.1.0 con Java 25. El entorno disponible contiene JDK 21, sin Maven ni Docker.

Se realizaron estas validaciones estáticas/locales:

- Parseo correcto de `pom.xml` como XML.
- Compilación sintáctica con `javac` de dominio + servicios de aplicación usando stubs mínimos de anotaciones Spring.
- Compilación sintáctica con `javac` de adaptadores PostgreSQL/messaging usando stubs mínimos de Spring/Jackson/pgjdbc para validar firmas, imports, records y métodos agregados.
- Verificación de existencia de estructura principal del proyecto.
- Verificación de incorporación de archivos nuevos para `LISTEN/NOTIFY`:
  - `datasets/flyway/V3__add_payment_event_listen_notify.sql`
  - `src/main/java/pe/axiz/paymentprocessing/infrastructure/adapter/out/messaging/PostgresPaymentEventNotificationListener.java`
  - `src/main/java/pe/axiz/paymentprocessing/infrastructure/adapter/out/persistence/JdbcPaymentEventNotificationRepository.java`
  - `src/main/java/pe/axiz/paymentprocessing/domain/model/PaymentEventNotification.java`
- Revisión de que el driver PostgreSQL JDBC ya no esté con scope `runtime`, porque `PGConnection` y `PGNotification` se usan en tiempo de compilación para el listener.
- Revisión de que Flyway contenga una migración separada para `LISTEN/NOTIFY`:
  - tabla `payment_event_notifications`
  - función `notify_payment_outbox_event()`
  - trigger `trg_notify_payment_outbox_event`
  - `pg_notify('payment_events', ...)`

## Validación completa recomendada

En una máquina con Docker instalado, ejecutar:

```bash
./scripts/maven-with-docker.sh clean verify
```

Luego levantar infraestructura y aplicación:

```bash
cd infraestructura
docker compose up -d
cd ..
./scripts/maven-with-docker.sh spring-boot:run
```

Para validar `LISTEN/NOTIFY`:

1. Ejecutar `POST /api/v1/payment-events` desde `infraestructura/http/payment-processing-api.http`.
2. Revisar logs de la aplicación. Debe aparecer un mensaje similar a:

```text
Received PostgreSQL NOTIFY action=EVENT_APPENDED eventId=... aggregateId=... eventType=... status=PENDING
```

3. Ejecutar:

```http
GET http://localhost:8080/api/v1/payment-events/notifications?limit=20
```

4. Debe retornar la notificación persistida en `payment_event_notifications`.


## Corrección aplicada por error de compilación reportado

Se recibió evidencia de `mvn clean verify` fallando en compilación porque no existían en el classpath los paquetes:

- `com.fasterxml.jackson.databind`
- `com.fasterxml.jackson.core.type`

Causa: el código usa Jackson 2 (`ObjectMapper`, `TypeReference`, `SerializationFeature`), pero el `pom.xml` no declaraba explícitamente Jackson. En Spring Boot 4 los módulos/starter están más desacoplados y no se debe asumir que `spring-boot-starter-webmvc` agregará automáticamente las APIs `com.fasterxml.jackson.*`.

Corrección aplicada al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-jackson2</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

Motivo:

- `spring-boot-jackson2` integra el soporte Boot para Jackson 2.
- `jackson-databind` aporta `ObjectMapper` y `SerializationFeature`.
- `jackson-datatype-jsr310` permite serializar/deserializar tipos Java Time usados en DTOs y modelos.

Validación disponible en este sandbox posterior a la corrección:

- `pom.xml` parsea correctamente como XML.
- Las dependencias requeridas aparecen en el `pom.xml`.
- El ZIP fue regenerado con la corrección.

Validación completa esperada en ambiente local con JDK 25 y Maven:

```bash
mvn clean verify
```

## Corrección adicional - Flyway PostgreSQL runtime

Se corrigió el error reportado en `mvn spring-boot:run`:

```text
Unsupported Database: PostgreSQL 16.14
```

Causa: el proyecto tenía `spring-boot-starter-flyway`, pero faltaba el módulo específico de base de datos `org.flywaydb:flyway-database-postgresql`, necesario para que Flyway 12 reconozca PostgreSQL en runtime.

Cambio aplicado en `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Validación local en este sandbox: no fue posible ejecutar `mvn spring-boot:run` porque no hay Maven ni JDK 25 instalados; sí se validó el parseo XML del `pom.xml` actualizado.
