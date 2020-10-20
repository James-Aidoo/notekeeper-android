package com.example.notekeeper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ModuleInfo @JvmOverloads constructor(val moduleId: String?, val title: String?, var isComplete: Boolean = false) : Parcelable {
    //isComplete = false

    /*constructor(parcel: Parcel) : this(
        moduleId = parcel.readString(),
        title = parcel.readString(),
        isComplete = parcel.readByte() == 1.toByte()
    ) {
        isComplete = parcel.readByte() != 0.toByte()
    }*/

    init {
        this.isComplete = isComplete
    }

    override fun toString(): String {
        return title.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as ModuleInfo?

        return moduleId == that!!.moduleId
    }

    override fun hashCode(): Int {
        return moduleId.hashCode()
    }

    /*override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(moduleId)
        parcel.writeString(title)
        parcel.writeByte(if (isComplete) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModuleInfo> {
        override fun createFromParcel(parcel: Parcel): ModuleInfo {
            return ModuleInfo(parcel)
        }

        override fun newArray(size: Int): Array<ModuleInfo?> {
            return arrayOfNulls(size)
        }
    }*/

}