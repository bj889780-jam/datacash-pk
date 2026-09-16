import 'dart:async';
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
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF00A859),
          primary: const Color(0xFF00A859),
          surface: const Color(0xFFF8FAF9),
        ),
        scaffoldBackgroundColor: const Color(0xFFF4F7F5),
        cardTheme: CardTheme(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: const BorderSide(color: Color(0xFFE5E7EB), width: 1),
          ),
          color: Colors.white,
        ),
      ),
      home: const MainNavigationScreen(),
    );
  }
}

class Transaction {
  final String id;
  final String title;
  final String method;
  final double amount;
  final String date;
  final String status;
  final bool isCredit;

  Transaction({
    required this.id,
    required this.title,
    required this.method,
    required this.amount,
    required this.date,
    required this.status,
    required this.isCredit,
  });
}

class WalletAccount {
  final String id;
  final String title;
  final String subtitle;
  final String accountNumber;
  final String type;
  bool isDefault;

  WalletAccount({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.accountNumber,
    required this.type,
    this.isDefault = false,
  });
}

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({super.key});

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;
  bool _isSharing = false;
  double _balance = 1250.00;
  double _dataSharedMb = 4166.7;
  Timer? _sharingTimer;

  final List<Transaction> _transactions = [
    Transaction(
      id: 'TXN-9841',
      title: 'Easypaisa Cash Out',
      method: 'Easypaisa',
      amount: 500.0,
      date: 'Today, 02:45 PM',
      status: 'Completed',
      isCredit: false,
    ),
    Transaction(
      id: 'TXN-9730',
      title: 'Data Yield (1,500 MB)',
      method: 'Bandwidth Sharing',
      amount: 450.0,
      date: 'Yesterday, 08:30 PM',
      status: 'Verified',
      isCredit: true,
    ),
    Transaction(
      id: 'TXN-9625',
      title: 'Referral Bonus (Invite)',
      method: 'DataCash Rewards',
      amount: 150.0,
      date: '14 Sep 2026',
      status: 'Completed',
      isCredit: true,
    ),
  ];

  final List<WalletAccount> _wallets = [
    WalletAccount(
      id: 'w-ep',
      title: 'Easypaisa Mobile Account',
      subtitle: 'Muhammad Ali',
      accountNumber: '0300-***4567',
      type: 'Easypaisa',
      isDefault: true,
    ),
    WalletAccount(
      id: 'w-jc',
      title: 'JazzCash Wallet',
      subtitle: 'Muhammad Ali',
      accountNumber: '0321-***7890',
      type: 'JazzCash',
      isDefault: false,
    ),
    WalletAccount(
      id: 'w-raast',
      title: 'Meezan Bank (Raast ID)',
      subtitle: 'PK36MEZN000123456789',
      accountNumber: 'Raast: 03001234567',
      type: 'Raast',
      isDefault: false,
    ),
  ];

  @override
  void dispose() {
    _sharingTimer?.cancel();
    super.dispose();
  }

  void _toggleSharing() {
    setState(() {
      _isSharing = !_isSharing;
    });

    if (_isSharing) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Bandwidth Sharing Active - Earning PKR in background!'),
          backgroundColor: Color(0xFF00A859),
          duration: Duration(seconds: 2),
        ),
      );
      _sharingTimer = Timer.periodic(const Duration(milliseconds: 1500), (timer) {
        if (!mounted) return;
        setState(() {
          _dataSharedMb += 1.4;
          _balance += 0.42;
        });
      });
    } else {
      _sharingTimer?.cancel();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Bandwidth Sharing Paused'),
          duration: Duration(seconds: 2),
        ),
      );
    }
  }

  void _openCashOutModal([String defaultMethod = 'Easypaisa']) {
    final amountController = TextEditingController(text: '500');
    final accountController = TextEditingController(
      text: defaultMethod == 'JazzCash' ? '03217890123' : '03001234567',
    );
    String selectedMethod = defaultMethod;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setModalState) {
            return Padding(
              padding: EdgeInsets.only(
                left: 20,
                right: 20,
                top: 20,
                bottom: MediaQuery.of(context).viewInsets.bottom + 24,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Cash Out via $selectedMethod',
                        style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(ctx),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  const Text('Select Method', style: TextStyle(fontSize: 12, color: Colors.grey, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: ChoiceChip(
                          label: const Text('Easypaisa'),
                          selected: selectedMethod == 'Easypaisa',
                          selectedColor: const Color(0xFF00A859).withOpacity(0.2),
                          onSelected: (val) {
                            if (val) setModalState(() => selectedMethod = 'Easypaisa');
                          },
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: ChoiceChip(
                          label: const Text('JazzCash'),
                          selected: selectedMethod == 'JazzCash',
                          selectedColor: Colors.red.withOpacity(0.2),
                          onSelected: (val) {
                            if (val) setModalState(() => selectedMethod = 'JazzCash');
                          },
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: ChoiceChip(
                          label: const Text('Raast / Bank'),
                          selected: selectedMethod == 'Raast / Bank',
                          selectedColor: Colors.blue.withOpacity(0.2),
                          onSelected: (val) {
                            if (val) setModalState(() => selectedMethod = 'Raast / Bank');
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: accountController,
                    decoration: InputDecoration(
                      labelText: selectedMethod.contains('Bank') ? 'IBAN / Raast ID' : 'Mobile Account Number (03xxxxxxxxx)',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                      prefixIcon: const Icon(Icons.phone_android),
                    ),
                    keyboardType: TextInputType.phone,
                  ),
                  const SizedBox(height: 14),
                  TextField(
                    controller: amountController,
                    decoration: InputDecoration(
                      labelText: 'Amount (PKR)',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                      prefixIcon: const Icon(Icons.attach_money),
                      helperText: 'Available: Rs. ${_balance.toStringAsFixed(2)}',
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 10),
                  Wrap(
                    spacing: 8,
                    children: [500, 1000, 2000].map((amt) {
                      return ActionChip(
                        label: Text('Rs. $amt'),
                        onPressed: () {
                          amountController.text = amt.toString();
                        },
                      );
                    }).toList()
                      ..add(
                        ActionChip(
                          label: const Text('All Balance'),
                          onPressed: () {
                            amountController.text = _balance.toInt().toString();
                          },
                        ),
                      ),
                  ),
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF0FDF4),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: const Color(0xFFBBF7D0)),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.check_circle, color: Color(0xFF00A859), size: 18),
                        SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Fee: Rs. 0.00 • Instant payout via State Bank 1Link Network',
                            style: TextStyle(fontSize: 12, color: Color(0xFF166534), fontWeight: FontWeight.w500),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 18),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF00A859),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    onPressed: () {
                      final enteredAmt = double.tryParse(amountController.text) ?? 0;
                      if (enteredAmt <= 0) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Please enter a valid amount')),
                        );
                        return;
                      }
                      if (enteredAmt > _balance) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Amount exceeds available balance of Rs. ${_balance.toStringAsFixed(2)}')),
                        );
                        return;
                      }

                      setState(() {
                        _balance -= enteredAmt;
                        _transactions.insert(
                          0,
                          Transaction(
                            id: 'TXN-${1000 + _transactions.length * 17}',
                            title: '$selectedMethod Cash Out',
                            method: selectedMethod,
                            amount: enteredAmt,
                            date: 'Just now',
                            status: 'Processing',
                            isCredit: false,
                          ),
                        );
                      });

                      Navigator.pop(ctx);
                      _showSuccessReceipt(selectedMethod, accountController.text, enteredAmt);
                    },
                    child: const Text('Confirm Cash Out', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  void _showSuccessReceipt(String method, String account, double amount) {
    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: const Color(0xFFDCFCE7),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.check, color: Color(0xFF00A859), size: 36),
              ),
              const SizedBox(height: 16),
              const Text(
                'Cash Out Initiated!',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                'Rs. ${amount.toStringAsFixed(2)} is being transferred to your $method account ($account).',
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.grey, fontSize: 13),
              ),
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFF3F4F6),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Est. Arrival:', style: TextStyle(fontSize: 12, color: Colors.grey)),
                    const Text('Under 60 seconds', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF00A859))),
                  ],
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Done', style: TextStyle(fontWeight: FontWeight.bold)),
            ),
          ],
        );
      },
    );
  }

  void _openAddWalletDialog() {
    final titleController = TextEditingController();
    final numberController = TextEditingController();
    String type = 'Easypaisa';

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Link New Account'),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  DropdownButtonFormField<String>(
                    value: type,
                    decoration: InputDecoration(
                      labelText: 'Account Type',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    items: const [
                      DropdownMenuItem(value: 'Easypaisa', child: Text('Easypaisa Mobile Account')),
                      DropdownMenuItem(value: 'JazzCash', child: Text('JazzCash Wallet')),
                      DropdownMenuItem(value: 'Raast', child: Text('Raast ID / 1Link Bank')),
                      DropdownMenuItem(value: 'Meezan', child: Text('Meezan Bank')),
                      DropdownMenuItem(value: 'HBL', child: Text('Habib Bank Limited (HBL)')),
                    ],
                    onChanged: (val) {
                      if (val != null) setDialogState(() => type = val);
                    },
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: titleController,
                    decoration: InputDecoration(
                      labelText: 'Account Holder Name',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: numberController,
                    decoration: InputDecoration(
                      labelText: type == 'Raast' ? 'IBAN / Raast ID' : 'Mobile Number',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                ],
              ),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                ElevatedButton(
                  style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00A859), foregroundColor: Colors.white),
                  onPressed: () {
                    if (titleController.text.isNotEmpty && numberController.text.isNotEmpty) {
                      setState(() {
                        _wallets.add(
                          WalletAccount(
                            id: 'w-${DateTime.now().millisecondsSinceEpoch}',
                            title: '$type Account',
                            subtitle: titleController.text,
                            accountNumber: numberController.text,
                            type: type,
                          ),
                        );
                      });
                      Navigator.pop(ctx);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('New account linked successfully!')),
                      );
                    }
                  },
                  child: const Text('Save Wallet'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 0,
        backgroundColor: const Color(0xFF00A859),
        foregroundColor: Colors.white,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.2),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.bolt, size: 20, color: Colors.white),
            ),
            const SizedBox(width: 10),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('DataCash PK', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
                Text('Pakistan 🇵🇰 Bandwidth Monetization', style: TextStyle(fontSize: 10, color: Colors.white70)),
              ],
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_none),
            onPressed: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('All payouts and network nodes are operational.')),
              );
            },
          ),
        ],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: [
          _buildHomeTab(),
          _buildWalletsTab(),
          _buildYieldTab(),
          _buildAccountTab(),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (idx) {
          setState(() {
            _currentIndex = idx;
          });
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home, color: Color(0xFF00A859)),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.account_balance_wallet_outlined),
            selectedIcon: Icon(Icons.account_balance_wallet, color: Color(0xFF00A859)),
            label: 'Wallets',
          ),
          NavigationDestination(
            icon: Icon(Icons.auto_graph_outlined),
            selectedIcon: Icon(Icons.auto_graph, color: Color(0xFF00A859)),
            label: 'Yield',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person, color: Color(0xFF00A859)),
            label: 'Account',
          ),
        ],
      ),
    );
  }

  Widget _buildHomeTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Balance Card
          Container(
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF09634F), Color(0xFF00A859)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF00A859).withOpacity(0.25),
                  blurRadius: 16,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Available Earnings',
                      style: TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.w500),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Row(
                        children: [
                          Container(
                            width: 8,
                            height: 8,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: _isSharing ? const Color(0xFF4ADE80) : Colors.grey,
                            ),
                          ),
                          const SizedBox(width: 6),
                          Text(
                            _isSharing ? 'Earning Active' : 'Idle',
                            style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Text(
                  'Rs. ${_balance.toStringAsFixed(2)}',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 34,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 18),
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Data Sold', style: TextStyle(color: Colors.white70, fontSize: 11)),
                          const SizedBox(height: 2),
                          Text(
                            '${_dataSharedMb.toStringAsFixed(1)} MB',
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                          ),
                        ],
                      ),
                    ),
                    Container(width: 1, height: 28, color: Colors.white24),
                    const SizedBox(width: 16),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Network Rate', style: TextStyle(color: Colors.white70, fontSize: 11)),
                          SizedBox(height: 2),
                          Text(
                            'Rs. 0.30 / MB',
                            style: TextStyle(color: Color(0xFF86EFAC), fontWeight: FontWeight.bold, fontSize: 15),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // Sharing Switch Tile
          Card(
            child: InkWell(
              borderRadius: BorderRadius.circular(16),
              onTap: _toggleSharing,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                child: Row(
                  children: [
                    Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: _isSharing ? const Color(0xFFDCFCE7) : const Color(0xFFF3F4F6),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        Icons.sensors,
                        color: _isSharing ? const Color(0xFF00A859) : Colors.grey,
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Bandwidth Sharing',
                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            _isSharing ? 'Active - Monetizing data at Rs. 0.30/MB' : 'Paused - Tap switch to start earning',
                            style: TextStyle(
                              fontSize: 12,
                              color: _isSharing ? const Color(0xFF00A859) : Colors.grey,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Switch(
                      value: _isSharing,
                      activeColor: const Color(0xFF00A859),
                      onChanged: (val) => _toggleSharing(),
                    ),
                  ],
                ),
              ),
            ),
          ),

          const SizedBox(height: 16),

          // Quick Actions Row
          Row(
            children: [
              _buildQuickActionBtn(
                icon: Icons.payments,
                label: 'Cash Out',
                color: const Color(0xFF00A859),
                onTap: () => _openCashOutModal('Easypaisa'),
              ),
              const SizedBox(width: 10),
              _buildQuickActionBtn(
                icon: Icons.add_card,
                label: 'Add Wallet',
                color: const Color(0xFF2563EB),
                onTap: _openAddWalletDialog,
              ),
              const SizedBox(width: 10),
              _buildQuickActionBtn(
                icon: Icons.share,
                label: 'Invite & Earn',
                color: const Color(0xFF7C3AED),
                onTap: () {
                  setState(() => _currentIndex = 3);
                },
              ),
              const SizedBox(width: 10),
              _buildQuickActionBtn(
                icon: Icons.insights,
                label: 'Analytics',
                color: const Color(0xFFEA580C),
                onTap: () {
                  setState(() => _currentIndex = 2);
                },
              ),
            ],
          ),

          const SizedBox(height: 20),

          // Supported Payment Gateways Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Instant Cash Out',
                style: TextStyle(fontSize: 17, fontWeight: FontWeight.bold),
              ),
              TextButton(
                onPressed: () => _openCashOutModal('Easypaisa'),
                child: const Text('Quick Payout', style: TextStyle(color: Color(0xFF00A859))),
              ),
            ],
          ),

          const SizedBox(height: 8),

          Row(
            children: [
              // Easypaisa Card
              Expanded(
                child: Card(
                  child: InkWell(
                    borderRadius: BorderRadius.circular(16),
                    onTap: () => _openCashOutModal('Easypaisa'),
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Image.asset(
                            'assets/easypaisa.png',
                            height: 38,
                            errorBuilder: (_, __, ___) => const Icon(Icons.wallet, size: 38, color: Color(0xFF00B14F)),
                          ),
                          const SizedBox(height: 12),
                          const Text('Easypaisa', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                          const Text('Instant Mobile Account', style: TextStyle(color: Colors.grey, fontSize: 11)),
                          const SizedBox(height: 10),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: const Color(0xFFDCFCE7),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: const Text('Withdraw', style: TextStyle(color: Color(0xFF166534), fontSize: 11, fontWeight: FontWeight.bold)),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              // JazzCash Card
              Expanded(
                child: Card(
                  child: InkWell(
                    borderRadius: BorderRadius.circular(16),
                    onTap: () => _openCashOutModal('JazzCash'),
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Image.asset(
                            'assets/jazzcash.png',
                            height: 38,
                            errorBuilder: (_, __, ___) => const Icon(Icons.wallet, size: 38, color: Colors.red),
                          ),
                          const SizedBox(height: 12),
                          const Text('JazzCash', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                          const Text('Instant Mobile Account', style: TextStyle(color: Colors.grey, fontSize: 11)),
                          const SizedBox(height: 10),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: const Color(0xFFFEE2E2),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: const Text('Withdraw', style: TextStyle(color: Color(0xFF991B1B), fontSize: 11, fontWeight: FontWeight.bold)),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 10),

          // Bank Transfer Card
          Card(
            child: ListTile(
              onTap: () => _openCashOutModal('Raast / Bank'),
              leading: Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: const Color(0xFFEFF6FF),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(Icons.account_balance, color: Color(0xFF2563EB)),
              ),
              title: const Text('1Link & Raast Bank Transfer', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              subtitle: const Text('Meezan, HBL, UBL, Bank Alfalah & all Pak Banks', style: TextStyle(fontSize: 11)),
              trailing: const Icon(Icons.chevron_right, color: Colors.grey),
            ),
          ),

          const SizedBox(height: 22),

          // Recent Activity Header
          const Text('Recent Transactions', style: TextStyle(fontSize: 17, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),

          ..._transactions.map((t) {
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: ListTile(
                leading: CircleAvatar(
                  backgroundColor: t.isCredit ? const Color(0xFFDCFCE7) : const Color(0xFFFEE2E2),
                  child: Icon(
                    t.isCredit ? Icons.arrow_downward : Icons.arrow_upward,
                    color: t.isCredit ? const Color(0xFF166534) : const Color(0xFF991B1B),
                    size: 18,
                  ),
                ),
                title: Text(t.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                subtitle: Text('${t.date} • ${t.status}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                trailing: Text(
                  '${t.isCredit ? "+" : "-"} Rs. ${t.amount.toStringAsFixed(2)}',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: t.isCredit ? const Color(0xFF00A859) : const Color(0xFFDC2626),
                    fontSize: 14,
                  ),
                ),
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildQuickActionBtn({
    required IconData icon,
    required String label,
    required Color color,
    required VoidCallback onTap,
  }) {
    return Expanded(
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: const Color(0xFFE5E7EB)),
          ),
          child: Column(
            children: [
              Icon(icon, color: color, size: 22),
              const SizedBox(height: 6),
              Text(
                label,
                style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF374151)),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildWalletsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Linked Payout Methods', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              IconButton(
                icon: const Icon(Icons.add_circle, color: Color(0xFF00A859)),
                onPressed: _openAddWalletDialog,
              ),
            ],
          ),
          const Text('Manage your Pakistani wallets & accounts for automated cash out.', style: TextStyle(fontSize: 12, color: Colors.grey)),
          const SizedBox(height: 16),

          ..._wallets.map((w) {
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Container(
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        color: w.type == 'Easypaisa'
                            ? const Color(0xFFDCFCE7)
                            : (w.type == 'JazzCash' ? const Color(0xFFFEE2E2) : const Color(0xFFEFF6FF)),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        Icons.account_balance_wallet,
                        color: w.type == 'Easypaisa'
                            ? const Color(0xFF00A859)
                            : (w.type == 'JazzCash' ? Colors.red : Colors.blue),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(w.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                          Text(w.subtitle, style: const TextStyle(fontSize: 12, color: Colors.grey)),
                          const SizedBox(height: 4),
                          Text(w.accountNumber, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                        ],
                      ),
                    ),
                    if (w.isDefault)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: const Color(0xFF00A859).withOpacity(0.15),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Text('Primary', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF00A859))),
                      )
                    else
                      TextButton(
                        onPressed: () {
                          setState(() {
                            for (var item in _wallets) {
                              item.isDefault = (item.id == w.id);
                            }
                          });
                        },
                        child: const Text('Set Primary', style: TextStyle(fontSize: 12)),
                      ),
                  ],
                ),
              ),
            );
          }),

          const SizedBox(height: 12),
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF00A859),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 14),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            icon: const Icon(Icons.add),
            label: const Text('Add Another Bank / Wallet'),
            onPressed: _openAddWalletDialog,
          ),
        ],
      ),
    );
  }

  Widget _buildYieldTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Bandwidth Yield Analytics', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          const Text('Real-time statistics on your internet data monetization.', style: TextStyle(fontSize: 12, color: Colors.grey)),
          const SizedBox(height: 16),

          // Yield Summary Cards
          Row(
            children: [
              Expanded(
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Today\'s Yield', style: TextStyle(fontSize: 12, color: Colors.grey)),
                        const SizedBox(height: 4),
                        const Text('Rs. 184.20', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF00A859))),
                        const SizedBox(height: 4),
                        const Text('614 MB Shared', style: TextStyle(fontSize: 11, color: Colors.grey)),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Avg. Hourly Rate', style: TextStyle(fontSize: 12, color: Colors.grey)),
                        const SizedBox(height: 4),
                        const Text('Rs. 18.00 / hr', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF2563EB))),
                        const SizedBox(height: 4),
                        const Text('99.8% Connectivity', style: TextStyle(fontSize: 11, color: Colors.grey)),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Node Status
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Connected Pakistan Edge Node', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Location:', style: TextStyle(fontSize: 13, color: Colors.grey)),
                      const Text('Karachi, PK (Node PK-01)', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                    ],
                  ),
                  const Divider(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Detected ISP:', style: TextStyle(fontSize: 13, color: Colors.grey)),
                      const Text('Nayatel / PTCL Fiber', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                    ],
                  ),
                  const Divider(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Network Latency:', style: TextStyle(fontSize: 13, color: Colors.grey)),
                      const Text('14 ms (Optimal)', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: Color(0xFF00A859))),
                    ],
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 16),

          // 7 Days Chart representation
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Weekly Earnings (PKR)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      _buildBar('Mon', 90, 0.45),
                      _buildBar('Tue', 140, 0.7),
                      _buildBar('Wed', 110, 0.55),
                      _buildBar('Thu', 170, 0.85),
                      _buildBar('Fri', 200, 1.0),
                      _buildBar('Sat', 160, 0.8),
                      _buildBar('Sun', 184, 0.92, isToday: true),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBar(String day, int pkr, double pct, {bool isToday = false}) {
    return Column(
      children: [
        Text('$pkr', style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600)),
        const SizedBox(height: 4),
        Container(
          width: 18,
          height: 90 * pct,
          decoration: BoxDecoration(
            color: isToday ? const Color(0xFF00A859) : const Color(0xFFCBD5E1),
            borderRadius: BorderRadius.circular(6),
          ),
        ),
        const SizedBox(height: 6),
        Text(day, style: TextStyle(fontSize: 11, color: isToday ? const Color(0xFF00A859) : Colors.grey, fontWeight: isToday ? FontWeight.bold : FontWeight.normal)),
      ],
    );
  }

  Widget _buildAccountTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Profile Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  const CircleAvatar(
                    radius: 28,
                    backgroundColor: Color(0xFF00A859),
                    child: Text('BJ', style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
                  ),
                  const SizedBox(width: 16),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('bj889780@gmail.com', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                        SizedBox(height: 4),
                        Row(
                          children: [
                            Icon(Icons.verified, color: Color(0xFF00A859), size: 16),
                            SizedBox(width: 4),
                            Text('Level 2 Verified (CNIC & Mobile)', style: TextStyle(fontSize: 11, color: Color(0xFF00A859), fontWeight: FontWeight.w600)),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 16),

          // Referral Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Referral Program', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                  const SizedBox(height: 4),
                  const Text('Earn Rs. 150 + 10% lifetime bandwidth bonus for each friend invited.', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF3F4F6),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: const Color(0xFFE5E7EB)),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Code: DATAPAK786', style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1.1)),
                        TextButton(
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Referral code DATAPAK786 copied!')),
                            );
                          },
                          child: const Text('Copy'),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 16),

          // Settings list
          Card(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Biometric / Fingerprint Lock', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
                  subtitle: const Text('Require authentication when withdrawing', style: TextStyle(fontSize: 11)),
                  value: true,
                  activeColor: const Color(0xFF00A859),
                  onChanged: (val) {},
                ),
                const Divider(height: 1),
                SwitchListTile(
                  title: const Text('Auto Cash-Out Threshold', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
                  subtitle: const Text('Automatically transfer to Easypaisa at Rs. 2,000', style: TextStyle(fontSize: 11)),
                  value: false,
                  activeColor: const Color(0xFF00A859),
                  onChanged: (val) {},
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.help_outline),
                  title: const Text('Help & Pakistan WhatsApp Helpline', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Support helpline: +92 300 0000000 (24/7 PKT)')),
                    );
                  },
                ),
                const Divider(height: 1),
                const ListTile(
                  leading: Icon(Icons.info_outline),
                  title: Text('App Version', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
                  trailing: Text('v1.0.0+1 Stable', style: TextStyle(color: Colors.grey, fontSize: 12)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
