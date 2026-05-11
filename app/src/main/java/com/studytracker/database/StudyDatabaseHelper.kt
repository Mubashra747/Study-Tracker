package com.studytracker.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.studytracker.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyDatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, StudyDatabaseContract.DATABASE_NAME, null, StudyDatabaseContract.DATABASE_VERSION) {

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabaseHelper? = null

        fun getInstance(context: Context): StudyDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StudyDatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(StudyDatabaseContract.UsersTable.CREATE_TABLE)
        db.execSQL(StudyDatabaseContract.SubjectsTable.CREATE_TABLE)
        db.execSQL(StudyDatabaseContract.StudySessionsTable.CREATE_TABLE)
        db.execSQL(StudyDatabaseContract.StudySessionsTable.CREATE_INDEX_SUBJECT)
        db.execSQL(StudyDatabaseContract.StudySessionsTable.CREATE_INDEX_DATE)
        db.execSQL(StudyDatabaseContract.StudySessionsTable.CREATE_INDEX_USER_SESSIONS)
        db.execSQL(StudyDatabaseContract.StudySessionsTable.CREATE_INDEX_USER_SUBJECTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${StudyDatabaseContract.SubjectsTable.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${StudyDatabaseContract.UsersTable.TABLE_NAME}")
            onCreate(db)
        } else if (oldVersion < 3) {
            db.execSQL("ALTER TABLE ${StudyDatabaseContract.SubjectsTable.TABLE_NAME} ADD COLUMN ${StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS} INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE ${StudyDatabaseContract.SubjectsTable.TABLE_NAME} ADD COLUMN ${StudyDatabaseContract.SubjectsTable.COL_LAST_MODIFIED} INTEGER DEFAULT 0")
            
            db.execSQL("ALTER TABLE ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME} ADD COLUMN ${StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS} INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME} ADD COLUMN ${StudyDatabaseContract.StudySessionsTable.COL_LAST_MODIFIED} INTEGER DEFAULT 0")
        }
    }

    // --- CRUD Operations for Users ---

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(StudyDatabaseContract.UsersTable.COL_NAME, user.name)
            put(StudyDatabaseContract.UsersTable.COL_EMAIL, user.email)
            put(StudyDatabaseContract.UsersTable.COL_LEVEL, user.level)
            put(StudyDatabaseContract.UsersTable.COL_STREAK, user.streak)
        }
        db.insert(StudyDatabaseContract.UsersTable.TABLE_NAME, null, values)
    }

    suspend fun getUserById(userId: Long): User? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            StudyDatabaseContract.UsersTable.TABLE_NAME,
            null,
            "${StudyDatabaseContract.UsersTable.COL_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return@withContext User(
                    id = it.getLong(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_NAME)),
                    email = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_EMAIL)),
                    level = it.getInt(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_LEVEL)),
                    streak = it.getInt(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_STREAK)),
                    createdAt = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.UsersTable.COL_CREATED_AT))
                )
            }
        }
        null
    }

    // --- CRUD Operations for Subjects ---

    suspend fun insertSubject(subject: Subject, userId: Long): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(StudyDatabaseContract.SubjectsTable.COL_USER_ID, userId)
            put(StudyDatabaseContract.SubjectsTable.COL_NAME, subject.name)
            put(StudyDatabaseContract.SubjectsTable.COL_EMOJI, subject.emoji)
            put(StudyDatabaseContract.SubjectsTable.COL_COLOR, subject.color)
            put(StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS, subject.syncStatus)
            put(StudyDatabaseContract.SubjectsTable.COL_LAST_MODIFIED, subject.lastModified)
        }
        db.insertWithOnConflict(StudyDatabaseContract.SubjectsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun getSubjectsByUser(userId: Long): List<Subject> = withContext(Dispatchers.IO) {
        val subjects = mutableListOf<Subject>()
        val db = readableDatabase
        val cursor = db.query(
            StudyDatabaseContract.SubjectsTable.TABLE_NAME,
            null,
            "${StudyDatabaseContract.SubjectsTable.COL_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                subjects.add(parseSubject(it))
            }
        }
        subjects
    }

    suspend fun getSubjectById(subjectId: Long): Subject? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            StudyDatabaseContract.SubjectsTable.TABLE_NAME,
            null,
            "${StudyDatabaseContract.SubjectsTable.COL_ID} = ?",
            arrayOf(subjectId.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return@withContext parseSubject(it)
            }
        }
        null
    }

    private fun parseSubject(cursor: android.database.Cursor): Subject {
        return Subject(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_NAME)),
            emoji = cursor.getString(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_EMOJI)),
            color = cursor.getString(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_COLOR)),
            syncStatus = cursor.getInt(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS)),
            lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_LAST_MODIFIED))
        )
    }

    suspend fun updateSubject(subject: Subject): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(StudyDatabaseContract.SubjectsTable.COL_NAME, subject.name)
            put(StudyDatabaseContract.SubjectsTable.COL_EMOJI, subject.emoji)
            put(StudyDatabaseContract.SubjectsTable.COL_COLOR, subject.color)
            put(StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS, 0)
            put(StudyDatabaseContract.SubjectsTable.COL_LAST_MODIFIED, System.currentTimeMillis())
        }
        val rows = db.update(
            StudyDatabaseContract.SubjectsTable.TABLE_NAME,
            values,
            "${StudyDatabaseContract.SubjectsTable.COL_ID} = ?",
            arrayOf(subject.id.toString())
        )
        rows > 0
    }

    suspend fun deleteSubject(subjectId: Long): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rows = db.delete(
            StudyDatabaseContract.SubjectsTable.TABLE_NAME,
            "${StudyDatabaseContract.SubjectsTable.COL_ID} = ?",
            arrayOf(subjectId.toString())
        )
        rows > 0
    }

    // --- CRUD Operations for StudySessions ---

    suspend fun insertStudySession(session: StudySession, userId: Long): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(StudyDatabaseContract.StudySessionsTable.COL_USER_ID, userId)
            put(StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID, session.subjectId)
            put(StudyDatabaseContract.StudySessionsTable.COL_DATE, session.date)
            put(StudyDatabaseContract.StudySessionsTable.COL_DURATION, session.duration)
            put(StudyDatabaseContract.StudySessionsTable.COL_SCORE, session.score)
            put(StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS, session.syncStatus)
            put(StudyDatabaseContract.StudySessionsTable.COL_LAST_MODIFIED, session.lastModified)
        }
        db.insertWithOnConflict(StudyDatabaseContract.StudySessionsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun getSessionsByUser(userId: Long): List<StudySessionWithSubject> = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val query = """
            SELECT s.*, sub.${StudyDatabaseContract.SubjectsTable.COL_NAME} AS subjectName, 
                   sub.${StudyDatabaseContract.SubjectsTable.COL_EMOJI}, 
                   sub.${StudyDatabaseContract.SubjectsTable.COL_COLOR}
            FROM ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME} s
            JOIN ${StudyDatabaseContract.SubjectsTable.TABLE_NAME} sub
            ON s.${StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID} = sub.${StudyDatabaseContract.SubjectsTable.COL_ID}
            WHERE s.${StudyDatabaseContract.StudySessionsTable.COL_USER_ID} = ?
            ORDER BY s.${StudyDatabaseContract.StudySessionsTable.COL_DATE} DESC
        """
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val sessions = mutableListOf<StudySessionWithSubject>()
        cursor.use {
            while (it.moveToNext()) {
                val session = parseSession(it)
                val subjectName = it.getString(it.getColumnIndexOrThrow("subjectName"))
                val emoji = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_EMOJI))
                val color = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_COLOR))
                sessions.add(StudySessionWithSubject(session, subjectName, emoji, color))
            }
        }
        sessions
    }

    private fun parseSession(cursor: android.database.Cursor): StudySession {
        return StudySession(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_ID)),
            subjectId = cursor.getLong(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_DATE)),
            duration = cursor.getInt(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_DURATION)),
            score = cursor.getInt(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_SCORE)),
            syncStatus = cursor.getInt(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS)),
            lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(StudyDatabaseContract.StudySessionsTable.COL_LAST_MODIFIED))
        )
    }

    suspend fun updateStudySession(session: StudySession): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(StudyDatabaseContract.StudySessionsTable.COL_DATE, session.date)
            put(StudyDatabaseContract.StudySessionsTable.COL_DURATION, session.duration)
            put(StudyDatabaseContract.StudySessionsTable.COL_SCORE, session.score)
            put(StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS, 0)
            put(StudyDatabaseContract.StudySessionsTable.COL_LAST_MODIFIED, System.currentTimeMillis())
        }
        val rowsAffected = db.update(
            StudyDatabaseContract.StudySessionsTable.TABLE_NAME,
            values,
            "${StudyDatabaseContract.StudySessionsTable.COL_ID} = ?",
            arrayOf(session.id.toString())
        )
        rowsAffected > 0
    }

    suspend fun deleteStudySession(sessionId: Long): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            StudyDatabaseContract.StudySessionsTable.TABLE_NAME,
            "${StudyDatabaseContract.StudySessionsTable.COL_ID} = ?",
            arrayOf(sessionId.toString())
        )
        rowsDeleted > 0
    }

    suspend fun searchSessionsBySubjectName(userId: Long, subjectName: String): List<StudySessionWithSubject> = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val query = """
            SELECT s.*, sub.${StudyDatabaseContract.SubjectsTable.COL_NAME} AS subjectName, 
                   sub.${StudyDatabaseContract.SubjectsTable.COL_EMOJI}, 
                   sub.${StudyDatabaseContract.SubjectsTable.COL_COLOR}
            FROM ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME} s
            JOIN ${StudyDatabaseContract.SubjectsTable.TABLE_NAME} sub
            ON s.${StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID} = sub.${StudyDatabaseContract.SubjectsTable.COL_ID}
            WHERE s.${StudyDatabaseContract.StudySessionsTable.COL_USER_ID} = ? AND sub.${StudyDatabaseContract.SubjectsTable.COL_NAME} LIKE ?
            ORDER BY s.${StudyDatabaseContract.StudySessionsTable.COL_DATE} DESC
        """
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), "%$subjectName%"))
        val sessions = mutableListOf<StudySessionWithSubject>()
        cursor.use {
            while (it.moveToNext()) {
                val session = parseSession(it)
                val sName = it.getString(it.getColumnIndexOrThrow("subjectName"))
                val emoji = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_EMOJI))
                val color = it.getString(it.getColumnIndexOrThrow(StudyDatabaseContract.SubjectsTable.COL_COLOR))
                sessions.add(StudySessionWithSubject(session, sName, emoji, color))
            }
        }
        sessions
    }

    // --- Analytics ---

    suspend fun getSubjectStatistics(userId: Long): List<SubjectStats> = withContext(Dispatchers.IO) {
        val statsList = mutableListOf<SubjectStats>()
        val query = """
            SELECT sub.${StudyDatabaseContract.SubjectsTable.COL_NAME}, 
                   SUM(s.${StudyDatabaseContract.StudySessionsTable.COL_DURATION}) as total_time,
                   AVG(s.${StudyDatabaseContract.StudySessionsTable.COL_SCORE}) as avg_score,
                   sub.${StudyDatabaseContract.SubjectsTable.COL_EMOJI},
                   sub.${StudyDatabaseContract.SubjectsTable.COL_COLOR}
            FROM ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME} s
            JOIN ${StudyDatabaseContract.SubjectsTable.TABLE_NAME} sub 
              ON s.${StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID} = sub.${StudyDatabaseContract.SubjectsTable.COL_ID}
            WHERE s.${StudyDatabaseContract.StudySessionsTable.COL_USER_ID} = ?
            GROUP BY s.${StudyDatabaseContract.StudySessionsTable.COL_SUBJECT_ID}
            ORDER BY total_time DESC
        """
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                statsList.add(SubjectStats(
                    subjectName = it.getString(0),
                    totalMinutes = it.getInt(1),
                    averageScore = it.getDouble(2).toInt(),
                    emoji = it.getString(3),
                    color = it.getString(4)
                ))
            }
        }
        statsList
    }

    suspend fun getWeeklySummary(userId: Long): Map<String, Int> = withContext(Dispatchers.IO) {
        val summary = mutableMapOf<String, Int>()
        val query = """
            SELECT ${StudyDatabaseContract.StudySessionsTable.COL_DATE}, 
                   SUM(${StudyDatabaseContract.StudySessionsTable.COL_DURATION})
            FROM ${StudyDatabaseContract.StudySessionsTable.TABLE_NAME}
            WHERE ${StudyDatabaseContract.StudySessionsTable.COL_USER_ID} = ?
              AND ${StudyDatabaseContract.StudySessionsTable.COL_DATE} >= date('now', '-7 days')
            GROUP BY ${StudyDatabaseContract.StudySessionsTable.COL_DATE}
        """
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                summary[it.getString(0)] = it.getInt(1)
            }
        }
        summary
    }

    // --- Sync Operations ---

    suspend fun getUnsyncedSubjects(userId: Long): List<Subject> = withContext(Dispatchers.IO) {
        val subjects = mutableListOf<Subject>()
        val cursor = readableDatabase.query(
            StudyDatabaseContract.SubjectsTable.TABLE_NAME,
            null,
            "${StudyDatabaseContract.SubjectsTable.COL_USER_ID} = ? AND ${StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS} = 0",
            arrayOf(userId.toString()),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                subjects.add(parseSubject(it))
            }
        }
        subjects
    }

    suspend fun getUnsyncedSessions(userId: Long): List<StudySession> = withContext(Dispatchers.IO) {
        val sessions = mutableListOf<StudySession>()
        val cursor = readableDatabase.query(
            StudyDatabaseContract.StudySessionsTable.TABLE_NAME,
            null,
            "${StudyDatabaseContract.StudySessionsTable.COL_USER_ID} = ? AND ${StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS} = 0",
            arrayOf(userId.toString()),
            null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                sessions.add(parseSession(it))
            }
        }
        sessions
    }

    suspend fun markSubjectSynced(subjectId: Long) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(StudyDatabaseContract.SubjectsTable.COL_SYNC_STATUS, 1)
        }
        writableDatabase.update(StudyDatabaseContract.SubjectsTable.TABLE_NAME, values, "${StudyDatabaseContract.SubjectsTable.COL_ID} = ?", arrayOf(subjectId.toString()))
    }
    
    suspend fun markSessionSynced(sessionId: Long) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(StudyDatabaseContract.StudySessionsTable.COL_SYNC_STATUS, 1)
        }
        writableDatabase.update(StudyDatabaseContract.StudySessionsTable.TABLE_NAME, values, "${StudyDatabaseContract.StudySessionsTable.COL_ID} = ?", arrayOf(sessionId.toString()))
    }
}
