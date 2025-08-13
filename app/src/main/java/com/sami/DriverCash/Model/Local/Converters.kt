package com.sami.DriverCash.Model.Local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTipoGasto(value: TipoGasto?): String? {
        return value?.name // Guarda el nombre del enum como String (ej. "COMBUSTIBLE")
    }

    @TypeConverter
    fun toTipoGasto(value: String?): TipoGasto? {
        return value?.let { TipoGasto.valueOf(it) } // Convierte el String de vuelta al enum
    }

    // Aquí podrías añadir otros conversores en el futuro, por ejemplo, para Date si decides usarlo
    /*
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    */
}