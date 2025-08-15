package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.HorasTrabajadasDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HorasTrabajadasRepository @Inject constructor(private val horasTrabajadasDao: HorasTrabajadasDao) {

    suspend fun insert(horasTrabajadas: HorasTrabajadas): Long {
        return horasTrabajadasDao.insert(horasTrabajadas)
    }

    suspend fun update(horasTrabajadas: HorasTrabajadas) {
        horasTrabajadasDao.update(horasTrabajadas)
    }

    suspend fun delete(horasTrabajadas: HorasTrabajadas) {
        horasTrabajadasDao.delete(horasTrabajadas)
    }

    fun getHorasTrabajadasById(id: Long): LiveData<HorasTrabajadas?> {
        return horasTrabajadasDao.getHorasTrabajadasById(id)
    }
    suspend fun deleteAllhorasTrabajadas() { // El nombre debe coincidir con lo que llamas en AuthManager
        horasTrabajadasDao.deleteAll() // Llama al método que definiste en el DAO
    }


    fun getAllHorasTrabajadas(): LiveData<List<HorasTrabajadas>> {
        return horasTrabajadasDao.getAllHorasTrabajadas()
    }

    fun getHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<List<HorasTrabajadas>> {
        return horasTrabajadasDao.getHorasTrabajadasByVehiculoId(vehiculoId)
    }

    // Nueva función suspendida para obtener la lista de horas trabajadas por ID de vehículo
    suspend fun getHorasListByVehiculoId(vehiculoId: Long): List<HorasTrabajadas> {
        // DEBERÁS ASEGURARTE de que tu HorasTrabajadasDao tenga un método como:
        // suspend fun getHorasListForVehiculo(vehiculoId: Long): List<HorasTrabajadas>
        // O si es síncrona: 
        // fun getHorasListForVehiculoSync(vehiculoId: Long): List<HorasTrabajadas>
        // y luego llamarías a withContext(Dispatchers.IO) { horasTrabajadasDao.getHorasListForVehiculoSync(vehiculoId) }
        // IMPLEMENTA LA LLAMADA A TU DAO AQUÍ.
        return horasTrabajadasDao.getHorasListForVehiculo(vehiculoId) // Asumiendo que esta función existe en el DAO y es suspend
    }

    fun getTotalHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return horasTrabajadasDao.getTotalHorasTrabajadasByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último registro de horas trabajadas desde el DAO
    fun getUltimasHorasTrabajadas(): LiveData<HorasTrabajadas?> {
        return horasTrabajadasDao.getUltimasHorasTrabajadas()
    }
}