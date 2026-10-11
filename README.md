# Calzado ERP

Aplicación web para administrar un catálogo de calzado, inventario, ventas, clientes y reclamos, con una tienda pública.

## Requisitos

- JDK 21 o superior
- Maven 3.9 o superior
- PostgreSQL 16 o superior

Crea la base `calzado_erp` en PostgreSQL. La aplicación crea y actualiza las tablas al iniciar.

## Configuración

Configura las variables del archivo `.env.example` en el entorno antes de iniciar. `DB_PASSWORD` e `INITIAL_ADMIN_PASSWORD` son obligatorias y no se guardan en el repositorio. El usuario inicial se crea solo si aún no existe; cambiar la variable posteriormente no restablece su contraseña.

Para cargar registros demostrativos en una base local, configura `DEMO_DATA_ENABLED=true` durante el arranque. El seeder es idempotente y no borra ni reemplaza registros. Déjalo desactivado en otros entornos.

## Ejecución

Desde PowerShell, después de configurar las variables:

```powershell
mvn spring-boot:run
```

- Tienda: <http://localhost:8080/tienda>
- Acceso de personal: <http://localhost:8080/login>
- Tablero: <http://localhost:8080/admin>

El checkout registra el pedido y descuenta inventario, pero no procesa pagos en línea.

## Pruebas

```powershell
mvn test
```

Las pruebas usan H2 y no requieren una conexión a PostgreSQL.