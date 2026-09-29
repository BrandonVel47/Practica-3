import 'dart:typed_data';

import 'package:image/image.dart' as img;

/// Filtros disponibles. Cada uno es una matriz de color 4x5 (formato de Flutter):
/// se usa tal cual en la vista previa (ColorFilter.matrix) y para procesar la foto
/// guardada, así lo que se ve es lo que se guarda.
enum FiltroFoto {
  normal('Normal', [
    1, 0, 0, 0, 0, //
    0, 1, 0, 0, 0, //
    0, 0, 1, 0, 0, //
    0, 0, 0, 1, 0,
  ]),
  byn('B/N', [
    0.2126, 0.7152, 0.0722, 0, 0, //
    0.2126, 0.7152, 0.0722, 0, 0, //
    0.2126, 0.7152, 0.0722, 0, 0, //
    0, 0, 0, 1, 0,
  ]),
  sepia('Sepia', [
    0.393, 0.769, 0.189, 0, 0, //
    0.349, 0.686, 0.168, 0, 0, //
    0.272, 0.534, 0.131, 0, 0, //
    0, 0, 0, 1, 0,
  ]),
  frio('Frío', [
    1, 0, 0, 0, -25, //
    0, 1, 0, 0, 0, //
    0, 0, 1, 0, 25, //
    0, 0, 0, 1, 0,
  ]),
  calido('Cálido', [
    1, 0, 0, 0, 25, //
    0, 1, 0, 0, 8, //
    0, 0, 1, 0, -25, //
    0, 0, 0, 1, 0,
  ]);

  const FiltroFoto(this.etiqueta, this.matriz);

  final String etiqueta;
  final List<double> matriz;
}

/// Aplica el filtro a una imagen y la devuelve como JPG.
/// Además corrige la orientación (EXIF) para que la foto no quede girada.
/// Se ejecuta en un isolate aparte con compute() para no congelar la interfaz.
Uint8List aplicarFiltroBytes((Uint8List, FiltroFoto) args) {
  final (bytes, filtro) = args;
  final decodificada = img.decodeImage(bytes);
  if (decodificada == null) {
    throw const FormatException('La imagen no es válida');
  }
  final imagen = img.bakeOrientation(decodificada);

  if (filtro != FiltroFoto.normal) {
    final m = filtro.matriz;
    for (final p in imagen) {
      final r = p.r.toDouble();
      final g = p.g.toDouble();
      final b = p.b.toDouble();
      p.r = (m[0] * r + m[1] * g + m[2] * b + m[4]).clamp(0, 255);
      p.g = (m[5] * r + m[6] * g + m[7] * b + m[9]).clamp(0, 255);
      p.b = (m[10] * r + m[11] * g + m[12] * b + m[14]).clamp(0, 255);
    }
  }
  return img.encodeJpg(imagen, quality: 90);
}