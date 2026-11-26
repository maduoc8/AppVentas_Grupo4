package com.example.appventas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.VentaErrores
import com.example.appventas.model.VentaUiState
import com.example.appventas.model.data.local.ClienteEntity
import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.esEditable
import com.example.appventas.model.esFechaValida
import com.example.appventas.model.fechaDeHoy
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.repository.VentaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel -> Repository -> DAO -> Room (la pantalla nunca toca la base de datos)
// Los repositorios llegan por el constructor: así se pueden simular en las pruebas unitarias
class VentaViewModel(
    private val repository: VentaRepository,
    private val convenioRepository: ConvenioRepository
) : ViewModel() {

    // Estado interno mutable
    private val _estado = MutableStateFlow(VentaUiState(fechaOperacion = fechaDeHoy()))

    // Estado expuesto para la UI
    val estado: StateFlow<VentaUiState> = _estado

    // Escucha la lista de ventas del vendedor conectado
    private var listaJob: Job? = null

    init {
        // Catálogos de Room para el formulario: convenios, clientes ficticios y estados
        viewModelScope.launch {
            repository.cargarDatosIniciales()
            convenioRepository.cargarConveniosIniciales()
        }
        viewModelScope.launch {
            convenioRepository.obtenerConvenios().collect { lista ->
                _estado.update { it.copy(convenios = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerClientes().collect { lista ->
                _estado.update { it.copy(clientes = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEstados().collect { lista ->
                _estado.update { it.copy(estados = lista) }
            }
        }
    }

    // ---------- Sesión del vendedor ----------

    // Se llama al entrar al inicio del vendedor: carga solo sus ventas
    fun iniciarVendedor(nombre: String, correo: String) {
        if (_estado.value.vendedorCorreo == correo && listaJob != null) return

        listaJob?.cancel()
        _estado.update {
            it.formularioVacio().copy(
                vendedorNombre = nombre,
                vendedorCorreo = correo,
                ventas = emptyList(),
                busqueda = "",
                filtroEstado = null
            )
        }

        // Flow de Room: la lista se actualiza sola al insertar o modificar
        listaJob = viewModelScope.launch {
            repository.obtenerVentasPorVendedor(correo).collect { lista ->
                _estado.update { it.copy(ventas = lista) }
            }
        }
    } // fin iniciarVendedor

    fun cerrarSesion() {
        listaJob?.cancel()
        listaJob = null
        _estado.update {
            it.formularioVacio().copy(
                vendedorNombre = "",
                vendedorCorreo = "",
                ventas = emptyList(),
                busqueda = "",
                filtroEstado = null
            )
        }
    } // fin cerrarSesion

    // ---------- Eventos del formulario ----------

    // Convenio elegido del catálogo (Room / API)
    fun onConvenioSeleccionado(convenio: ConvenioEntity) {
        _estado.update {
            it.copy(
                convenioId = convenio.id,
                tipoServicio = convenio.nombre,
                errores = it.errores.copy(tipoServicio = null)
            )
        }
    } // fin onConvenioSeleccionado

    fun onFechaChange(valor: String) {
        _estado.update {
            it.copy(
                fechaOperacion = valor,
                errores = it.errores.copy(fecha = null)
            )
        }
    } // fin onFechaChange

    // Cliente ficticio elegido de la tabla clientes
    fun onClienteSeleccionado(cliente: ClienteEntity) {
        _estado.update {
            it.copy(
                cliente = cliente.nombre,
                errores = it.errores.copy(cliente = null)
            )
        }
    } // fin onClienteSeleccionado

    fun onMontoChange(valor: String) {
        _estado.update {
            it.copy(
                monto = valor,
                errores = it.errores.copy(monto = null)
            )
        }
    } // fin onMontoChange

    fun onCodigoChange(valor: String) {
        _estado.update {
            it.copy(
                codigoAutorizacion = valor,
                errores = it.errores.copy(codigo = null)
            )
        }
    } // fin onCodigoChange

    fun onObservacionesChange(valor: String) {
        _estado.update { it.copy(observaciones = valor) }
    } // fin onObservacionesChange

    // Foto de la boleta o vale (ruta del archivo guardado en el teléfono)
    fun onComprobanteChange(ruta: String?) {
        _estado.update {
            it.copy(
                comprobante = ruta,
                errores = it.errores.copy(comprobante = null)
            )
        }
    } // fin onComprobanteChange

    fun onBusquedaChange(valor: String) {
        _estado.update { it.copy(busqueda = valor) }
    } // fin onBusquedaChange

    // null = mostrar todos los estados
    fun onFiltroEstadoChange(estado: String?) {
        _estado.update { it.copy(filtroEstado = estado) }
    } // fin onFiltroEstadoChange

    // La cámara leyó un QR: se usa como código de autorización y queda como respaldo
    fun onQrDetectado(contenido: String) {
        val codigo = contenido.trim()
        _estado.update {
            it.copy(
                codigoAutorizacion = codigo,
                respaldo = "QR: $codigo",
                errores = it.errores.copy(codigo = null)
            )
        }
    } // fin onQrDetectado

    // ---------- Validación ----------

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val monto = actual.monto.trim().toDoubleOrNull()

        val errores = VentaErrores(
            tipoServicio = if (actual.tipoServicio.isBlank())
                "Debe elegir un convenio o servicio"
            else null,

            fecha = when {
                actual.fechaOperacion.isBlank() -> "Fecha obligatoria"
                !esFechaValida(actual.fechaOperacion) -> "Fecha inválida (use dd/mm/aaaa)"
                else -> null
            },

            cliente = if (actual.cliente.isBlank())
                "Debe elegir un cliente"
            else null,

            monto = when {
                actual.monto.isBlank() -> "Monto obligatorio"
                monto == null -> "El monto debe ser numérico"
                monto <= 0 -> "El monto debe ser mayor que 0"
                else -> null
            },

            codigo = when {
                actual.codigoAutorizacion.isBlank() -> "Código obligatorio"
                actual.codigoAutorizacion.trim().contains(" ") -> "El código no puede tener espacios"
                else -> null
            },

            comprobante = if (actual.comprobante == null)
                "Debe adjuntar la foto del comprobante"
            else null
        )

        val hayErrores = listOfNotNull(
            errores.tipoServicio,
            errores.fecha,
            errores.cliente,
            errores.monto,
            errores.codigo,
            errores.comprobante
        ).isNotEmpty()

        _estado.update {
            it.copy(errores = errores)
        }

        return !hayErrores
    } // fin validarFormulario

    // ---------- Lista ----------

    // Deja el formulario vacío (botón "Nueva venta")
    fun iniciarNuevaVenta() {
        _estado.update { it.formularioVacio() }
    } // fin iniciarNuevaVenta

    // Pasa una venta de la lista al formulario (solo si no está validada ni rechazada)
    fun cargarVentaParaEditar(venta: VentaEntity) {
        iniciarNuevaVenta()

        if (!venta.esEditable()) {
            _estado.update {
                it.copy(errorGeneral = "Una venta ${venta.estado.lowercase()} no se puede modificar")
            }
            return
        }

        _estado.update {
            it.copy(
                ventaSeleccionadaId = venta.id,
                convenioId = venta.convenioId,
                tipoServicio = venta.tipoServicio,
                fechaOperacion = venta.fechaOperacion,
                cliente = venta.cliente,
                // 25990.0 -> "25990" (sin decimales si es un monto entero)
                monto = if (venta.monto % 1.0 == 0.0) venta.monto.toLong().toString()
                else venta.monto.toString(),
                codigoAutorizacion = venta.codigoAutorizacion,
                observaciones = venta.observaciones,
                respaldo = venta.respaldo,
                comprobante = venta.comprobante,
                observacionRevision = venta.observacionRevision
            )
        }
    } // fin cargarVentaParaEditar

    // Elimina una venta de la lista (misma regla que editar)
    fun eliminarVenta(venta: VentaEntity) {
        viewModelScope.launch {
            // Se revisa la versión guardada en Room por si cambió de estado
            val actual = repository.obtenerPorId(venta.id) ?: return@launch
            if (!actual.esEditable()) return@launch

            repository.eliminar(actual)
            // El Flow de Room actualiza la lista automáticamente
        }
    } // fin eliminarVenta

    // ---------- Envío ----------

    // Valida y envía; toda venta enviada (o corregida) queda "En revisión"
    fun guardarVenta() {
        if (!validarFormulario()) return

        val actual = _estado.value
        val idEditando = actual.ventaSeleccionadaId
        val codigoNormalizado = actual.codigoAutorizacion.trim().uppercase()

        val venta = VentaEntity(
            id = idEditando ?: 0,
            convenioId = actual.convenioId,
            tipoServicio = actual.tipoServicio.trim(),
            fechaOperacion = actual.fechaOperacion.trim(),
            cliente = actual.cliente.trim(),
            monto = actual.monto.trim().toDouble(),
            codigoAutorizacion = codigoNormalizado,
            observaciones = actual.observaciones.trim(),
            respaldo = actual.respaldo,
            comprobante = actual.comprobante,
            estado = ESTADO_EN_REVISION,
            observacionRevision = null,
            vendedorNombre = actual.vendedorNombre,
            vendedorCorreo = actual.vendedorCorreo
        )

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }

            // Código de autorización no duplicado (se ignora la propia venta al editar)
            val ventaConMismoCodigo = repository.obtenerPorCodigo(codigoNormalizado)
            if (ventaConMismoCodigo != null && ventaConMismoCodigo.id != idEditando) {
                _estado.update {
                    it.copy(
                        errores = it.errores.copy(codigo = "Código ya registrado"),
                        guardando = false
                    )
                }
                return@launch
            }

            if (idEditando != null) {
                val original = repository.obtenerPorId(idEditando)
                if (original != null && !original.esEditable()) {
                    _estado.update {
                        it.copy(
                            errorGeneral = "Una venta ${original.estado.lowercase()} no se puede modificar",
                            guardando = false
                        )
                    }
                    return@launch
                }
                repository.corregirVenta(venta, actual.vendedorNombre)
            } else {
                repository.registrarVenta(venta, actual.vendedorNombre)
            }

            // Formulario limpio y aviso a la pantalla para que vuelva a la lista
            _estado.update { it.formularioVacio().copy(guardadoExitosamente = true) }
        }
    } // fin guardarVenta

    fun limpiarGuardadoExitoso() {
        _estado.update { it.copy(guardadoExitosamente = false) }
    } // fin limpiarGuardadoExitoso

    // Deja el formulario en blanco, pero conserva la sesión, la lista y los catálogos
    private fun VentaUiState.formularioVacio() = copy(
        convenioId = null,
        tipoServicio = "",
        fechaOperacion = fechaDeHoy(),
        cliente = "",
        monto = "",
        codigoAutorizacion = "",
        observaciones = "",
        respaldo = null,
        comprobante = null,
        observacionRevision = null,
        errores = VentaErrores(),
        errorGeneral = null,
        ventaSeleccionadaId = null,
        guardando = false,
        guardadoExitosamente = false
    )
}
