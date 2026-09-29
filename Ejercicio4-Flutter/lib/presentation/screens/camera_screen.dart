import 'dart:io';
import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../core/filters/filtro_foto.dart';
import '../providers/media_provider.dart';

/// Pestaña Cámara: vista previa, flash, temporizador, filtros y
/// selección desde la fototeca (alternativa cuando no hay cámara, p. ej. simulador iOS).
class CameraScreen extends StatefulWidget {
  const CameraScreen({super.key});

  @override
  State<CameraScreen> createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> with WidgetsBindingObserver {
  static const _opcionesTemporizador = [0, 3, 5, 10];

  List<CameraDescription> _camaras = [];
  CameraController? _controlador;
  int _indiceCamara = 0;
  FlashMode _flash = FlashMode.off;
  FiltroFoto _filtro = FiltroFoto.normal;
  int _temporizador = 0; // segundos
  int? _cuenta; // cuenta regresiva visible
  bool _procesando = false;
  String? _error;

  // ---------------- Ciclo de vida ----------------

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _cargarCamaras();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controlador?.dispose();
    super.dispose();
  }

  /// Libera la cámara cuando la app pasa a segundo plano y la reactiva al volver.
  @override
  void didChangeAppLifecycleState(AppLifecycleState estado) {
    if (estado == AppLifecycleState.inactive || estado == AppLifecycleState.paused) {
      final c = _controlador;
      _controlador = null;
      c?.dispose();
      if (mounted) setState(() {});
    } else if (estado == AppLifecycleState.resumed &&
        _controlador == null &&
        _camaras.isNotEmpty) {
      _iniciarCamara();
    }
  }

  // ---------------- Cámara ----------------

  Future<void> _cargarCamaras() async {
    try {
      _camaras = await availableCameras();
    } on CameraException {
      _camaras = [];
    }
    if (!mounted) return;
    if (_camaras.isEmpty) {
      setState(() => _error =
      'Este dispositivo no tiene cámara (por ejemplo, el simulador de iOS).\n'
          'Usa el botón de la fototeca de arriba para elegir una foto.');
      return;
    }
    await _iniciarCamara();
  }

  Future<void> _iniciarCamara() async {
    final anterior = _controlador;
    _controlador = null;
    await anterior?.dispose();

    final c = CameraController(
      _camaras[_indiceCamara],
      ResolutionPreset.high,
      enableAudio: false,
      imageFormatGroup: ImageFormatGroup.jpeg,
    );
    try {
      await c.initialize(); // aquí el sistema pide el permiso de cámara
      try {
        await c.setFlashMode(_flash);
      } catch (_) {
        // la cámara frontal normalmente no tiene flash
      }
      if (!mounted) {
        await c.dispose();
        return;
      }
      setState(() {
        _controlador = c;
        _error = null;
      });
    } on CameraException catch (e) {
      await c.dispose();
      if (!mounted) return;
      final sinPermiso = e.code.contains('AccessDenied') || e.code.contains('Restricted');
      setState(() => _error = sinPermiso
          ? 'Sin permiso para usar la cámara.\n'
          'Actívalo en Ajustes del sistema > Apps > esta app > Permisos.'
          : 'No se pudo iniciar la cámara: ${e.description}');
    }
  }

  void _cambiarCamara() {
    if (_camaras.length < 2) return;
    _indiceCamara = (_indiceCamara + 1) % _camaras.length;
    _iniciarCamara();
  }

  Future<void> _cambiarFlash() async {
    final siguiente = switch (_flash) {
      FlashMode.off => FlashMode.auto,
      FlashMode.auto => FlashMode.always,
      _ => FlashMode.off,
    };
    try {
      await _controlador?.setFlashMode(siguiente);
      setState(() => _flash = siguiente);
    } on CameraException {
      _avisar('Esta cámara no tiene flash');
    }
  }

  IconData get _iconoFlash => switch (_flash) {
    FlashMode.auto => Icons.flash_auto,
    FlashMode.always => Icons.flash_on,
    _ => Icons.flash_off,
  };

  void _cambiarTemporizador() {
    final i = _opcionesTemporizador.indexOf(_temporizador);
    setState(() => _temporizador = _opcionesTemporizador[(i + 1) % _opcionesTemporizador.length]);
  }

  // ---------------- Captura ----------------

  Future<void> _disparar() async {
    final c = _controlador;
    if (c == null || !c.value.isInitialized) return;

    // Cuenta regresiva
    for (var s = _temporizador; s > 0; s--) {
      setState(() => _cuenta = s);
      await Future.delayed(const Duration(seconds: 1));
      if (!mounted) return;
    }
    setState(() {
      _cuenta = null;
      _procesando = true;
    });

    try {
      final archivo = await c.takePicture();
      final bytes = await archivo.readAsBytes();
      await _guardar(bytes);
      File(archivo.path).delete().ignore(); // borrar el temporal
    } catch (e) {
      _avisar('No se pudo tomar la foto: $e');
    } finally {
      if (mounted) setState(() => _procesando = false);
    }
  }

