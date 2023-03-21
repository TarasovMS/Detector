package com.example.detector.common.contextProvider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import java.io.File
import java.io.OutputStream

interface ResourceProviderContext {

    fun getContext(): Context

    fun getInternalFolder(innerFolder: String): File

    fun approveUriPermission(uri: Uri)

    fun getContentResolver(): ContentResolver

    fun getFileUri(file: File): Uri

    fun contentResolverInsert(uri: Uri, contentValues: ContentValues): Uri?

    fun contentResolverOpenOutputStream(uri: Uri): OutputStream?

    fun getString(@StringRes stringResource: Int): String
}
