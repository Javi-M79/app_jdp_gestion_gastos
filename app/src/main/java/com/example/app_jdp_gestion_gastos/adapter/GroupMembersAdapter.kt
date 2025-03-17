package com.example.app_jdp_gestion_gastos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_jdp_gestion_gastos.databinding.ItemMemberBinding

class GroupMembersAdapter : RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder>() {

    private var members: List<String> = emptyList()

    fun submitList(newMembers: List<String>) {
        if (members != newMembers) {
            members = newMembers
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    class MemberViewHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(memberId: String) {
            binding.tvMemberName.text = memberId // Reemplázalo por un nombre si tienes la información
        }
    }
}