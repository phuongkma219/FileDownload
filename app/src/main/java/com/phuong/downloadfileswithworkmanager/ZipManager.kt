package com.phuong.downloadfileswithworkmanager

import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class ZipManager {
    private val BUFFER_SIZE = 6 * 1024
    fun zip(_files: Array<String?>, zipFileName: String?) {
        try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(BUFFER_SIZE)
            for (i in _files.indices) {
                Log.v("Compress", "Adding: " + _files[i])
                val fi = FileInputStream(_files[i])
                origin = BufferedInputStream(fi, BUFFER_SIZE)
                val entry = ZipEntry(_files[i]!!.substring(_files[i]!!.lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
     fun unpackZip(path: String, zipname: String): Boolean {
        val `is`: InputStream
        val zis: ZipInputStream
        try {
            `is` = FileInputStream(path + zipname)
            zis = ZipInputStream(`is`)
            var ze: ZipEntry
            while (zis.nextEntry.also { ze = it } != null) {
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var count: Int
                val filename = ze.name
                val fout = FileOutputStream(path  +filename)

                // reading and writing
                while (zis.read(buffer).also { count = it } != -1) {
                    baos.write(buffer, 0, count)
                    val bytes = baos.toByteArray()
                    fout.write(bytes)
                    baos.reset()
                }
                fout.close()
                zis.closeEntry()
            }
            zis.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }
    @Throws(IOException::class)
    fun newFile(destinationDir: File, zipEntry: ZipEntry): File? {
        val destFile = File(destinationDir, zipEntry.name)
        val destDirPath = destinationDir.canonicalPath
        val destFilePath = destFile.canonicalPath
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
        }
        return destFile
    }
}