package com.anonymous.amorous.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
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
    fun getLongSizeFromPath(pathFile: String): Long
    fun removeFile(path: String): Boolean
    fun getAllFilesFromCacheFolder(context: Context): Array<File>
    fun checkCacheFolderIsEmpty(context: Context): Boolean
    fun checkFileExists(path: String): Boolean
    fun clearCacheFolder(context: Context): Boolean
    fun getCacheFolderSize(context: Context): Long
    fun getTotalFreeSpace(): Long
    fun getTotalSpaceSize(): Long
    fun getCacheFilesSize(context: Context): Long

}

class FileUtilsImpl(private val tracker: TrackingUtils) : FileUtils {

    override fun getCacheFilesSize(context: Context): Long = if (checkCreatedCacheFolder(context)) File(getCacheFolderPath(context)).listFiles().size.toLong() else 0L

    override fun getTotalSpaceSize(): Long {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        return stat.blockSizeLong * stat.blockCountLong
    }

    override fun getLongSizeFromPath(pathFile: String): Long = File(pathFile).length()

    override fun getTotalFreeSpace(): Long {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        return stat.blockSizeLong * stat.availableBlocksLong
    }

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
        return try {
            addEvent("Copy file! Original path: $originalPath")
            val distFile = File(getCacheFolderPath(context), originalPath.substring(originalPath.lastIndexOf("/") + 1))
            File(originalPath).copyTo(target = distFile, overwrite = true)
            val resultPath = distFile.absolutePath
            addEvent("Copy file is done! Copy file path: $resultPath")
            resultPath
        } catch (e: Exception) {
            addEvent("Copy file error! ${e.message}")
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

    private fun addEvent(msg: String) {
        tracker.sendEvent(TAG, msg)
    }

    companion object {
        const val CACHE_FOLDER_NAME = "cachefiles"
        private const val TAG = "FileUtils"
    }
}