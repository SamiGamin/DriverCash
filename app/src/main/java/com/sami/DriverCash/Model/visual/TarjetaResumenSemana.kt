package com.sami.DriverCash.Model.visual

data class TarjetaResumenSemana(
    val identificadorSemana: String,
    val fechaInicioSemana: Long,
    val fechaFinSemana: Long,
    val totalIngresos: Double,
    val totalGastos: Double,
    val gananciaNeta: Double,
    val totalHoras: Double,
    val totalKilometros: Double, // NUEVO
    val gananciaPorHora: Double, // NUEVO
    val gananciaPorKm: Double,   // NUEVO
    val strIngresos: String,
    val strGastos: String,
    val strNeto: String,
    val strHoras: String,
    val strKilometros: String,      // NUEVO
    val strGananciaPorHora: String, // NUEVO
    val strGananciaPorKm: String
)
