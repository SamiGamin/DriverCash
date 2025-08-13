package com.sami.DriverCash.Ui.Model

import androidx.annotation.IdRes

// Enum para definir los tipos de tarjeta del dashboard
enum class DashboardCardType {
    INGRESOS,
    GASTOS,
    HORAS_TRABAJADAS,
    KILOMETRAJE,
    HISTORIAL_TOTALES // NUEVO TIPO
}

data class DashboardCardItem(
    val type: DashboardCardType,
    val title: String,
    var lastEntrySummary: String, // 'var' porque se actualizará dinámicamente
    @IdRes val addActionNavId: Int,       // ID de navegación para el botón '+'
    @IdRes val detailsActionNavId: Int? = null // ID de navegación para la tarjeta (opcional por ahora)
)