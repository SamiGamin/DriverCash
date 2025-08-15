package com.sami.DriverCash.Model.Local

// Asegúrate de que los imports de tus clases de modelo sean correctos aquí si es necesario
 import com.sami.DriverCash.Model.Local.Vehicle
 import com.sami.DriverCash.Model.Local.Ingreso
 import com.sami.DriverCash.Model.Local.Gasto
 import com.sami.DriverCash.Model.Local.HorasTrabajadas
 import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
 import com.sami.DriverCash.Model.Local.TipoGasto //

fun Vehicle.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "marca" to marca,
        "modelo" to modelo,
        "anio" to anio,
        "color" to color,
        "placa" to placa,
        "esPredeterminado" to esPredeterminado,
        "apodo" to apodo, // Añadido
        "numeroEconomico" to numeroEconomico, // Añadido
        "tipoCombustible" to tipoCombustible?.name // Añadido - guardar nombre del enum
        // Asegúrate de que todos los campos que quieres respaldar estén aquí
    )
}

fun Ingreso.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "vehiculoId" to vehiculoId,
        "monto" to monto,
        "fecha" to fecha,
        "descripcion" to descripcion,
        "categoria" to categoria // Asumiendo que 'categoria' en Ingreso es un String
    )
}

fun Gasto.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "vehiculoId" to vehiculoId,
        "monto" to monto,
        "fecha" to fecha,
        "descripcion" to descripcion,
        "tipoGasto" to categoria.name, // 'categoria' en Gasto es TipoGasto, guardamos su nombre
        "pathFoto" to pathFoto
    )
}

fun HorasTrabajadas.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "vehiculoId" to vehiculoId,
        "fecha" to fecha,
        "horas" to horas,
        "descripcion" to descripcion
    )
}

fun KilometrajeRecorrido.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "vehiculoId" to vehiculoId,
        "fecha" to fecha,
        "kilometros" to kilometros,
        "descripcion" to descripcion
    )
}
