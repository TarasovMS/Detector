package com.example.detector.data.repository

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import arrow.core.Either
import com.example.detector.common.INT_0
import com.example.detector.common.contextProvider.ResourceProviderContext
import com.example.detector.domain.DetectorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class DetectorRepositoryImpl @Inject constructor(
    private val resourceProviderContext: ResourceProviderContext
) : DetectorRepository {

    private val internalDir by lazy {
        resourceProviderContext.getInternalFolder(PHOTOS_FOLDER_NAME)
    }

    private val tempPhotoFiles by lazy {
        File(internalDir, PHOTO_URI + IMAGE_EXTENSION)
    }

    override suspend fun getUriForPhoto(): Either<Nothing, Uri> {
        return Either.Right(
            resourceProviderContext.getFileUri(tempPhotoFiles)
        )
    }

    override suspend fun addImageToGalleryAndPost(): Either<Error, Bitmap> {
        val uri = resourceProviderContext.contentResolverInsert(
            uri = Media.EXTERNAL_CONTENT_URI,
            contentValues = createContentValues(),
        ) ?: run {
            Uri.EMPTY
        }

        withContext(Dispatchers.IO) {
            FileInputStream(tempPhotoFiles.path).use { input ->
                resourceProviderContext.contentResolverOpenOutputStream(uri).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val numBytesRead = input.read(buffer)
                        if (numBytesRead <= INT_0)
                            break

                        output?.write(buffer, INT_0, numBytesRead)
                    }
                }
            }
        }

        return sendImage(uri)
    }

    override suspend fun getImageFromGalleryAndPost(uri: Uri): Either<Error, Bitmap> {
        return sendImage(uri)
    }

    override fun createTempFilesForPhotos() {
        if (!internalDir.exists())
            internalDir.mkdirs()

        tempPhotoFiles.run {
            if (!exists())
                createNewFile()
        }
    }

    private fun createContentValues(): ContentValues {
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, IMAGE_NAME_PART + System.currentTimeMillis())
            put(MediaStore.MediaColumns.MIME_TYPE, IMAGE_JPEG)
        }
    }

    private suspend fun sendImage(uri: Uri): Either<Error, Bitmap> {

        val bitmap = if (Build.VERSION.SDK_INT < P)
            Media.getBitmap(resourceProviderContext.getContentResolver(), uri)
        else {
            val source =
                ImageDecoder.createSource(resourceProviderContext.getContentResolver(), uri)
            ImageDecoder.decodeBitmap(source)
        }

        return Either.Right(bitmap)
    }

    companion object {
        const val PHOTO_URI = "Photo"
        const val PHOTOS_FOLDER_NAME = "photos"
        const val BUFFER_SIZE = 1024
        const val IMAGE_NAME_PART = "image_"
        const val IMAGE_JPEG = "image/jpeg"
        const val IMAGE_EXTENSION = ".jpeg"
    }
}
