import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/theme/app_theme.dart';
import '../providers/theme_provider.dart';

/// Pestaña de ajustes: elección del tema institucional.
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final temas = context.watch<ThemeProvider>();
    final colores = Theme.of(context).colorScheme;
    final esOscuro = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(title: const Text('Ajustes')),
      body: ListView(
        children: [
          const ListTile(
            title: Text('Tema'),
            subtitle: Text('Colores institucionales'),
          ),
          for (final tipo in AppThemeType.values)
            ListTile(
              leading: CircleAvatar(backgroundColor: tipo.color, radius: 14),
              title: Text(tipo.etiqueta),
              trailing: temas.tema == tipo
                  ? Icon(Icons.check_circle, color: colores.primary)
                  : null,
              onTap: () => temas.cambiarTema(tipo),
            ),
          const Divider(),
          ListTile(
            leading: Icon(esOscuro ? Icons.dark_mode : Icons.light_mode),
            title: const Text('Modo claro / oscuro'),
            subtitle: Text(
              'Se adapta automáticamente al sistema. '
                  'Actual: ${esOscuro ? 'oscuro' : 'claro'}',
            ),
          ),
        ],
      ),
    );
  }
}