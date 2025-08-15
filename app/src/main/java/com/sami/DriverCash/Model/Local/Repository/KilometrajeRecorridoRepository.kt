package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.KilometrajeRecorridoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class KilometrajeRecorridoRepository @Inject constructor(private val kilometrajeRecorridoDao: KilometrajeRecorridoDao) {

    suspend fun insert(kilometrajeRecorrido: KilometrajeRecorrido): Long {
        return kilometrajeRecorridoDao.insert(kilometrajeRecorrido)
    }

    suspend fun update(kilometrajeRecorrido: KilometrajeRecorrido) {
        kilometrajeRecorridoDao.update(kilometrajeRecorrido)
    }

    suspend fun delete(kilometrajeRecorrido: KilometrajeRecorrido) {
        kilometrajeRecorridoDao.delete(kilometrajeRecorrido)
    }
    suspend fun deleteAllkilometrajeRecorrido() { // El nombre debe coincidir con lo que llamas en AuthManager
        kilometrajeRecorridoDao.deleteAll() // Llama al método que definiste en el DAO
    }


    fun getKilometrajeRecorridoById(id: Long): LiveData<KilometrajeRecorrido?> {
        return kilometrajeRecorridoDao.getKilometrajeRecorridoById(id)
    }

    fun getAllKilometrajeRecorrido(): LiveData<List<KilometrajeRecorrido>> {
        return kilometrajeRecorridoDao.getAllKilometrajeRecorrido()
    }

    fun getKilometrajeRecorridoByVehiculoId(vehiculoId: Long): LiveData<List<KilometrajeRecorrido>> {
        // Este método devuelve LiveData, lo mantenemos por si se usa en otras partes.
        return kilometrajeRecorridoDao.getKilometrajeRecorridoByVehiculoId(vehiculoId)
    }

    // Nueva función suspendida para obtener la lista de kilometrajes por ID de vehículo
    suspend fun getKilometrajeListByVehiculoId(vehiculoId: Long): List<KilometrajeRecorrido> {
        // DEBERÁS ASEGURARTE de que tu KilometrajeRecorridoDao tenga un método como:
        // suspend fun getKilometrajeListForVehiculo(vehiculoId: Long): List<KilometrajeRecorrido>
        // O si es síncrona: 
        // fun getKilometrajeListForVehiculoSync(vehiculoId: Long): List<KilometrajeRecorrido>
        // y luego llamarías a withContext(Dispatchers.IO) { kilometrajeRecorridoDao.getKilometrajeListForVehiculoSync(vehiculoId) }
        // Por ahora, asumimos que existe una función suspendida en el DAO.
        // IMPLEMENTA LA LLAMADA A TU DAO AQUÍ.
        return kilometrajeRecorridoDao.getKilometrajeListForVehiculo(vehiculoId) // Asumiendo que esta función existe en el DAO y es suspend
    }

    fun getTotalKilometrosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return kilometrajeRecorridoDao.getTotalKilometrosByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último registro de kilometraje desde el DAO
    fun getUltimoKilometrajeRecorrido(): LiveData<KilometrajeRecorrido?> {
        return kilometrajeRecorridoDao.getUltimoKilometrajeRecorrido()
    }
}