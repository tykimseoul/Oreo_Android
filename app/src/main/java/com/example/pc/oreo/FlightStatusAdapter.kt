package com.example.pc.oreo

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.example.pc.oreo.StatusIconView.StatusIconType

class FlightStatusAdapter(private val context: Context, private val titles: Array<String>, private val icons: TypedArray, private val values: DoubleArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var startTimer = false

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0, 1 -> return StatusIconType.UNCLICKABLE.value
            2 -> return StatusIconType.TIMER.value
            3 -> return StatusIconType.BATTERY.value
            4 -> return StatusIconType.WIFI.value
            5 -> return StatusIconType.BLUETOOTH.value
            6 -> return StatusIconType.GPS.value
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        when (viewType) {
            StatusIconType.UNCLICKABLE.value -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_status, parent, false)
                return FlightStatusViewHolder(itemView)
            }
            StatusIconType.BATTERY.value -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_status_battery, parent, false)
                return BatteryFlightStatusViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_status_wifi, parent, false)
                return WifiFlightStatusViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            StatusIconType.UNCLICKABLE.value -> (holder as FlightStatusViewHolder).bind(position)
            StatusIconType.BATTERY.value -> (holder as BatteryFlightStatusViewHolder).bind(position)
            StatusIconType.WIFI.value -> (holder as WifiFlightStatusViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    fun startTimer() {
        startTimer = true
        notifyItemChanged(2)
    }

    private inner class FlightStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val tvField: TextView = view.findViewById(R.id.status)
        private val tvValue: TextView = view.findViewById(R.id.value)
        private val ivIcon: ImageView = view.findViewById(R.id.icon)

        fun bind(position: Int) {
            tvField.text = titles[position]
            when (position) {
                0 -> tvValue.text = String.format("%.1f m/s", values[position])
                1 -> tvValue.text = String.format("%.1f m", values[position])
            }
            ivIcon.setImageResource(icons.getResourceId(position, -1))
        }
    }

    private inner class BatteryFlightStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val tvValue: TextView = view.findViewById(R.id.value)
        private val ivIcon: BatteryIconView = view.findViewById(R.id.icon)

        fun bind(position: Int) {
            tvValue.text = String.format("%.1fV", values[position])
            ivIcon.updateView(values[position])
        }
    }

    private inner class WifiFlightStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: WifiIconView = view.findViewById(R.id.icon)

        init {
            ivIcon.iconClickListener = context as MainActivity
        }

        fun bind(position: Int) {
            ivIcon.updateView(values[position])
        }
    }
}
