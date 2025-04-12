package com.example.app_jdp_gestion_gastos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.databinding.ItemTransactionBinding

class IncomeAdapter(
    private val onTransactionSelected: (Income) -> Unit,
    private val  onRequestDelete:(Income) -> Unit
):ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {

    private var selectedPosition: Int = -1 // Para almacenar la posición del ítem seleccionado

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IncomeViewHolder(binding, this, onRequestDelete ) // Pasar el adaptador al ViewHolder
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position), onTransactionSelected, position, selectedPosition)
    }

    // Función para actualizar la posición seleccionada
    fun setSelectedPosition(position: Int) {
        val previousSelectedPosition = selectedPosition
        selectedPosition = if (selectedPosition == position) -1 else position // Alternar selección
        notifyItemChanged(previousSelectedPosition) // Notificar cambio en el ítem anterior
        notifyItemChanged(selectedPosition) // Notificar cambio en el ítem actual
    }

    class IncomeViewHolder(
        private val binding: ItemTransactionBinding,
        private val adapter: IncomeAdapter,
        private val onRequestDelete: (Income) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(income: Income, onTransactionSelected: (Income) -> Unit, position: Int, selectedPosition: Int) {
            binding.tvDescription.text = income.name
            binding.tvAmount.text = "${"%.2f".format(income.amount)} €"
            binding.tvDate.text = income.date?.toDate().toString()
            binding.tvType.text = "Ingreso"
            binding.tvType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primaryColor))
            binding.ivIcon.setImageResource(R.drawable.incomes)

            // Cambiar el fondo del item según si está seleccionado
            if (position == selectedPosition) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray)) // Color cuando
            // se selecciona
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white)) // Fondo por defecto
            }


            //Pulsacion corta ->Editar ingreso
            itemView.setOnClickListener {
                onTransactionSelected(income)
            }
            //Pulsacion larga-> Eliminar ingreso
            itemView.setOnLongClickListener {
                onRequestDelete(income)
                true
            }
        }
    }

    class IncomeDiffCallback : DiffUtil.ItemCallback<Income>() {
        override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean = oldItem == newItem
    }
}