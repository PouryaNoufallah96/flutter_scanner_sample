package com.example.native_scanner_for_flutter.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.device.ScanManager
import android.device.scanner.configuration.Symbology
import android.util.Log


class MReceiver : BroadcastReceiver() {

    interface ISocketMessageReceiver {
        fun sendSocketMessage(socketMessage: String?)
    }

    //Also declare the interface in your BroadcastReceiver as static
    private var iSocketMessageReceiver: ISocketMessageReceiver? = null
    public fun registerCallback(iSocketMessageReceiver: ISocketMessageReceiver?) {
        this.iSocketMessageReceiver = iSocketMessageReceiver
    }
    override fun onReceive(context: Context?, nullAbleIntent: Intent?) {
        nullAbleIntent?.let { intent ->
            with(intent) {
                // Get scan results, including string and byte data etc.
                // val barcode = getByteArrayExtra(ScanManager.DECODE_DATA_TAG)
                // val barcodeLen = getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0)
                // val barcodeType = getByteExtra(ScanManager.BARCODE_TYPE_TAG, 0.toByte())
                val barcodeStr = getStringExtra(ScanManager.BARCODE_STRING_TAG)
                iSocketMessageReceiver!!.sendSocketMessage(barcodeStr)
                // if (barcode != null && barcode.isNotEmpty()) {
                // val scanResult = String(barcode, 0, barcodeLen)
                // log
                // printDataScan(action, barcode, barcodeLen, scanResult, barcodeStr, barcodeType)
                // }

                // Result
                //                    loggerBarcodeReceiver(barcodeStr)
//                callback.invoke(barcodeStr)

                if (intent.hasExtra(BarcodeReceiver.SYMBOLOGY_NAME_TAG)) {
                    val symName = intent.getStringExtra(BarcodeReceiver.SYMBOLOGY_NAME_TAG)
                }
                if (intent.hasExtra(BarcodeReceiver.SYMBOLOGY_ID_TAG)) {
                    val symId = intent.getIntExtra(BarcodeReceiver.SYMBOLOGY_ID_TAG, Symbology.NONE.toInt())
                } else {
                    val type = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, 0.toByte())
                    val symId: Int = OEMSymbologyId.getHSMSymbologyId(type.toInt())
                }
                if (intent.hasExtra(BarcodeReceiver.DECODE_TIME_TAG)) {
                    val time = intent.getLongExtra(BarcodeReceiver.DECODE_TIME_TAG, 0L)
                }

                try {
                    val resultBundle = intent.extras
                    val keys = resultBundle!!.keySet()
                    for (key in keys) {
                        val obj = resultBundle[key]
                        if (obj is String) {
                        } else if (obj is Array<*> && obj.isArrayOf<String>()) {
                            for (code in obj) {
                            }
                        } else if (obj is ByteArray) {
                        } else {
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

}