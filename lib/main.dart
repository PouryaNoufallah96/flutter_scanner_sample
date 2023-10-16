import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const _platform =
      MethodChannel("com.example.native_scanner_for_flutter/scanner");
  static const _channel = EventChannel('ScannerChannel');

  StreamSubscription? _scannerSubscription;
  var _barcode = '';
  var _lastScanTime = DateTime.now();
  final _throttleDuration = const Duration(milliseconds: 500);

  @override
  void initState() {
    super.initState();
    _init();
  }

  void _init() async {
    final result = (await _platform.invokeMethod('initScanner')) as bool;
    if (!result) return;

    _scannerSubscription = _channel
        .receiveBroadcastStream()
        .map((event) => event.toString())
        .listen((event) {
      final currentTime = DateTime.now();
      if (currentTime.difference(_lastScanTime) > _throttleDuration) {
        _lastScanTime = currentTime;
        setState(() {
          _barcode = event;
        });
      }
    });
  }

  @override
  void dispose() {
    _scannerSubscription?.cancel();
    _closeScanner();
    super.dispose();
  }

  _closeScanner() async {
    _platform.invokeMethod('closeScanner');
  }

  _start() async {
    _platform.invokeMethod('startScan');
  }

  _stop() async {
    _platform.invokeMethod('stopScan');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _barcode,
            ),
            GestureDetector(
              onTapDown: (details) {
                _start();
              },
              onTapUp: (details) {
                _stop();
              },
              child: Container(
                width: 100,
                height: 100,
                color: Colors.red,
              ),
            )
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {},
        child: const Icon(Icons.scanner_outlined),
      ),
    );
  }
}
