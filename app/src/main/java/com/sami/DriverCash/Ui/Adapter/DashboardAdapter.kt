package com.sami.DriverCash.Ui.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sami.DriverCash.Ui.Model.DashboardCardItem
import com.sami.DriverCash.databinding.ItemDashboardCardBinding

class DashboardAdapter(
    private val onAddItemClick: (DashboardCardItem) -> Unit,
    private val onCardClick: (DashboardCardItem) -> Unit
) : ListAdapter<DashboardCardItem, DashboardAdapter.DashboardViewHolder>(DashboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding = ItemDashboardCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DashboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class DashboardViewHolder(private val binding: ItemDashboardCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnCardAddItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddItemClick(getItem(position))
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position).detailsActionNavId?.let { // Solo navega si detailsActionNavId no es null
                        onCardClick(getItem(position))
                    }
                }
            }
        }

        fun bind(item: DashboardCardItem) {
            binding.tvCardTitle.text = item.title
            binding.tvCardLastEntrySummary.text = item.lastEntrySummary
            // El icono del botón ya está en el XML, pero podrías cambiarlo aquí si fuera necesario
        }
    }

    class DashboardDiffCallback : DiffUtil.ItemCallback<DashboardCardItem>() {
        override fun areItemsTheSame(oldItem: DashboardCardItem, newItem: DashboardCardItem): Boolean {
            return oldItem.type == newItem.type // Asumimos que el tipo es un identificador único
        }

        override fun areContentsTheSame(oldItem: DashboardCardItem, newItem: DashboardCardItem): Boolean {
            return oldItem == newItem
        }
    }
}