package com.wrbug.developerhelper.commonutil

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtils {
    fun zip(src: File, outFile: File) {
        //提供了一个数据项压缩成一个ZIP归档输出流
        var out: ZipOutputStream? = null
        try {
            out = ZipOutputStream(FileOutputStream(outFile))
            //如果此文件是一个文件，否则为false。
            if (src.isFile) {
                zipFileOrDirectory(out, src, "")
            } else {
                //返回一个文件或空阵列。
                val entries = src.listFiles()
                for (i in entries!!.indices) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], "")
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            //关闭输出流
            if (out != null) {
                try {
                    out.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

            }
        }
    }

    fun zip(src: File): File? {
        val outFile = File(src.parent, src.name + ".zip")//源文件或者目录
        zip(src, outFile)
        return outFile
    }

    @Throws(IOException::class)
    private fun zipFileOrDirectory(
        out: ZipOutputStream,
        fileOrDirectory: File, curPath: String
    ) {
        //从文件中读取字节的输入流
        var fileInputStream: FileInputStream? = null
        try {
            //如果此文件是一个目录，否则返回false。
            if (!fileOrDirectory.isDirectory) {
                // 压缩文件
                val buffer = ByteArray(4096)
                fileInputStream = FileInputStream(fileOrDirectory)
                //实例代表一个条目内的ZIP归档
                val entry = ZipEntry(curPath + fileOrDirectory.name)
                //条目的信息写入底层流
                out.putNextEntry(entry)
                var bytesRead: Int = fileInputStream.read(buffer)
                while (bytesRead != -1) {
                    out.write(buffer, 0, bytesRead)
                    bytesRead = fileInputStream.read(buffer)
                }
                out.closeEntry()
            } else {
                // 压缩目录
                val entries = fileOrDirectory.listFiles()
                for (i in entries!!.indices) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(
                        out, entries[i], curPath
                                + fileOrDirectory.name + "/"
                    )
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            // throw ex;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

            }
        }
    }

    @Throws(IOException::class)
    fun unzip(zipFileName: String, outputDirectory: String) {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(zipFileName)
            val e = zipFile.entries()
            var zipEntry: ZipEntry? = null
            val dest = File(outputDirectory)
            dest.mkdirs()
            while (e.hasMoreElements()) {
                zipEntry = e.nextElement() as ZipEntry
                val entryName = zipEntry.name
                var inputStream: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    if (zipEntry.isDirectory) {
                        var name = zipEntry.name
                        name = name.substring(0, name.length - 1)
                        val f = File(
                            outputDirectory + File.separator
                                    + name
                        )
                        f.mkdirs()
                    } else {
                        var index = entryName.lastIndexOf("\\")
                        if (index != -1) {
                            val df = File(
                                outputDirectory + File.separator
                                        + entryName.substring(0, index)
                            )
                            df.mkdirs()
                        }
                        index = entryName.lastIndexOf("/")
                        if (index != -1) {
                            val df = File(
                                outputDirectory + File.separator
                                        + entryName.substring(0, index)
                            )
                            df.mkdirs()
                        }
                        val f = File(
                            outputDirectory + File.separator
                                    + zipEntry.name
                        )
                        // f.createNewFile();
                        inputStream = zipFile.getInputStream(zipEntry)
                        out = FileOutputStream(f)
                        val by = ByteArray(1024)
                        var c: Int = inputStream?.read(by) ?: -1
                        while (c != -1) {
                            out.write(by, 0, c)
                            c = inputStream?.read(by) ?: -1
                        }
                        out.flush()
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw IOException("解压失败：" + ex.toString())
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close()
                        } catch (ex: IOException) {
                        }

                    }
                    if (out != null) {
                        try {
                            out.close()
                        } catch (ex: IOException) {
                        }

                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            throw IOException("解压失败：" + ex.toString())
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (ex: IOException) {
                }

            }
        }
    }
}


fun File.zip(): File? {
    return ZipUtils.zip(this)
}


fun File.zip(outFile: File) {
    return ZipUtils.zip(this, outFile)
}