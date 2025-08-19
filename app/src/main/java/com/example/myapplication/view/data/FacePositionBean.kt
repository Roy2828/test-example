package com.example.myapplication.view.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FacePositionBean(
    var dimensionMark: String? = null,
    var positionX: Float = 0f,
    var positionY: Float = 0f
) : Parcelable, Serializable {

}

@Parcelize
data class FacePositionResult(
    var testingProjectMark: String? = null,
    var imageUrl: String? = null,
    var partMarks: List<FacePositionBean>? = null
) :
    Parcelable, Serializable

@Parcelize
data class SaveFacePosition(var flag: Boolean? = false, var imageUrl: String? = null) : Parcelable,
    Serializable
