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

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ExpenseViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.name
            binding.tvAmount.text = "-${expense.amount} €"
            binding.tvDate.text = expense.date?.toDate().toString()
            binding.tvType.text = "Gasto"

            // Cambiar color de texto según el tipo
            binding.tvType.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.error)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}