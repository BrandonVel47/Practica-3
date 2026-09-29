import 'package:flutter/material.dart';

import '../widgets/en_construccion.dart';

/// Pestaña Grabadora (temporal).
class RecorderScreen extends StatelessWidget {
  const RecorderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Grabadora')),
      body: const EnConstruccion(
        icono: Icons.mic,
        texto: 'La grabadora se implementará más adelante.',
      ),
    );
  }
}