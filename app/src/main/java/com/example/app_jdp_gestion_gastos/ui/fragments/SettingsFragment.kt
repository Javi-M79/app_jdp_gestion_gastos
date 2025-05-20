package com.example.app_jdp_gestion_gastos.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.ui.activities.MainActivity
import com.example.login.ui.dialog.AyudaDialogo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class SettingsFragment : Fragment() {

    private lateinit var switchTheme: Switch
    private lateinit var switchNotifications: Switch
    private lateinit var prefs: SharedPreferences
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        switchTheme = view.findViewById(R.id.switchTheme)
        switchNotifications = view.findViewById(R.id.switchNotifications)

        val layoutBudget = view.findViewById<LinearLayout>(R.id.layoutBudget)
        val layoutExport = view.findViewById<LinearLayout>(R.id.layoutExport)
        val layoutReset = view.findViewById<LinearLayout>(R.id.layoutReset)
        val layoutLogout = view.findViewById<LinearLayout>(R.id.layoutLogout)
        val layoutHelp = view.findViewById<LinearLayout>(R.id.layoutHelp)
        val layoutPassword = view.findViewById<LinearLayout>(R.id.layoutChangePassword)

        switchTheme.isChecked = prefs.getBoolean("dark_mode", false)
        switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            Toast.makeText(context, "Tema actualizado", Toast.LENGTH_SHORT).show()
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            Toast.makeText(
                context,
                "Notificaciones ${if (isChecked) "activadas" else "desactivadas"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        layoutBudget.setOnClickListener { showBudgetDialog() }
        layoutExport.setOnClickListener { checkPermissionAndExportData() }
        layoutReset.setOnClickListener { confirmResetData() }
        layoutLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        layoutHelp.setOnClickListener {
            AyudaDialogo().show(parentFragmentManager, "AyudaDialogo")
        }
        layoutPassword.setOnClickListener { resetPassword() }

        return view
    }

    private fun showBudgetDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Establecer presupuesto mensual")

        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefs.getString("monthly_budget", ""))
            hint = "Ejemplo: 500.00"
        }

        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val budget = input.text.toString()
            if (budget.isNotBlank()) {
                prefs.edit().putString("monthly_budget", budget).apply()
                Toast.makeText(context, "Presupuesto guardado: $budget €", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Introduce un valor válido", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun confirmResetData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restablecer datos")
            .setMessage("¿Estás seguro de que deseas eliminar todos los datos?")
            .setPositiveButton("Sí") { _, _ ->
                auth.currentUser?.uid?.let {
                    deleteUserData(it)
                } ?: Toast.makeText(context, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUserData(userId: String) {
        val collections = listOf("incomes", "expenses")
        var deletions = 0
        var totalToDelete = 0
        var errors = false

        collections.forEach { collection ->
            firestore.collection(collection)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    totalToDelete += documents.size()
                    if (documents.isEmpty) {
                        deletions++
                        if (deletions == collections.size) {
                            onDeleteComplete(errors)
                        }
                    } else {
                        for (doc in documents) {
                            firestore.collection(collection).document(doc.id).delete()
                                .addOnSuccessListener {
                                    deletions++
                                    if (deletions == totalToDelete) onDeleteComplete(errors)
                                }
                                .addOnFailureListener {
                                    errors = true
                                    deletions++
                                    if (deletions == totalToDelete) onDeleteComplete(errors)
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    errors = true
                    deletions++
                    if (deletions == collections.size) onDeleteComplete(errors)
                }
        }
    }

    private fun onDeleteComplete(errorOccurred: Boolean) {
        Toast.makeText(
            context,
            if (errorOccurred) "Error eliminando algunos datos"
            else "Todos los datos han sido eliminados",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkPermissionAndExportData() {
        // Para Android 11+ no es necesario pedir permiso para escribir en Downloads (usando MediaStore)
        // Pero para versiones anteriores sí
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
                return
            }
        }
        exportDataToFile()
    }

    private fun exportDataToFile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        val collections = listOf("incomes", "expenses")
        val data = mutableMapOf<String, List<Map<String, Any>>>()
        var loaded = 0

        for (collection in collections) {
            firestore.collection(collection)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val items = result.documents.map { it.data ?: emptyMap() }
                    data[collection] = items
                    loaded++
                    if (loaded == collections.size) {
                        showPdfExportOptions(data)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createPdfFromData(data: Map<String, List<Map<String, Any>>>, share: Boolean) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        var pageNumber = 1
        var yPosition = 50
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        for ((collection, items) in data) {
            canvas.drawText("===== ${collection.uppercase()} =====", 20f, yPosition.toFloat(), titlePaint)
            yPosition += 30

            for (item in items) {
                val line = item.entries.joinToString(" | ") { "${it.key}: ${it.value}" }
                if (yPosition > 800) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    yPosition = 50
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                }
                canvas.drawText(line, 20f, yPosition.toFloat(), paint)
                yPosition += 20
            }

            yPosition += 40
        }

        pdfDocument.finishPage(page)

        val fileName = "DatosGastos_${System.currentTimeMillis()}.pdf"

        // Guardar usando MediaStore para Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                try {
                    requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                        pdfDocument.writeTo(outputStream!!)
                    }
                    pdfDocument.close()
                    Toast.makeText(context, "PDF exportado correctamente", Toast.LENGTH_SHORT).show()

                    if (share) {
                        sharePdf(uri)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Error creando archivo PDF", Toast.LENGTH_LONG).show()
            }
        } else {
            // Android 9 y anteriores, usar File y pedir permiso WRITE_EXTERNAL_STORAGE
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            try {
                val outputStream = FileOutputStream(file)
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                pdfDocument.close()
                Toast.makeText(context, "PDF exportado correctamente", Toast.LENGTH_SHORT).show()

                if (share) {
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        file
                    )
                    sharePdf(uri)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sharePdf(uri: android.net.Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
    }

    private fun showPdfExportOptions(data: Map<String, List<Map<String, Any>>>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exportar PDF")
        builder.setMessage("¿Qué deseas hacer con el archivo PDF?")

        builder.setPositiveButton("Guardar") { _, _ ->
            createPdfFromData(data, share = false)
        }

        builder.setNegativeButton("Guardar y Compartir") { _, _ ->
            createPdfFromData(data, share = true)
        }

        builder.setNeutralButton("Cancelar", null)
        builder.show()
    }

    private fun resetPassword() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = it.email
            if (email != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Correo de restablecimiento enviado a $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al enviar el correo de restablecimiento", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "No se encontró el correo del usuario", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportDataToFile()
            } else {
                Toast.makeText(requireContext(), "Permiso denegado para exportar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}