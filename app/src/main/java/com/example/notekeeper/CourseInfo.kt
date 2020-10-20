package com.example.notekeeper

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CourseInfo(val courseId: String, val title: String, val modules: List<ModuleInfo>?) : Parcelable {

    var modulesCompletionStatus: BooleanArray
        get() {
            val status = BooleanArray(modules!!.size)

            if (modules != null) {
                for (i in modules.indices)
                    status[i] = modules[i].isComplete
            }

            return status
        }
        set(status) {
            if (modules != null) {
                for (i in modules.indices)
                    modules[i].isComplete = status[i]
            }
        }

    /*constructor(parcel: Parcel) : this(
        courseId = parcel.readString(),
        title = parcel.readString(),
        modules = ArrayList<ModuleInfo>()
    ){
        parcel.readTypedList(modules, ModuleInfo.CREATOR)
    }*/

    fun getModule(moduleId: String): ModuleInfo? {
        if (modules != null) {
            for (moduleInfo in modules) {
                if (moduleId == moduleInfo.moduleId)
                    return moduleInfo
            }
        }
        return null
    }

    override fun toString(): String {
        return title
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as CourseInfo?

        return courseId == that!!.courseId

    }

    override fun hashCode(): Int {
        return courseId.hashCode()
    }

    /*override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(courseId)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CourseInfo> {
        override fun createFromParcel(parcel: Parcel): CourseInfo {
            return CourseInfo(parcel)
        }

        override fun newArray(size: Int): Array<CourseInfo?> {
            return arrayOfNulls(size)
        }
    }*/

}

