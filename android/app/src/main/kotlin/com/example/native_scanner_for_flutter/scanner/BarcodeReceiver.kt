package com.mortezaqn.samplescannerurovo.scanner

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Symbology
import androidx.lifecycle.*
import timber.log.Timber

class BarcodeReceiver(
    private val context: Context,
    lifecycle: Lifecycle,
    private val callback: (String?) -> Unit,
) : DefaultLifecycleObserver {

    private val _filter = IntentFilter()
    private val _scanManager: ScanManager = ScanManager()

    init {
        lifecycle.addObserver(this)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, nullAbleIntent: Intent?) {
            nullAbleIntent?.let { intent ->
                with(intent) {
                    // Get scan results, including string and byte data etc.
                    // val barcode = getByteArrayExtra(ScanManager.DECODE_DATA_TAG)
                    // val barcodeLen = getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0)
                    // val barcodeType = getByteExtra(ScanManager.BARCODE_TYPE_TAG, 0.toByte())
                    val barcodeStr = getStringExtra(ScanManager.BARCODE_STRING_TAG)

                    // if (barcode != null && barcode.isNotEmpty()) {
                    // val scanResult = String(barcode, 0, barcodeLen)
                    // log
                    // printDataScan(action, barcode, barcodeLen, scanResult, barcodeStr, barcodeType)
                    // }

                    // Result
                    //                    loggerBarcodeReceiver(barcodeStr)
                    callback.invoke(barcodeStr)

                    if (intent.hasExtra(SYMBOLOGY_NAME_TAG)) {
                        val symName = intent.getStringExtra(SYMBOLOGY_NAME_TAG)
                        Timber.v(">>> SYMBOLOGY_NAME_TAG - symName = $symName")
                    }
                    if (intent.hasExtra(SYMBOLOGY_ID_TAG)) {
                        val symId = intent.getIntExtra(SYMBOLOGY_ID_TAG, Symbology.NONE.toInt())
                        Timber.v(">>> SYMBOLOGY_ID_TAG - symId = $symId")
                    } else {
                        val type = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, 0.toByte())
                        val symId: Int = OEMSymbologyId.getHSMSymbologyId(type.toInt())
                        Timber.v(">>> BARCODE_TYPE_TAG - symId = $symId")
                    }
                    if (intent.hasExtra(DECODE_TIME_TAG)) {
                        val time = intent.getLongExtra(DECODE_TIME_TAG, 0L)
                        Timber.v(">>> DECODE_TIME_TAG - time = $time")
                    }

                    try {
                        val resultBundle = intent.extras
                        val keys = resultBundle!!.keySet()
                        for (key in keys) {
                            val obj = resultBundle[key]
                            if (obj is String) {
                                Timber.v(">>> key: $key value: $obj")
                            } else if (obj is Array<*> && obj.isArrayOf<String>()) {
                                for (code in obj) {
                                    Timber.v(">>> key: $key value: $code")
                                }
                            } else if (obj is ByteArray) {
                                Timber.v(">>> key: $key - value: $obj")
                            } else {
                                Timber.v(">>> key: $key object: $obj")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(">>> Exception -> $e")
                    }
                }
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        registerBarcodeReceiver()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        unregisterBarcodeReceiver()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerBarcodeReceiver() {
        Timber.v("++ register BarcodeReceiver")
        context.initBarcodeParams(Symbology.DATAMATRIX)

        _scanManager.openScanner()

        val bufferValues: Array<String> = _scanManager.getParameterString(bufferID)
        actionBufferValue = _scanManager.getParameterString(bufferID)
        idMode = _scanManager.getParameterInts(idModeBuf)
        if (bufferValues.isNotEmpty().and(bufferValues[0] == "")) {
            _filter.addAction(bufferValues[0])
        } else {
            _filter.addAction(ScanManager.ACTION_DECODE)
        }
        context.registerReceiver(receiver, _filter)
    }

    private fun unregisterBarcodeReceiver() {
        Timber.v("-- unregister BarcodeReceiver")
        _scanManager.stopDecode()
        context.unregisterReceiver(receiver)
    }

    companion object {

        private const val SCAN_ACTION = ScanManager.ACTION_DECODE // default action

        /**
         * String contains the label type of the bar code
         */
        const val BARCODE_TYPE_TAG = "barcodeType"
        const val SYMBOLOGY_ID_TAG = "symbologyId"
        const val SYMBOLOGY_NAME_TAG = "symName"
        const val DECODE_TIME_TAG = "decodeTime"

        /**
         * String contains the label length of the bar code
         */
        const val BARCODE_LENGTH_TAG = "length"

        /**
         * String contains the output data as a byte array. In the case of concatenated bar codes, the decode data is
         * concatenated and sent out as a single array
         */
        const val DECODE_DATA_TAG = "barcode"
        private val bufferID = intArrayOf(
            PropertyID.WEDGE_INTENT_ACTION_NAME,
            PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
            PropertyID.WEDGE_INTENT_DECODE_DATA_TAG,
        )
        var idBuf = intArrayOf(
            PropertyID.WEDGE_INTENT_ACTION_NAME,
            PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
            PropertyID.WEDGE_INTENT_DECODE_DATA_TAG,
        )
        var idModeBuf = intArrayOf(
            PropertyID.WEDGE_KEYBOARD_ENABLE,
            PropertyID.TRIGGERING_MODES,
            PropertyID.LABEL_APPEND_ENTER,
            PropertyID.DEC_Multiple_Decode_INTERVAL,
        )
        var actionBufferValue = arrayOf(
            ScanManager.ACTION_DECODE,
            ScanManager.BARCODE_STRING_TAG,
            ScanManager.DECODE_DATA_TAG,
        )
        var idMode: IntArray = intArrayOf()

        //        mScanManager.openScanner();
        //        actionValueBuf = mScanManager.getParameterString(idBuf);
        //        idMode = mScanManager.getParameterInts(idModeBuf);

        fun bytesToHexString(src: ByteArray?): String? {
            val stringBuilder = StringBuilder("")
            if (src == null || src.isEmpty()) {
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
    }
}