package com.example.myapplication.expandablelayout

import android.animation.Animator
import android.widget.LinearLayout
import android.animation.TimeInterpolator
import android.view.animation.LinearInterpolator
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import kotlin.jvm.JvmOverloads
import android.annotation.TargetApi
import android.os.Build
import com.example.myapplication.R
import android.os.Parcelable
import android.animation.ValueAnimator
 import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.lang.IllegalStateException
import java.util.ArrayList

class ExpandableLinearLayout : LinearLayout, ExpandableLayout {
    private var duration = 0
    private var interpolator: TimeInterpolator = LinearInterpolator()

    /**
     * Default state of expanse
     *
     * @see .defaultChildIndex
     *
     * @see .defaultPosition
     */
    private var defaultExpanded = false

    /**
     * You cannot define [.defaultExpanded], [.defaultChildIndex]
     * and [.defaultPosition] at the same time.
     * [.defaultPosition] has priority over [.defaultExpanded]
     * and [.defaultChildIndex] if you set them at the same time.
     *
     *
     *
     *
     * Priority
     * [.defaultPosition] > [.defaultChildIndex] > [.defaultExpanded]
     */
    private var defaultChildIndex = 0
    private var defaultPosition = 0
    /**
     * Gets the width from left of layout if orientation is horizontal.
     * Gets the height from top of layout if orientation is vertical.
     *
     * @return
     *
     * @see .closePosition
     */
    /**
     * Sets the close position directly.
     *
     * @param position
     *
     * @see .closePosition
     *
     * @see .setClosePositionIndex
     */
    /**
     * The close position is width from left of layout if orientation is horizontal.
     * The close position is height from top of layout if orientation is vertical.
     */
    var closePosition = 0
    private var listener: ExpandableLayoutListener? = null
    private var savedState: ExpandableSavedState? = null
    private var isExpanded = false
    private var layoutSize = 0
    private var inRecyclerView = false
    private var isArranged = false
    private var isCalculatedSize = false
    private var isAnimating = false

    /**
     * State of expanse in recycler view.
     */
    private var recyclerExpanded = false

