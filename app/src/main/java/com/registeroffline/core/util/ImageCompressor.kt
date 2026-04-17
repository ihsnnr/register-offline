package com.registeroffline.core.util

import android.content.Context
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.size
import java.io.File

object ImageCompressor {
    suspend fun compress(context: Context, file: File): File {
        return Compressor.compress(context, file) {
            default(width = 1024, height = 1024)
            size(maxFileSize = 1_048_576) // 1MB
        }
    }
}
