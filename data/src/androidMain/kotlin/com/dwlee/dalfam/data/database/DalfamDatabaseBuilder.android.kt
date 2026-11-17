package com.dwlee.dalfam.data.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

internal fun dalfamDatabaseBuilder(context: Context): RoomDatabase.Builder<DalfamDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<DalfamDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(DATABASE_FILE_NAME).absolutePath,
    )
}
