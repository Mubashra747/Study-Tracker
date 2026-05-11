package com.studytracker.database

object StudyDatabaseContract {
    const val DATABASE_NAME = "StudyTracker.db"
    const val DATABASE_VERSION = 3 // Incremented for sync columns

    object UsersTable {
        const val TABLE_NAME = "Users"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_EMAIL = "email"
        const val COL_LEVEL = "level"
        const val COL_STREAK = "streak"
        const val COL_CREATED_AT = "createdAt"

        const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_LEVEL INTEGER DEFAULT 1,
                $COL_STREAK INTEGER DEFAULT 0,
                $COL_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """
    }

    object SubjectsTable {
        const val TABLE_NAME = "Subjects"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_NAME = "name"
        const val COL_EMOJI = "emoji"
        const val COL_COLOR = "color"
        const val COL_SYNC_STATUS = "syncStatus"
        const val COL_LAST_MODIFIED = "lastModified"

        const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL DEFAULT 1,
                $COL_NAME TEXT NOT NULL,
                $COL_EMOJI TEXT,
                $COL_COLOR TEXT,
                $COL_SYNC_STATUS INTEGER DEFAULT 0,
                $COL_LAST_MODIFIED INTEGER DEFAULT 0,
                UNIQUE($COL_USER_ID, $COL_NAME),
                FOREIGN KEY ($COL_USER_ID) REFERENCES ${UsersTable.TABLE_NAME}(${UsersTable.COL_ID}) ON DELETE CASCADE
            )
        """
    }

    object StudySessionsTable {
        const val TABLE_NAME = "StudySessions"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_SUBJECT_ID = "subjectId"
        const val COL_DATE = "date"
        const val COL_DURATION = "duration"
        const val COL_SCORE = "score"
        const val COL_SYNC_STATUS = "syncStatus"
        const val COL_LAST_MODIFIED = "lastModified"

        const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL DEFAULT 1,
                $COL_SUBJECT_ID INTEGER NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_DURATION INTEGER NOT NULL,
                $COL_SCORE INTEGER,
                $COL_SYNC_STATUS INTEGER DEFAULT 0,
                $COL_LAST_MODIFIED INTEGER DEFAULT 0,
                FOREIGN KEY ($COL_USER_ID) REFERENCES ${UsersTable.TABLE_NAME}(${UsersTable.COL_ID}) ON DELETE CASCADE,
                FOREIGN KEY ($COL_SUBJECT_ID) REFERENCES ${SubjectsTable.TABLE_NAME}(${SubjectsTable.COL_ID}) ON DELETE CASCADE
            )
        """

        const val CREATE_INDEX_SUBJECT = "CREATE INDEX idx_${COL_SUBJECT_ID} ON $TABLE_NAME ($COL_SUBJECT_ID)"
        const val CREATE_INDEX_DATE = "CREATE INDEX idx_${COL_DATE} ON $TABLE_NAME ($COL_DATE)"
        const val CREATE_INDEX_USER_SESSIONS = "CREATE INDEX idx_sessions_user ON $TABLE_NAME ($COL_USER_ID)"
        const val CREATE_INDEX_USER_SUBJECTS = "CREATE INDEX idx_subjects_user ON ${SubjectsTable.TABLE_NAME} ($COL_USER_ID)"
    }
}
