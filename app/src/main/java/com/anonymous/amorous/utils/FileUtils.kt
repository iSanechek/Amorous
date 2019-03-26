package com.anonymous.amorous.utils

import android.content.Context
import com.anonymous.amorous.empty
import java.io.File
import java.text.DecimalFormat

interface FileUtils {
    fun checkCreatedCacheFolder(context: Context): Boolean
    fun getRootCacheFolder(context: Context): String
    fun copyToCacheFolder(context: Context, originalPath: String): String
    fun getFileType(fileName: String): String
    fun parseFileNameFromPath(filePath: String): String
    fun getReadableFileSize(size: Long): String
    fun getFileSizeFromPath(pathFile: String): String
    fun removeFile(path: String): Boolean
    fun getAllFilesFromCacheFolder(context: Context): Array<File>
    fun checkCacheFolderIsEmpty(context: Context): Boolean
    fun checkFileExists(path: String): Boolean
    fun clearCacheFolder(context: Context): Boolean
    fun getCacheFolderSize(context: Context): Long
    fun getTotalFreeSpace(context: Context): Long
}

class FileUtilsImpl(private val tracker: TrackingUtils) : FileUtils {

    override fun getTotalFreeSpace(context: Context): Long = File(getCacheFolderPath(context)).usableSpace

    override fun getCacheFolderSize(context: Context): Long = getFolderSize(File(getCacheFolderPath(context)))

    override fun clearCacheFolder(context: Context): Boolean = File(getCacheFolderPath(context)).deleteRecursively()

    override fun checkCacheFolderIsEmpty(context: Context): Boolean {
        var empty = false
        if (checkCreatedCacheFolder(context)) {
            empty = File(getCacheFolderPath(context)).usableSpace == 0L
        }
        return empty
    }

    override fun checkFileExists(path: String): Boolean {
        if (path.isEmpty()) return false
        val file = File(path)
        var exists = file.exists()
        if (exists) {
            if (file.length() == 0L) {
                removeFile(path)
                exists = false
            }
        }
        return exists
    }

    override fun getAllFilesFromCacheFolder(context: Context): Array<File> {
        val cache = File(getCacheFolderPath(context) + File.separator)
        return if (cache.isDirectory) cache.listFiles() else emptyArray()
    }

    override fun removeFile(path: String): Boolean {
        var result = false
        val file = File(path)
        if (file.exists()) {
            result = file.delete()
        }
        return result
    }

    override fun getFileSizeFromPath(pathFile: String): String = getReadableFileSize(File(pathFile).length())

    override fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    override fun parseFileNameFromPath(filePath: String): String = File(filePath).name

    override fun getFileType(fileName: String): String = when {
                fileName.endsWith(".jpg", ignoreCase = true) -> "image"
                fileName.endsWith(".mp4", ignoreCase = true) -> "video"
                else -> "unknown"
            }


    override fun copyToCacheFolder(context: Context, originalPath: String): String {
        val events = hashSetOf<String>()
        return try {
            events.add("Copy file! Original path: $originalPath")
            val distFile = File(getCacheFolderPath(context), originalPath.substring(originalPath.lastIndexOf("/") + 1))
            File(originalPath).copyTo(target = distFile, overwrite = true)
            val resultPath = distFile.absolutePath
            events.add("Copy file is done! Copy file path: $resultPath")
            tracker.sendEvent(TAG, events)
            resultPath
        } catch (e: Exception) {
            events.add("Copy file error! ${e.message}")
            tracker.sendEvent(TAG, events)
            String.empty()
        }
    }

    override fun getRootCacheFolder(context: Context): String = getRootCacheFolder(context)

    override fun checkCreatedCacheFolder(context: Context): Boolean {
        var isOk: Boolean
        val dir = File(getCacheFolderPath(context))
        isOk = dir.exists()
        when {
            !isOk -> isOk = dir.mkdirs()
        }
        val nomedia = File(getCacheFolderPath(context) + File.separator + ".nomedia")
        when {
            !nomedia.exists() -> nomedia.mkdirs()
        }
        return isOk
    }

    private fun getCacheFolderPath(context: Context): String = context.filesDir.absolutePath + File.separator + CACHE_FOLDER_NAME

    private fun getFolderSize(file: File): Long = when {
        file.listFiles() != null -> file
                .listFiles()
                .asSequence()
                .map {
                    when {
                        it.isFile -> it.length()
                        else -> getFolderSize(it)
                    }
                }.sum()
        else -> 0L
    }

    companion object {
        const val CACHE_FOLDER_NAME = "cachefiles"
        private const val TAG = "FileUtils"
    }
}