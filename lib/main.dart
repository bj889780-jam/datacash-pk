import 'package:flutter/material.dart';

void main() {
  runApp(const DataCashApp());
}

class DataCashApp extends StatelessWidget {
  const DataCashApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DataCash PK',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF00A859),
          primary: const Color(0xFF00A859),
        ),
        useMaterial3: true,
      ),
      home: const DataCashHomeScreen(),
    );
  }
}

class DataCashHomeScreen extends StatefulWidget {
  const DataCashHomeScreen({super.key});

  @override
  State<DataCashHomeScreen> createState() => _DataCashHomeScreenState();
}

class _DataCashHomeScreenState extends State<DataCashHomeScreen> {
  bool _isSharing = false;
  double _balance = 1250.0;
  double _dataSharedMb = 4166.7;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'DataCash PK',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF00A859),
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  children: [
                    const Text(
                      'Available Balance',
                      style: TextStyle(fontSize: 16, color: Colors.grey),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Rs. ${_balance.toStringAsFixed(2)}',
                      style: const TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF00A859),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Column(
                          children: [
                            const Text('Data Sold', style: TextStyle(color: Colors.grey)),
                            Text(
                              '${_dataSharedMb.toStringAsFixed(1)} MB',
                              style: const TextStyle(fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                        Column(
                          children: [
                            const Text('Rate', style: TextStyle(color: Colors.grey)),
                            const Text(
                              'Rs. 0.30 / MB',
                              style: TextStyle(fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            Card(
              elevation: 2,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              child: SwitchListTile(
                title: const Text(
                  'Bandwidth Sharing',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                subtitle: Text(
                  _isSharing ? 'Active - Earning PKR in background' : 'Paused - Toggle to start earning',
                ),
                value: _isSharing,
                activeColor: const Color(0xFF00A859),
                onChanged: (bool value) {
                  setState(() {
                    _isSharing = value;
                  });
                },
              ),
            ),
            const SizedBox(height: 20),
            const Text(
              'Supported Withdrawal Methods',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: Card(
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        children: [
                          Image.asset('assets/easypaisa.png', height: 48, errorBuilder: (_, __, ___) => const Icon(Icons.wallet, size: 48, color: Colors.green)),
                          const SizedBox(height: 8),
                          const Text('Easypaisa', style: TextStyle(fontWeight: FontWeight.w600)),
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Card(
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        children: [
                          Image.asset('assets/jazzcash.png', height: 48, errorBuilder: (_, __, ___) => const Icon(Icons.wallet, size: 48, color: Colors.red)),
                          const SizedBox(height: 8),
                          const Text('JazzCash', style: TextStyle(fontWeight: FontWeight.w600)),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
