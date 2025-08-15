package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.IngresoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IngresoRepository @Inject constructor(private val ingresoDao: IngresoDao) {

    suspend fun insert(ingreso: Ingreso): Long {
        return ingresoDao.insert(ingreso)
    }

    suspend fun update(ingreso: Ingreso) {
        ingresoDao.update(ingreso)
    }

    suspend fun delete(ingreso: Ingreso) {
        ingresoDao.delete(ingreso)
    }
    suspend fun deleteAllIngresos() { // El nombre debe coincidir con lo que llamas en AuthManager
        ingresoDao.deleteAll() // Llama al método que definiste en el DAO
    }

    fun getIngresoById(ingresoId: Long): LiveData<Ingreso?> {
        return ingresoDao.getIngresoById(ingresoId)
    }

    fun getAllIngresos(): LiveData<List<Ingreso>> {
        return ingresoDao.getAllIngresos()
    }

    fun getIngresosByVehiculoId(vehiculoId: Long): LiveData<List<Ingreso>> {
        // Este método devuelve LiveData, lo mantenemos por si se usa en otras partes.
        return ingresoDao.getIngresosByVehiculoId(vehiculoId)
    }

    // Nueva función suspendida para obtener la lista de ingresos por ID de vehículo
    suspend fun getIngresosListByVehiculoId(vehiculoId: Long): List<Ingreso> {
        // DEBERÁS ASEGURARTE de que tu IngresoDao tenga un método como:
        // suspend fun getIngresosListForVehiculo(vehiculoId: Long): List<Ingreso>
        // O si es síncrona: 
        // fun getIngresosListForVehiculoSync(vehiculoId: Long): List<Ingreso>
        // y luego llamarías a withContext(Dispatchers.IO) { ingresoDao.getIngresosListForVehiculoSync(vehiculoId) }
        // Por ahora, asumimos que existe una función suspendida en el DAO.
        // IMPLEMENTA LA LLAMADA A TU DAO AQUÍ.
        return ingresoDao.getIngresosListForVehiculo(vehiculoId) // Asumiendo que esta función existe en el DAO y es suspend
    }

    fun getTotalIngresosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return ingresoDao.getTotalIngresosByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último ingreso desde el DAO
    fun getUltimoIngreso(): LiveData<Ingreso?> {
        return ingresoDao.getUltimoIngreso()
    }

    // Aquí podrías añadir más lógica de negocio o combinar fuentes de datos si fuera necesario
}