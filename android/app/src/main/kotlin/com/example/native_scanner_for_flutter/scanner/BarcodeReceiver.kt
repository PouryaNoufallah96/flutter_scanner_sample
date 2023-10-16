package com.example.native_scanner_for_flutter.scanner

import android.annotation.SuppressLint
import android.content.*
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Symbology
import android.util.Log
import androidx.lifecycle.*

class BarcodeReceiver(
    private val context: Context,
    lifecycle: Lifecycle,
    private val callback: (String?) -> Unit,
) : DefaultLifecycleObserver, MReceiver.ISocketMessageReceiver {

    private val _filter = IntentFilter()
    private val _scanManager: ScanManager = ScanManager()
    private var receiver = MReceiver()
    private var iSocketMessageReceiver: MReceiver.ISocketMessageReceiver? = null


    init {
        lifecycle.addObserver(this)

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
        context.initBarcodeParams(Symbology.DATAMATRIX)

        _scanManager.openScanner()

        iSocketMessageReceiver = this;
        receiver.registerCallback(iSocketMessageReceiver);

        val bufferValues: Array<String> = _scanManager.getParameterString(bufferID)
        actionBufferValue = _scanManager.getParameterString(bufferID)
        idMode = _scanManager.getParameterInts(idModeBuf)
        if (bufferValues.isNotEmpty().and(bufferValues[0] == "")) {
            _filter.addAction(bufferValues[0])
        } else {
            _filter.addAction(ScanManager.ACTION_DECODE)
        }
        var result = context.registerReceiver(receiver, _filter)
    }

    fun unregisterBarcodeReceiver() {
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

    override fun sendSocketMessage(socketMessage: String?) {
        callback(socketMessage)
    }

    fun startDecode() {
        _scanManager.startDecode()
    }

    fun getScannerState():Boolean{
        return try{
            _scanManager.scannerState
        }catch (e:Exception) {
            false
        }

    }
    fun stopDecode(){
        _scanManager.stopDecode()
    }
}