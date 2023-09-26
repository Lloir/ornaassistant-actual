package com.rockethat.ornaassistant.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rockethat.ornaassistant.R
import com.rockethat.ornaassistant.db.DungeonVisitDatabaseHelper
import com.rockethat.ornaassistant.DungeonVisit
import java.time.LocalDate

import android.content.res.Configuration

class MainFragment : Fragment() {

    private lateinit var mDb: DungeonVisitDatabaseHelper
    private lateinit var mSharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDb = DungeonVisitDatabaseHelper(requireContext())
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_main, container, false)
        val allowPermission: Button = view.findViewById(R.id.allowPermission)
        val donate: Button = view.findViewById(R.id.donate)

        allowPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        donate.setOnClickListener {
            val uri = Uri.parse("https://www.paypal.com/donate/?business=L7Q94HMXMHA5A&no_recurring=0&item_name=Orna+assistant+development&currency_code=EUR")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        drawWeeklyChart(view)

        return view
    }

    inner class WeekAxisFormatter(private val startDay: Int) : ValueFormatter() {
        private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            var index = startDay - 1 + value.toInt()
            if (index > 6) {
                index -= 7
            }
            return days.getOrNull(index) ?: value.toString()
        }
    }

    inner class IntegerFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return value.toInt().toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun drawWeeklyChart(view: View? = this.view) {
        val chart: BarChart = view?.findViewById(R.id.cWeeklyDungeons) as BarChart

        val eDung = mutableListOf<BarEntry>()
        val eFailedDung = mutableListOf<BarEntry>()
        val eOrns = mutableListOf<BarEntry>()

        val startOfToday = LocalDate.now().atStartOfDay()
        val startDay = startOfToday.minusDays(6).dayOfWeek.value
        for (i in 6 downTo 0) {
            val entries = mDb.getEntriesBetween(
                startOfToday.minusDays(i.toLong()),
                startOfToday.minusDays((i - 1).toLong())
            )

            val completed = entries.filter { it.completed } as ArrayList<DungeonVisit>
            val failed = entries.filter { !it.completed } as ArrayList<DungeonVisit>

            eDung.add(BarEntry(i.toFloat(), completed.size.toFloat()))
            eFailedDung.add(BarEntry(i.toFloat(), failed.size.toFloat()))
            var orns = 0f
            entries.forEach {
                orns += it.orns
            }
            orns /= 1000000
            eOrns.add(BarEntry(i.toFloat(), orns))
        }

        val textColor = if (requireContext().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            Color.LTGRAY
        } else {
            Color.BLACK
        }

        val sDung = BarDataSet(eDung, "Dungeons")
        val sFailedDung = BarDataSet(eFailedDung, "Failed dungeons")
        val sOrns = BarDataSet(eOrns, "Orns gained (mil)")
        sDung.valueFormatter = IntegerFormatter()
        sFailedDung.valueFormatter = IntegerFormatter()
        sDung.valueTextSize = 12f
        sFailedDung.valueTextSize = 12f
        sOrns.valueTextSize = 12f
        sDung.color = Color.parseColor("#ff6d00")
        sFailedDung.color = Color.parseColor("#c62828")
        sOrns.color = Color.parseColor("#558b2f")

        sDung.valueTextColor = textColor
        sFailedDung.valueTextColor = textColor
        sOrns.valueTextColor = textColor
        val data = BarData(sDung, sFailedDung, sOrns)

        val groupSpace = 0.06f
        val barSpace = 0.02f // x2 dataset

        val barWidth = 0.29f
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        data.barWidth = barWidth // x2 dataset

        chart.data = data
        chart.xAxis.valueFormatter = WeekAxisFormatter(startDay)
        chart.xAxis.textSize = 10f
        chart.xAxis.textColor = textColor
        chart.xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        chart.groupBars(0F, groupSpace, barSpace)
        chart.xAxis.axisMaximum = 7f
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.setDrawGridLines(false)
        chart.description.isEnabled = false

        chart.axisLeft.textColor = textColor
        chart.axisRight.textColor = textColor
        chart.legend.textColor = textColor
        chart.invalidate()
    }
}