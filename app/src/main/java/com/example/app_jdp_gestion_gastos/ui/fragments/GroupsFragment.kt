package com.example.app_jdp_gestion_gastos.ui.fragments

import GroupDetailsDialog
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.example.app_jdp_gestion_gastos.databinding.FragmentGroupsBinding
import com.example.app_jdp_gestion_gastos.ui.adapters.GroupAdapter
import com.example.app_jdp_gestion_gastos.ui.dialog.CreateGroupDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupsViewModel by viewModels()
    private lateinit var groupAdapter: GroupAdapter
    private var allGroups: List<Group> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Para mostrar el SearchView en la toolbar
    }

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

        binding.btnInvitarUsuario.setOnClickListener {
            showInviteUserDialog()
        }

        binding.btnExportarGrupo.setOnClickListener {
            exportGroupToPdf()
        }
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            viewModel.selectGroup(group)
            groupAdapter.setSelectedGroup(group)
            Toast.makeText(requireContext(), "Grupo seleccionado: ${group.name}", Toast.LENGTH_SHORT).show()

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
            allGroups = groups
            groupAdapter.submitList(groups)
        }

        viewModel.selectedGroup.observe(viewLifecycleOwner) { selectedGroup ->
            binding.btnBorrarGrupo.isEnabled = selectedGroup != null
            binding.btnInvitarUsuario.isEnabled = selectedGroup != null
            binding.btnExportarGrupo.isEnabled = selectedGroup != null

            selectedGroup?.let {
                groupAdapter.setSelectedGroup(it)
            }
        }
    }
    private fun showInviteUserDialog() {
        val selectedGroup = viewModel.selectedGroup.value
        if (selectedGroup == null) {
            Toast.makeText(requireContext(), "Selecciona un grupo primero", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Invitar usuario por correo electrónico")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Invitar") { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                inviteUserByEmail(email, selectedGroup)
            } else {
                Toast.makeText(requireContext(), "El correo no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun inviteUserByEmail(email: String, group: Group) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    val userId = userDoc.id

                    if (!group.members.contains(userId)) {
                        val updatedMembers = group.members.toMutableList()
                        updatedMembers.add(userId)

                        db.collection("groups").document(group.id)
                            .update("members", updatedMembers)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Usuario invitado exitosamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error al invitar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "El usuario ya es miembro del grupo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontró un usuario con ese correo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al buscar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exportGroupToPdf() {
        val group = viewModel.selectedGroup.value
        if (group == null) {
            Toast.makeText(requireContext(), "Selecciona un grupo primero", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val document = Document()
            val fileName = "Grupo_${group.name.replace(" ", "_")}.pdf"
            val filePath = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()

            document.add(Paragraph("Nombre del grupo: ${group.name}"))
            document.add(Paragraph("Creado por: ${group.createdBy}"))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val createdAt = group.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Fecha desconocida"
            document.add(Paragraph("Fecha de creación: $createdAt"))
            document.add(Paragraph("Miembros:"))
            group.members.forEach { memberId ->
                document.add(Paragraph("- $memberId"))
            }

            document.close()

            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                filePath
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al exportar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_groups, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Buscar grupos"
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrEmpty()) {
                    allGroups.filter { it.name.contains(newText, ignoreCase = true) }
                } else {
                    allGroups
                }
                groupAdapter.submitList(filteredList)

                // Mantener selección activa si está en la lista filtrada
                viewModel.selectedGroup.value?.let { selected ->
                    if (filteredList.any { it.id == selected.id }) {
                        groupAdapter.setSelectedGroup(selected)
                    } else {
                        viewModel.selectGroup(null)
                    }
                }

                return true
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}