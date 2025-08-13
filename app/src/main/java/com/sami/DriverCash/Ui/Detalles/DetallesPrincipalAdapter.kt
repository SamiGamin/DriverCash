package com.sami.DriverCash.Ui.Detalles

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sami.DriverCash.Model.visual.TarjetaResumenDia
import com.sami.DriverCash.Model.visual.TarjetaResumenMes
import com.sami.DriverCash.Model.visual.TarjetaResumenSemana
import com.sami.DriverCash.Utils.CurrencyUtils // Importación añadida
import com.sami.DriverCash.databinding.ItemTarjetaResumenDiaBinding
import com.sami.DriverCash.databinding.ItemTarjetaResumenMesBinding
import com.sami.DriverCash.databinding.ItemTarjetaResumenSemanaBinding
import java.lang.IllegalArgumentException

private const val VIEW_TYPE_DIA = 1
private const val VIEW_TYPE_SEMANA = 2
private const val VIEW_TYPE_MES = 3

class DetallesPrincipalAdapter(
    private val onCardClick: (item: Any) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DetallesDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TarjetaResumenDia -> VIEW_TYPE_DIA
            is TarjetaResumenSemana -> VIEW_TYPE_SEMANA
            is TarjetaResumenMes -> VIEW_TYPE_MES
            else -> throw IllegalArgumentException("Tipo de item desconocido en DetallesPrincipalAdapter: ${getItem(position).javaClass.name}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DIA -> {
                val binding = ItemTarjetaResumenDiaBinding.inflate(inflater, parent, false)
                DiaViewHolder(binding)
            }
            VIEW_TYPE_SEMANA -> {
                val binding = ItemTarjetaResumenSemanaBinding.inflate(inflater, parent, false)
                SemanaViewHolder(binding)
            }
            VIEW_TYPE_MES -> {
                val binding = ItemTarjetaResumenMesBinding.inflate(inflater, parent, false)
                MesViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Tipo de vista desconocido en onCreateViewHolder")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is DiaViewHolder -> holder.bind(item as TarjetaResumenDia, onCardClick)
            is SemanaViewHolder -> holder.bind(item as TarjetaResumenSemana, onCardClick)
            is MesViewHolder -> holder.bind(item as TarjetaResumenMes, onCardClick)
        }
    }

    class DiaViewHolder(private val binding: ItemTarjetaResumenDiaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TarjetaResumenDia, onCardClick: (item: Any) -> Unit) {
            binding.tvFechaResumen.text = item.fechaFormateada
            binding.tvIngresosDiaValor.text = item.strIngresos
            binding.tvGastosDiaValor.text = item.strGastos
            binding.tvHorasDiaValor.text = "${CurrencyUtils.formatDecimalHoursToHHMM(item.totalHoras)} hrs" // Modificado
            binding.tvKmDiaValor.text = item.strKilometros
            binding.tvGananciaHoraDiaValor.text = item.strGananciaPorHora
            binding.tvGananciaKmDiaValor.text = item.strGananciaPorKm
            binding.tvNetoDiaValor.text = item.strNeto
            binding.root.setOnClickListener { onCardClick(item) }
        }
    }

    class SemanaViewHolder(private val binding: ItemTarjetaResumenSemanaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TarjetaResumenSemana, onCardClick: (item: Any) -> Unit) {
            binding.tvIdentificadorSemana.text = item.identificadorSemana
            binding.tvIngresosSemanaValor.text = item.strIngresos
            binding.tvGastosSemanaValor.text = item.strGastos
            binding.tvHorasSemanaValor.text = "${CurrencyUtils.formatDecimalHoursToHHMM(item.totalHoras)} hrs" // Modificado
            binding.tvKmSemanaValor.text = item.strKilometros
            binding.tvGananciaHoraSemanaValor.text = item.strGananciaPorHora
            binding.tvGananciaKmSemanaValor.text = item.strGananciaPorKm
            binding.tvNetoSemanaValor.text = item.strNeto
            binding.root.setOnClickListener { onCardClick(item) }
        }
    }

    class MesViewHolder(private val binding: ItemTarjetaResumenMesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: TarjetaResumenMes, onCardClick: (item: Any) -> Unit) {
            binding.tvNombreMesAnio.text = item.nombreMesAnio
            binding.tvIngresosMesValor.text = item.strIngresos
            binding.tvGastosMesValor.text = item.strGastos
            binding.tvHorasMesValor.text = "${CurrencyUtils.formatDecimalHoursToHHMM(item.totalHoras)} hrs" 
            binding.tvKmMesValor.text = item.strKilometros
            binding.tvGananciaHoraMesValor.text = item.strGananciaPorHora
            binding.tvGananciaKmMesValor.text = item.strGananciaPorKm
            binding.tvNetoMesValor.text = item.strNeto
            binding.root.setOnClickListener { onCardClick(item) }
        }
    }
}

class DetallesDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is TarjetaResumenDia && newItem is TarjetaResumenDia ->
                oldItem.fechaTimestamp == newItem.fechaTimestamp
            oldItem is TarjetaResumenSemana && newItem is TarjetaResumenSemana ->
                oldItem.fechaInicioSemana == newItem.fechaInicioSemana && oldItem.fechaFinSemana == newItem.fechaFinSemana
            oldItem is TarjetaResumenMes && newItem is TarjetaResumenMes ->
                oldItem.mes == newItem.mes && oldItem.anio == newItem.anio
            else -> oldItem.javaClass == newItem.javaClass
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem.javaClass != newItem.javaClass) {
            return false
        }
        return when (oldItem) {
            is TarjetaResumenDia, is TarjetaResumenSemana, is TarjetaResumenMes -> newItem == oldItem
            else -> false
        }
    }
}
