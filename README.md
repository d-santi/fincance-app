# Finance

Aplicación Android de finanzas personales para registrar gastos, definir presupuestos mensuales por categoría y dividir gastos compartidos.

## Stack

- Kotlin
- Jetpack Compose (Material 3)
- Room (SQLite local)
- Navigation Compose
- ViewModel + StateFlow

## Funcionalidades

- **Autenticación:** registro e inicio de sesión por usuario (datos locales).
- **Gastos:** alta, edición, eliminación, filtros (todos, recurrentes, categoría, rango de fechas), total del mes.
- **Presupuesto:** límites por categoría y mes, comparación con gasto real y barra de progreso.
- **Compartidos:** división entre participantes, marcar pagos, liquidar, filtro de pendientes.

## Cómo ejecutar

1. Abre el proyecto en Android Studio.
2. Sincroniza Gradle.
3. Ejecuta en emulador o dispositivo (minSdk 24).

```bash
./gradlew assembleDebug
```

## Estructura

```
app/src/main/java/com/example/financeapp/
├── data/local/     # Room, DAOs, sesión
├── domain/model/   # Entidades
├── viewmodel/      # Lógica de pantalla
└── ui/             # Compose: theme, components, screens, navigation
```

## Limitaciones conocidas

- Los datos se guardan solo en el dispositivo.
- Las contraseñas se almacenan en texto plano (adecuado para proyecto académico; no usar en producción).

## Pruebas manuales sugeridas

1. Registro → gastos → cerrar sesión → login.
2. Crear, editar y eliminar un gasto; probar filtros.
3. Crear presupuesto, ver progreso, cambiar de mes.
4. Crear gasto compartido con 2+ participantes, marcar pagos y liquidar.
5. Cerrar y reabrir la app: la sesión debe mantenerse.
