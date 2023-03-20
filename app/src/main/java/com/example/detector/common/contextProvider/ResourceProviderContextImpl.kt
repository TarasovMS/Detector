package com.example.detector.common.contextProvider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ResourceProviderContextImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ResourceProviderContext {

    override fun getInternalFolder(innerFolder: String) = File(context.filesDir, innerFolder)

    override fun getContentResolver(): ContentResolver = context.contentResolver

    override fun approveUriPermission(uri: Uri) {
        getContentResolver().takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }

    override fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(context, getAuthority(context), file)
    }

    override fun contentResolverInsert(uri: Uri, contentValues: ContentValues): Uri? {
        return getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues,
        )
    }

    override fun contentResolverOpenOutputStream(uri: Uri) =
        getContentResolver().openOutputStream(uri)

    override fun getString(stringResource: Int): String {
        return context.getString(stringResource)
    }

    private fun getAuthority(context: Context): String {
        return "${context.packageName}.fileprovider"
    }
}
