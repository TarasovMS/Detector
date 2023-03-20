package com.example.detector.common

data class ContentError(val throwable: Throwable = Throwable()) : Error()
