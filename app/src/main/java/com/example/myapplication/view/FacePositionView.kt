package com.example.myapplication.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.myapplication.view.data.DetectionUtil
import com.example.myapplication.view.data.FacePositionBean
import com.example.myapplication.view.data.PartBean


class FacePositionView : View {
    private var size = 15f
    private var mCanvas: Canvas? = null
    private var mBitmap: Bitmap? = null
    private var mSrcBitmap: Bitmap? = null
    private var originWidth: Int = 0
    private var originHeight: Int = 0
    private var positionList = mutableListOf<PartBean>()
    private lateinit var mFillPaint: Paint//填充画笔
    private lateinit var mTextPaint: Paint//文字画笔
    private lateinit var mStokePaint: Paint//边框画笔
    private var facePositionSelectListener: FacePositionSelectListener? = null
    private var radius: Float = 6f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        size = 25f.dpToPx()
        radius =  6f.dpToPx()
        mFillPaint = Paint()
        mFillPaint.isAntiAlias = true
        mFillPaint.style = Paint.Style.FILL
        mFillPaint.setColor(Color.parseColor("#336381FF"))
        mStokePaint = Paint()
        mStokePaint.isAntiAlias = true
        mStokePaint.style = Paint.Style.STROKE
        mStokePaint.setColor(Color.parseColor("#FF6381FF"))
        mStokePaint.strokeWidth =   3f.dpToPx()
        mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.setColor(Color.parseColor("#FF6381FF"))
        mTextPaint.textSize = 32f.dpToPx()
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        positionList.forEachIndexed { _, bean ->
            if (bean.isSelectedFacePosition) {
                size = DetectionUtil.getFaceViewRectSize(measuredWidth)
                // 计算矩形中心
                val centerX = bean.positionX
                val centerY = bean.positionY
                val rect = RectF(
                    (centerX - size), (centerY - size),
                    (centerX + size), (centerY + size)
                )
                canvas.drawRoundRect(rect, radius, radius, mFillPaint)
                canvas.drawRoundRect(rect, radius, radius, mStokePaint)
                // 获取FontMetrics以计算垂直居中偏移
                val fontMetrics = mTextPaint.fontMetrics
                val textCenterY = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
                canvas.drawText(
                    "${bean.position + 1}",
                    rect.centerX(),
                    textCenterY,
                    mTextPaint
                )
            }
        }
    }

    fun setImageResource2(srcBitmap: Bitmap) {
        this.mSrcBitmap = srcBitmap
        //图片适配控件大小
        this.originWidth = srcBitmap.width
        this.originHeight = srcBitmap.height
        if (this.originWidth > 0 && this.originHeight > 0 && measuredWidth > 0 && measuredHeight > 0) {
            // 创建一个与缩放后的 Bitmap 相同大小的可变 Bitmap
            val scaleBitmap =
                Bitmap.createScaledBitmap(srcBitmap, measuredWidth, measuredHeight, true)
            this.mBitmap =
                Bitmap.createBitmap(scaleBitmap.width, scaleBitmap.height, Bitmap.Config.ARGB_8888)
            mBitmap?.let {
                mCanvas = Canvas(it)
                mCanvas?.drawBitmap(scaleBitmap, 0f, 0f, Paint())
            }
            postInvalidate()
        }
    }

    fun setImageResource3(srcBitmap: Bitmap) {
        this.mSrcBitmap = srcBitmap
        this.originWidth = srcBitmap.width
        this.originHeight = srcBitmap.height

        if (originWidth > 0 && originHeight > 0 && measuredWidth > 0 && measuredHeight > 0) {
            // 1. 计算目标控件比例
            val targetRatio = measuredWidth.toFloat() / measuredHeight.toFloat()
            // 2. 计算原始图片比例
            val srcRatio = originWidth.toFloat() / originHeight.toFloat()

            // 3. 确定裁剪策略
            val (cropWidth, cropHeight) = if (srcRatio > targetRatio) {
                // 原始图更宽：按目标比例裁剪高度
                val scaledHeight = originHeight
                val scaledWidth = (originHeight * targetRatio).toInt()
                Pair(scaledWidth, scaledHeight)
            } else {
                // 原始图更高：按目标比例裁剪宽度
                val scaledWidth = originWidth
                val scaledHeight = (originWidth / targetRatio).toInt()
                Pair(scaledWidth, scaledHeight)
            }

            // 4. 计算裁剪起始位置（居中裁剪）
            val cropX = (originWidth - cropWidth) / 2
            val cropY = (originHeight - cropHeight) / 2

            // 5. 裁剪原始图核心区域
            val croppedBitmap = Bitmap.createBitmap(
                srcBitmap,
                cropX,
                cropY,
                cropWidth,
                cropHeight
            )

            // 6. 将裁剪后的图缩放到控件尺寸
            val scaledBitmap = Bitmap.createScaledBitmap(
                croppedBitmap,
                measuredWidth,
                measuredHeight,
                true
            )
            croppedBitmap.recycle() // 及时回收临时Bitmap

            // 7. 创建最终Bitmap并绘制
            mBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            mBitmap?.let {
                mCanvas = Canvas(it)
                mCanvas?.drawBitmap(scaledBitmap, 0f, 0f, null)
                scaledBitmap.recycle()
            }
            postInvalidate()
        }
    }

    fun setImageResourceFitCenter(srcBitmap: Bitmap) {
        this.mSrcBitmap = srcBitmap
        this.originWidth = srcBitmap.width
        this.originHeight = srcBitmap.height

        if (this.originWidth <= 0 || this.originHeight <= 0 || measuredWidth <= 0 || measuredHeight <= 0) {
            this.mBitmap = null
            postInvalidate()
            return
        }

        val srcAspectRatio = originWidth.toFloat() / originHeight.toFloat()
        val measuredAspectRatio = measuredWidth.toFloat() / measuredHeight.toFloat()

        val finalScaledWidth: Int
        val finalScaledHeight: Int

        if (srcAspectRatio > measuredAspectRatio) {
            // 源图片比控件更“宽”或同样比例，以控件宽度为基准进行缩放，高度会按比例缩小
            finalScaledWidth = measuredWidth
            finalScaledHeight = (measuredWidth / srcAspectRatio).toInt()
        } else {
            // 源图片比控件更“高”或同样比例，以控件高度为基准进行缩放，宽度会按比例缩小
            finalScaledHeight = measuredHeight
            finalScaledWidth = (measuredHeight * srcAspectRatio).toInt()
        }

        // 确保计算出的尺寸有效
        if (finalScaledWidth <= 0 || finalScaledHeight <= 0) {
            this.mBitmap = null
            postInvalidate()
            return
        }

        // 1. 将原始 Bitmap 按比例缩放到计算出的 finalScaledWidth 和 finalScaledHeight
        val tempFitBitmap = Bitmap.createScaledBitmap(srcBitmap, finalScaledWidth, finalScaledHeight, true)

        // 2. 创建一个与控件测量尺寸相同的 mBitmap
        //    这个 mBitmap 将作为最终在 onDraw 中绘制的 Bitmap
        //    如果 tempFitBitmap 比 mBitmap 小，它将被绘制在 mBitmap 的中央
        this.mBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

        mBitmap?.let { bmp ->
            mCanvas = Canvas(bmp) // 使用 mBitmap 创建 Canvas

            // 可选：如果你希望 mBitmap 的背景不是透明的，可以在这里填充一个颜色
            // mCanvas?.drawColor(Color.LTGRAY) // 例如，填充浅灰色

            // 计算 tempFitBitmap 在 mBitmap 中居中绘制的起始坐标
            val left = (measuredWidth - finalScaledWidth) / 2f
            val top = (measuredHeight - finalScaledHeight) / 2f

            // 将按比例缩放后的 tempFitBitmap 绘制到 mBitmap 的中央
            mCanvas?.drawBitmap(tempFitBitmap, left, top, null) // 使用 null paint 即可

            // 回收临时的 tempFitBitmap，因为它已经被绘制到 mBitmap 上了
            if (!tempFitBitmap.isRecycled) {
                tempFitBitmap.recycle()
            }
        }
        postInvalidate()
    }

    fun setImageResource(srcBitmap: Bitmap) {
        this.mSrcBitmap = srcBitmap
        this.originWidth = srcBitmap.width
        this.originHeight = srcBitmap.height

        if (this.originWidth <= 0 || this.originHeight <= 0 || measuredWidth <= 0 || measuredHeight <= 0) {
            // 处理无效尺寸
            this.mBitmap = null
            postInvalidate()
            return
        }

        val srcAspectRatio = originWidth.toFloat() / originHeight.toFloat()
        val measuredAspectRatio = measuredWidth.toFloat() / measuredHeight.toFloat()

        val scaledWidth: Int
        val scaledHeight: Int
        var offsetX = 0f
        var offsetY = 0f

        if (srcAspectRatio > measuredAspectRatio) {
            // 源图片比控件更宽，需要按高度缩放，然后裁剪宽度
            scaledHeight = measuredHeight
            scaledWidth = (measuredHeight * srcAspectRatio).toInt()
            offsetX = (scaledWidth - measuredWidth) / 2f
        } else {
            // 源图片比控件更高，需要按宽度缩放，然后裁剪高度
            scaledWidth = measuredWidth
            scaledHeight = (measuredWidth / srcAspectRatio).toInt()
            offsetY = (scaledHeight - measuredHeight) / 2f
        }

        if (scaledWidth <= 0 || scaledHeight <= 0) {
            this.mBitmap = null
            postInvalidate()
            return
        }

        // 先将原始 Bitmap 按比例缩放到能覆盖目标 View 的尺寸
        val tempScaledBitmap = Bitmap.createScaledBitmap(srcBitmap, scaledWidth, scaledHeight, true)

        // 创建一个最终大小为 measuredWidth x measuredHeight 的 Bitmap
        //    并从 tempScaledBitmap 中裁剪出中间部分绘制到这个最终 Bitmap 上
        this.mBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        mBitmap?.let {
            mCanvas = Canvas(it)
            //源矩形 (从 tempScaledBitmap 中裁剪的区域)
            val srcRect = Rect(offsetX.toInt(), offsetY.toInt(), (offsetX + measuredWidth).toInt(), (offsetY + measuredHeight).toInt())
            //目标矩形 (在 mBitmap 中绘制的区域，即整个 mBitmap)
            val destRect = Rect(0, 0, measuredWidth, measuredHeight)
            mCanvas?.drawBitmap(tempScaledBitmap, srcRect, destRect, null)
            tempScaledBitmap.recycle()
        }
        postInvalidate()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                if (event.x > 0 && event.y > 0 && event.x < measuredWidth && event.y < measuredHeight) {
                    val facePositionBean = facePositionSelectListener?.getFacePositionToSelect()
                    facePositionBean?.let { bean ->
                        if (!bean.isSelectedFacePosition) {
                            bean.positionX = event.x
                            bean.positionY = event.y
                            bean.isSelectedFacePosition = true
                            postInvalidate()
                        }
                        facePositionSelectListener?.onFacePositionSelected(bean.position)
                    }
                }
            }
        }
        return true
    }

    fun setFacePositionClickedListener(listener: FacePositionSelectListener) {
        this.facePositionSelectListener = listener
    }

    interface FacePositionSelectListener {
        fun onFacePositionSelected(position: Int)
        fun getRecyclerViewVSelected(): PartBean?
        fun getFacePositionToSelect(): PartBean?
    }

    fun setFacePositionList(list: MutableList<PartBean>) {
        this.positionList = list
    }


    /**
     * 将空间中选中的点，转换成图片上原始点
     * */
    fun getBitmapFacePositionList(): MutableList<FacePositionBean> {
        val list = mutableListOf<FacePositionBean>()
        positionList.forEach {
            if (this.originWidth > 0 && this.originHeight > 0 && measuredWidth > 0 && measuredHeight > 0) {
                val scaleX = originWidth.toFloat() / measuredWidth
                val scaleY = originHeight.toFloat() / measuredHeight
                val x = scaleX * it.positionX
                val y = scaleY * it.positionY
                list.add(FacePositionBean(it.part, x, y))
            }
        }
        return list
    }

    fun convertToBitmapPosition(url: String?) {
        positionList.forEach { partBean ->
            if (originWidth > 0 && originHeight > 0 && measuredWidth > 0 && measuredHeight > 0) {
                val scaleX = originWidth.toFloat() / measuredWidth
                val scaleY = originHeight.toFloat() / measuredHeight
                partBean.positionX *= scaleX
                partBean.positionY *= scaleY
                url?.let {
                    partBean.faceImage = it
                }
            }
        }
    }

    /**
     * 将图片上的点转换成控件上的点
     */
    fun covertToViewPosition(srcWidth: Int, srcHeight: Int) {
        positionList.forEach { partBean ->
            if (srcWidth > 0 && srcHeight > 0 && measuredWidth > 0 && measuredHeight > 0 && partBean.isSelectedFacePosition) {
                //初始化时,如果部位有选中为图片的坐标位置，需要转换成控件的坐标
                val scaleX = measuredWidth.toFloat() / srcWidth
                val scaleY = measuredHeight.toFloat() / srcHeight
                partBean.positionX *= scaleX
                partBean.positionY *= scaleY
            }
        }
        postInvalidate()
    }

    /**
     * 取消最后一个选中的点
     * */
    fun revokeLastPosition(): Int {
        if (positionList.isNotEmpty()) {
            var facePositionBean = facePositionSelectListener?.getRecyclerViewVSelected()
            facePositionBean?.let { bean ->
                if (bean.isSelectedFacePosition) {
                    bean.isSelectedFacePosition = false
                    postInvalidate()
                    return bean.position
                }
            }
            if (facePositionBean != null) {
                for (index in facePositionBean.position downTo 0) {
                    if (positionList[index].isSelectedFacePosition) {
                        positionList[index].isSelectedFacePosition = false
                        postInvalidate()
                        return positionList[index].position
                    }
                }
                for (index in (positionList.size - 1) downTo facePositionBean.position) {
                    if (positionList[index].isSelectedFacePosition) {
                        positionList[index].isSelectedFacePosition = false
                        postInvalidate()
                        return positionList[index].position
                    }
                }
            } else {
                positionList.reversed().forEach { bean ->
                    if (bean.isSelectedFacePosition) {
                        bean.isSelectedFacePosition = false
                        postInvalidate()
                        return bean.position
                    }
                }
            }
        }
        return -1
    }

    fun revokePosition(position: Int) {
        if (positionList.isNotEmpty() && position < positionList.size) {
            positionList[position].isSelectedFacePosition = false
            postInvalidate()
        }
    }

    fun revokeAllPosition() {
        positionList.forEach { bean ->
            var needRefreshView = false
            if (bean.isSelectedFacePosition) {
                bean.isSelectedFacePosition = false
                needRefreshView = true
            }
            if (needRefreshView) {
                postInvalidate()
            }
        }
    }

    fun getBitmapWidth(): Int {
        return originWidth
    }

    fun getBitmapHeight(): Int {
        return originHeight
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue
     * @param fontScale （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    fun sp2px(context: Context, spValue: Float): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity;
        return (spValue * fontScale + 0.5f)
    }

    /**
     * 查找下一个没有选择人脸位置的部位
     * */
    fun findNextUnSelectPartBean(current: Int): PartBean? {
        for (index in current until positionList.size) {
            if (!positionList[index].isSelectedFacePosition) {
                return positionList[index]
            }
        }
        for (index in 0 until current) {
            if (!positionList[index].isSelectedFacePosition) {
                return positionList[index]
            }
        }
        return null
    }

}

fun Float.dpToPx() =
    (this * Resources.getSystem().displayMetrics.density + 0.5f)

fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()