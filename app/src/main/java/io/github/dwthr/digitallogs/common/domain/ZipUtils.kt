package io.github.dwthr.digitallogs.common.domain

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun ZipOutputStream.useEntry(entry: ZipEntry, mutateStream: (ZipOutputStream) -> Unit) {
	this.putNextEntry(entry)
	mutateStream(this)
	this.closeEntry()
}