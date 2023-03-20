package com.example.detector.domain

import android.graphics.Bitmap
import android.net.Uri
import arrow.core.Either

interface DetectorRepository {

    fun createTempFilesForPhotos()

    suspend fun getUriForPhoto(): Either<Nothing, Uri>

    suspend fun addImageToGalleryAndPost(): Either<Error, Bitmap>

    suspend fun getImageFromGalleryAndPost(uri: Uri): Either<Error, Bitmap>

}
