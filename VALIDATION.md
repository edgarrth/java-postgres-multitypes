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
