package com.example.pc.oreo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.beust.klaxon.Klaxon
import com.example.pc.oreo.StatusIconView.StatusIconType
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), WifiConnector.OreoWifiDataChangeListener, StatusIconView.IconClickListener, ControlView.ControlViewListener {


    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET)
    private var missingPermissions: MutableList<Int>? = null
    private var wifiManager: WifiManager? = null
    private var wifiScanResults: List<ScanResult>? = null
    private val connectionReceiver = ConnectionReceiver()
    private val oreo: Oreo by lazy { Oreo() }
    private var driveStatusAdapter: DriveStatusAdapter? = null
    private lateinit var driveFields: Array<String>
    private lateinit var driveIcons: TypedArray
    private val driveValues = IntArray(10)
    private val currentFrame = ByteArray(20480)
    private var chunkOffset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        oreo.wifiConnector.wifiDataChangeListener = this
        requestAllPermissions()

        driveFields = resources.getStringArray(R.array.drive_status_fields)
        driveIcons = resources.obtainTypedArray(R.array.drive_status_icons)
        driveValues[StatusIconType.GEARBOX.index] = Oreo.GearType.PARK.index
        driveStatusAdapter = DriveStatusAdapter(this, driveFields, driveIcons, driveValues)
        val driveLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        driveStatusRecyclerView.layoutManager = driveLayoutManager
        (driveStatusRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        driveStatusRecyclerView.adapter = driveStatusAdapter
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(connectionReceiver, intentFilter)
        enableWifi()
        oreo.startControlStream()
    }

    private fun hideStatusBar() {
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        window.decorView.systemUiVisibility = uiOptions
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableWifi() {
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        wifiManager?.apply {
            isWifiEnabled = true
            startScan()
        }
    }

    private fun connectTello() {
        //        ScanResult scanResult = null;
        //        for (ScanResult result : wifiScanResults) {
        //            if (result.SSID.contains("TELLO")) {
        //                scanResult = result;
        //                Log.e("WIFI", result.SSID);
        //                break;
        //            }
        //        }
        //        if (scanResult == null) {
        //            Log.e("WIFI", "null result");
        //            updateWifiConnection(WifiIconView.WIFI_STATE_DISCONNECTED);
        //            return;
        //        }
        //        WifiConfiguration wifiConfig = new WifiConfiguration();
        //        wifiConfig.SSID = String.format("\"%s\"", scanResult.SSID);
        //        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //
        //        try {
        //            int netId = wifiManager.addNetwork(wifiConfig);
        //            if (netId == -1) {
        //                netId = getExistingNetworkId(wifiConfig.SSID);
        //            }
        //            wifiManager.disconnect();
        //            wifiManager.enableNetwork(netId, true);
        //            wifiManager.reconnect();
        //            updateWifiConnection(WifiIconView.WIFI_STATE_CONNECTED);
        //        } catch (Exception e) {
        //            updateWifiConnection(WifiIconView.WIFI_STATE_DISCONNECTED);
        //        }
        oreo.connect()
    }

    @Throws(Exception::class)
    private fun getExistingNetworkId(SSID: String): Int {
        val wifiManager = super.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configuredNetworks = wifiManager.configuredNetworks
        if (configuredNetworks != null) {
            for (existingConfig in configuredNetworks) {
                if (existingConfig.SSID == SSID) {
                    return existingConfig.networkId
                }
            }
        }
        throw Exception()
    }

    private fun updateWifiConnection(connection: Int) {
        when (connection) {
            WifiIconView.WIFI_STATE_CONNECTED -> {
                oreo.setWifiConnection(true)
                driveValues[StatusIconType.WIFI.index] = WifiIconView.WIFI_STATE_CONNECTED
            }
            WifiIconView.WIFI_STATE_DISCONNECTED -> {
                oreo.setWifiConnection(false)
                driveValues[StatusIconType.WIFI.index] = WifiIconView.WIFI_STATE_DISCONNECTED
            }
            WifiIconView.WIFI_STATE_PENDING -> {
                oreo.setWifiConnection(false)
                driveValues[StatusIconType.WIFI.index] = WifiIconView.WIFI_STATE_PENDING
            }
        }
        runOnUiThread {
            driveStatusAdapter?.notifyItemChanged(StatusIconType.WIFI.index)
        }
    }

    public override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        oreo.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectionReceiver)
    }

    override fun onDriveDataReceived(jsonData: String) {
        Klaxon().parse<DriveData>(jsonData)?.apply {
            driveValues[StatusIconType.BATTERY.index] = (voltage.times(10)).toInt()
            driveValues[0] = (speed * 10).toInt()
            runOnUiThread {
                driveStatusAdapter?.notifyItemChanged(StatusIconType.BATTERY.index)
                driveStatusAdapter?.notifyItemChanged(StatusIconType.UNCLICKABLE.index)
            }
        }
    }

    override fun onVideoDataReceived(videoBuffer: ByteArray, size: Int) {
        if (videoBuffer[2].toInt() == 0 && videoBuffer[3].toInt() == 0 && videoBuffer[4].toInt() == 0 && videoBuffer[5].toInt() == 1) { //if nal
            if (chunkOffset > 0) {
                Log.e("offset", chunkOffset.toString() + "")
//              decoderView!!.decode(Arrays.copyOfRange(currentFrame, 0, chunkOffset))
                chunkOffset = 0
            }
        }
        System.arraycopy(videoBuffer, 2, currentFrame, chunkOffset, size)
        chunkOffset += size
    }

    override fun onIconClick(type: StatusIconType) {
        when (type) {
            StatusIconType.WIFI -> enableWifi()
            StatusIconType.GEARBOX -> {
                oreo.gear.shift()
                oreo.controlCommand.reset()
            }
            StatusIconType.UNCLICKABLE, StatusIconType.BATTERY -> {
            }
            StatusIconType.SELF_DRIVE -> {
                oreo.controlCommand.selfDrive = Oreo.DriveMode.shiftFrom(oreo.controlCommand.selfDrive)
                oreo.controlCommand.reset()
                driveValues[StatusIconType.SELF_DRIVE.index] = (driveValues[StatusIconType.SELF_DRIVE.index] + 1) % Oreo.DriveMode.values().size
                driveStatusAdapter?.notifyItemChanged(StatusIconType.SELF_DRIVE.index)
            }
        }
    }

    override fun onAccelerate(power: Float) {
        if (oreo.controlCommand.selfDrive != Oreo.DriveMode.AUTONOMOUS)
            oreo.controlCommand.speedPercentage = power * oreo.gear.type.value
    }

    override fun onSteer(angle: Float) {
        if (oreo.controlCommand.selfDrive != Oreo.DriveMode.AUTONOMOUS)
            oreo.controlCommand.steerPercentage = angle
    }

    private fun requestAllPermissions() {
        missingPermissions?.apply {
            if (isNotEmpty()) {
                forEach { requestPermissions(arrayOf(permissions[it]), 0) }
            }
            return
        }
        requestPermissions(permissions, 87)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            87 -> if (grantResults.isNotEmpty()) {
                missingPermissions = grantResults.indices.filter {
                    grantResults[it] != PackageManager.PERMISSION_GRANTED
                }.toMutableList()
            }
        }
    }

    private inner class ConnectionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                when (intent.action) {
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> if (!oreo.isConnected) {
                        wifiManager?.apply {
                            wifiScanResults = scanResults
                        }
                        connectTello()
                    }
                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                        val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                        val wifiConnected = info.isConnected
                        val telloConnected = info.extraInfo.contains("TELLO")
                        if (wifiConnected && telloConnected) {
                            updateWifiConnection(WifiIconView.WIFI_STATE_CONNECTED)
                        } else if (info.isConnectedOrConnecting) {
                            updateWifiConnection(WifiIconView.WIFI_STATE_PENDING)
                        } else {
                            updateWifiConnection(WifiIconView.WIFI_STATE_DISCONNECTED)
                        }
                    }
                }
            }
        }
    }
}
