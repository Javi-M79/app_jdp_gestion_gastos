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

class IncomeAdapter : ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IncomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncomeViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(income: Income) {
            binding.tvDescription.text = income.name
            binding.tvAmount.text = "${"%.2f".format(income.amount)} €"
            binding.tvDate.text = income.date?.toDate().toString()
            binding.tvType.text = "Ingreso"

            // Cambiar color de texto según el tipo
            binding.tvType.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.primaryColor)
            )

            // Establecer icono (si no hay, poner uno predeterminado)
            binding.ivIcon.setImageResource(R.drawable.otros)
        }
    }

    class IncomeDiffCallback : DiffUtil.ItemCallback<Income>() {
        override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem.date == newItem.date && oldItem.amount == newItem.amount
        }

        override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem == newItem
        }
    }
}