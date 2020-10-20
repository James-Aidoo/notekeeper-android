package com.example.notekeeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import kotlin.math.min

/**
 * TODO: document your custom view class.
 */
class ModuleStatusView : View {

    private val EDIT_MODE_MODULE_COUNT = 7
    private var outlineWidth: Float = 0f
    private var shapeSize: Float = 0f
    private var spacing: Float = 0f
    private var radius: Float = 0f
    private var DEFAULT_OUTLINE_WIDTH_DP = 2f
    private var DEFAULT_SHAPE_SIZE_DP = 144f
    private var DEFAULT_SPACING_DP = 30f
    val SHAPE_CIRCLE = 0
    var shape: Int = SHAPE_CIRCLE

    private var maxHorizontalModules: Int = 0
    private val invalidIndex = -1

    var outlineColor: Int? = null
    var paintOutline: Paint? = null

    var fillColor: Int? = null
    var paintFill: Paint? = null

    private var accessibilityHelper: ModuleStatusAccessibilityHelper? = null

    var moduleStatus: Array<Boolean> = Array(3){false}
    private var moduleRectangles: Array<Rect> = arrayOf()

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode)
            setupEditMode()

        isFocusable = true
        accessibilityHelper = ModuleStatusAccessibilityHelper(this)
        ViewCompat.setAccessibilityDelegate(this, accessibilityHelper)

        val dm = context.resources.displayMetrics
        val defaultOutlineWidth = dm.density * DEFAULT_OUTLINE_WIDTH_DP
        val defaultShapeSize = dm.density * DEFAULT_SHAPE_SIZE_DP
        val defaultSpacing = dm.density * DEFAULT_SPACING_DP

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ModuleStatusView, defStyle, 0
        )

        outlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK)
        shape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE)
        outlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidth)
        shapeSize = a.getDimension(R.styleable.ModuleStatusView_shapeSize, defaultShapeSize)
        spacing = a.getDimension(R.styleable.ModuleStatusView_spacing, defaultSpacing)

        a.recycle()
        
        shapeSize = 144f
        spacing = 30f
        radius = (shapeSize - outlineWidth) / 2

        outlineColor = Color.BLACK
        paintOutline = Paint(Paint.ANTI_ALIAS_FLAG)
        paintOutline!!.style = Paint.Style.STROKE
        paintOutline!!.strokeWidth = outlineWidth
        paintOutline!!.color = outlineColor!!

        fillColor = context.resources.getColor(R.color.pluralSight_orange)
        paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill!!.style = Paint.Style.FILL
        paintFill!!.color = fillColor!!
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        accessibilityHelper!!.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return accessibilityHelper!!.dispatchKeyEvent(event!!) || super.dispatchKeyEvent(event)
    }

    override fun dispatchHoverEvent(event: MotionEvent?): Boolean {
        return accessibilityHelper!!.dispatchHoverEvent(event!!) || super.dispatchHoverEvent(event)
    }

    private fun setupEditMode() {
        val exampleModuleValues = Array(EDIT_MODE_MODULE_COUNT) {false}
        val middle = EDIT_MODE_MODULE_COUNT / 2
        for (index in 0 until middle){
            exampleModuleValues[index] = true
        }
        moduleStatus = exampleModuleValues
    }

    private fun setupModuleRectangle(width: Int) {
        val availableWidth = width - paddingEnd - paddingStart
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        val maxHorizontalModules = min(horizontalModulesThatCanFit, moduleStatus.size)

        moduleRectangles = Array(moduleStatus.size){Rect()}
        for (moduleIndex in 0 until moduleRectangles.size){
            val column = moduleIndex % maxHorizontalModules
            val row = moduleIndex / maxHorizontalModules
            val leftEdge = paddingStart + (column * (shapeSize + spacing).toInt())
            val topEdge = paddingTop + (row * (shapeSize + spacing)).toInt()
            moduleRectangles[moduleIndex] = Rect(leftEdge, topEdge, leftEdge + shapeSize.toInt(),
                topEdge + shapeSize.toInt()
            )
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = specWidth - paddingStart - paddingEnd
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        maxHorizontalModules = min(horizontalModulesThatCanFit, moduleStatus.size)

        var desiredWidth = ((maxHorizontalModules * (shapeSize + spacing)) - spacing).toInt()
        desiredWidth += paddingStart + paddingEnd

        val rows = ((moduleStatus.size - 1) / maxHorizontalModules) + 1

        var desiredHeight = ((rows * (shapeSize + spacing)) - spacing).toInt()
        desiredHeight += paddingTop + paddingBottom

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        setupModuleRectangle(w)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action){
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val moduleIndex = findItemAtPoint(event.x, event.y)
                onModuleSelected(moduleIndex)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun onModuleSelected(moduleIndex: Int) {
        if (moduleIndex == invalidIndex)
            return

        moduleStatus[moduleIndex] = !moduleStatus[moduleIndex]
        invalidate()

        accessibilityHelper!!.invalidateVirtualView(moduleIndex)
        accessibilityHelper!!.sendEventForVirtualView(moduleIndex, AccessibilityNodeInfoCompat.ACTION_CLICK)
    }

    fun findItemAtPoint(x: Float, y: Float): Int {
        var moduleIndex = invalidIndex
        for (i in 0 until moduleRectangles.size){
            if (moduleRectangles[i].contains(x.toInt(), y.toInt())){
                moduleIndex = i
                break
            }
        }
        return moduleIndex
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (moduleIndex in 0 until moduleRectangles.size){
            if (shape == SHAPE_CIRCLE){
                val cx = (moduleRectangles[moduleIndex].centerX()).toFloat()
                val cy = (moduleRectangles[moduleIndex].centerY()).toFloat()

                if (moduleStatus[moduleIndex])
                    canvas.drawCircle(cx, cy, radius, paintFill!!)

                canvas.drawCircle(cx, cy, radius, paintOutline!!)
            }else {
                drawSquare(canvas, moduleIndex)
            }
        }
    }

    private fun drawSquare(canvas: Canvas, moduleIndex: Int){
        val moduleRectangle = moduleRectangles[moduleIndex]

        if (moduleStatus[moduleIndex])
            canvas.drawRect(moduleRectangle, paintFill!!)

        canvas.drawRect(moduleRectangle.left + (outlineWidth / 2),
            moduleRectangle.top + (outlineWidth / 2),
            moduleRectangle.right - (outlineWidth / 2),
            moduleRectangle.bottom - (outlineWidth / 2),
            paintOutline!!)
    }

    inner class ModuleStatusAccessibilityHelper(host: View) : ExploreByTouchHelper(host) {
        override fun getVirtualViewAt(x: Float, y: Float): Int {
            val moduleIndex = findItemAtPoint(x, y)
            return if (moduleIndex == invalidIndex) invalidIndex; else moduleIndex
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>?) {
            for (moduleIndex in moduleRectangles.indices)
                virtualViewIds?.add(moduleIndex)
        }

        override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            return when (action) {
                AccessibilityNodeInfoCompat.ACTION_CLICK -> {
                    onModuleSelected(virtualViewId)
                    true
                }
                else -> false
            }
        }

        override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
            node.apply {
                isFocusable = true
                setBoundsInParent(moduleRectangles[virtualViewId])
                contentDescription = "Module $virtualViewId"
                isCheckable = true
                isChecked = moduleStatus[virtualViewId]
                addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
        }

    }
}
