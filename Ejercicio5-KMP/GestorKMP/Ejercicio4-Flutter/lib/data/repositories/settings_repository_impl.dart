import 'package:shared_preferences/shared_preferences.dart';

import '../../core/theme/app_theme.dart';
import '../../domain/repositories/settings_repository.dart';

/// Implementación (capa de datos): guarda los ajustes con shared_preferences
/// (NSUserDefaults en iOS, SharedPreferences en Android).
class SettingsRepositoryImpl implements SettingsRepository {
  SettingsRepositoryImpl(this._prefs);

  final SharedPreferences _prefs;

  static const _claveTema = 'tema';

  @override
  AppThemeType obtenerTema() {
    final nombre = _prefs.getString(_claveTema);
    return AppThemeType.values.firstWhere(
          (t) => t.name == nombre,
      orElse: () => AppThemeType.guinda,
    );
  }

  @override
  Future<void> guardarTema(AppThemeType tema) async {
    await _prefs.setString(_claveTema, tema.name);
  }
}