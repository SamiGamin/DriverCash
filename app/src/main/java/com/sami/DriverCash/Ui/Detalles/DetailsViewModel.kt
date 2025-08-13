package com.sami.DriverCash.Ui.Detalles

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.GastoDao
import com.sami.DriverCash.Model.Local.HorasTrabajadasDao
import com.sami.DriverCash.Model.Local.IngresoDao
import com.sami.DriverCash.Model.Local.KilometrajeRecorridoDao
import com.sami.DriverCash.Model.Local.VehicleDao
import com.sami.DriverCash.Model.visual.TarjetaResumenDia
import com.sami.DriverCash.Model.visual.TarjetaResumenMes
import com.sami.DriverCash.Model.visual.TarjetaResumenSemana
import com.sami.DriverCash.Utils.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class PeriodoFiltro { DIA, SEMANA, MES, ANO }

@HiltViewModel
class DetailsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val vehicleDao: VehicleDao,
    private val ingresoDao: IngresoDao,
    private val gastoDao: GastoDao,
    private val horasTrabajadasDao: HorasTrabajadasDao,
    private val kilometrajeRecorridoDao: KilometrajeRecorridoDao
) : ViewModel() {

    private val TAG = "DetailsViewModel"

    private val _filtroPrincipalActual = MutableLiveData<PeriodoFiltro>(PeriodoFiltro.DIA)
    val filtroPrincipalActual: LiveData<PeriodoFiltro> = _filtroPrincipalActual

    private val _fechaContextoActual = MutableLiveData<Calendar>(Calendar.getInstance())

    private enum class NivelDetalleRv { DIAS, SEMANAS, MESES }
    private var nivelDetalleRvActual: NivelDetalleRv = NivelDetalleRv.DIAS

    private var vehiculoIdPredeterminado: Long? = null

    private val _listaResumenesRv = MutableLiveData<List<Any>>()
    val listaResumenesRv: LiveData<List<Any>> = _listaResumenesRv

    private val _textoPeriodoSeleccionado = MutableLiveData<String>()
    val textoPeriodoSeleccionado: LiveData<String> = _textoPeriodoSeleccionado

    // NUEVO LiveData para el evento de navegación
    private val _navegarADetalleDia = MutableLiveData<Long?>()
    val navegarADetalleDia: LiveData<Long?> = _navegarADetalleDia

    init {
        Log.d(TAG, "ViewModel inicializado.")
        viewModelScope.launch {
            vehiculoIdPredeterminado = vehicleDao.getVehiculoPredeterminadoSuspend()?.id
            if (vehiculoIdPredeterminado == null) {
                Log.w(TAG, "No hay vehículo predeterminado al iniciar ViewModel.")
                _listaResumenesRv.postValue(emptyList())
                _textoPeriodoSeleccionado.postValue("Seleccione Vehículo")
            } else {
                ajustarNivelDetalleSegunFiltroPrincipal(PeriodoFiltro.DIA)
                recargarDatosPrincipales()
            }
        }
    }

    fun seleccionarFiltroPrincipal(nuevoFiltro: PeriodoFiltro) {
        if (_filtroPrincipalActual.value == nuevoFiltro && nivelDetalleRvActual == getNivelDetalleInicialParaFiltro(nuevoFiltro)) return
        _filtroPrincipalActual.value = nuevoFiltro
        ajustarNivelDetalleSegunFiltroPrincipal(nuevoFiltro)
        recargarDatosPrincipales()
    }

    private fun getNivelDetalleInicialParaFiltro(filtro: PeriodoFiltro): NivelDetalleRv {
        return when (filtro) {
            PeriodoFiltro.DIA -> NivelDetalleRv.DIAS
            PeriodoFiltro.SEMANA -> NivelDetalleRv.DIAS
            PeriodoFiltro.MES -> NivelDetalleRv.SEMANAS
            PeriodoFiltro.ANO -> NivelDetalleRv.MESES
        }
    }

    fun navegarAPeriodoAnterior() {
        val calendario = _fechaContextoActual.value ?: Calendar.getInstance()
        when (_filtroPrincipalActual.value) {
            PeriodoFiltro.DIA -> calendario.add(Calendar.DAY_OF_YEAR, -1)
            PeriodoFiltro.SEMANA -> calendario.add(Calendar.WEEK_OF_YEAR, -1)
            PeriodoFiltro.MES -> calendario.add(Calendar.MONTH, -1)
            PeriodoFiltro.ANO -> calendario.add(Calendar.YEAR, -1)
            null -> calendario.add(Calendar.DAY_OF_YEAR, -1)
        }
        _fechaContextoActual.value = calendario
        _filtroPrincipalActual.value?.let {
            ajustarNivelDetalleSegunFiltroPrincipal(it)
        }
        recargarDatosPrincipales()
    }

    fun navegarAPeriodoPosterior() {
        val calendario = _fechaContextoActual.value ?: Calendar.getInstance()
        when (_filtroPrincipalActual.value) {
            PeriodoFiltro.DIA -> calendario.add(Calendar.DAY_OF_YEAR, 1)
            PeriodoFiltro.SEMANA -> calendario.add(Calendar.WEEK_OF_YEAR, 1)
            PeriodoFiltro.MES -> calendario.add(Calendar.MONTH, 1)
            PeriodoFiltro.ANO -> calendario.add(Calendar.YEAR, 1)
            null -> calendario.add(Calendar.DAY_OF_YEAR, 1)
        }
        _fechaContextoActual.value = calendario
        _filtroPrincipalActual.value?.let {
            ajustarNivelDetalleSegunFiltroPrincipal(it)
        }
        recargarDatosPrincipales()
    }

    fun seleccionarFechaEspecifica(year: Int, month: Int, dayOfMonth: Int) {
        val calendario = Calendar.getInstance()
        calendario.set(year, month, dayOfMonth)
        _fechaContextoActual.value = calendario
        _filtroPrincipalActual.value?.let {
            ajustarNivelDetalleSegunFiltroPrincipal(it)
        }
        recargarDatosPrincipales()
    }

    fun gestionarClicEnItemRv(item: Any) {
        Log.d(TAG, "Clic en item: $item")
        val currentFiltro = _filtroPrincipalActual.value ?: return

        when (item) {
            is TarjetaResumenSemana -> {
                if (currentFiltro == PeriodoFiltro.MES && nivelDetalleRvActual == NivelDetalleRv.SEMANAS) {
                    _fechaContextoActual.value = Calendar.getInstance().apply { timeInMillis = item.fechaInicioSemana }
                    nivelDetalleRvActual = NivelDetalleRv.DIAS
                    recargarDatosPrincipales()
                }
            }
            is TarjetaResumenMes -> {
                if (currentFiltro == PeriodoFiltro.ANO && nivelDetalleRvActual == NivelDetalleRv.MESES) {
                    _fechaContextoActual.value = Calendar.getInstance().apply {
                        set(Calendar.YEAR, item.anio)
                        set(Calendar.MONTH, item.mes)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    nivelDetalleRvActual = NivelDetalleRv.SEMANAS
                    recargarDatosPrincipales()
                }
            }
            is TarjetaResumenDia -> {
                Log.i(TAG, "Se solicitó navegar al detalle del día con timestamp: ${item.fechaTimestamp}")
                _navegarADetalleDia.value = item.fechaTimestamp // Emitir evento
            }
        }
    }

    // NUEVA FUNCIÓN para resetear el evento de navegación
    fun onNavegacionADetalleDiaCompletada() {
        _navegarADetalleDia.value = null
    }

    private fun ajustarNivelDetalleSegunFiltroPrincipal(filtro: PeriodoFiltro) {
        nivelDetalleRvActual = getNivelDetalleInicialParaFiltro(filtro)
    }

    private fun recargarDatosPrincipales() {
        if (vehiculoIdPredeterminado == null) {
            Log.w(TAG, "Intento de recargar datos sin vehículo predeterminado.")
            _listaResumenesRv.postValue(emptyList())
            _textoPeriodoSeleccionado.postValue("Error: Vehículo no definido")
            return
        }
        val vehiculoId = vehiculoIdPredeterminado!!

        viewModelScope.launch {
            val calendarioActual = _fechaContextoActual.value ?: Calendar.getInstance().also { _fechaContextoActual.postValue(it) }
            Log.d(TAG, "Recargando datos. Filtro: ${_filtroPrincipalActual.value}, Nivel RV: $nivelDetalleRvActual, Fecha Contexto: ${formatDateForLog(calendarioActual)}")

            _textoPeriodoSeleccionado.postValue(formatearTextoPeriodo(calendarioActual, _filtroPrincipalActual.value ?: PeriodoFiltro.DIA, nivelDetalleRvActual))

            val nuevaListaResumenes = mutableListOf<Any>()
            val currentFiltroPrincipal = _filtroPrincipalActual.value ?: PeriodoFiltro.DIA

            when (nivelDetalleRvActual) {
                NivelDetalleRv.DIAS -> {
                    when (currentFiltroPrincipal) {
                        PeriodoFiltro.DIA -> {
                            val (inicioDia, finDia) = getInicioFinDia(calendarioActual)
                            calcularResumenDia(vehiculoId, inicioDia, finDia)?.let { nuevaListaResumenes.add(it) }
                        }
                        PeriodoFiltro.SEMANA -> {
                             val (inicioSemanaCal, _) = getSemanaDesdeCalendario(calendarioActual)
                            for (i in 0..6) {
                                val diaDeLaSemana = Calendar.getInstance().apply {
                                    timeInMillis = inicioSemanaCal.timeInMillis
                                    add(Calendar.DAY_OF_YEAR, i)
                                }
                                val (inicioDia, finDia) = getInicioFinDia(diaDeLaSemana)
                                calcularResumenDia(vehiculoId, inicioDia, finDia)?.let { nuevaListaResumenes.add(it) }
                            }
                        }
                        PeriodoFiltro.MES -> {
                            val (inicioSemanaCal, _) = getSemanaDesdeCalendario(calendarioActual)
                            for (i in 0..6) {
                                val diaDeLaSemana = Calendar.getInstance().apply {
                                    timeInMillis = inicioSemanaCal.timeInMillis
                                    add(Calendar.DAY_OF_YEAR, i)
                                }
                                val (inicioDia, finDia) = getInicioFinDia(diaDeLaSemana)
                                calcularResumenDia(vehiculoId, inicioDia, finDia)?.let { nuevaListaResumenes.add(it) }
                            }
                        }
                        PeriodoFiltro.ANO -> { /* No mostrar días individuales para filtro AÑO directamente */ }
                    }
                }
                NivelDetalleRv.SEMANAS -> {
                     val mesActual = calendarioActual.get(Calendar.MONTH)
                     val anioActual = calendarioActual.get(Calendar.YEAR)
                     val semanasDelMes = getSemanasDelMes(anioActual, mesActual)

                    semanasDelMes.forEach { parFechasSemana ->
                        calcularResumenSemana(vehiculoId, parFechasSemana.first, parFechasSemana.second)?.let { nuevaListaResumenes.add(it) }
                    }
                }
                NivelDetalleRv.MESES -> {
                    val anioActual = calendarioActual.get(Calendar.YEAR)
                    for (mes in 0..11) {
                        calcularResumenMes(vehiculoId, anioActual, mes)?.let { nuevaListaResumenes.add(it) }
                    }
                }
            }
            _listaResumenesRv.postValue(nuevaListaResumenes)
            if (nuevaListaResumenes.isEmpty()){
                 Log.d(TAG, "Lista de resúmenes vacía para el periodo.")
            }
        }
    }

    private suspend fun calcularResumenDia(vehiculoId: Long, fechaInicioMillis: Long, fechaFinMillis: Long): TarjetaResumenDia? {
        val ganancias = ingresoDao.getSumaIngresosEntreFechas(vehiculoId, fechaInicioMillis, fechaFinMillis) ?: 0.0
        val gastos = gastoDao.getSumaGastosEntreFechas(vehiculoId, fechaInicioMillis, fechaFinMillis) ?: 0.0
        val horas = horasTrabajadasDao.getSumaHorasEntreFechas(vehiculoId, fechaInicioMillis, fechaFinMillis) ?: 0.0
        val kilometros = kilometrajeRecorridoDao.getSumaKilometrosEntreFechas(vehiculoId, fechaInicioMillis, fechaFinMillis) ?: 0.0
        val neto = ganancias - gastos

        val gananciaPorHora = if (horas > 0) neto / horas else 0.0
        val gananciaPorKm = if (kilometros > 0) neto / kilometros else 0.0

        val cal = Calendar.getInstance().apply { timeInMillis = fechaInicioMillis }
        val sdfDia = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

        val strKm = String.format(Locale.US, "%.1f km", kilometros)
        val strGananciaHora = "${CurrencyUtils.formatCurrency(appContext, gananciaPorHora)}/hr"
        val strGananciaKm = "${CurrencyUtils.formatCurrency(appContext, gananciaPorKm)}/km"
        
        // No añadir la tarjeta si no hay datos significativos, excepto si es el único día visible
        val currentFiltro = _filtroPrincipalActual.value
        val esUnicoDiaVisible = currentFiltro == PeriodoFiltro.DIA && nivelDetalleRvActual == NivelDetalleRv.DIAS
        if (!esUnicoDiaVisible && ganancias == 0.0 && gastos == 0.0 && horas == 0.0 && kilometros == 0.0) {
            return null
        }

        return TarjetaResumenDia(

            fechaTimestamp = fechaInicioMillis,
            fechaFormateada = sdfDia.format(cal.time).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            totalIngresos = ganancias,
            totalGastos = gastos,
            gananciaNeta = neto,
            totalHoras = horas,
            totalKilometros = kilometros, // NUEVO
            gananciaPorHora = gananciaPorHora, // NUEVO
            gananciaPorKm = gananciaPorKm,   // NUEVO
            strIngresos = CurrencyUtils.formatCurrency(appContext, ganancias),
            strGastos = CurrencyUtils.formatCurrency(appContext, gastos),
            strNeto = CurrencyUtils.formatCurrency(appContext, neto),
            strHoras = String.format(Locale.US, "%.1f hrs", horas),
            strKilometros = strKm, // NUEVO
            strGananciaPorHora = strGananciaHora, // NUEVO
            strGananciaPorKm = strGananciaKm    // NUEVO
        )
    }

    private suspend fun calcularResumenSemana(vehiculoId: Long, fechaInicioSemana: Long, fechaFinSemana: Long): TarjetaResumenSemana? {
        val ganancias = ingresoDao.getSumaIngresosEntreFechas(vehiculoId, fechaInicioSemana, fechaFinSemana) ?: 0.0
        val gastos = gastoDao.getSumaGastosEntreFechas(vehiculoId, fechaInicioSemana, fechaFinSemana) ?: 0.0
        val horas = horasTrabajadasDao.getSumaHorasEntreFechas(vehiculoId, fechaInicioSemana, fechaFinSemana) ?: 0.0
        val kilometros = kilometrajeRecorridoDao.getSumaKilometrosEntreFechas(vehiculoId, fechaInicioSemana, fechaFinSemana) ?: 0.0 // NUEVO
        val neto = ganancias - gastos

        val gananciaPorHora = if (horas > 0) neto / horas else 0.0 // NUEVO
        val gananciaPorKm = if (kilometros > 0) neto / kilometros else 0.0 // NUEVO

        val calInicio = Calendar.getInstance().apply { timeInMillis = fechaInicioSemana }
        val calFin = Calendar.getInstance().apply { timeInMillis = fechaFinSemana }
        val sdfRango = SimpleDateFormat("dd MMM", Locale.getDefault())
        val numSemana = calInicio.get(Calendar.WEEK_OF_YEAR)
        val identificador = "Sem. $numSemana (${sdfRango.format(calInicio.time)} - ${sdfRango.format(calFin.time)})"

        val strKm = String.format(Locale.US, "%.1f km", kilometros) // NUEVO
        val strGananciaHora = "${CurrencyUtils.formatCurrency(appContext, gananciaPorHora)}/hr" // NUEVO
        val strGananciaKm = "${CurrencyUtils.formatCurrency(appContext, gananciaPorKm)}/km" // NUEVO

        return TarjetaResumenSemana(
            identificadorSemana = identificador,
            fechaInicioSemana = fechaInicioSemana,
            fechaFinSemana = fechaFinSemana,
            totalIngresos = ganancias,
            totalGastos = gastos,
            gananciaNeta = neto,
            totalHoras = horas,
            totalKilometros = kilometros, // NUEVO
            gananciaPorHora = gananciaPorHora, // NUEVO
            gananciaPorKm = gananciaPorKm,   // NUEVO
            strIngresos = CurrencyUtils.formatCurrency(appContext, ganancias),
            strGastos = CurrencyUtils.formatCurrency(appContext, gastos),
            strNeto = CurrencyUtils.formatCurrency(appContext, neto),
            strHoras = String.format(Locale.US, "%.1f hrs", horas),
            strKilometros = strKm, // NUEVO
            strGananciaPorHora = strGananciaHora, // NUEVO
            strGananciaPorKm = strGananciaKm    // NUEVO
        )
    }

    private suspend fun calcularResumenMes(vehiculoId: Long, anio: Int, mes: Int): TarjetaResumenMes? {
        val (inicioMes, finMes) = getInicioFinMes(anio, mes)
        val ganancias = ingresoDao.getSumaIngresosEntreFechas(vehiculoId, inicioMes, finMes) ?: 0.0
        val gastos = gastoDao.getSumaGastosEntreFechas(vehiculoId, inicioMes, finMes) ?: 0.0
        val horas = horasTrabajadasDao.getSumaHorasEntreFechas(vehiculoId, inicioMes, finMes) ?: 0.0
        val kilometros =
            kilometrajeRecorridoDao.getSumaKilometrosEntreFechas(vehiculoId, inicioMes, finMes)
                ?: 0.0 // NUEVO
        val neto = ganancias - gastos

        val gananciaPorHora = if (horas > 0) neto / horas else 0.0 // NUEVO
        val gananciaPorKm = if (kilometros > 0) neto / kilometros else 0.0 // NUEVO

        val cal = Calendar.getInstance().apply { set(anio, mes, 1) }
        val sdfMesAnio = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        val strKm = String.format(Locale.US, "%.1f km", kilometros) // NUEVO
        val strGananciaHora =
            "${CurrencyUtils.formatCurrency(appContext, gananciaPorHora)}/hr" // NUEVO
        val strGananciaKm = "${CurrencyUtils.formatCurrency(appContext, gananciaPorKm)}/km" // NUEVO

        return TarjetaResumenMes(
            nombreMesAnio = sdfMesAnio.format(cal.time)
                .replaceFirstChar { it.titlecase(Locale.getDefault()) },
            mes = mes,
            anio = anio,
            totalIngresos = ganancias,
            totalGastos = gastos,
            gananciaNeta = neto,
            totalHoras = horas,
            totalKilometros = kilometros, // NUEVO
            gananciaPorHora = gananciaPorHora, // NUEVO
            gananciaPorKm = gananciaPorKm,   // NUEVO
            strIngresos = CurrencyUtils.formatCurrency(appContext, ganancias),
            strGastos = CurrencyUtils.formatCurrency(appContext, gastos),
            strNeto = CurrencyUtils.formatCurrency(appContext, neto),
            strHoras = String.format(Locale.US, "%.1f hrs", horas),
            strKilometros = strKm,  // NUEVO
            strGananciaPorHora = strGananciaHora, // NUEVO
            strGananciaPorKm = strGananciaKm     // NUEVO
        )
    }

    private fun getInicioFinDia(calendar: Calendar): Pair<Long, Long> {
        val inicio = calendar.clone() as Calendar
        inicio.set(Calendar.HOUR_OF_DAY, 0); inicio.set(Calendar.MINUTE, 0); inicio.set(Calendar.SECOND, 0); inicio.set(Calendar.MILLISECOND, 0)
        val fin = calendar.clone() as Calendar
        fin.set(Calendar.HOUR_OF_DAY, 23); fin.set(Calendar.MINUTE, 59); fin.set(Calendar.SECOND, 59); fin.set(Calendar.MILLISECOND, 999)
        return Pair(inicio.timeInMillis, fin.timeInMillis)
    }

    private fun getSemanaDesdeCalendario(calendar: Calendar): Pair<Calendar, Calendar> {
        val inicioSemana = calendar.clone() as Calendar
        inicioSemana.firstDayOfWeek = Calendar.MONDAY
        inicioSemana.set(Calendar.DAY_OF_WEEK, inicioSemana.firstDayOfWeek)
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0); inicioSemana.set(Calendar.MINUTE, 0); inicioSemana.set(Calendar.SECOND, 0); inicioSemana.set(Calendar.MILLISECOND, 0)
        val finSemana = inicioSemana.clone() as Calendar
        finSemana.add(Calendar.DAY_OF_YEAR, 6)
        finSemana.set(Calendar.HOUR_OF_DAY, 23); finSemana.set(Calendar.MINUTE, 59); finSemana.set(Calendar.SECOND, 59); finSemana.set(Calendar.MILLISECOND, 999)
        return Pair(inicioSemana, finSemana)
    }

    private fun getSemanasDelMes(anio: Int, mes: Int): List<Pair<Long, Long>> {
        val semanas = mutableListOf<Pair<Long, Long>>()
        val calendario = Calendar.getInstance().apply {
            clear(); set(Calendar.YEAR, anio); set(Calendar.MONTH, mes); set(Calendar.DAY_OF_MONTH, 1); firstDayOfWeek = Calendar.MONDAY
        }
        calendario.add(Calendar.DAY_OF_MONTH, -(calendario.get(Calendar.DAY_OF_WEEK) - calendario.firstDayOfWeek + 7) % 7)
        while (calendario.get(Calendar.MONTH) <= mes && calendario.get(Calendar.YEAR) <= anio) {
            if (calendario.get(Calendar.YEAR) == anio && calendario.get(Calendar.MONTH) > mes) break
            if (calendario.get(Calendar.YEAR) > anio) break
            val inicioSemanaCal = calendario.clone() as Calendar
            inicioSemanaCal.set(Calendar.HOUR_OF_DAY, 0); inicioSemanaCal.set(Calendar.MINUTE, 0); inicioSemanaCal.set(Calendar.SECOND, 0); inicioSemanaCal.set(Calendar.MILLISECOND, 0)
            val finSemanaCal = inicioSemanaCal.clone() as Calendar
            finSemanaCal.add(Calendar.DAY_OF_YEAR, 6)
            finSemanaCal.set(Calendar.HOUR_OF_DAY, 23); finSemanaCal.set(Calendar.MINUTE, 59); finSemanaCal.set(Calendar.SECOND, 59); finSemanaCal.set(Calendar.MILLISECOND, 999)
            if ( (inicioSemanaCal.get(Calendar.MONTH) == mes && inicioSemanaCal.get(Calendar.YEAR) == anio) ||
                 (finSemanaCal.get(Calendar.MONTH) == mes && finSemanaCal.get(Calendar.YEAR) == anio) ) {
                semanas.add(Pair(inicioSemanaCal.timeInMillis, finSemanaCal.timeInMillis))
            }
            calendario.add(Calendar.WEEK_OF_YEAR, 1)
            if (semanas.size >= 6) break
        }
        return semanas
    }

    private fun getInicioFinMes(anio: Int, mes: Int): Pair<Long, Long> {
        val inicio = Calendar.getInstance().apply { clear(); set(Calendar.YEAR, anio); set(Calendar.MONTH, mes); set(Calendar.DAY_OF_MONTH, 1); setTimeOfDayToStart() }
        val fin = Calendar.getInstance().apply { clear(); set(Calendar.YEAR, anio); set(Calendar.MONTH, mes); set(Calendar.DAY_OF_MONTH, inicio.getActualMaximum(Calendar.DAY_OF_MONTH)); setTimeOfDayToEnd() }
        return Pair(inicio.timeInMillis, fin.timeInMillis)
    }
    
    private fun Calendar.setTimeOfDayToStart() { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    private fun Calendar.setTimeOfDayToEnd() { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }


    private fun formatearTextoPeriodo(calendario: Calendar, filtroPrincipal: PeriodoFiltro, nivelDetalle: NivelDetalleRv): String {
        val sdfDia = SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", Locale.getDefault())
        val sdfSemana = SimpleDateFormat("'Semana del' dd 'de' MMMM 'al'", Locale.getDefault())
        val sdfMes = SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault())
        val sdfAnio = SimpleDateFormat("yyyy", Locale.getDefault())

        return when (filtroPrincipal) {
            PeriodoFiltro.DIA -> sdfDia.format(calendario.time)
            PeriodoFiltro.SEMANA -> {
                val (inicioSem, finSem) = getSemanaDesdeCalendario(calendario)
                val finSemanaTexto = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault()).format(finSem.time)
                "${sdfSemana.format(inicioSem.time)} $finSemanaTexto"
            }
            PeriodoFiltro.MES -> {
                if (nivelDetalle == NivelDetalleRv.DIAS) {
                     val (inicioSem, finSem) = getSemanaDesdeCalendario(calendario)
                     val finSemanaTexto = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault()).format(finSem.time)
                    "${sdfSemana.format(inicioSem.time)} $finSemanaTexto"
                } else {
                    sdfMes.format(calendario.time)
                }
            }
            PeriodoFiltro.ANO -> {
                 if (nivelDetalle == NivelDetalleRv.SEMANAS) {
                    sdfMes.format(calendario.time)
                 } else {
                    sdfAnio.format(calendario.time)
                 }
            }
        }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    private fun formatDateForLog(calendar: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}