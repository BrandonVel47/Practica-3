import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

/// Fuente de datos: base SQLite con los metadatos de fotos y audios.
class MediaDatabase {
  MediaDatabase._(this._db);

  final Database _db;

  static const tabla = 'media';

  static Future<MediaDatabase> abrir() async {
    final ruta = p.join(await getDatabasesPath(), 'media.db');
    final db = await openDatabase(
      ruta,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE $tabla (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo        TEXT    NOT NULL,
            ruta        TEXT    NOT NULL,
            fecha       INTEGER NOT NULL,
            latitud     REAL,
            longitud    REAL,
            etiquetas   TEXT    NOT NULL DEFAULT '',
            album       TEXT,
            duracion_ms INTEGER
          )
        ''');
      },
    );
    return MediaDatabase._(db);
  }

  Future<int> insertar(Map<String, Object?> fila) => _db.insert(tabla, fila);

  Future<List<Map<String, Object?>>> todos() => _db.query(tabla, orderBy: 'fecha DESC');

  Future<int> actualizar(int id, Map<String, Object?> fila) =>
      _db.update(tabla, fila, where: 'id = ?', whereArgs: [id]);

  Future<int> eliminar(int id) => _db.delete(tabla, where: 'id = ?', whereArgs: [id]);
}