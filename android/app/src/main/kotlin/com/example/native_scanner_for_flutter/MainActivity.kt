package com.example.native_scanner_for_flutter

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Symbology
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.ListPreference
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
            mScanManager!!.stopDecode()
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
     * mBarcodeMap helper
     */
    private fun initBarcodeParameters() {
        mBarcodeMap.clear()
        var holder = BarcodeHolder()
        // Symbology.AZTEC
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.AZTEC_ENABLE)
        holder.mParaKeys = arrayOf("AZTEC_ENABLE")
        mBarcodeMap[Symbology.AZTEC.toString() + ""] = holder
        // Symbology.CHINESE25
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.C25_ENABLE)
        holder.mParaKeys = arrayOf("C25_ENABLE")
        mBarcodeMap[Symbology.CHINESE25.toString() + ""] = holder
        // Symbology.CODABAR
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeNOTIS = CheckBoxPreference(this)
        holder.mBarcodeCLSI = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODABAR_ENABLE, PropertyID.CODABAR_LENGTH1, PropertyID.CODABAR_LENGTH2, PropertyID.CODABAR_NOTIS, PropertyID.CODABAR_CLSI)
        holder.mParaKeys = arrayOf("CODABAR_ENABLE", "CODABAR_LENGTH1", "CODABAR_LENGTH2", "CODABAR_NOTIS", "CODABAR_CLSI")
        mBarcodeMap[Symbology.CODABAR.toString() + ""] = holder
        // Symbology.CODE11
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeCheckDigit = ListPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE11_ENABLE, PropertyID.CODE11_LENGTH1, PropertyID.CODE11_LENGTH2, PropertyID.CODE11_SEND_CHECK)
        holder.mParaKeys = arrayOf("CODE11_ENABLE", "CODE11_LENGTH1", "CODE11_LENGTH2", "CODE11_SEND_CHECK")
        mBarcodeMap[Symbology.CODE11.toString() + ""] = holder
        // Symbology.CODE32
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE32_ENABLE)
        holder.mParaKeys = arrayOf("CODE32_ENABLE")
        mBarcodeMap[Symbology.CODE32.toString() + ""] = holder
        // Symbology.CODE39
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeChecksum = CheckBoxPreference(this)
        holder.mBarcodeSendCheck = CheckBoxPreference(this)
        holder.mBarcodeFullASCII = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE39_ENABLE, PropertyID.CODE39_LENGTH1, PropertyID.CODE39_LENGTH2, PropertyID.CODE39_ENABLE_CHECK, PropertyID.CODE39_SEND_CHECK, PropertyID.CODE39_FULL_ASCII)
        holder.mParaKeys = arrayOf("CODE39_ENABLE", "CODE39_LENGTH1", "CODE39_LENGTH2", "CODE39_ENABLE_CHECK", "CODE39_SEND_CHECK", "CODE39_FULL_ASCII")
        mBarcodeMap[Symbology.CODE39.toString() + ""] = holder
        // Symbology.CODE93
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE93_ENABLE, PropertyID.CODE93_LENGTH1, PropertyID.CODE93_LENGTH2)
        holder.mParaKeys = arrayOf("CODE93_ENABLE", "CODE93_LENGTH1", "CODE93_LENGTH2")
        mBarcodeMap[Symbology.CODE93.toString() + ""] = holder
        // Symbology.CODE128
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeISBT = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE128_ENABLE, PropertyID.CODE128_LENGTH1, PropertyID.CODE128_LENGTH2, PropertyID.CODE128_CHECK_ISBT_TABLE)
        holder.mParaKeys = arrayOf("CODE128_ENABLE", "CODE128_LENGTH1", "CODE128_LENGTH2", "CODE128_CHECK_ISBT_TABLE")
        mBarcodeMap[Symbology.CODE128.toString() + ""] = holder
        // Symbology.COMPOSITE_CC_AB
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.COMPOSITE_CC_AB_ENABLE)
        holder.mParaKeys = arrayOf("COMPOSITE_CC_AB_ENABLE")
        mBarcodeMap[Symbology.COMPOSITE_CC_AB.toString() + ""] = holder
        // Symbology.COMPOSITE_CC_C
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.COMPOSITE_CC_C_ENABLE)
        holder.mParaKeys = arrayOf("COMPOSITE_CC_C_ENABLE")
        mBarcodeMap[Symbology.COMPOSITE_CC_C.toString() + ""] = holder
        // Symbology.DATAMATRIX
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.DATAMATRIX_ENABLE)
        holder.mParaKeys = arrayOf("DATAMATRIX_ENABLE")
        mBarcodeMap[Symbology.DATAMATRIX.toString() + ""] = holder
        // Symbology.DISCRETE25
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.D25_ENABLE)
        holder.mParaKeys = arrayOf("D25_ENABLE")
        mBarcodeMap[Symbology.DISCRETE25.toString() + ""] = holder
        // Symbology.EAN8
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.EAN8_ENABLE)
        holder.mParaKeys = arrayOf("EAN8_ENABLE")
        mBarcodeMap[Symbology.EAN8.toString() + ""] = holder
        // Symbology.EAN13
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeBookland = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.EAN13_ENABLE, PropertyID.EAN13_BOOKLANDEAN)
        holder.mParaKeys = arrayOf("EAN13_ENABLE", "EAN13_BOOKLANDEAN")
        mBarcodeMap[Symbology.EAN13.toString() + ""] = holder
        // Symbology.GS1_14
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.GS1_14_ENABLE)
        holder.mParaKeys = arrayOf("GS1_14_ENABLE")
        mBarcodeMap[Symbology.GS1_14.toString() + ""] = holder
        // Symbology.GS1_128
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.CODE128_GS1_ENABLE)
        holder.mParaKeys = arrayOf("CODE128_GS1_ENABLE")
        mBarcodeMap[Symbology.GS1_128.toString() + ""] = holder
        // Symbology.GS1_EXP
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.GS1_EXP_ENABLE, PropertyID.GS1_EXP_LENGTH1, PropertyID.GS1_EXP_LENGTH2)
        holder.mParaKeys = arrayOf("GS1_EXP_ENABLE", "GS1_EXP_LENGTH1", "GS1_EXP_LENGTH2")
        mBarcodeMap[Symbology.GS1_EXP.toString() + ""] = holder
        // Symbology.GS1_LIMIT
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.GS1_LIMIT_ENABLE)
        holder.mParaKeys = arrayOf("GS1_LIMIT_ENABLE")
        mBarcodeMap[Symbology.GS1_LIMIT.toString() + ""] = holder
        // Symbology.INTERLEAVED25
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeChecksum = CheckBoxPreference(this)
        holder.mBarcodeSendCheck = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.I25_ENABLE, PropertyID.I25_LENGTH1, PropertyID.I25_LENGTH2, PropertyID.I25_ENABLE_CHECK, PropertyID.I25_SEND_CHECK)
        holder.mParaKeys = arrayOf("I25_ENABLE", "I25_LENGTH1", "I25_LENGTH2", "I25_ENABLE_CHECK", "I25_SEND_CHECK")
        mBarcodeMap[Symbology.INTERLEAVED25.toString() + ""] = holder
        // Symbology.MATRIX25
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.M25_ENABLE)
        holder.mParaKeys = arrayOf("M25_ENABLE")
        mBarcodeMap[Symbology.MATRIX25.toString() + ""] = holder
        // Symbology.MAXICODE
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.MAXICODE_ENABLE)
        holder.mParaKeys = arrayOf("MAXICODE_ENABLE")
        mBarcodeMap[Symbology.MAXICODE.toString() + ""] = holder
        // Symbology.MICROPDF417
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.MICROPDF417_ENABLE)
        holder.mParaKeys = arrayOf("MICROPDF417_ENABLE")
        mBarcodeMap[Symbology.MICROPDF417.toString() + ""] = holder
        // Symbology.MSI
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeLength1 = EditTextPreference(this)
        holder.mBarcodeLength2 = EditTextPreference(this)
        holder.mBarcodeSecondChecksum = CheckBoxPreference(this)
        holder.mBarcodeSendCheck = CheckBoxPreference(this)
        holder.mBarcodeSecondChecksumMode = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.MSI_ENABLE, PropertyID.MSI_LENGTH1, PropertyID.MSI_LENGTH2, PropertyID.MSI_REQUIRE_2_CHECK, PropertyID.MSI_SEND_CHECK, PropertyID.MSI_CHECK_2_MOD_11)
        holder.mParaKeys = arrayOf("MSI_ENABLE", "MSI_LENGTH1", "MSI_LENGTH2", "MSI_REQUIRE_2_CHECK", "MSI_SEND_CHECK", "MSI_CHECK_2_MOD_11")
        mBarcodeMap[Symbology.MSI.toString() + ""] = holder
        // Symbology.PDF417
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.PDF417_ENABLE)
        holder.mParaKeys = arrayOf("PDF417_ENABLE")
        mBarcodeMap[Symbology.PDF417.toString() + ""] = holder
        // Symbology.QRCODE
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.QRCODE_ENABLE)
        holder.mParaKeys = arrayOf("QRCODE_ENABLE")
        mBarcodeMap[Symbology.QRCODE.toString() + ""] = holder
        // Symbology.TRIOPTIC
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.TRIOPTIC_ENABLE)
        holder.mParaKeys = arrayOf("TRIOPTIC_ENABLE")
        mBarcodeMap[Symbology.TRIOPTIC.toString() + ""] = holder
        // Symbology.UPCA
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeChecksum = CheckBoxPreference(this)
        holder.mBarcodeSystemDigit = CheckBoxPreference(this)
        holder.mBarcodeConvertEAN13 = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.UPCA_ENABLE, PropertyID.UPCA_SEND_CHECK, PropertyID.UPCA_SEND_SYS, PropertyID.UPCA_TO_EAN13)
        holder.mParaKeys = arrayOf("UPCA_ENABLE", "UPCA_SEND_CHECK", "UPCA_SEND_SYS", "UPCA_TO_EAN13")
        mBarcodeMap[Symbology.UPCA.toString() + ""] = holder
        // Symbology.UPCE
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mBarcodeChecksum = CheckBoxPreference(this)
        holder.mBarcodeSystemDigit = CheckBoxPreference(this)
        holder.mBarcodeConvertUPCA = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.UPCE_ENABLE, PropertyID.UPCE_SEND_CHECK, PropertyID.UPCE_SEND_SYS, PropertyID.UPCE_TO_UPCA)
        holder.mParaKeys = arrayOf("UPCE_ENABLE", "UPCE_SEND_CHECK", "UPCE_SEND_SYS", "UPCE_TO_UPCA")
        mBarcodeMap[Symbology.UPCE.toString() + ""] = holder
        // Symbology.UPCE1
        holder = BarcodeHolder()
        holder.mBarcodeEnable = CheckBoxPreference(this)
        holder.mParaIds = intArrayOf(PropertyID.UPCE1_ENABLE)
        holder.mParaKeys = arrayOf("UPCE1_ENABLE")
        mBarcodeMap[Symbology.UPCE1.toString() + ""] = holder
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
