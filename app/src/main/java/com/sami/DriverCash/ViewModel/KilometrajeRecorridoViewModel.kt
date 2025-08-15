package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.Repository.KilometrajeRecorridoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Asegúrate de que este import esté si lo usas directamente aquí, aunque es mejor en el Repositorio
import javax.inject.Inject

@HiltViewModel
class KilometrajeRecorridoViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val repository: KilometrajeRecorridoRepository

    val allKilometrajeRecorrido: LiveData<List<KilometrajeRecorrido>>
    val ultimoKilometrajeRecorrido: LiveData<KilometrajeRecorrido?>

    init {
        val kilometrajeRecorridoDao = AppDatabase.getDatabase(application).kilometrajeRecorridoDao()
        repository = KilometrajeRecorridoRepository(kilometrajeRecorridoDao)
        allKilometrajeRecorrido = repository.getAllKilometrajeRecorrido()
        ultimoKilometrajeRecorrido = repository.getUltimoKilometrajeRecorrido()
    }

    fun insert(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(kilometrajeRecorrido)
    }

    fun update(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(kilometrajeRecorrido)
    }

    fun delete(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(kilometrajeRecorrido)
    }

    fun getKilometrajeRecorridoByVehiculoId(vehiculoId: Long): LiveData<List<KilometrajeRecorrido>> {
        return repository.getKilometrajeRecorridoByVehiculoId(vehiculoId)
    }
    
    // Función suspendida para obtener la lista directamente desde el repositorio
    suspend fun getKilometrajesByVehiculoIdSuspend(vehiculoId: Long): List<KilometrajeRecorrido> {
        // DEBERÁS ASEGURARTE de que tu KilometrajeRecorridoRepository tenga una función suspendida como esta:
        // suspend fun getKilometrajeListByVehiculoId(vehiculoId: Long): List<KilometrajeRecorrido>
        // Esta función en el repositorio debería obtener los datos del DAO (también con suspend o withContext).
        return repository.getKilometrajeListByVehiculoId(vehiculoId)
    }

    fun getTotalKilometrosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalKilometrosByVehiculoId(vehiculoId)
    }

    fun getKilometrajeRecorridoById(id: Long): LiveData<KilometrajeRecorrido?> {
        return repository.getKilometrajeRecorridoById(id)
    }
}