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

class DriveStatusAdapter(private val context: Context, private val titles: Array<String>, private val icons: TypedArray, private val values: IntArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return StatusIconType.UNCLICKABLE.index
            1 -> return StatusIconType.BATTERY.index
            2 -> return StatusIconType.WIFI.index
            3 -> return StatusIconType.SELF_DRIVE.index
            4 -> return StatusIconType.GEARBOX.index
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        return when (viewType) {
            StatusIconType.UNCLICKABLE.index -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status, parent, false)
                DriveStatusViewHolder(itemView)
            }
            StatusIconType.BATTERY.index -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_battery, parent, false)
                BatteryDriveStatusViewHolder(itemView)
            }
            StatusIconType.WIFI.index -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_wifi, parent, false)
                WifiDriveStatusViewHolder(itemView)
            }
            StatusIconType.SELF_DRIVE.index -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_self_drive, parent, false)
                SelfDriveStatusViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drive_status_gear, parent, false)
                GearboxDriveStatusViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            StatusIconType.UNCLICKABLE.index -> (holder as DriveStatusViewHolder).bind(position)
            StatusIconType.BATTERY.index -> (holder as BatteryDriveStatusViewHolder).bind(position)
            StatusIconType.WIFI.index -> (holder as WifiDriveStatusViewHolder).bind(position)
            StatusIconType.SELF_DRIVE.index -> (holder as SelfDriveStatusViewHolder).bind(position)
            StatusIconType.GEARBOX.index -> (holder as GearboxDriveStatusViewHolder).bind(position)
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
                0 -> tvValue.text = String.format("%d m/s", values[position])
                1 -> tvValue.text = String.format("%d m", values[position])
            }
            ivIcon.setImageResource(icons.getResourceId(position, -1))
        }
    }

    private inner class BatteryDriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val tvValue: TextView = view.findViewById(R.id.value)
        private val ivIcon: BatteryIconView = view.findViewById(R.id.icon)

        fun bind(position: Int) {
            tvValue.text = String.format("%dV", values[position])
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

    private inner class GearboxDriveStatusViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: GearboxIconView = view.findViewById(R.id.icon)

        init {
            ivIcon.iconClickListener = context as MainActivity
        }

        fun bind(position: Int) {
            ivIcon.updateView(values[position])
        }
    }
}
