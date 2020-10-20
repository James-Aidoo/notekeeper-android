package com.example.notekeeper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class NoteInfo(var course: CourseInfo?, var title: String?, var text: String?) : Parcelable {
    var id: Int = 0
    constructor(course: CourseInfo?, title: String?, text: String?, id: Int) : this(course, title, text){
        this.id  = id
    }

    private val compareKey: String
        get() = """${course!!.courseId}|${this.title}|${this.text}"""

    /*constructor(parcel: Parcel) : this(
        course = parcel.readParcelable<CourseInfo>(CourseInfo::class.java.classLoader),
        title = parcel.readString(),
        text = parcel.readString()
    )*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as NoteInfo?

        return compareKey == that!!.compareKey
    }

    override fun hashCode(): Int {
        return compareKey.hashCode()
    }

    override fun toString(): String {
        return this.compareKey
    }

    /*override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(course, 0)
        parcel.writeString(title)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NoteInfo> {
        override fun createFromParcel(parcel: Parcel): NoteInfo {
            return NoteInfo(parcel)
        }

        override fun newArray(size: Int): Array<NoteInfo?> {
            return arrayOfNulls(size)
        }
    }*/

}
