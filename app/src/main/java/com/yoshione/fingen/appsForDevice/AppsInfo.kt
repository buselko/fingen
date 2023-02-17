package com.yoshione.fingen.appsForDevice

import android.graphics.drawable.Drawable

class AppsInfo internal constructor(
    var name: String,
    var package_name: String,
    var icon: Drawable,
    var isActive: Boolean
)

