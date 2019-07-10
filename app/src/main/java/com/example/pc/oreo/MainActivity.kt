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
import com.example.pc.oreo.StatusIconView.StatusIconType
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), Oreo.OreoStatusChangeListener, WifiConnector.OreoWifiDataChangeListener, StatusIconView.IconClickListener, ControlView.ControlViewListener {
    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET)
    private var missingPermissions: MutableList<Int>? = null
    private var wifiManager: WifiManager? = null
    private var wifiScanResults: List<ScanResult>? = null
    private val connectionReceiver = ConnectionReceiver()
    private val oreo = Oreo()
    private var driveStatusAdapter: DriveStatusAdapter? = null
    private lateinit var driveFields: Array<String>
    private lateinit var driveIcons: TypedArray
    private val driveValues = DoubleArray(10)
    private val currentFrame = ByteArray(20480)
    private var chunkOffset = 0
    private var streamPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        oreo.oreoStatusChangeListener = this
        oreo.wifiConnector.setWifiDataChangeListener(this)
        requestAllPermissions()

        driveFields = resources.getStringArray(R.array.drive_status_fields)
        driveIcons = resources.obtainTypedArray(R.array.drive_status_icons)
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
    }

    private fun updateDriveIcons(changes: BooleanArray?) {
        runOnUiThread {
            if (changes != null) {
                for (i in changes.indices) {
                    if (changes[i]) {
                        driveStatusAdapter!!.notifyItemChanged(i)
                    }
                }
            }
            driveStatusAdapter!!.notifyItemRangeChanged(StatusIconType.WIFI.value, 2)
        }
    }

    private fun hideStatusBar() {
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        window.decorView.systemUiVisibility = uiOptions
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableWifi() {
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager != null && !wifiManager!!.isWifiEnabled) {
            wifiManager!!.isWifiEnabled = true
        }
        if (wifiManager != null) {
            wifiManager!!.startScan()
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
                driveValues[StatusIconType.WIFI.value] = WifiIconView.WIFI_STATE_CONNECTED.toDouble()
            }
            WifiIconView.WIFI_STATE_DISCONNECTED -> {
                oreo.setWifiConnection(false)
                driveValues[StatusIconType.WIFI.value] = WifiIconView.WIFI_STATE_DISCONNECTED.toDouble()
            }
            WifiIconView.WIFI_STATE_PENDING -> {
                oreo.setWifiConnection(false)
                driveValues[StatusIconType.WIFI.value] = WifiIconView.WIFI_STATE_PENDING.toDouble()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        hideStatusBar()
        streamPaused = false
    }

    override fun onPause() {
        streamPaused = true
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

    override fun onConnectionChanged() {
        updateDriveIcons(null)
    }

    override fun onBatteryStateUpdate(voltage: Double, amperage: Int) {
        runOnUiThread {
            driveValues[StatusIconType.BATTERY.value] = voltage
            driveStatusAdapter!!.notifyItemChanged(2)
        }
    }

    override fun onMovementUpdate(velocity: Double) {
        runOnUiThread {
            driveValues[0] = velocity
            driveStatusAdapter!!.notifyItemRangeChanged(0, 1)
        }
    }

    override fun onWifiCommandReceived(command: WifiCommand) {
        when (command.type) {
            WifiCommand.WifiCommandCode.APPROVE_CONNECTION -> updateWifiConnection(WifiIconView.WIFI_STATE_CONNECTED)
            WifiCommand.WifiCommandCode.FRAME_DATA -> {
            }
            WifiCommand.WifiCommandCode.DRIVE_DATA -> oreo.updateDriveData(command.driveDataPayload)
        }
    }

    override fun onVideoDataReceived(videoBuffer: ByteArray, size: Int) {
        if (videoBuffer[2].toInt() == 0 && videoBuffer[3].toInt() == 0 && videoBuffer[4].toInt() == 0 && videoBuffer[5].toInt() == 1) { //if nal
            if (chunkOffset > 0) {
                if (!streamPaused) {
                    Log.e("offset", chunkOffset.toString() + "")
//                    decoderView!!.decode(Arrays.copyOfRange(currentFrame, 0, chunkOffset))
                }
                chunkOffset = 0
            }
        }
        System.arraycopy(videoBuffer, 2, currentFrame, chunkOffset, size)
        chunkOffset += size
    }

    override fun onIconClick(type: StatusIconType) {
        when (type) {
            StatusIconType.WIFI -> enableWifi()
            StatusIconType.BATTERY -> {
            }
        }
    }

    override fun onAccelerate(power: Float) {

    }

    override fun onSteer(angle: Float) {

    }

    private fun requestAllPermissions() {
        if (missingPermissions == null) {
            requestPermissions(permissions, 87)
        } else if (missingPermissions!!.size > 0) {
            for (i in missingPermissions!!) {
                requestPermissions(arrayOf(permissions[missingPermissions!![i]]), 0)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            87 -> if (grantResults.isNotEmpty()) {
                missingPermissions = ArrayList()
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        missingPermissions!!.add(i)
                    }
                }
            }
        }
    }

    private fun dispatchControls(command: ControlCommand) {

    }

    private inner class ConnectionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                when (intent.action) {
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> if (!oreo.isConnected && wifiManager != null) {
                        wifiScanResults = wifiManager!!.scanResults
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
