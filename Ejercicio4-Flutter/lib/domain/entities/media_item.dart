/// Tipo de contenido capturado.
enum TipoMedia { foto, audio }

/// Entidad de dominio: una foto o grabación con sus metadatos.
class MediaItem {
  const MediaItem({
    this.id,
    required this.tipo,
    required this.ruta,
    required this.fecha,
    this.latitud,
    this.longitud,
    this.etiquetas = const [],
    this.album,
    this.duracion,
  });

  final int? id;
  final TipoMedia tipo;

  /// Ruta RELATIVA dentro de la carpeta de documentos de la app
  /// (en iOS la ruta absoluta cambia al reinstalar, por eso no se guarda completa).
  final String ruta;
  final DateTime fecha;
  final double? latitud;
  final double? longitud;
  final List<String> etiquetas;
  final String? album;

  /// Solo para audios.
  final Duration? duracion;

  MediaItem copyWith({
    int? id,
    List<String>? etiquetas,
    String? album,
    bool quitarAlbum = false,
    Duration? duracion,
  }) {
    return MediaItem(
      id: id ?? this.id,
      tipo: tipo,
      ruta: ruta,
      fecha: fecha,
      latitud: latitud,
      longitud: longitud,
      etiquetas: etiquetas ?? this.etiquetas,
      album: quitarAlbum ? null : (album ?? this.album),
      duracion: duracion ?? this.duracion,
    );
  }
}