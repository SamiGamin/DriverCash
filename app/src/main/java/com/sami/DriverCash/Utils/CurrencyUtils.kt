package com.sami.DriverCash.Utils

import android.content.Context
import androidx.preference.PreferenceManager
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt // Importación añadida

object CurrencyUtils {

    fun formatCurrency(context: Context, amount: Double): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currencyPreference = sharedPreferences.getString("pref_currency_format", "auto") ?: "auto"
        val userLocale = Locale.getDefault()

        var formatter: NumberFormat

        when (currencyPreference) {
            "COP_NO_DECIMALS" -> {
                val localeCO = Locale("es", "CO")
                formatter = NumberFormat.getCurrencyInstance(localeCO)
                try {
                    formatter.currency = Currency.getInstance("COP")
                    formatter.maximumFractionDigits = 0
                    formatter.minimumFractionDigits = 0
                } catch (e: Exception) {
                    formatter = NumberFormat.getCurrencyInstance(userLocale)
                }
            }
            "COP" -> {
                val localeCO = Locale("es", "CO")
                formatter = NumberFormat.getCurrencyInstance(localeCO)
                try {
                    formatter.currency = Currency.getInstance("COP")
                } catch (e: Exception) {
                    formatter = NumberFormat.getCurrencyInstance(userLocale)
                }
            }
            "USD" -> {
                formatter = NumberFormat.getCurrencyInstance(Locale.US)
                try {
                    formatter.currency = Currency.getInstance("USD")
                } catch (e: Exception) {
                    formatter = NumberFormat.getCurrencyInstance(userLocale)
                }
            }
            "EUR" -> {
                formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                 try {
                    formatter.currency = Currency.getInstance("EUR")
                } catch (e: Exception) {
                    formatter = NumberFormat.getCurrencyInstance(userLocale)
                }
            }
            "auto" -> {
                formatter = NumberFormat.getCurrencyInstance(userLocale)
            }
            else -> {
                formatter = NumberFormat.getCurrencyInstance(userLocale)
            }
        }
        return formatter.format(amount)
    }

    /**
     * Formatea una cadena de entrada numérica en tiempo real para mostrar separadores de miles.
     * Por ejemplo, "1000000" se convierte en "1.000.000".
     * Utiliza el punto "." como separador de miles.
     *
     * @param rawInput La cadena de texto ingresada por el usuario.
     * @return La cadena formateada, o una cadena vacía si la entrada no contiene dígitos válidos.
     */
    fun formatNumberStringWithThousands(rawInput: String): String {
        val digitsOnly = rawInput.replace(Regex("[^\\d]"), "") // Elimina todo excepto los dígitos

        if (digitsOnly.isEmpty()) {
            return ""
        }

        return try {
            val number = digitsOnly.toLong()
            val symbols = DecimalFormatSymbols(Locale.GERMAN)
            symbols.groupingSeparator = '.'
            val formatter = DecimalFormat("#,###", symbols)
            formatter.format(number)
        } catch (e: NumberFormatException) {
            digitsOnly
        }
    }

    /**
     * Convierte horas en formato decimal a una cadena en formato HH:MM.
     * Por ejemplo, 6.25 (6 horas y 15 minutos) se convierte en "06:15".
     * Si el valor decimal es negativo, devuelve "00:00".
     *
     * @param decimalHours Las horas en formato decimal.
     * @return Una cadena formateada como "HH:MM".
     */
    fun formatDecimalHoursToHHMM(decimalHours: Double): String {
        if (decimalHours < 0) {
            // Para valores negativos o inválidos, devolvemos "00:00" como placeholder
            return String.format(Locale.getDefault(), "%02d:%02d", 0, 0)
        }
        val hours = decimalHours.toInt()
        val minutesFraction = decimalHours - hours
        var minutes = (minutesFraction * 60).roundToInt()

        // Ajuste en caso de que el redondeo lleve los minutos a 60
        var adjustedHours = hours
        if (minutes == 60) {
            adjustedHours += 1
            minutes = 0
        }

        return String.format(Locale.getDefault(), "%02d:%02d", adjustedHours, minutes)
    }
}
