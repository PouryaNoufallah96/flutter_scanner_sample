import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_barcode_listener/flutter_barcode_listener.dart';

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
  final _duation = const Duration(milliseconds: 1000);
  var _lastMili = 0;

  start() async {
    _platform.invokeMethod('startScan');
  }

  _close() async {
    _platform.invokeMethod('stopScan');
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPersistentFrameCallback((timeStamp) {
      _init();
    });
  }

  _init() async {
    final result = (await _platform.invokeMethod('getScannerState')) as bool;
    print('Is support: $result');
    _platform.invokeMethod('initScanner');
    _channel
        .receiveBroadcastStream()
        .map((event) => event.toString())
        .listen((event) {
      setState(() {
        _barcode = event;
      });
    });
  }

  var _barcode = '';

  @override
  void dispose() {
    _platform.invokeMethod('closeScanner');
    super.dispose();
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
                start();
              },
              onTapUp: (details) {
                _close();
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
