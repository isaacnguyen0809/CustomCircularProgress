package com.kevin.canvascircularprogess

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.*


class CustomCircularProgress constructor(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    companion object {
        private const val DEFAULT_STARTING_ANGLE = 270
        private const val DEFAULT_PROGRESS_BAR_THICKNESS_DP = 40f
        private const val DEFAULT_PADDING = 50f
        private const val DEFAULT_PROGRESS_COLOR = Color.BLACK
    }

    private val defaultStrokeProgressBarThickness =
        dpToPx(DEFAULT_PROGRESS_BAR_THICKNESS_DP).toFloat()
    private val parentArcColor = ContextCompat.getColor(context, R.color.blue_circle)
    private val fillArcColor = ContextCompat.getColor(context, R.color.blue_circle_outer)
    private var mDiameter: Float = 0f
    private var mProgress: Float = 0f
    private var mProgressBarThickness: Float = defaultStrokeProgressBarThickness
    private var mMaxProgress: Int = 0
    private var mProgressColor: Int = DEFAULT_PROGRESS_COLOR
    private var mCircleBackgroundColor: Int = DEFAULT_PROGRESS_COLOR
    private lateinit var spaceCircle: RectF
    private lateinit var shadowSpaceCircle: RectF
    private lateinit var gradient: SweepGradient
    private lateinit var progressAnimator: ValueAnimator

    private val outerArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = parentArcColor
        strokeWidth = DEFAULT_PROGRESS_BAR_THICKNESS_DP + 10f
    }

    private val shadowArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.WHITE
        strokeWidth = DEFAULT_PROGRESS_BAR_THICKNESS_DP + 10f
        setShadowLayer(20f, 0f, 8f, ContextCompat.getColor(context, R.color.blackopa))
    }

    private val innerArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillArcColor
        strokeWidth = DEFAULT_PROGRESS_BAR_THICKNESS_DP
        strokeCap = Paint.Cap.ROUND
    }

    private val outerDotPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
        color = Color.WHITE
        isAntiAlias = true
    }

    private val innerDotPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.blue_sg)
        isAntiAlias = true
    }

    private var colors = intArrayOf(
        ContextCompat.getColor(context, R.color.color_100),
        ContextCompat.getColor(context, R.color.color_10),
        ContextCompat.getColor(context, R.color.color_20),
        ContextCompat.getColor(context, R.color.color_30),
        ContextCompat.getColor(context, R.color.color_40),
        ContextCompat.getColor(context, R.color.color_50),
        ContextCompat.getColor(context, R.color.color_60),
        ContextCompat.getColor(context, R.color.color_70),
        ContextCompat.getColor(context, R.color.color_80),
        ContextCompat.getColor(context, R.color.color_90),
        ContextCompat.getColor(context, R.color.color_100)
    )

    var positions = floatArrayOf(0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f)

    init {
        setUpAttrs(context, attrs)
    }

    private fun setUpAttrs(context: Context, attrs: AttributeSet?) {
        spaceCircle = RectF()
        shadowSpaceCircle = RectF()
        attrs?.let {
            val typedArray = context.theme.obtainStyledAttributes(
                it,
                R.styleable.CustomCircularProgress,
                0,
                0
            )
            typedArray.apply {
                try {
                    mProgress =
                        getFloat(R.styleable.CustomCircularProgress_progress, 0f)
                    mProgressBarThickness =
                        getDimension(
                            R.styleable.CustomCircularProgress_progressBarThickness,
                            defaultStrokeProgressBarThickness
                        )
                    mProgressColor = getInt(R.styleable.CustomCircularProgress_progressColor, 0)
                    mCircleBackgroundColor =
                        getInt(R.styleable.CustomCircularProgress_circleBackgroundColor, 0)
                    mMaxProgress = getInt(R.styleable.CustomCircularProgress_maxProgress, 0)
                } finally {
                    typedArray.recycle()
                }
            }
        }
        innerArcPaint.color = mProgressColor
        innerArcPaint.strokeWidth = mProgressBarThickness
        outerArcPaint.color = mCircleBackgroundColor
        outerArcPaint.strokeWidth = mProgressBarThickness + 25f
        shadowArcPaint.strokeWidth = mProgressBarThickness + 25f
        outerDotPaint.strokeWidth = mProgressBarThickness
        innerDotPaint.strokeWidth = mProgressBarThickness - 14f
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val radiusInnerCircle = mDiameter / 2
        spaceCircle.apply {
            set(
                horizontalCenter - radiusInnerCircle,
                verticalCenter - radiusInnerCircle,
                horizontalCenter + radiusInnerCircle,
                verticalCenter + radiusInnerCircle
            )
        }
        val radiusShadowCircle = radiusInnerCircle + 15f
        shadowSpaceCircle.apply {
            set(
                horizontalCenter - radiusShadowCircle,
                verticalCenter - radiusShadowCircle,
                horizontalCenter + radiusShadowCircle,
                verticalCenter + radiusShadowCircle
            )
        }
        gradient = SweepGradient(horizontalCenter, verticalCenter, colors, positions)
        innerArcPaint.shader = gradient
        canvas?.let {
            //draw shadow arc
            it.drawArc(shadowSpaceCircle, 0f, 360f, false, shadowArcPaint)
            //draw outer Arc
            it.drawArc(spaceCircle, 0f, 360f, false, outerArcPaint)
            //Draw inner arc
            val previousAngle = DEFAULT_STARTING_ANGLE.toFloat()
            val sweepAngle = 360 * mProgress / mMaxProgress
            it.drawArc(
                spaceCircle,
                DEFAULT_STARTING_ANGLE.toFloat(),
                sweepAngle,
                false,
                innerArcPaint
            )
            //draw dot
            val endX =
                cos(Math.toRadians((previousAngle + sweepAngle.toDouble()))) * radiusInnerCircle + spaceCircle.centerX()
            val endY =
                sin(Math.toRadians(previousAngle + sweepAngle.toDouble())) * radiusInnerCircle + spaceCircle.centerY()
            canvas.drawPoint(
                endX.toFloat(),
                endY.toFloat(),
                outerDotPaint
            )
            canvas.drawPoint(
                endX.toFloat(),
                endY.toFloat(),
                innerDotPaint
            )
        }
    }

    private fun dpToPx(dp: Float) =
        ceil(dp * Resources.getSystem().displayMetrics.density.toDouble()).toInt()

    fun setProgress(progress: Int) {
        progressAnimator =
            ValueAnimator().apply {
                interpolator = AccelerateDecelerateInterpolator()
                setObjectValues(mProgress, progress)
                duration = 1000
                addUpdateListener {
                    val percentage = it.animatedValue as Float
                    mProgress = percentage
                    invalidate()
                }
            }
        progressAnimator.start()
        requestLayout()
    }

    fun setMaxProgress(maxProgress: Int) {
        mMaxProgress = maxProgress
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height =
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) MeasureSpec.getSize(
                heightMeasureSpec
            ) else 200
        val rawMeasuredDim = max(min(width, height), 0)
        mDiameter = rawMeasuredDim.toFloat() - dpToPx(DEFAULT_PADDING)
        setMeasuredDimension(rawMeasuredDim, rawMeasuredDim)
    }

}