    /**
     * view size of children
     */
    private val childSizeList: MutableList<Int> = ArrayList()
    private var mGlobalLayoutListener: OnGlobalLayoutListener? = null

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet?,
        defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.expandableLayout, defStyleAttr, 0
        )
        duration = a.getInteger(
            R.styleable.expandableLayout_ael_duration,
            ExpandableLayout.DEFAULT_DURATION
        )
        defaultExpanded = a.getBoolean(
            R.styleable.expandableLayout_ael_expanded,
            ExpandableLayout.DEFAULT_EXPANDED
        )
        defaultChildIndex =
            a.getInteger(R.styleable.expandableLayout_ael_defaultChildIndex, Int.MAX_VALUE)
        defaultPosition =
            a.getDimensionPixelSize(R.styleable.expandableLayout_ael_defaultPosition, Int.MIN_VALUE)
        val interpolatorType = a.getInteger(
            R.styleable.expandableLayout_ael_interpolator,
            Utils.LINEAR_INTERPOLATOR
        )
        a.recycle()
        interpolator = Utils.createInterpolator(interpolatorType)
        isExpanded = defaultExpanded
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!isCalculatedSize) {
            // calculate a size of children
            childSizeList.clear()
            val childCount = childCount
            if (childCount > 0) {
                var sumSize = 0
                var view: View
                var params: LayoutParams
                for (i in 0 until childCount) {
                    view = getChildAt(i)
                    params = view.layoutParams as LayoutParams
                    if (0 < i) {
                        sumSize = childSizeList[i - 1]
                    }
                    childSizeList.add(
                        (if (isVertical) view.measuredHeight + params.topMargin + params.bottomMargin else view.measuredWidth + params.leftMargin + params.rightMargin) + sumSize
                    )
                }
                layoutSize = childSizeList[childCount - 1] +
                        if (isVertical) paddingTop + paddingBottom else paddingLeft + paddingRight
                isCalculatedSize = true
            } else {
                throw IllegalStateException("The expandableLinearLayout must have at least one child")
            }
        }
        if (isArranged) return

        // adjust default position if a user set a value.
        if (!defaultExpanded) {
            setLayoutSize(closePosition)
        }
        if (inRecyclerView) {
            setLayoutSize(if (recyclerExpanded) layoutSize else closePosition)
        }
        val childNumbers = childSizeList.size
        if (childNumbers > defaultChildIndex && childNumbers > 0) {
            moveChild(defaultChildIndex, 0, null)
        }
        if (defaultPosition > 0 && layoutSize >= defaultPosition && layoutSize > 0) {
            move(defaultPosition, 0, null)
        }
        isArranged = true
        if (savedState == null) return
        setLayoutSize(savedState!!.size)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val ss = ExpandableSavedState(parcelable)
        ss.size = currentPosition
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is ExpandableSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        super.onRestoreInstanceState(ss.superState)
        savedState = ss
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: ExpandableLayoutListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun toggle() {
        toggle(duration.toLong(), interpolator)
    }

    /**
     * {@inheritDoc}
     */
    override fun toggle(duration: Long, interpolator: TimeInterpolator?) {
        if (closePosition < currentPosition) {
            collapse(duration, interpolator)
        } else {
            expand(duration, interpolator)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun expand() {
        if (isAnimating) return
        createExpandAnimator(currentPosition, layoutSize, duration.toLong(), interpolator).start()
    }

    /**
     * {@inheritDoc}
     */
    override fun expand(duration: Long, interpolator: TimeInterpolator?) {
        if (isAnimating) return
        if (duration <= 0) {
            move(layoutSize, duration, interpolator)
            return
        }
        createExpandAnimator(currentPosition, layoutSize, duration, interpolator).start()
    }

    /**
     * {@inheritDoc}
     */
    override fun collapse() {
        if (isAnimating) return
        createExpandAnimator(
            currentPosition,
            closePosition,
            duration.toLong(),
            interpolator
        ).start()
    }

    /**
     * {@inheritDoc}
     */
    override fun collapse(duration: Long, interpolator: TimeInterpolator?) {
        if (isAnimating) return
        if (duration <= 0) {
            move(closePosition, duration, interpolator)
            return
        }
        createExpandAnimator(currentPosition, closePosition, duration, interpolator).start()
    }

    /**
     * {@inheritDoc}
     */
    override fun setDuration(duration: Int) {
        require(duration >= 0) {
            "Animators cannot have negative duration: " +
                    duration
        }
        this.duration = duration
    }

    /**
     * {@inheritDoc}
     */
    override fun setExpanded(expanded: Boolean) {
        if (inRecyclerView) recyclerExpanded = expanded
        val currentPosition = currentPosition
        if (expanded && currentPosition == layoutSize
            || !expanded && currentPosition == closePosition
        ) return
        isExpanded = expanded
        setLayoutSize(if (expanded) layoutSize else closePosition)
        requestLayout()
    }

    /**
     * {@inheritDoc}
     */
    override fun isExpanded(): Boolean {
        return isExpanded
    }

    /**
     * {@inheritDoc}
     */
    override fun setInterpolator(interpolator: TimeInterpolator) {
        this.interpolator = interpolator
    }

    /**
     * Initializes this layout.
     */
    fun initLayout() {
        closePosition = 0
        layoutSize = 0
        isArranged = false
        isCalculatedSize = false
        savedState = null
        if (isVertical) {
            measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED)
            )
        } else {
            measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        }
    }
    /**
     * Moves to position.
     * Sets 0 to duration if you want to move immediately.
     *
     * @param position
     * @param duration
     * @param interpolator use the default interpolator if the argument is null.
     */
    /**
     * @param position
     *
     * @see .move
     */
    @JvmOverloads
    fun move(
        position: Int,
        duration: Long = this.duration.toLong(),
        interpolator: TimeInterpolator? = this.interpolator
    ) {
        if (isAnimating || 0 > position || layoutSize < position) return
        if (duration <= 0) {
            isExpanded = position > closePosition
            setLayoutSize(position)
            requestLayout()
            notifyListeners()
            return
        }
        createExpandAnimator(
            currentPosition, position, duration,
            interpolator ?: this.interpolator
        ).start()
    }
    /**
     * Moves to bottom(VERTICAL) or right(HORIZONTAL) of child view
     * Sets 0 to duration if you want to move immediately.
     *
     * @param index        index child view index
     * @param duration
     * @param interpolator use the default interpolator if the argument is null.
     */
    /**
     * @param index child view index
     *
     * @see .moveChild
     */
    @JvmOverloads
    fun moveChild(
        index: Int,
        duration: Long = this.duration.toLong(),
        interpolator: TimeInterpolator? = this.interpolator
    ) {
        if (isAnimating) return
        val destination = getChildPosition(index) +
                if (isVertical) paddingBottom else paddingRight
        if (duration <= 0) {
            isExpanded = destination > closePosition
            setLayoutSize(destination)
            requestLayout()
            notifyListeners()
            return
        }
        createExpandAnimator(
            currentPosition, destination,
            duration, interpolator ?: this.interpolator
        ).start()
    }

    /**
     * Gets the width from left of layout if orientation is horizontal.
     * Gets the height from top of layout if orientation is vertical.
     *
     * @param index index of child view
     *
     * @return position from top or left
     */
    fun getChildPosition(index: Int): Int {
        require(!(0 > index || childSizeList.size <= index)) { "There aren't the view having this index." }
        return childSizeList[index]
    }

    /**
     * Gets the current position.
     *
     * @return
     */
    val currentPosition: Int
        get() = if (isVertical) measuredHeight else measuredWidth

    /**
     * Sets close position using index of child view.
     *
     * @param childIndex
     *
     * @see .closePosition
     *
     * @see .setClosePosition
     */
    fun setClosePositionIndex(childIndex: Int) {
        closePosition = getChildPosition(childIndex)
    }

    /**
     * Set true if expandable layout is used in recycler view.
     *
     * @param inRecyclerView
     */
    fun setInRecyclerView(inRecyclerView: Boolean) {
        this.inRecyclerView = inRecyclerView
    }

    private val isVertical: Boolean
        private get() = orientation == VERTICAL

    private fun setLayoutSize(size: Int) {
        if (isVertical) {
            layoutParams.height = size
        } else {
            layoutParams.width = size
        }
    }

    /**
     * Creates value animator.
     * Expand the layout if {@param to} is bigger than {@param from}.
     * Collapse the layout if {@param from} is bigger than {@param to}.
     *
     * @param from
     * @param to
     * @param duration
     * @param interpolator
     *
     * @return
     */
    private fun createExpandAnimator(
        from: Int, to: Int, duration: Long, interpolator: TimeInterpolator?
    ): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(from, to)
        valueAnimator.duration = duration
        valueAnimator.interpolator = interpolator
        valueAnimator.addUpdateListener { animator ->
            if (isVertical) {
                layoutParams.height = animator.animatedValue as Int
            } else {
                layoutParams.width = animator.animatedValue as Int
            }
            requestLayout()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                isAnimating = true
                if (listener == null) return
                listener?.onAnimationStart()
                if (layoutSize == to) {
                    listener?.onPreOpen()
                    return
                }
                if (closePosition == to) {
                    listener?.onPreClose()
                }
            }

            override fun onAnimationEnd(animator: Animator) {
                isAnimating = false
                isExpanded = to > closePosition
                if (listener == null) return
                listener?.onAnimationEnd()
                if (to == layoutSize) {
                    listener?.onOpened()
                    return
                }
                if (to == closePosition) {
                    listener?.onClosed()
                }
            }
        })
        return valueAnimator
    }

    /**
     * Notify listeners
     */
    private fun notifyListeners() {
        if (listener == null) return
        listener?.onAnimationStart()
        if (isExpanded) {
            listener?.onPreOpen()
        } else {
            listener?.onPreClose()
        }
        mGlobalLayoutListener = OnGlobalLayoutListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                viewTreeObserver.removeGlobalOnLayoutListener(mGlobalLayoutListener)
            } else {
                viewTreeObserver.removeOnGlobalLayoutListener(mGlobalLayoutListener)
            }
            listener?.onAnimationEnd()
            if (isExpanded) {
                listener?.onOpened()
            } else {
                listener?.onClosed()
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(mGlobalLayoutListener)
    }
}