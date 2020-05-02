package com.viewport.commons

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class ViewPortSavedState : View.BaseSavedState {
    companion object {
        val CREATOR: Parcelable.Creator<ViewPortSavedState?> =
            object : Parcelable.Creator<ViewPortSavedState?> {
                override fun createFromParcel(`in`: Parcel): ViewPortSavedState? {
                    return ViewPortSavedState(`in`)
                }

                override fun newArray(size: Int): Array<ViewPortSavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }

    var isHearBeatStarted = false
    var isLibStarted = false
    var currentVisibleItemsList = mutableListOf<Int>()
    var viewedItemsFinalList = mutableListOf<Int>()
    var oldItemsList = mutableListOf<Int>()

    constructor(superState: Parcelable?) : super(superState)

    private constructor(parcel: Parcel) : super(parcel) {
        isHearBeatStarted = parcel.readByte() != 0.toByte()
        isLibStarted = parcel.readByte() != 0.toByte()
        currentVisibleItemsList =
            (parcel.createIntArray() ?: mutableListOf<Int>()) as MutableList<Int>
        viewedItemsFinalList = (parcel.createIntArray() ?: mutableListOf<Int>()) as MutableList<Int>
        oldItemsList = (parcel.createIntArray() ?: mutableListOf<Int>()) as MutableList<Int>
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeByte(if (isHearBeatStarted) 1 else 0)
        out.writeByte(if (isLibStarted) 1 else 0)
        out.writeList(currentVisibleItemsList as MutableList<*>?)
        out.writeList(viewedItemsFinalList as MutableList<*>?)
        out.writeList(oldItemsList as MutableList<*>?)
    }
}