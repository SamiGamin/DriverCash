package com.sami.DriverCash.Model.Local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una entidad de vehículo en la base de datos.
 *
 * Esta clase de datos se utiliza para almacenar información sobre un vehículo,
 * incluyendo su identificador único, matrícula, marca, modelo,
 * año de fabricación, color, tipo de combustible y si es el vehículo predeterminado.
 *
 * @property id El identificador único del vehículo, autogenerado por la base de datos.
 * @property placa El número de matrícula del vehículo.
 * @property marca La marca o fabricante del vehículo.
 * @property modelo El nombre del modelo del vehículo.
 * @property anio El año de fabricación del vehículo.
 * @property color El color del vehículo.
 * @property tipoCombustible El tipo de combustible que utiliza el vehículo (por ejemplo, Gasolina, Diésel, Eléctrico).
 * @property esPredeterminado Indica si este vehículo es el predeterminado para la aplicación.
 */
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val color: String,
    val tipoCombustible: String,
    @ColumnInfo(defaultValue = "0") // SQLite no tiene un booleano real, 0 para false, 1 para true
    val esPredeterminado: Boolean = false
)
