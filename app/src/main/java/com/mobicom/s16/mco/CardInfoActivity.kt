package com.mobicom.s16.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.databinding.CardinfoPageBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color
import com.github.mikephil.charting.components.XAxis


class CardInfoActivity : AppCompatActivity()   {
    private lateinit var binding: CardinfoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CardinfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDummyLineChart(binding.priceChart)
        binding.imgBack.setOnClickListener {
            finish()
        }


    }
}


fun setupDummyLineChart(lineChart: LineChart) {
    // Dummy data entries (x: days, y: price)
    val entries = listOf(
        Entry(1f, 120f),
        Entry(2f, 122f),
        Entry(3f, 118f),
        Entry(4f, 135f),
        Entry(5f, 149f)
    )

    val dataSet = LineDataSet(entries, "Price History").apply {
        color = Color.parseColor("#8c55b0")
        valueTextColor = Color.BLACK
        lineWidth = 2f
        circleRadius = 4f
        setCircleColor(Color.parseColor("#8c55b0"))
        setDrawFilled(true)
        fillColor = Color.parseColor("#E8DAF5")
    }

    val lineData = LineData(dataSet)

    lineChart.data = lineData

    // Styling
    lineChart.axisRight.isEnabled = false
    lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    lineChart.description.isEnabled = false
    lineChart.setTouchEnabled(false)
    lineChart.legend.isEnabled = false
    lineChart.invalidate() // Refresh the chart
}
