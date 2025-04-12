package com.example.app_jdp_gestion_gastos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.databinding.ItemTransactionBinding

class ExpenseAdapter(
    private val onTransactionSelected: (Expense) -> Unit,
    private val onRequestDelete: (Expense) -> Unit
) :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    private var selectedPosition: Int = -1 // Para almacenar la posición del ítem seleccionado

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding, this, onRequestDelete) // Pasar el adaptador al ViewHolder
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), onTransactionSelected, position, selectedPosition)
    }

    // Función para actualizar la posición seleccionada
    fun setSelectedPosition(position: Int) {
        val previousSelectedPosition = selectedPosition
        selectedPosition = if (selectedPosition == position) -1 else position // Alternar selección
        notifyItemChanged(previousSelectedPosition) // Notificar cambio en el ítem anterior
        notifyItemChanged(selectedPosition) // Notificar cambio en el ítem actual
    }

    class ExpenseViewHolder(
        private val binding: ItemTransactionBinding,
        private val adapter: ExpenseAdapter,// Recibir el adaptador
        private val onRequestDelete: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            expense: Expense,
            onTransactionSelected: (Expense) -> Unit,
            position: Int,
            selectedPosition: Int
        ) {
            binding.tvDescription.text = expense.name
            binding.tvAmount.text = "- ${"%.2f".format(expense.amount)} €"
            binding.tvDate.text = expense.date?.toDate().toString()
            binding.tvType.text = "Gasto"
            binding.tvType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.error))
            binding.ivIcon.setImageResource(R.drawable.expense)

            // Cambiar el fondo del item según si está seleccionado
            if (position == selectedPosition) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.gray
                    )
                ) // Color cuando
                // se selecciona
            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                ) // Fondo por defecto
            }

            //Click corto. Lanza el dialogo para editar el gasto.
            itemView.setOnClickListener {
                onTransactionSelected(expense)
            }

            itemView.setOnLongClickListener {
                onRequestDelete(expense)
                true
            }

        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem == newItem
    }
}