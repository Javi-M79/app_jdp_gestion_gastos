package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.databinding.FragmentGroupDetailsBinding
import com.example.app_jdp_gestion_gastos.ui.adapters.GroupMembersAdapter
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupViewModel

class GroupDetailsFragment : Fragment() {
    private var _binding: FragmentGroupDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupViewModel by viewModels()
    private lateinit var membersAdapter: GroupMembersAdapter
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = GroupDetailsFragmentArgs.fromBundle(it).groupId
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (::groupId.isInitialized && groupId.isNotEmpty()) {
            viewModel.loadGroupDetails(groupId)
        } else {
            Toast.makeText(requireContext(), "ID de grupo no válido", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        membersAdapter = GroupMembersAdapter()
        binding.recyclerViewGroupMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }
    }

    private fun setupObservers() {
        viewModel.selectedGroup.observe(viewLifecycleOwner) { group ->
            group?.let {
                binding.tvGroupName.text = it.name
                binding.tvGroupMembers.text = "Miembros: ${it.members.size}"
                membersAdapter.submitList(it.members)

                binding.btnDeleteGroup.visibility = if (viewModel.getCurrentUserId() == it.createdBy)
                    View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLeaveGroup.setOnClickListener {
            viewModel.leaveGroup()
            Toast.makeText(requireContext(), "Saliste del grupo", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        binding.btnDeleteGroup.setOnClickListener {
            viewModel.deleteSelectedGroup()
            Toast.makeText(requireContext(), "Grupo eliminado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}