package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
// import androidx.lifecycle.Transformations // Este import ya no es necesario para totalGastosHistoricos
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.TipoGasto
import com.sami.DriverCash.Model.Local.Repository.GastoRepository
import com.sami.DriverCash.Model.Local.VehicleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GastoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GastoRepository
    private val vehicleDao: VehicleDao

    val allGastos: LiveData<List<Gasto>>
    val ultimoGasto: LiveData<Gasto?>
    val totalGastosHistoricos: MediatorLiveData<Double?> // Cambiado a MediatorLiveData explícitamente

    init {
        val db = AppDatabase.getDatabase(application)
        val gastoDao = db.gastoDao()
        vehicleDao = db.vehicleDao()
        repository = GastoRepository(gastoDao)

        allGastos = repository.getAllGastos()
        ultimoGasto = repository.getUltimoGasto()

        // NUEVO: Calcular el total histórico de gastos para el vehículo predeterminado (Alternativa con MediatorLiveData)
        val liveDataVehiculoGastos = vehicleDao.getVehiculoPredeterminado()
        totalGastosHistoricos = MediatorLiveData<Double?>().apply {
            var currentSource: LiveData<Double?>? = null

            addSource(liveDataVehiculoGastos) { vehiculo ->
                // Si ya hay una fuente observada del repositorio, la removemos
                currentSource?.let {
                    removeSource(it)
                    currentSource = null
                }

                if (vehiculo != null) {
                    val nuevaFuente = repository.getTotalGastosByVehiculoId(vehiculo.id)
                    currentSource = nuevaFuente
                    addSource(nuevaFuente) { total ->
                        value = total
                    }
                } else {
                    value = null // O puedes poner 0.0 si prefieres
                }
            }
        }
    }

    fun insert(gasto: Gasto) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(gasto)
    }

    fun update(gasto: Gasto) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(gasto)
    }

    fun delete(gasto: Gasto) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(gasto)
    }

    fun getGastosByVehiculoId(vehiculoId: Long): LiveData<List<Gasto>> {
        return repository.getGastosByVehiculoId(vehiculoId)
    }

    fun getGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<List<Gasto>> {
        return repository.getGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    fun getTotalGastosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalGastosByVehiculoId(vehiculoId)
    }

    fun getTotalGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<Double?> {
        return repository.getTotalGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    fun getGastoById(id: Long): LiveData<Gasto?> {
        return repository.getGastoById(id)
    }
}