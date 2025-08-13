package com.sami.DriverCash.Model.Local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface KilometrajeRecorridoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kilometrajeRecorrido: KilometrajeRecorrido): Long

    @Update
    suspend fun update(kilometrajeRecorrido: KilometrajeRecorrido)

    @Delete
    suspend fun delete(kilometrajeRecorrido: KilometrajeRecorrido)

    @Query("SELECT * FROM kilometraje_recorrido WHERE id = :id LIMIT 1")
    fun getKilometrajeRecorridoById(id: Long): LiveData<KilometrajeRecorrido?>

    @Query("SELECT * FROM kilometraje_recorrido ORDER BY fecha DESC")
    fun getAllKilometrajeRecorrido(): LiveData<List<KilometrajeRecorrido>>

    @Query("SELECT * FROM kilometraje_recorrido WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    fun getKilometrajeRecorridoByVehiculoId(vehiculoId: Long): LiveData<List<KilometrajeRecorrido>>

    @Query("SELECT SUM(kilometros) FROM kilometraje_recorrido WHERE vehiculoId = :vehiculoId")
    fun getTotalKilometrosByVehiculoId(vehiculoId: Long): LiveData<Double?>

    @Query("SELECT * FROM kilometraje_recorrido ORDER BY fecha DESC, id DESC LIMIT 1")
    fun getUltimoKilometrajeRecorrido(): LiveData<KilometrajeRecorrido?>

    @Query("SELECT SUM(kilometros) FROM kilometraje_recorrido WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin")
    suspend fun getSumaKilometrosEntreFechas(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): Double?

    // CORREGIDO: Apunta a 'kilometraje_recorrido' y devuelve List<KilometrajeRecorrido>
    @Query("SELECT * FROM kilometraje_recorrido WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha ASC")
    suspend fun getKilometrajeEntreFechasParaVehiculo(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): List<KilometrajeRecorrido>
}
