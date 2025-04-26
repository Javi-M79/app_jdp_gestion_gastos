package com.example.app_jdp_gestion_gastos.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsDialogBinding

class StatsDialog : DialogFragment() {

    private var _binding: FragmentStatsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var title: String
    private lateinit var items: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el título
        binding.tvDialogTitle.text = title

        // Configurar el RecyclerView
        val adapter = StatsItemsAdapter(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, items: List<String>): StatsDialog {
            val fragment = StatsDialog()
            val args = Bundle()
            args.putString("title", title)
            args.putStringArrayList("items", ArrayList(items))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title", "")
            items = it.getStringArrayList("items") ?: emptyList()
        }
    }
}