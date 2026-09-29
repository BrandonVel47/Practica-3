import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'core/theme/app_theme.dart';
import 'data/datasources/file_storage.dart';
import 'data/datasources/media_database.dart';
import 'data/repositories/media_repository_impl.dart';
import 'data/repositories/settings_repository_impl.dart';
import 'presentation/providers/media_provider.dart';
import 'presentation/providers/theme_provider.dart';
import 'presentation/screens/home_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Capa de datos
  final prefs = await SharedPreferences.getInstance();
  final settingsRepo = SettingsRepositoryImpl(prefs);
  final mediaRepo = MediaRepositoryImpl(
    await MediaDatabase.abrir(),
    await FileStorage.crear(),
  );

  // Inyección de dependencias con Provider
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider(settingsRepo)),
        ChangeNotifierProvider(create: (_) => MediaProvider(mediaRepo)),
      ],
      child: const CamaraApp(),
    ),
  );
}

class CamaraApp extends StatelessWidget {
  const CamaraApp({super.key});

  @override
  Widget build(BuildContext context) {
    final tema = context.watch<ThemeProvider>().tema;
    return MaterialApp(
      title: 'Cámara y Micrófono',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(tema),
      darkTheme: AppTheme.dark(tema),
      themeMode: ThemeMode.system, // claro/oscuro según el sistema
      home: const HomeScreen(),
    );
  }
}