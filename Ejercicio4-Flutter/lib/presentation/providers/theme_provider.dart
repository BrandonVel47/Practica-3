import 'package:flutter/foundation.dart';

import '../../core/theme/app_theme.dart';
import '../../domain/repositories/settings_repository.dart';

/// Estado del tema (capa de presentación). Notifica a la interfaz cuando cambia.
class ThemeProvider extends ChangeNotifier {
  ThemeProvider(this._repo) {
    _tema = _repo.obtenerTema();
  }

  final SettingsRepository _repo;
  late AppThemeType _tema;

  AppThemeType get tema => _tema;

  Future<void> cambiarTema(AppThemeType nuevo) async {
    if (nuevo == _tema) return;
    _tema = nuevo;
    notifyListeners();
    await _repo.guardarTema(nuevo);
  }
}