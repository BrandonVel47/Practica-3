import 'package:flutter/material.dart';

import '../widgets/en_construccion.dart';

/// Pestaña Cámara (temporal).
class CameraScreen extends StatelessWidget {
  const CameraScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Cámara')),
      body: const EnConstruccion(
        icono: Icons.photo_camera,
        texto: 'La cámara se implementará en el siguiente paso.',
      ),
    );
  }
}