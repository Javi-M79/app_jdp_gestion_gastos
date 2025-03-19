package com.example.app_jdp_gestion_gastos.ui.fragments

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
import com.example.app_jdp_gestion_gastos.ui.dialog.GroupDetailsDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodels.GroupsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : Fragment() {
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupsViewModel by viewModels()
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

        binding.fabAddGroup.setOnClickListener {
            val dialog = CreateGroupDialog()
            dialog.show(childFragmentManager, "CreateGroupDialog")
        }

        binding.btnBorrarGrupo.setOnClickListener {
            viewModel.deleteSelectedGroup()
        }
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            viewModel.selectGroup(group)
            Toast.makeText(requireContext(), "Grupo seleccionado: ${group.name}", Toast.LENGTH_SHORT).show()


            // Mostrar el diálogo con los detalles del grupo seleccionado
            val dialog = GroupDetailsDialog.newInstance(group)
            dialog.show(childFragmentManager, "GroupDetailsDialog")

        }
        binding.recyclerViewGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun setupObservers() {
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupAdapter.submitList(groups)
        }

        viewModel.selectedGroup.observe(viewLifecycleOwner) { selectedGroup ->
            binding.btnBorrarGrupo.isEnabled = selectedGroup != null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}