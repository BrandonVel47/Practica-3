import 'dart:io';
import 'dart:typed_data';

import '../../domain/entities/media_item.dart';
import '../../domain/repositories/media_repository.dart';
import '../datasources/file_storage.dart';
import '../datasources/media_database.dart';

/// Implementación (capa de datos): archivos en disco + metadatos en SQLite.
class MediaRepositoryImpl implements MediaRepository {
  MediaRepositoryImpl(this._db, this._storage);

  final MediaDatabase _db;
  final FileStorage _storage;

  @override
  Future<List<MediaItem>> obtenerTodos() async {
    final filas = await _db.todos();
    return filas.map(_deFila).toList();
  }

  @override
  Future<MediaItem> guardarFoto(Uint8List bytesJpg) async {
    final nombre = 'foto_${DateTime.now().millisecondsSinceEpoch}.jpg';
    final ruta = await _storage.guardarBytes('fotos', nombre, bytesJpg);
    final item = MediaItem(tipo: TipoMedia.foto, ruta: ruta, fecha: DateTime.now());
    final id = await _db.insertar(_aFila(item));
    return item.copyWith(id: id);
  }

  @override
  Future<void> eliminar(MediaItem item) async {
    await _storage.eliminar(item.ruta);
    if (item.id != null) await _db.eliminar(item.id!);
  }

  @override
  File archivoDe(MediaItem item) => _storage.resolver(item.ruta);

  // ---------- Conversión entidad <-> fila de la base ----------

  Map<String, Object?> _aFila(MediaItem i) => {
    'tipo': i.tipo.name,
    'ruta': i.ruta,
    'fecha': i.fecha.millisecondsSinceEpoch,
    'latitud': i.latitud,
    'longitud': i.longitud,
    'etiquetas': i.etiquetas.join(','),
    'album': i.album,
    'duracion_ms': i.duracion?.inMilliseconds,
  };

  MediaItem _deFila(Map<String, Object?> f) => MediaItem(
    id: f['id'] as int,
    tipo: TipoMedia.values.byName(f['tipo'] as String),
    ruta: f['ruta'] as String,
    fecha: DateTime.fromMillisecondsSinceEpoch(f['fecha'] as int),
    latitud: (f['latitud'] as num?)?.toDouble(),
    longitud: (f['longitud'] as num?)?.toDouble(),
    etiquetas: ((f['etiquetas'] as String?) ?? '')
        .split(',')
        .where((e) => e.isNotEmpty)
        .toList(),
    album: f['album'] as String?,
    duracion: f['duracion_ms'] == null
        ? null
        : Duration(milliseconds: f['duracion_ms'] as int),
  );
}