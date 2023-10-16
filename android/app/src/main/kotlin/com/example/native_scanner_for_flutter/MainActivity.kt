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

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val events = EventChannel(flutterEngine.dartExecutor.binaryMessenger,EVENT_CHANNEL)
        events.setStreamHandler(this)

        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL)
        methodChannel!!.setMethodCallHandler(this)
    }


    private fun startDecode() {
        barcodeReceiver?.startDecode()
    }
    private fun stopDecode(){
        barcodeReceiver?.stopDecode()
    }
    private fun closeConnection(){
        barcodeReceiver?.unregisterBarcodeReceiver();
    }

    private fun initScan() {
        try {
            barcodeReceiver = BarcodeReceiver(activity, lifecycle) { value ->
                eventSink?.success(value!!)
            }
        } catch (e: Exception) {
        }
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
                initScan()
            }
            "getScannerState"->{
                result.success(getScannerState())
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
