import 'package:flutter/material.dart';

/// Temas disponibles en la app.
enum AppThemeType {
  guinda('Guinda IPN', Color(0xFF6C1D45)),
  azul('Azul ESCOM', Color(0xFF003D79));

  const AppThemeType(this.etiqueta, this.color);

  final String etiqueta;
  final Color color;
}

/// Genera los ThemeData claro y oscuro de cada tema (Material Design 3).
class AppTheme {
  AppTheme._();

  static ThemeData light(AppThemeType tipo) {
    final esquema = ColorScheme.fromSeed(
      seedColor: tipo.color,
      brightness: Brightness.light,
    ).copyWith(primary: tipo.color, onPrimary: Colors.white);
    return _base(esquema);
  }

  static ThemeData dark(AppThemeType tipo) {
    final esquema = ColorScheme.fromSeed(
      seedColor: tipo.color,
      brightness: Brightness.dark,
    );
    return _base(esquema);
  }

  static ThemeData _base(ColorScheme esquema) {
    return ThemeData(
      useMaterial3: true,
      colorScheme: esquema,
      appBarTheme: AppBarTheme(
        backgroundColor: esquema.primaryContainer,
        foregroundColor: esquema.onPrimaryContainer,
      ),
    );
  }
}