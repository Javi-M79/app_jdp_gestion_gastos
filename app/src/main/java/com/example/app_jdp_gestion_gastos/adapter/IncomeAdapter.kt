package com.example.app_jdp_gestion_gastos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Income

class IncomeAdapter : ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        private val typeTextView: TextView = itemView.findViewById(R.id.tvType)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        private val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        private val iconImageView: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(income: Income) {
            descriptionTextView.text = income.name
            typeTextView.text = "Ingreso"
            dateTextView.text = income.date?.toDate().toString()
            amountTextView.text = "$${income.amount}"
            iconImageView.setImageResource(R.drawable.otros) // Ícono predeterminado
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