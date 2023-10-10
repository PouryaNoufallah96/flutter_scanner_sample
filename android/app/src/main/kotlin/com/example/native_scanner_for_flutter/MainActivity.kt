package com.example.native_scanner_for_flutter

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.device.scanner.configuration.Constants
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Symbology
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.util.Log
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    private val CHANNEL = "com.example.native_scanner_for_flutter/scanner"
    private var mScanManager: ScanManager? = null
    private var result: MethodChannel.Result? = null
    private val mBarcodeMap: MutableMap<String, BarcodeHolder> = HashMap()
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_SHOW_SCAN_RESULT -> {
                    val scanResult = msg.obj as String
                    printScanResult(scanResult)
                }

                MSG_SHOW_SCAN_IMAGE -> {
                    val bitmap = msg.obj as Bitmap
                }
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            when (call.method) {
                "startScan" -> {
                    this.result = result
                    initScan()
                    startDecode();
                }
                "stopScan" -> {
                    registerReceiverInit(false)
                    stopDecode();
                    result.success(null);

                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun startDecode() {
        try {
            if (!mScanEnable) {
                LogI("startDecode ignore, Scan enable:" + mScanEnable)
                return
            }
            val lockState = getlockTriggerState()
            if (lockState) {
                LogI("startDecode ignore, Scan lockTrigger state:$lockState")
                return
            }
            if (mScanManager != null) {
                mScanManager!!.startDecode()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this,"This device unsupported scanner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopDecode() {
        if (!mScanEnable) {
            LogI("stopDecode ignore, Scan enable:" + mScanEnable)
            return
        }
        if (mScanManager != null) {
            mScanManager!!.stopDecode()
        }
    }
    private fun getlockTriggerState(): Boolean {
        return mScanManager!!.triggerLockState
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            LogI("onReceive , action:$action")
            // Get Scan Image . Make sure to make a request before getting a scanned image
            if (ACTION_CAPTURE_IMAGE == action) {
                val imagedata = intent.getByteArrayExtra(DECODE_CAPTURE_IMAGE_KEY)
                if (imagedata != null && imagedata.size > 0) {
                    val bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.size)
                    val msg = mHandler.obtainMessage(MSG_SHOW_SCAN_IMAGE)
                    msg.obj = bitmap
                    mHandler.sendMessage(msg)
                } else {
                    LogI("onReceive , ignore imagedata:$imagedata")
                }
            } else {
                // Get scan results, including string and byte data etc.
                val barcode = intent.getByteArrayExtra(DECODE_DATA_TAG)
                val barcodeLen = intent.getIntExtra(BARCODE_LENGTH_TAG, 0)
                val temp = intent.getByteExtra(BARCODE_TYPE_TAG, 0.toByte())
                val barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG)
                if (barcodeStr != null) {
                    result?.success(barcodeStr)  // Send the scanned barcode back to Flutter
                } else {
                    result?.error("SCAN_FAILED", "Failed to scan barcode", null)
                }
                if (mScanCaptureImageShow) {
                    // Request images of this scan
                    context.sendBroadcast(Intent(ACTION_DECODE_IMAGE_REQUEST))
                }
                LogI("barcode type:$temp")

                var scanResult = String(barcode!!, 0, barcodeLen)
                // print scan results.
                scanResult = """ length：$barcodeLen
barcode：$scanResult
bytesToHexString：${bytesToHexString(barcode)}
barcodeStr:$barcodeStr"""
                val msg = mHandler.obtainMessage(MSG_SHOW_SCAN_RESULT)
                msg.obj = scanResult
                mHandler.sendMessage(msg)
            }
        }
    }
    private fun initScan() {
        try {
            mScanManager = ScanManager()
            var powerOn = mScanManager!!.scannerState
            if (!powerOn) {
                powerOn = mScanManager!!.openScanner()
                if (!powerOn) {
                    showUnsupportedScanner();
                }
            }
            initBarcodeParameters()
            registerReceiverInit(true)
        } catch (e: Exception) {
            showUnsupportedScanner();
        }
    }

    private fun showUnsupportedScanner(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Scanner cannot be turned on!")
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        val mAlertDialog = builder.create()
        mAlertDialog.show()
    }

    private fun registerReceiverInit(register: Boolean) {
        if (register && mScanManager != null) {
            val filter = IntentFilter()
            val idbuf = intArrayOf(PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG)
            val value_buf = mScanManager!!.getParameterString(idbuf)
            if (value_buf != null && value_buf[0] != null && value_buf[0] != "") {
                filter.addAction(value_buf[0])
            } else {
                filter.addAction(ACTION_DECODE)
            }
            filter.addAction(ACTION_CAPTURE_IMAGE)
            registerReceiver(mReceiver, filter)
        } else if (mScanManager != null) {
            stopDecode()
            // mScanManager!!.stopDecode()
            unregisterReceiver(mReceiver)
        }
    }
    private fun printScanResult(msg: String?) {
        if (msg == null ) {
            LogI("printScanResult , ignore to show msg:$msg")
            return
        }
    }
    private fun LogI(msg: String) {
        Log.i(TAG, msg)
    }

    /**
     * SettingsBarcodeList helper
     */
    class SettingsBarcodeList : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        private var root: PreferenceScreen? = null
        private var mBarcode: Preference? = null
        private var mScanDemo: MainActivity? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            root = this.preferenceScreen
            if (root != null) {
                root!!.removeAll()
            }
            Log.d(TAG, "onCreate , ,root:$root") //Symbology s = BARCODE_SUPPORT_SYMBOLOGY[9];
            initSymbology()
        }

        /**
         * Use Symbology enumeration
         */
        private fun initSymbology() {
            if (mScanDemo != null) {
                val length = BARCODE_SUPPORT_SYMBOLOGY.size
                Log.d(TAG, "initSymbology  length : $length")
                for (i in 0 until length) {
                    if (mScanDemo != null && mScanDemo!!.isSymbologySupported(BARCODE_SUPPORT_SYMBOLOGY[i])) {
                        mBarcode = Preference(mScanDemo)
                        mBarcode!!.title = BARCODE_SUPPORT_SYMBOLOGY[i].toString() + ""
                        mBarcode!!.key = BARCODE_SUPPORT_SYMBOLOGY[i].toString() + ""
                        this.preferenceScreen.addPreference(mBarcode)
                    } else {
                        Log.d(TAG, "initSymbology , Not Support Barcode " + BARCODE_SUPPORT_SYMBOLOGY[i])
                    }
                }
            }
        }

        fun setScanManagerDemo(demo: MainActivity?) {
            mScanDemo = demo
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
            Log.d(TAG, "onPreferenceTreeClick preference:$preference")
            val key = preference.key
            if (key != null) {
                mScanDemo!!.updateScanSettingsBarcode(key)
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference)
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return false
        }
    }

    private fun isSymbologySupported(symbology: Symbology): Boolean {
        var isSupport = false
        if (mScanManager != null) {
            isSupport = mScanManager!!.isSymbologySupported(symbology)
        }
        return isSupport
    }

    /**
     * mBarcodeMap helper
     */
    private fun initBarcodeParameters() {
        //params
    }

    /**
     * BarcodeHolder helper
     */
    internal class BarcodeHolder {
        var mBarcodeEnable: CheckBoxPreference? = null
        var mBarcodeLength1: EditTextPreference? = null
        var mBarcodeLength2: EditTextPreference? = null
        var mBarcodeNOTIS: CheckBoxPreference? = null
        var mBarcodeCLSI: CheckBoxPreference? = null
        var mBarcodeISBT: CheckBoxPreference? = null
        var mBarcodeChecksum: CheckBoxPreference? = null
        var mBarcodeSendCheck: CheckBoxPreference? = null
        var mBarcodeFullASCII: CheckBoxPreference? = null
        var mBarcodeCheckDigit: ListPreference? = null
        var mBarcodeBookland: CheckBoxPreference? = null
        var mBarcodeSecondChecksum: CheckBoxPreference? = null
        var mBarcodeSecondChecksumMode: CheckBoxPreference? = null
        var mBarcodePostalCode: ListPreference? = null
        var mBarcodeSystemDigit: CheckBoxPreference? = null
        var mBarcodeConvertEAN13: CheckBoxPreference? = null
        var mBarcodeConvertUPCA: CheckBoxPreference? = null
        var mBarcodeEanble25DigitExtensions: CheckBoxPreference? = null
        var mBarcodeDPM: CheckBoxPreference? = null
        var mParaIds: IntArray? = null
        var mParaKeys: Array<String>? = null
    }
    /**
     * Use of android.device.scanner.configuration.Constants.Symbology Class
     */
    private val BARCODE_SYMBOLOGY = intArrayOf(
        Constants.Symbology.AZTEC, Constants.Symbology.CHINESE25, Constants.Symbology.CODABAR, Constants.Symbology.CODE11, Constants.Symbology.CODE32, Constants.Symbology.CODE39, Constants.Symbology.CODE93, Constants.Symbology.CODE128, Constants.Symbology.COMPOSITE_CC_AB, Constants.Symbology.COMPOSITE_CC_C, Constants.Symbology.COMPOSITE_TLC39, Constants.Symbology.DATAMATRIX, Constants.Symbology.DISCRETE25, Constants.Symbology.EAN8, Constants.Symbology.EAN13, Constants.Symbology.GS1_14, Constants.Symbology.GS1_128, Constants.Symbology.GS1_EXP, Constants.Symbology.GS1_LIMIT, Constants.Symbology.HANXIN, Constants.Symbology.INTERLEAVED25, Constants.Symbology.MATRIX25, Constants.Symbology.MAXICODE, Constants.Symbology.MICROPDF417, Constants.Symbology.MSI, Constants.Symbology.PDF417, Constants.Symbology.POSTAL_4STATE, Constants.Symbology.POSTAL_AUSTRALIAN, Constants.Symbology.POSTAL_JAPAN, Constants.Symbology.POSTAL_KIX, Constants.Symbology.POSTAL_PLANET, Constants.Symbology.POSTAL_POSTNET, Constants.Symbology.POSTAL_ROYALMAIL, Constants.Symbology.POSTAL_UPUFICS, Constants.Symbology.QRCODE, Constants.Symbology.TRIOPTIC, Constants.Symbology.UPCA, Constants.Symbology.UPCE, Constants.Symbology.UPCE1, Constants.Symbology.NONE, Constants.Symbology.RESERVED_6, Constants.Symbology.RESERVED_13, Constants.Symbology.RESERVED_15, Constants.Symbology.RESERVED_16, Constants.Symbology.RESERVED_20, Constants.Symbology.RESERVED_21, Constants.Symbology.RESERVED_27, Constants.Symbology.RESERVED_28, Constants.Symbology.RESERVED_30, Constants.Symbology.RESERVED_33
    )
    companion object {
        private const val TAG = "ScanManagerDemo"
        private const val DEBUG = true
        private const val ACTION_DECODE = ScanManager.ACTION_DECODE // default action
        private const val ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image"
        private const val ACTION_CAPTURE_IMAGE = "scanner_capture_image_result"
        private const val BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG
        private const val BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG
        private const val BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG
        private const val DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG
        private const val DECODE_ENABLE = "decode_enable"
        private const val DECODE_TRIGGER_MODE = "decode_trigger_mode"
        private const val DECODE_TRIGGER_MODE_HOST = "HOST"
        private const val DECODE_TRIGGER_MODE_CONTINUOUS = "CONTINUOUS"
        private const val DECODE_TRIGGER_MODE_PAUSE = "PAUSE"
        private var DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST
        private const val DECODE_OUTPUT_MODE_INTENT = 0
        private const val DECODE_OUTPUT_MODE_FOCUS = 1
        private var DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS
        private const val DECODE_OUTPUT_MODE = "decode_output_mode"
        private const val DECODE_CAPTURE_IMAGE_KEY = "bitmapBytes"
        private const val DECODE_CAPTURE_IMAGE_SHOW = "scan_capture_image"
        private var mScanEnable = true
        private var mScanSettingsView = false
        private var mScanCaptureImageShow = false
        private var mScanBarcodeSettingsMenuBarcodeList = false
        private var mScanBarcodeSettingsMenuBarcode = false
        private val mBarcodeMap: MutableMap<String, BarcodeHolder> = HashMap()
        private const val MSG_SHOW_SCAN_RESULT = 1
        private const val MSG_SHOW_SCAN_IMAGE = 2
        private val SCAN_KEYCODE = intArrayOf(520, 521, 522, 523)
        fun bytesToHexString(src: ByteArray?): String? {
            val stringBuilder = StringBuilder("")
            if (src == null || src.size <= 0) {
                return null
            }
            for (i in src.indices) {
                val v = src[i].toInt() and 0xFF
                val hv = Integer.toHexString(v)
                if (hv.length < 2) {
                    stringBuilder.append(0)
                }
                stringBuilder.append(hv)
            }
            return stringBuilder.toString()
        }

        /**
         * Use of android.device.scanner.configuration.Symbology enums
         */
        private val BARCODE_SUPPORT_SYMBOLOGY = arrayOf(
            Symbology.AZTEC, Symbology.CHINESE25, Symbology.CODABAR, Symbology.CODE11, Symbology.CODE32, Symbology.CODE39, Symbology.CODE93, Symbology.CODE128, Symbology.COMPOSITE_CC_AB, Symbology.COMPOSITE_CC_C, Symbology.DATAMATRIX, Symbology.DISCRETE25, Symbology.EAN8, Symbology.EAN13, Symbology.GS1_14, Symbology.GS1_128, Symbology.GS1_EXP, Symbology.GS1_LIMIT, Symbology.INTERLEAVED25, Symbology.MATRIX25, Symbology.MAXICODE, Symbology.MICROPDF417, Symbology.MSI, Symbology.PDF417, Symbology.POSTAL_4STATE, Symbology.POSTAL_AUSTRALIAN, Symbology.POSTAL_JAPAN, Symbology.POSTAL_KIX, Symbology.POSTAL_PLANET, Symbology.POSTAL_POSTNET, Symbology.POSTAL_ROYALMAIL, Symbology.POSTAL_UPUFICS, Symbology.QRCODE, Symbology.TRIOPTIC, Symbology.UPCA, Symbology.UPCE, Symbology.UPCE1, Symbology.NONE
        )
    }

}
