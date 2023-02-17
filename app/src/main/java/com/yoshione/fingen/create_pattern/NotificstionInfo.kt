package com.yoshione.fingen.create_pattern

import android.os.Parcel
import android.os.Parcelable


class NotificationInfo(
    val notify_id: String?,
    val title: String?,
    val text: String?,
    val packageName: String?,
    var isActive: Boolean,
    var action: String?
) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(notify_id)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeString(packageName)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeString(action)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationInfo> {
        override fun createFromParcel(parcel: Parcel): NotificationInfo {
            return NotificationInfo(parcel)
        }

        override fun newArray(size: Int): Array<NotificationInfo?> {
            return arrayOfNulls(size)
        }
    }
}
