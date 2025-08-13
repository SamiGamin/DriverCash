package com.sami.DriverCash.Ui.Preferences.MisVeiculos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sami.DriverCash.Model.Local.Vehicle
import com.sami.DriverCash.databinding.ItemVehiculoRegistradoBinding

class VehiculoRegistradoAdapter(
    private val onItemClicked: (Vehicle) -> Unit,
    private val onItemLongClicked: (Vehicle) -> Boolean // Nueva lambda para clic largo
) :
    ListAdapter<Vehicle, VehiculoRegistradoAdapter.VehiculoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehiculoViewHolder {
        val binding = ItemVehiculoRegistradoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehiculoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehiculoViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.itemView.setOnLongClickListener { // Configurar el listener para clic largo
            onItemLongClicked(current) // Llamar a la nueva lambda y devolver su resultado
        }
        holder.bind(current)
    }

    class VehiculoViewHolder(private val binding: ItemVehiculoRegistradoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicle: Vehicle) {
            binding.tvPlacaVehiculo.text = vehicle.placa
            binding.tvMarcaModeloVehiculo.text = "${vehicle.marca} ${vehicle.modelo}"
            binding.tvAnioColorVehiculo.text = "${vehicle.anio} - ${vehicle.color}"
            binding.tvTipoCombustibleVehiculo.text = vehicle.tipoCombustible
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Vehicle>() {
            override fun areItemsTheSame(oldItem: Vehicle, newItem: Vehicle):
                Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Vehicle, newItem: Vehicle):
                Boolean {
                return oldItem == newItem
            }
        }
    }
}