package com.yoshione.fingen.interfaces

import android.content.Context

interface InterfaceGetData {
    fun getData(context: Context, table: String, key: String): String?
}