package com.example.app_jdp_gestion_gastos.ui.fragments

import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.databinding.FragmentGroupsBinding
import com.example.app_jdp_gestion_gastos.ui.adapters.GroupAdapter
import com.example.app_jdp_gestion_gastos.ui.dialog.CreateGroupDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupViewModel
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R

class GroupsFragment : Fragment() {
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupViewModel by viewModels()
    private lateinit var groupAdapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            viewModel.selectGroup(group)
            navigateToGroupDetails(group.id)
        }

        binding.recyclerViewGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun navigateToGroupDetails(groupId: String) {
        if (groupId.isNotBlank()) {

            val action =
                GroupsFragmentDirections.actionGroupsFragmentToGroupDetailsFragment(groupId)
            findNavController().navigate(action)

        } else {
            Log.e("GroupsFragment", "Error: group.id es nulo o vacío, no se puede navegar")
        }
    }

    private fun setupObservers() {
        viewModel.userGroups.observe(viewLifecycleOwner) { groups ->
            Log.d("GroupsFragment", "Lista de grupos actualizada: $groups")
            groupAdapter.submitList(groups)
        }

        viewModel.selectedGroup.observe(viewLifecycleOwner) { selectedGroup ->
            Log.d("GroupsFragment", "Grupo seleccionado en ViewModel: $selectedGroup")
            binding.btnBorrarGrupo.apply {
                visibility = if (selectedGroup != null) View.VISIBLE else View.GONE
                isEnabled = selectedGroup != null
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddGroup.setOnClickListener {
            CreateGroupDialog().show(childFragmentManager, "CreateGroupDialog")
        }

        binding.btnBorrarGrupo.setOnClickListener {
            viewModel.deleteSelectedGroup()
            Toast.makeText(requireContext(), "Grupo eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
