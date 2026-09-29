import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';

import '../../domain/entities/media_item.dart';
import '../../domain/repositories/media_repository.dart';

/// Estado de las fotos y audios guardados (capa de presentación).
class MediaProvider extends ChangeNotifier {
  MediaProvider(this._repo) {
    cargar();
  }

  final MediaRepository _repo;
  List<MediaItem> _items = [];

  List<MediaItem> get items => List.unmodifiable(_items);

  /// Archivo de la foto más reciente (para la miniatura de la cámara).
  File? get ultimaFoto {
    for (final item in _items) {
      if (item.tipo == TipoMedia.foto) return _repo.archivoDe(item);
    }
    return null;
  }

  File archivoDe(MediaItem item) => _repo.archivoDe(item);

  Future<void> cargar() async {
    _items = await _repo.obtenerTodos();
    notifyListeners();
  }

  Future<MediaItem> guardarFoto(Uint8List bytesJpg) async {
    final item = await _repo.guardarFoto(bytesJpg);
    _items.insert(0, item);
    notifyListeners();
    return item;
  }

  Future<void> eliminar(MediaItem item) async {
    await _repo.eliminar(item);
    _items.removeWhere((i) => i.id == item.id);
    notifyListeners();
  }
}