  /// Alternativa sin cámara: elegir una imagen de la fototeca / galería.
  Future<void> _elegirDeFototeca() async {
    final elegida = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (elegida == null) return;
    setState(() => _procesando = true);
    try {
      await _guardar(await elegida.readAsBytes());
    } catch (e) {
      _avisar('No se pudo guardar la imagen: $e');
    } finally {
      if (mounted) setState(() => _procesando = false);
    }
  }

  /// Aplica el filtro (en otro isolate) y guarda la foto con sus metadatos.
  Future<void> _guardar(Uint8List bytes) async {
    final media = context.read<MediaProvider>();
    final procesada = await compute(aplicarFiltroBytes, (bytes, _filtro));
    await media.guardarFoto(procesada);
    _avisar('Foto guardada');
  }

  void _avisar(String texto) {
    if (!mounted) return;
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(texto), duration: const Duration(seconds: 2)));
  }

  // ---------------- Interfaz ----------------

  @override
  Widget build(BuildContext context) {
    final c = _controlador;
    final lista = c != null && c.value.isInitialized;
    final puedeDisparar = lista && !_procesando && _cuenta == null;
    final ultima = context.watch<MediaProvider>().ultimaFoto;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Cámara'),
        actions: [
          IconButton(
            tooltip: 'Flash',
            icon: Icon(_iconoFlash),
            onPressed: lista ? _cambiarFlash : null,
          ),
          TextButton.icon(
            onPressed: _cuenta == null ? _cambiarTemporizador : null,
            icon: const Icon(Icons.timer_outlined),
            label: Text(_temporizador == 0 ? 'No' : '${_temporizador}s'),
          ),
          IconButton(
            tooltip: 'Elegir de la fototeca',
            icon: const Icon(Icons.add_photo_alternate_outlined),
            onPressed: _procesando ? null : _elegirDeFototeca,
          ),
        ],
      ),
      body: Column(
        children: [
          // Vista previa
          Expanded(
            child: Container(
              color: Colors.black,
              child: Stack(
                fit: StackFit.expand,
                children: [
                  if (lista)
                    Center(
                      child: ColorFiltered(
                        colorFilter: ColorFilter.matrix(_filtro.matriz),
                        child: CameraPreview(c!),
                      ),
                    )
                  else if (_error != null)
                    Center(
                      child: Padding(
                        padding: const EdgeInsets.all(24),
                        child: Text(
                          _error!,
                          textAlign: TextAlign.center,
                          style: const TextStyle(color: Colors.white),
                        ),
                      ),
                    )
                  else
                    const Center(child: CircularProgressIndicator()),
                  if (_cuenta != null)
                    Center(
                      child: Text(
                        '$_cuenta',
                        style: const TextStyle(
                          fontSize: 120,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                          shadows: [Shadow(blurRadius: 16)],
                        ),
                      ),
                    ),
                  if (_procesando)
                    const ColoredBox(
                      color: Colors.black45,
                      child: Center(child: CircularProgressIndicator()),
                    ),
                ],
              ),
            ),
          ),

          // Filtros
          SizedBox(
            height: 56,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              children: [
                for (final f in FiltroFoto.values)
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(f.etiqueta),
                      selected: f == _filtro,
                      onSelected: (_) => setState(() => _filtro = f),
                    ),
                  ),
              ],
            ),
          ),

          // Controles inferiores
          Padding(
            padding: const EdgeInsets.fromLTRB(24, 4, 24, 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _MiniaturaUltima(archivo: ultima),
                SizedBox(
                  width: 76,
                  height: 76,
                  child: FilledButton(
                    style: FilledButton.styleFrom(
                      shape: const CircleBorder(),
                      padding: EdgeInsets.zero,
                    ),
                    onPressed: puedeDisparar ? _disparar : null,
                    child: const Icon(Icons.camera_alt, size: 36),
                  ),
                ),
                IconButton.filledTonal(
                  iconSize: 28,
                  tooltip: 'Cambiar cámara',
                  onPressed: _camaras.length > 1 && !_procesando ? _cambiarCamara : null,
                  icon: const Icon(Icons.cameraswitch),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Miniatura de la última foto guardada.
class _MiniaturaUltima extends StatelessWidget {
  const _MiniaturaUltima({required this.archivo});

  final File? archivo;

  @override
  Widget build(BuildContext context) {
    final colores = Theme.of(context).colorScheme;
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: Container(
        width: 56,
        height: 56,
        color: colores.surfaceContainerHighest,
        child: archivo == null
            ? Icon(Icons.image_outlined, color: colores.onSurfaceVariant)
            : Image.file(
          archivo!,
          key: ValueKey(archivo!.path),
          fit: BoxFit.cover,
          cacheWidth: 150,
        ),
      ),
    );
  }
}