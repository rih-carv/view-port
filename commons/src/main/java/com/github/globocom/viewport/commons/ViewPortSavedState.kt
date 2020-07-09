package com.github.globocom.viewport.commons

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.RequiresApi

class ViewPortSavedState : View.BaseSavedState {
    companion object {
        @JvmField
        val CREATOR: Parcelable.ClassLoaderCreator<ViewPortSavedState?> =
            object : Parcelable.ClassLoaderCreator<ViewPortSavedState?> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun createFromParcel(
                    source: Parcel,
                    loader: ClassLoader?
                ): ViewPortSavedState? {
                    return ViewPortSavedState(source, loader)
                }

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
    var previouslyVisibleItemsList = mutableListOf<Int>()
    var oldItemsList = mutableListOf<Int>()

    constructor(superState: Parcelable?) : super(superState)

    @RequiresApi(Build.VERSION_CODES.N)
    private constructor(parcel: Parcel, loader: ClassLoader?) : super(parcel, loader) {
        setupWith(parcel)
    }

    private constructor(parcel: Parcel) : super(parcel) {
        setupWith(parcel)
    }

    private fun setupWith(parcel: Parcel) {
        isHearBeatStarted = parcel.readByte() != 0.toByte()
        isLibStarted = parcel.readByte() != 0.toByte()
        currentVisibleItemsList = (parcel.createIntArray()?.toMutableList() ?: mutableListOf())
        previouslyVisibleItemsList = (parcel.createIntArray()?.toMutableList() ?: mutableListOf())
        oldItemsList = (parcel.createIntArray()?.toMutableList() ?: mutableListOf())
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeByte(if (isHearBeatStarted) 1 else 0)
        out.writeByte(if (isLibStarted) 1 else 0)
        out.writeIntArray(currentVisibleItemsList.toIntArray())
        out.writeIntArray(previouslyVisibleItemsList.toIntArray())
        out.writeIntArray(oldItemsList.toIntArray())
    }
}