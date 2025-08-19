package com.example.myapplication.view.data



object DetectionUtil {



    private fun setPartBeanCachedData(partBean: PartBean, data: FacePositionResult) {
        data.partMarks?.forEach {
            if (partBean.part == it.dimensionMark) {
                partBean.positionX = it.positionX
                partBean.positionY = it.positionY
                partBean.isSelectedFacePosition = true
            }
        }
    }

    fun getFaceViewRectSize(viewWidth: Int): Float {
        return (20f / 640f) * viewWidth
    }

}