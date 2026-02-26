package com.example.airich.ui.moodtracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.R
import com.example.airich.data.FoodDatabase
import com.example.airich.databinding.FragmentMoodLogBinding
import com.example.airich.repository.MoodRepository
import com.example.airich.viewmodel.MoodViewModel
import com.example.airich.viewmodel.MoodViewModelFactory
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class MoodLogFragment : Fragment() {

    private var _binding: FragmentMoodLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MoodViewModel
    private lateinit var adapter: MoodEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val context = requireContext().applicationContext

            val database = FoodDatabase.getDatabase(context)
            val repository = MoodRepository(database)
            val factory = MoodViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[MoodViewModel::class.java]
        } catch (e: Exception) {

            android.util.Log.e("MoodLogFragment", "Ошибка инициализации", e)
            android.util.Log.e("MoodLogFragment", "Stack trace", e)
            return
        }

        adapter = MoodEntryAdapter { moodEntryId ->
            viewModel.deleteMoodEntry(moodEntryId)
        }
        binding.rvMoodEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoodEntries.adapter = adapter

        setupChart()

        viewModel.weeklyMoodData.observe(viewLifecycleOwner) { dataPoints ->
            dataPoints?.let {
                if (it.isNotEmpty()) {
                    updateChart(it)
                }
            }
        }

        viewModel.recentEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries ?: emptyList())
        }

        viewModel.hasTodayEntry.observe(viewLifecycleOwner) { hasEntry ->
            binding.cardReminder.visibility = if (hasEntry == true) View.GONE else View.VISIBLE
        }

        binding.btnAddMood.setOnClickListener {
            showAddMoodDialog()
        }
    }

    private fun setupChart() {
        val chart = binding.chartMood

        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)

        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 7

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.granularity = 1f
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawZeroLine(false)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.animateX(500)
    }

    private fun updateChart(dataPoints: List<MoodViewModel.MoodDataPoint>) {
        if (_binding == null) {
            return
        }

        try {
            val entries = mutableListOf<Entry>()

            dataPoints.forEachIndexed { index, point ->
                entries.add(Entry(index.toFloat(), point.moodScore))
            }

            if (entries.isEmpty()) {
                return
            }

            val dataSet = LineDataSet(entries, "Настроение")
            val color = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
            dataSet.color = color
            dataSet.setCircleColor(color)
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(false)
            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 10f
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            dataSet.cubicIntensity = 0.2f

            val lineData = LineData(dataSet)
            binding.chartMood.data = lineData

            val xAxis = binding.chartMood.xAxis
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index >= 0 && index < dataPoints.size) {
                        return dataPoints[index].dayName
                    }
                    return ""
                }
            }

            binding.chartMood.invalidate()
        } catch (e: Exception) {

        }
    }

    private fun showAddMoodDialog() {
        try {
            if (!::viewModel.isInitialized) {
                return
            }

            val dialog = AddMoodDialogFragment.newInstance { moodScore, note ->
                if (::viewModel.isInitialized) {
                    viewModel.addMoodEntry(moodScore, note)
                }
            }
            dialog.show(parentFragmentManager, "AddMoodDialog")
        } catch (e: Exception) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
