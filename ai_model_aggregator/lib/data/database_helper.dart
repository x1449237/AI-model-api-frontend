import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/conversation.dart';
import '../models/message.dart';

class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._init();
  static Database? _database;

  DatabaseHelper._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('ai_aggregator.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);
    return await openDatabase(path, version: 1, onCreate: _createDB);
  }

  Future<void> _createDB(Database db, int version) async {
    await db.execute('''
      CREATE TABLE conversations (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        modelId TEXT NOT NULL,
        modelName TEXT NOT NULL,
        vendorName TEXT NOT NULL,
        type TEXT NOT NULL,
        createdAt TEXT NOT NULL,
        updatedAt TEXT NOT NULL,
        clusterModelIds TEXT DEFAULT ''
      )
    ''');
    await db.execute('''
      CREATE TABLE messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        conversationId TEXT NOT NULL,
        role TEXT NOT NULL,
        content TEXT NOT NULL,
        modelId TEXT NOT NULL,
        modelName TEXT NOT NULL,
        vendorName TEXT NOT NULL,
        timestamp TEXT NOT NULL,
        responseTimeMs INTEGER DEFAULT 0,
        FOREIGN KEY (conversationId) REFERENCES conversations(id) ON DELETE CASCADE
      )
    ''');
    await db.execute('''
      CREATE TABLE cluster_results (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        clusterSessionId TEXT NOT NULL,
        modelId TEXT NOT NULL,
        modelName TEXT NOT NULL,
        vendorName TEXT NOT NULL,
        content TEXT NOT NULL,
        responseTimeMs INTEGER DEFAULT 0,
        timestamp TEXT NOT NULL
      )
    ''');
  }

  // Conversation CRUD
  Future<int> insertConversation(Conversation conv) async {
    final db = await database;
    return await db.insert('conversations', conv.toMap());
  }

  Future<List<Conversation>> getConversations({String? type}) async {
    final db = await database;
    final where = type != null ? 'type = ?' : null;
    final whereArgs = type != null ? [type] : null;
    final maps = await db.query('conversations',
        where: where, whereArgs: whereArgs, orderBy: 'updatedAt DESC');
    return maps.map((map) => Conversation.fromMap(map)).toList();
  }

  Future<int> deleteConversation(String id) async {
    final db = await database;
    await db.delete('messages', where: 'conversationId = ?', whereArgs: [id]);
    return await db.delete('conversations', where: 'id = ?', whereArgs: [id]);
  }

  Future<void> updateConversationTime(String id, DateTime time) async {
    final db = await database;
    await db.update('conversations', {'updatedAt': time.toIso8601String()},
        where: 'id = ?', whereArgs: [id]);
  }

  // Message CRUD
  Future<int> insertMessage(Message msg) async {
    final db = await database;
    return await db.insert('messages', msg.toMap());
  }

  Future<List<Message>> getMessages(String conversationId) async {
    final db = await database;
    final maps = await db.query('messages',
        where: 'conversationId = ?',
        whereArgs: [conversationId],
        orderBy: 'timestamp ASC');
    return maps.map((map) => Message.fromMap(map)).toList();
  }

  // Cluster results
  Future<void> insertClusterResult({
    required String clusterSessionId,
    required String modelId,
    required String modelName,
    required String vendorName,
    required String content,
    required int responseTimeMs,
    required DateTime timestamp,
  }) async {
    final db = await database;
    await db.insert('cluster_results', {
      'clusterSessionId': clusterSessionId,
      'modelId': modelId,
      'modelName': modelName,
      'vendorName': vendorName,
      'content': content,
      'responseTimeMs': responseTimeMs,
      'timestamp': timestamp.toIso8601String(),
    });
  }

  Future<List<Map<String, dynamic>>> getClusterResults(String sessionId) async {
    final db = await database;
    return await db.query('cluster_results',
        where: 'clusterSessionId = ?',
        whereArgs: [sessionId],
        orderBy: 'responseTimeMs ASC');
  }

  Future<void> close() async {
    final db = await database;
    db.close();
    _database = null;
  }
}