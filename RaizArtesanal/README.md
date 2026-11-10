# Raíz Artesanal — Android

Proyecto inicial nativo para Android, desarrollado con Kotlin y XML, basado en el documento técnico de planeación de Raíz Artesanal.

## Requisitos
- Android Studio reciente (Android Gradle Plugin 8.7.3).
- JDK 17.
- Android SDK Platform 35.
- Emulador o dispositivo Android 7.0 (API 24) o superior.

## Abrir
1. Descomprime el ZIP.
2. En Android Studio selecciona **Open** y elige la carpeta `RaizArtesanal`.
3. Espera la sincronización de Gradle.
4. Ejecuta la configuración `app` en un emulador o dispositivo.

Android Studio descargará las dependencias y el Gradle 8.9 si es necesario.

## Qué incluye esta versión
- Proyecto Gradle multimódulo, Kotlin y layouts XML.
- **Splash** con logo y nombre (tema de arranque terracota, sin destello blanco; en Android 12+ el splash del sistema muestra el mismo logo).
- **Login y registro** con el diseño de los bocetos (Google y huella son botones pendientes de configurar).
- **Comprador** (siguiendo los bocetos):
  - Inicio con buscador y catálogo en cuadrícula, y navegación inferior (Inicio, Carrito, Compras, Perfil).
  - Detalle del producto: nombre, precio, taller, descripción, selector de cantidad, stock y "Agregar al carrito".
  - Carrito: cambiar cantidades (respeta el stock), eliminar con opción de deshacer, total y estado vacío.
  - Pago: dirección, método (Tarjeta, PSE, Nequi), resumen con subtotal, envío y total, y confirmación del pedido.
  - Mis compras y Perfil básicos.
- Vendedor y Administrador conservan el panel de demostración anterior.

### Cómo probar la demo
- Cualquier correo válido y contraseña entra como **Comprador**.
- Un correo que empiece por `vendedor` (ej. `vendedor@correo.com`) abre el panel de vendedor; uno que empiece por `admin`, el de administrador.

## Estructura
- `app`: punto de entrada y navegación inicial.
- `core/*`: módulos compartidos para UI, datos, red, seguridad y dispositivo.
- `feature/*`: autenticación, catálogo, carrito, pagos, perfil y administración.

## Importante: alcance de esta entrega
Este ZIP es una **base funcional de interfaz y navegación**, no una tienda lista para producción. Las integraciones reales con Firebase Authentication/Firestore/Storage, Google Sign-In, Wompi, CameraX, ubicación, biometría, persistencia Room, APIs, validación de webhooks, control de roles del lado servidor y cumplimiento operativo/legal quedan para las siguientes etapas. No se deben introducir credenciales ni claves secretas en el APK.

## Datos de demostración
Productos, carrito y pedidos viven **en memoria** (`app/.../data`): se pierden al cerrar la app. Las fotos son un marcador de posición; el envío es un valor fijo de $14.000 y el pago es **simulado** (no hay cobro). Todo esto se reemplazará por Firestore/Storage, Room y Wompi.

## Paleta
- Terracota: `#B5482A`
- Terracota oscuro: `#8F3519`
- Marrón tierra: `#2E211A`
- Crema: `#FAF3E8`
- Verde azulado: `#1E7C70`
- Dorado: `#E0A526`
- Arena (fondos de selección y botón de huella): `#F4E2D3`
- Error: `#C0392B`
- Éxito: `#2E8B57`
