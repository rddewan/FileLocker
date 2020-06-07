package com.richarddewan.filelocker.data

import java.io.File

data class SecureFileEntity(
    val fileName: String,
    val file: File,
    val fileSize: String
)