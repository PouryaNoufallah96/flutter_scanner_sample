package com.example.native_scanner_for_flutter

import android.os.Bundle
import android.util.Log
import com.example.native_scanner_for_flutter.scanner.BarcodeReceiver
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity(), MethodChannel.MethodCallHandler, EventChannel.StreamHandler {
    private var METHOD_CHANNEL = "com.example.native_scanner_for_flutter/scanner"
    private var EVENT_CHANNEL = "ScannerChannel"

    private var eventSink: EventChannel.EventSink? = null
    private var barcodeReceiver: BarcodeReceiver? = null
    private var methodChannel : MethodChannel? = null

    private var lastProcessedTime = 0L
    private val MIN_SCAN_INTERVAL = 500
    private val TAG = "MainActivity"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        Log.d(TAG, "Configuring Flutter engine.")
        super.configureFlutterEngine(flutterEngine)
        if (methodChannel == null) {
            methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL)
            methodChannel!!.setMethodCallHandler(this)
        }

        if (eventSink == null) {
            val events = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
            events.setStreamHandler(this)
        }
    }
    private fun initScan():Boolean {
        try {
            Log.i(TAG, "Initializing scanner.")
            barcodeReceiver = BarcodeReceiver(activity, lifecycle) { value ->
                Log.d(TAG, "Received barcode value: $value")
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessedTime > MIN_SCAN_INTERVAL) {
                    lastProcessedTime = currentTime
                    eventSink?.success(value!!)
                }
            }
            return getScannerState();
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing scanner: ", e)
            return false
        }
    }
    private fun closeConnection(){
        barcodeReceiver?.unregisterBarcodeReceiver();
    }
    private fun startDecode() {
        barcodeReceiver?.startDecode()
    }
    private fun stopDecode(){
        barcodeReceiver?.stopDecode()
    }
    private fun getScannerState():Boolean{
        return barcodeReceiver?.getScannerState() == true
    }
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "startScan" -> {
                startDecode();
            }
            "stopScan" -> {
                 stopDecode()
            }
            "initScanner" -> {
                result.success(initScan())
            }
            "closeScanner" ->{
                closeConnection()
            }
            else -> {
                result.notImplemented()

            }
        }
    }
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }
    override fun onCancel(arguments: Any?) {
        eventSink = null
    }
}
