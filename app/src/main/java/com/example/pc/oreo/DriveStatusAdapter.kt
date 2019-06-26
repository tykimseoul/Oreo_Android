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

class DriveStatusAdapter(private val context: Context, private val titles: Array<String>, private val icons: TypedArray, private val values: DoubleArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return StatusIconType.UNCLICKABLE.value
            1 -> return StatusIconType.BATTERY.value
            2 -> return StatusIconType.WIFI.value
            3-> return StatusIconType.SELF_DRIVE.value
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        return when (viewType) {
            StatusIconType.UNCLICKABLE.value -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status, parent, false)
                DriveStatusViewHolder(itemView)
            }
            StatusIconType.BATTERY.value -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_battery, parent, false)
                BatteryDriveStatusViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_wifi, parent, false)
                WifiDriveStatusViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            StatusIconType.UNCLICKABLE.value -> (holder as DriveStatusViewHolder).bind(position)
            StatusIconType.BATTERY.value -> (holder as BatteryDriveStatusViewHolder).bind(position)
            StatusIconType.WIFI.value -> (holder as WifiDriveStatusViewHolder).bind(position)
            StatusIconType.SELF_DRIVE.value->(holder as SelfDriveStatusViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    private inner class DriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
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

    private inner class BatteryDriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val tvValue: TextView = view.findViewById(R.id.value)
        private val ivIcon: BatteryIconView = view.findViewById(R.id.icon)

        fun bind(position: Int) {
            tvValue.text = String.format("%.1fV", values[position])
            ivIcon.updateView(values[position])
        }
    }

    private inner class WifiDriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: WifiIconView = view.findViewById(R.id.icon)

        init {
            ivIcon.iconClickListener = context as MainActivity
        }

        fun bind(position: Int) {
            ivIcon.updateView(values[position])
        }
    }

    private inner class SelfDriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: SelfDriveIconView = view.findViewById(R.id.icon)

        init {
            ivIcon.iconClickListener = context as MainActivity
        }

        fun bind(position: Int) {
            ivIcon.updateView(values[position])
        }
    }
}
