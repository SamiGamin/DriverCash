package com.sami.DriverCash.Model.Local

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object (DAO) for the Vehicle entity.
 * Provides methods for interacting with the vehicles table in the database.
 *
 * Tambien al español:
 * Objeto de Acceso a Datos (DAO) para la entidad Vehículo.
 * Proporciona métodos para interactuar con la tabla de vehículos en la base de datos.
 */
@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle)
    // Tambien al español: Inserta un nuevo vehículo en la base de datos. Si ya existe un vehículo con el mismo ID, lo reemplaza.

    @Update
    suspend fun update(vehicle: Vehicle)
    // Tambien al español: Actualiza un vehículo existente en la base de datos.

    @Delete
    suspend fun delete(vehicle: Vehicle)
    // Tambien al español: Elimina un vehículo de la base de datos.

    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    suspend fun getAll(): List<Vehicle>
    // Tambien al español: Obtiene todos los vehículos de la base de datos, ordenados por ID en orden descendente.

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Vehicle?
    // Tambien al español: Obtiene un vehículo específico de la base de datos por su ID. Devuelve nulo si no se encuentra.

    @Query("DELETE FROM vehicles")
    suspend fun deleteAll()
    // Tambien al español: Elimina todos los vehículos de la base de datos.

    @Query("SELECT * FROM vehicles ORDER BY CASE WHEN esPredeterminado THEN 0 ELSE 1 END, id DESC") // Los predeterminados primero
    fun getAllVehicles(): LiveData<List<Vehicle>>

    // Métodos para vehículo predeterminado
    @Query("SELECT * FROM vehicles WHERE esPredeterminado = 1 LIMIT 1")
    fun getVehiculoPredeterminado(): LiveData<Vehicle?>

    @Query("SELECT * FROM vehicles WHERE esPredeterminado = 1 LIMIT 1")
    suspend fun getVehiculoPredeterminadoSuspend(): Vehicle?

    @Query("UPDATE vehicles SET esPredeterminado = 0 WHERE esPredeterminado = 1")
    suspend fun _resetearPredeterminadosAnteriores(): Int // Método auxiliar

    @Query("UPDATE vehicles SET esPredeterminado = 1 WHERE id = :vehicleId")
    suspend fun _marcarComoPredeterminado(vehicleId: Long) // Método auxiliar

    @Transaction
    suspend fun establecerVehiculoComoPredeterminado(vehicleId: Long) {
        _resetearPredeterminadosAnteriores() // Desmarca el predeterminado anterior si existe
        _marcarComoPredeterminado(vehicleId)    // Marca el nuevo como predeterminado
    }
}