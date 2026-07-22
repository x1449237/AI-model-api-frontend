import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'theme/app_theme.dart';
import 'screens/home_screen.dart';
import 'screens/code_generation_screen.dart';
import 'screens/image_generation_screen.dart';
import 'screens/cluster_screen.dart';
import 'screens/history_screen.dart';
import 'data/database_helper.dart';
import 'data/api_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DatabaseHelper.instance.database;
  runApp(const AIAggregatorApp());
}

class AIAggregatorApp extends StatelessWidget {
  const AIAggregatorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ApiService()),
        ChangeNotifierProvider(create: (_) => AppState()),
      ],
      child: MaterialApp(
        title: 'AI聚合助手',
        theme: AppTheme.lightTheme,
        debugShowCheckedModeBanner: false,
        home: const MainScreen(),
      ),
    );
  }
}

class AppState extends ChangeNotifier {
  int _currentTab = 0;
  int get currentTab => _currentTab;

  void setTab(int index) {
    _currentTab = index;
    notifyListeners();
  }
}

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();
    final tabs = const [
      HomeScreen(),
      CodeGenerationScreen(),
      ImageGenerationScreen(),
      ClusterScreen(),
      HistoryScreen(),
    ];

    return Scaffold(
      body: IndexedStack(
        index: appState.currentTab,
        children: tabs,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: appState.currentTab,
        onTap: (index) => context.read<AppState>().setTab(index),
        type: BottomNavigationBarType.fixed,
        selectedItemColor: Theme.of(context).primaryColor,
        unselectedItemColor: Colors.grey,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.chat_bubble_outline), label: '对话'),
          BottomNavigationBarItem(icon: Icon(Icons.code), label: '代码'),
          BottomNavigationBarItem(icon: Icon(Icons.image_outlined), label: '图片'),
          BottomNavigationBarItem(icon: Icon(Icons.compare_arrows), label: '集群'),
          BottomNavigationBarItem(icon: Icon(Icons.history), label: '历史'),
        ],
      ),
    );
  }
}