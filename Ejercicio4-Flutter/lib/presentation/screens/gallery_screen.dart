import 'package:flutter/material.dart';

import '../widgets/en_construccion.dart';

/// Pestaña Galería (temporal).
class GalleryScreen extends StatelessWidget {
  const GalleryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Galería')),
      body: const EnConstruccion(
        icono: Icons.photo_library,
        texto: 'La galería se implementará más adelante.',
      ),
    );
  }
}