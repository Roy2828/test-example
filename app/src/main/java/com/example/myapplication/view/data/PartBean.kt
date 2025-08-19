package com.example.myapplication.view.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * 拍照部位
 */
@Parcelize
data class PartBean(
    var partId: Int = -1,//位置索引
    var bizType: String? = null,
    var isFace: Boolean = false,
    var faceImage: String? = null,//全脸图片路径
    var partIcon: Uri? = null,//部位图标资源id
    var partTips: String? = null,//部位拍摄预览提示
    var partImage: Uri? = null,//部位拍摄指示图片id
    var partImageText: String? = null,//部位拍摄指示图描述文案
    var partName: String? = null,//部位名称
    var part: String? = "",//部位 dimensionMark
    var sortValue: String? = "",
    var isTakePhoto: Boolean = false,//是否拍照成功
    var isSelected: Boolean = false,//是否选中：用于比较界面展示
    var isUploaded: Boolean = false,//部位是否已经分析过了
    var step: Boolean = false,//是否分布

    var pathMap: HashMap<String, String?> = HashMap(),
    var positionX: Float = 0f,
    var positionY: Float = 0f,
    var isSelectedFacePosition: Boolean = false,
    var position: Int = 0,
    var hasFacePositionMark: Boolean = false,//表示后台部位配置了展示部位标记
) : Parcelable {

    constructor(parcel: Parcel) :
            this(
                partId = parcel.readInt(),
                isFace = parcel.readByte().toInt() != 0,
                faceImage = parcel.readString(),
                partIcon = parcel.readParcelable<Uri?>(Uri::class.java.classLoader),
                partTips = parcel.readString(),
                partImage = parcel.readParcelable<Uri?>(Uri::class.java.classLoader),
                partImageText = parcel.readString(),
                partName = parcel.readString(),
                part = parcel.readString(),
                isTakePhoto = parcel.readByte().toInt() != 0,
                isSelected = parcel.readByte().toInt() != 0,
                isUploaded = parcel.readByte().toInt() != 0,
                step = parcel.readByte().toInt() != 0,
                pathMap = parcel.readHashMap(HashMap<String, String?>::javaClass.javaClass.classLoader) as HashMap<String, String?>
            )

    companion object : Parceler<PartBean> {
        const val PART_PHOTO = 1000//人脸
        const val PART_COLLARBONE = "clavicle"//锁骨
        const val PART_FOREHEAD = "forehead"//额头
        const val PART_FACE = "face"//脸部
        const val PART_NOSE = "beak"//鼻子
        const val PART_EYE = "periocular"//眼部
        const val PART_JAW = "chin"//下颚

        const val PART_FREE_1 = "part1"//自由点1
        const val PART_FREE_2 = "part2"//自由点2
        const val PART_FREE_3 = "part3"//自由点3
        const val PART_FREE_4 = "part4"//自由点4
        const val PART_FREE_5 = "part5"//自由点5
        const val PART_FREE_6 = "part6"//自由点6

        override fun create(parcel: Parcel): PartBean {
            return PartBean(parcel)
        }

        override fun PartBean.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(partId)
            parcel.writeByte(if (isFace) 1 else 0)
            parcel.writeString(faceImage)
            parcel.writeParcelable(partIcon, 0)
            parcel.writeString(partTips)
            parcel.writeParcelable(partImage, 0)
            parcel.writeString(partImageText)
            parcel.writeString(partName)
            parcel.writeString(part)
            parcel.writeByte(if (isTakePhoto) 1 else 0)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeByte(if (isUploaded) 1 else 0)
            parcel.writeByte(if (step) 1 else 0)
            parcel.writeMap(pathMap)
        }
    }
}

