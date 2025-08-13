package com.sami.DriverCash.Ui.Adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.R
import com.sami.DriverCash.Utils.CurrencyUtils
import com.sami.DriverCash.databinding.ItemTransaccionDiaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialDiaAdapter(
    private val context: Context,
    private val onItemClicked: (item: Any) -> Unit
) : ListAdapter<Any, HistorialDiaAdapter.TransaccionViewHolder>(TransaccionDiffCallback()) {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val binding = ItemTransaccionDiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransaccionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context, timeFormatter)
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    class TransaccionViewHolder(private val binding: ItemTransaccionDiaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Any, context: Context, timeFormatter: SimpleDateFormat) {
            binding.ivTransaccionIcono.setImageResource(R.drawable.ic_launcher_background) 

            when (item) {
                is Ingreso -> {
                    binding.ivTransaccionIcono.setImageResource(R.drawable.ic_money)
                    binding.tvTransaccionDescripcionPrincipal.text = "Ingreso (${timeFormatter.format(Date(item.fecha))})"
                    binding.tvTransaccionMonto.text = CurrencyUtils.formatCurrency(context, item.monto)
                    binding.tvTransaccionDescripcionSecundaria.text = item.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción"
                    binding.tvTransaccionMonto.setTextColor(ContextCompat.getColor(context, R.color.md_theme_primary))
                }
                is Gasto -> {
                    binding.ivTransaccionIcono.setImageResource(R.drawable.ic_money) 
                    binding.tvTransaccionDescripcionPrincipal.text = "Gasto: ${item.categoria.name.replace("_", " ")} (${timeFormatter.format(Date(item.fecha))})"
                    binding.tvTransaccionMonto.text = CurrencyUtils.formatCurrency(context, item.monto)
                    binding.tvTransaccionDescripcionSecundaria.text = item.descripcion?.takeIf { it.isNotBlank() } ?: item.categoria.name.replace("_", " ")
                    binding.tvTransaccionMonto.setTextColor(ContextCompat.getColor(context, R.color.md_theme_error))
                }
                is HorasTrabajadas -> {
                    binding.ivTransaccionIcono.setImageResource(R.drawable.ic_money) 
                    binding.tvTransaccionDescripcionPrincipal.text = "Horas Trabajadas (${timeFormatter.format(Date(item.fecha))})"
                    binding.tvTransaccionMonto.text = "${CurrencyUtils.formatDecimalHoursToHHMM(item.horas)} hrs"
                    binding.tvTransaccionDescripcionSecundaria.text = item.descripcion?.takeIf { it.isNotBlank() } ?: "Registro de horas"
                    binding.tvTransaccionMonto.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface))
                }
                is KilometrajeRecorrido -> {
                    binding.ivTransaccionIcono.setImageResource(R.drawable.ic_money) 
                    binding.tvTransaccionDescripcionPrincipal.text = "Kilometraje (${timeFormatter.format(Date(item.fecha))})"
                    val kmFormateados = if (item.kilometros % 1 == 0.0) String.format(Locale.US, "%.0f", item.kilometros)
                    else String.format(Locale.US, "%.2f", item.kilometros)
                    binding.tvTransaccionMonto.text = "$kmFormateados km" 
                    binding.tvTransaccionDescripcionSecundaria.text = item.descripcion?.takeIf { it.isNotBlank() } ?: "Registro de kilometraje"
                    binding.tvTransaccionMonto.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface))
                }
                else -> {
                    binding.tvTransaccionDescripcionPrincipal.text = "Elemento desconocido"
                    binding.tvTransaccionMonto.text = ""
                    binding.tvTransaccionDescripcionSecundaria.text = ""
                }
            }
        }
    }

    class TransaccionDiffCallback : DiffUtil.ItemCallback<Any>() {
        private val DIFF_TAG = "TransaccionDiffCallback" // TAG para logs de DiffUtil

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem::class != newItem::class) {
                // Log.d(DIFF_TAG, "areItemsTheSame: FALSE (Tipos diferentes) - old=${oldItem::class.simpleName}, new=${newItem::class.simpleName}")
                return false
            }

            val result = when (oldItem) {
                is Ingreso -> oldItem.id == (newItem as Ingreso).id
                is Gasto -> oldItem.id == (newItem as Gasto).id
                is HorasTrabajadas -> oldItem.id == (newItem as HorasTrabajadas).id
                is KilometrajeRecorrido -> oldItem.id == (newItem as KilometrajeRecorrido).id
                else -> {
                    // Log.d(DIFF_TAG, "areItemsTheSame: Tipo no manejado explícitamente ${oldItem::class.simpleName}, comparando referencias: ${oldItem == newItem}")
                    oldItem == newItem 
                }
            }
            // if (oldItem is Ingreso) Log.d(DIFF_TAG, "areItemsTheSame for Ingreso (id=${(oldItem as Ingreso).id} vs newId=${(newItem as Ingreso).id}): $result")
            return result
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            // Asumimos que ListAdapter solo llama a areContentsTheSame si areItemsTheSame devolvió true,
            // por lo que los tipos deberían ser los mismos aquí.
            // if (oldItem::class != newItem::class) { return false } // Redundante pero seguro

            val result = when (oldItem) {
                is Ingreso -> oldItem == (newItem as Ingreso) // Compara contenido de data class
                is Gasto -> oldItem == (newItem as Gasto)     // Compara contenido de data class
                is HorasTrabajadas -> oldItem == (newItem as HorasTrabajadas) // Compara contenido de data class
                is KilometrajeRecorrido -> oldItem == (newItem as KilometrajeRecorrido) // Compara contenido de data class
                else -> {
                    // Log.d(DIFF_TAG, "areContentsTheSame: Tipo no manejado explícitamente ${oldItem::class.simpleName}, comparando con '==': ${oldItem == newItem}")
                    oldItem == newItem // Fallback, si son data classes esto es comparación de contenido.
                }
            }
            // if (oldItem is Ingreso) Log.d(DIFF_TAG, "areContentsTheSame for Ingreso (id=${(oldItem as Ingreso).id}): $result. Old: $oldItem, New: $newItem")
            return result
        }
    }
}
