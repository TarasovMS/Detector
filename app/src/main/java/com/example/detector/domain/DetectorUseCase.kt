package com.example.detector.domain

import android.graphics.Bitmap
import android.net.Uri
import arrow.core.Either
import javax.inject.Inject

class DetectorUseCase @Inject constructor(
    private val detectorRepository: DetectorRepository,
) {

    fun createTempFilesForPhotos() {
        detectorRepository.createTempFilesForPhotos()
    }

    suspend fun getUriForPhoto(): Either<Nothing, Uri> {
        return detectorRepository.getUriForPhoto()
    }

    suspend fun getImageFromGalleryAndPost(uri: Uri): Either<Error, Bitmap> {
        return detectorRepository.getImageFromGalleryAndPost(uri = uri)
    }

    suspend fun addImageToGalleryAndPost(): Either<Error, Bitmap> {
        return detectorRepository.addImageToGalleryAndPost()
    }
}
