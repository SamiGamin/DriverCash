package com.sami.DriverCash.Model.Local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
// import java.util.Date // Decidimos usar Long para fecha.

@Entity(
    tableName = "gastos",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehiculoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["vehiculoId"])]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,          // ID del vehículo asociado
    val fecha: Long,               // Timestamp
    val monto: Double,
    val categoria: TipoGasto,      // Usará un TypeConverter para guardarse como String
    val descripcion: String? = null, // Puede ser nullable
    var pathFoto: String? = null // Opcional
)