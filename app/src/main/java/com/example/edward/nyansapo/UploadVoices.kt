package com.example.edward.nyansapo

import java.io.File


object UploadVoices {

    private fun fillFilesRecursively(file: File, resultFiles: MutableList<File>) {
        if (file.isFile()) {
            resultFiles.add(file)
        } else {
            for (child in file.listFiles()) {
                fillFilesRecursively(child, resultFiles)
            }
        }
    }
}