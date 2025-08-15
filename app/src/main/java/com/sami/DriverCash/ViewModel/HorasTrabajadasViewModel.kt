package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Repository.HorasTrabajadasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Asegúrate de que este import esté si lo usas directamente aquí, aunque es mejor en el Repositorio
import javax.inject.Inject

@HiltViewModel
class HorasTrabajadasViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val repository: HorasTrabajadasRepository

    val allHorasTrabajadas: LiveData<List<HorasTrabajadas>>
    val ultimasHorasTrabajadas: LiveData<HorasTrabajadas?>

    init {
        val horasTrabajadasDao = AppDatabase.getDatabase(application).horasTrabajadasDao()
        repository = HorasTrabajadasRepository(horasTrabajadasDao)
        allHorasTrabajadas = repository.getAllHorasTrabajadas()
        ultimasHorasTrabajadas = repository.getUltimasHorasTrabajadas()
    }

    fun insert(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(horasTrabajadas)
    }

    fun update(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(horasTrabajadas)
    }

    fun delete(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(horasTrabajadas)
    }

    fun getHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<List<HorasTrabajadas>> {
        return repository.getHorasTrabajadasByVehiculoId(vehiculoId)
    }

    // Función suspendida para obtener la lista directamente desde el repositorio
    suspend fun getHorasTrabajadasByVehiculoIdSuspend(vehiculoId: Long): List<HorasTrabajadas> {
        // DEBERÁS ASEGURARTE de que tu HorasTrabajadasRepository tenga una función suspendida como esta:
        // suspend fun getHorasListByVehiculoId(vehiculoId: Long): List<HorasTrabajadas>
        // Esta función en el repositorio debería obtener los datos del DAO (también con suspend o withContext).
        return repository.getHorasListByVehiculoId(vehiculoId) 
    }

    fun getTotalHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalHorasTrabajadasByVehiculoId(vehiculoId)
    }

    fun getHorasTrabajadasById(id: Long): LiveData<HorasTrabajadas?> {
        return repository.getHorasTrabajadasById(id)
    }
}