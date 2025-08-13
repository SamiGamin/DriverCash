package com.sami.DriverCash.Model.Local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
// import java.util.Date // Decidimos usar Long para fecha.

@Entity(
    tableName = "horas_trabajadas",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehiculoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["vehiculoId"])]
)
data class HorasTrabajadas(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,          // ID del vehículo asociado
    val fecha: Long,               // Timestamp del día o inicio
    val horas: Double,             // Cantidad de horas
    val descripcion: String? = null // Opcional
)