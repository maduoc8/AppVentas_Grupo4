# AppVentas – Grupo 4 (Caso 004D)

App Android (Kotlin + Jetpack Compose, MVVM + Room) para el registro móvil y la validación de ventas.

## Usuarios de prueba

La app tiene **2 perfiles**:

| Perfil | Usuario | Contraseña |
|---|---|---|
| Admin = Encargado de digitación | `admin` | `123` |
| Vendedor (precargado) | `vendedor` | `123456` |

El admin crea más vendedores con **Crear vendedor**; cada vendedor entra con su nombre (o correo) y su contraseña.

## Flujo

1. **Vendedor**: Registro de ventas → Nueva venta → convenio, cliente ficticio, fecha, monto, código de autorización (a mano o con QR) y foto de la boleta/vale → **Enviar venta**. Queda *En revisión*.
2. **Admin (encargado de digitación)**: Ventas recibidas → buscar / filtrar por estado → abrir la venta → revisar comprobante y validación del código → **Aprobar**, **Observar** o **Rechazar** (observar y rechazar piden el motivo).
3. Si queda *Observada*, el vendedor la corrige y la reenvía. Si queda *Validada*, se genera la **comisión simulada** (% del convenio).

Cada paso queda en el **historial de cambios** de la venta.

## Estructura (MVVM)

```
Pantalla (ui/screen) → ViewModel (viewmodel) → Repository (repository) → DAO (model/data/local) → Room
                                               ConvenioRepository → ApiService / RetrofitInstance (remote) → API REST
```

Room:
- `UsuarioDatabase`: vendedores creados por el admin
- `VentaDatabase`: ventas, clientes, convenios, estados, historial, comisiones

## API REST (catálogo de convenios)

`GET https://raw.githubusercontent.com/maduoc8/AppVentas_Grupo4/main/api/convenios.json`

Es el archivo `api/convenios.json` de este repositorio (funciona cuando está en `main`). Lo que llega se guarda en Room; si no hay internet, la app usa los convenios ya guardados.

## Pruebas

- Unitarias (Kotest + MockK): `./gradlew testDebugUnitTest`
  - `ReglasVentaTest`, `VentaViewModelTest`, `RevisionViewModelTest`, `ConvenioViewModelTest`
- Interfaz (con emulador abierto): `./gradlew connectedDebugAndroidTest`
  - `ConveniosScreenTest`
