package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.Vehicle
import com.sami.DriverCash.Model.Local.VehicleDao
import jakarta.inject.Inject



class VehicleRepository @Inject constructor(private val vehicleDao: VehicleDao) {
    val allVehicles: LiveData<List<Vehicle>> = vehicleDao.getAllVehicles()
    val vehiculoPredeterminado: LiveData<Vehicle?> = vehicleDao.getVehiculoPredeterminado()

    suspend fun insert(vehicle: Vehicle) {
        vehicleDao.insert(vehicle)
    }

    suspend fun update(vehicle: Vehicle) { // Nueva función para actualizar
        vehicleDao.update(vehicle)
    }

    suspend fun delete(vehicle: Vehicle) {
        vehicleDao.delete(vehicle)
    }
    suspend fun deleteAllVehicles() { // El nombre debe coincidir con lo que llamas en AuthManager
        vehicleDao.deleteAll() // Llama al método que definiste en el DAO
    }

    suspend fun getVehicleById(id: Long): Vehicle? { // Nueva función para obtener por ID
        return vehicleDao.getById(id)
    }

    suspend fun establecerVehiculoPredeterminado(vehicleId: Long) {
        vehicleDao.establecerVehiculoComoPredeterminado(vehicleId)

    }
    suspend fun getVehiculoPredeterminadoSuspend(): Vehicle? {
        return vehicleDao.getVehiculoPredeterminadoSuspend()
    }
    suspend fun getAllSuspend(): List<Vehicle> {
        return vehicleDao.getAll()
    }
}