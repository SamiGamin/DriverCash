package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.GastoDao
import com.sami.DriverCash.Model.Local.TipoGasto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GastoRepository @Inject constructor(private val gastoDao: GastoDao) {

    suspend fun insert(gasto: Gasto): Long {
        return gastoDao.insert(gasto)
    }

    suspend fun update(gasto: Gasto) {
        gastoDao.update(gasto)
    }

    suspend fun delete(gasto: Gasto) {
        gastoDao.delete(gasto)
    }

    fun getGastoById(id: Long): LiveData<Gasto?> {
        return gastoDao.getGastoById(id)
    }

    fun getAllGastos(): LiveData<List<Gasto>> {
        return gastoDao.getAllGastos()
    }
    suspend fun deleteAllGastos() { // El nombre debe coincidir con lo que llamas en AuthManager
        gastoDao.deleteAll() // Llama al método que definiste en el DAO
    }


    fun getGastosByVehiculoId(vehiculoId: Long): LiveData<List<Gasto>> {
        // Este método devuelve LiveData, lo mantenemos por si se usa en otras partes.
        return gastoDao.getGastosByVehiculoId(vehiculoId)
    }
    
    // Nueva función suspendida para obtener la lista de gastos por ID de vehículo
    suspend fun getGastosListByVehiculoId(vehiculoId: Long): List<Gasto> {
        // DEBERÁS ASEGURARTE de que tu GastoDao tenga un método como:
        // suspend fun getGastosListForVehiculo(vehiculoId: Long): List<Gasto>
        // O si es síncrona: 
        // fun getGastosListForVehiculoSync(vehiculoId: Long): List<Gasto>
        // y luego llamarías a withContext(Dispatchers.IO) { gastoDao.getGastosListForVehiculoSync(vehiculoId) }
        // Por ahora, asumimos que existe una función suspendida en el DAO.
        // IMPLEMENTA LA LLAMADA A TU DAO AQUÍ.
        return gastoDao.getGastosListForVehiculo(vehiculoId) // Asumiendo que esta función existe en el DAO y es suspend
    }

    fun getGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<List<Gasto>> {
        return gastoDao.getGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    fun getTotalGastosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return gastoDao.getTotalGastosByVehiculoId(vehiculoId)
    }

    fun getTotalGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<Double?> {
        return gastoDao.getTotalGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    // Nueva función para obtener el último gasto desde el DAO
    fun getUltimoGasto(): LiveData<Gasto?> {
        return gastoDao.getUltimoGasto()
    }
}