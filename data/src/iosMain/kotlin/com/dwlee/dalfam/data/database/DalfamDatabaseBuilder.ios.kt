package com.dwlee.dalfam.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** 사용자에게 보이지 않는 Application Support 디렉터리에 둔다(Documents는 파일 앱에 노출될 수 있다). */
@OptIn(ExperimentalForeignApi::class)
internal fun dalfamDatabaseBuilder(): RoomDatabase.Builder<DalfamDatabase> {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(directory?.path) { "Application Support 디렉터리를 찾을 수 없습니다." }
    return Room.databaseBuilder<DalfamDatabase>(name = "$path/$DATABASE_FILE_NAME")
}
