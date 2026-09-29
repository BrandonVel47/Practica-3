import '../../core/theme/app_theme.dart';

/// Contrato (capa de dominio): qué necesita la app de los ajustes,
/// sin importar cómo o dónde se guardan.
abstract class SettingsRepository {
  AppThemeType obtenerTema();

  Future<void> guardarTema(AppThemeType tema);
}