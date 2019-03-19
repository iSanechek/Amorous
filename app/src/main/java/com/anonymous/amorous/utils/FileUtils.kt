package com.anonymous.amorous.utils

import android.content.Context
import com.anonymous.amorous.debug.logDebug
import com.anonymous.amorous.empty
import java.io.File
import java.text.DecimalFormat

interface FileUtils {
    fun checkTempFolder(context: Context): Boolean
    fun getRootTempFolder(context: Context): String
    fun copyToTempFolder(context: Context, originalPath: String): String
    fun getFileType(fileName: String): String
    fun parseFilenameFromPath(filePath: String): String
    fun getReadableFileSize(size: Long): String
    fun getFileSizeFromPath(pathFile: String): String
    fun removeFile(path: String): Boolean
    fun getAllFilesFromCache(context: Context): Array<File>
}

class FileUtilsImpl : FileUtils {

    override fun getAllFilesFromCache(context: Context): Array<File> {
        val cache = File(getTempFolderPath(context) + File.separator)
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

    override fun parseFilenameFromPath(filePath: String): String {
        val index = filePath.lastIndexOf('/') + 1
        return filePath.substring(index)
    }

    override fun getFileType(fileName: String): String = when {
                fileName.endsWith(".jpg", ignoreCase = true) -> "image"
                fileName.endsWith(".mp4", ignoreCase = true) -> "video"
                else -> "unknown"
            }

    override fun copyToTempFolder(context: Context, originalPath: String): String {
        try {
            val distFile = File(getTempFolderPath(context), originalPath.substring(originalPath.lastIndexOf("/") + 1))
            File(originalPath).copyTo(target = distFile, overwrite = true)
            return distFile.absolutePath

        } catch (e: Exception) {
            logDebug {
                "copyToTempFolder error ${e.message}"
            }
        }
        return String.empty()
    }

    override fun getRootTempFolder(context: Context): String = getRootTempFolder(context)

    override fun checkTempFolder(context: Context): Boolean {
        var isOk: Boolean
        val dir = File(getTempFolderPath(context))
        isOk = dir.exists()
        when {
            !isOk -> isOk = dir.mkdirs()
        }
        val nomedia = File(getTempFolderPath(context) + File.separator + ".nomedia")
        when {
            !nomedia.exists() -> nomedia.mkdirs()
        }
        return isOk
    }

    private fun getTempFolderPath(context: Context): String = context.filesDir.absolutePath + File.separator + CACHE_FOLDER_NAME

    companion object {
        const val CACHE_FOLDER_NAME = "cachefiles"
    }
}