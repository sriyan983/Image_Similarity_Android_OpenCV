package com.augray.imsimsdk.viewmodel

import android.graphics.Bitmap

data class ImageViewModel(
    // on below line we are creating a
    // two variable one for course name
    // and other for course image.
    var fileName: String,
    var imgBitmap: Bitmap
)