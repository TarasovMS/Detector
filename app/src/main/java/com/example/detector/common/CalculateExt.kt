package com.example.detector.common

fun translateX(x: Float): Float {
//    return this.width - scaleX(x.toFloat())  //зеркально отображение
    return scaleX(x)
}

fun translateY(y: Float): Float {
    return scaleY(y)
}

fun scaleX(x: Float): Float {
    return x * 1.0f
}

fun scaleY(y: Float): Float {
    return y * 1.0f
}
