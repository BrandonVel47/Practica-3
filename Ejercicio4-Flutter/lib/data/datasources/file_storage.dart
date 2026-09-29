import 'dart:io';
import 'dart:typed_data';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

/// Fuente de datos: archivos dentro de la carpeta de documentos de la app
/// (sandbox privado en Android e iOS).
class FileStorage {
  FileStorage._(this._base);

  final Directory _base;

  static Future<FileStorage> crear() async {
    final dir = await getApplicationDocumentsDirectory();
    return FileStorage._(dir);
  }

  /// Convierte una ruta relativa (p. ej. "fotos/foto_1.jpg") en un archivo real.
  File resolver(String rutaRelativa) => File(p.join(_base.path, rutaRelativa));

  /// Guarda bytes en [carpeta]/[nombre] y devuelve la ruta relativa.
  Future<String> guardarBytes(String carpeta, String nombre, Uint8List bytes) async {
    final dir = Directory(p.join(_base.path, carpeta));
    await dir.create(recursive: true);
    final relativa = p.join(carpeta, nombre);
    await resolver(relativa).writeAsBytes(bytes, flush: true);
    return relativa;
  }

  Future<void> eliminar(String rutaRelativa) async {
    final archivo = resolver(rutaRelativa);
    if (await archivo.exists()) await archivo.delete();
  }
}