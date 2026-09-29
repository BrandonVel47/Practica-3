import 'dart:io';
import 'dart:typed_data';

import '../entities/media_item.dart';

/// Contrato (capa de dominio) para guardar y consultar fotos y audios.
abstract class MediaRepository {
  /// Todos los elementos, del más reciente al más antiguo.
  Future<List<MediaItem>> obtenerTodos();

  /// Guarda una foto (bytes JPG) y registra sus metadatos.
  Future<MediaItem> guardarFoto(Uint8List bytesJpg);

  Future<void> eliminar(MediaItem item);

  /// Archivo real en el dispositivo para un elemento.
  File archivoDe(MediaItem item);
}