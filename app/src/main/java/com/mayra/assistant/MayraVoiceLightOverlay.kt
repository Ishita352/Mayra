package com.mayra.assistant

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

/**
 * In-app edge-light layer used while Mayra is speaking.
 * It never changes the Activity background and never consumes touch/focus.
 */
class MayraVoiceLightOverlay(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFF70E8FF.toInt()
        strokeCap = Paint.Cap.ROUND
    }
    private var progress = 0f
    private var active = false
    private var pattern = 0
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1700L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: android.animation.Animator) {
                if (active) {
                    pattern = (pattern + 1) % 3
                    invalidate()
                }
            }
        })
    }

    fun setActive(value: Boolean) {
        active = value
        visibility = if (value) VISIBLE else GONE
        if (value) {
            if (!animator.isStarted) animator.start() else if (!animator.isRunning) animator.start()
        } else {
            animator.cancel()
            progress = 0f
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!active || width <= 0 || height <= 0) return
        val w = width.toFloat()
        val h = height.toFloat()
        paint.alpha = 220
        paint.strokeWidth = 6f

        when (pattern) {
            0 -> {
                val inset = 8f + 7f * progress
                canvas.drawRoundRect(inset, inset, w - inset, h - inset, 30f, 30f, paint)
            }
            1 -> {
                paint.alpha = 45
                paint.strokeWidth = 22f
                canvas.drawRoundRect(10f, 10f, w - 10f, h - 10f, 34f, 34f, paint)
                paint.alpha = 235
                paint.strokeWidth = 5f
                canvas.drawArc(
                    7f, 7f, w - 7f, h - 7f,
                    (progress * 360f) % 360f, 115f, false, paint
                )
            }
            else -> {
                paint.alpha = 225
                paint.strokeWidth = 5f
                canvas.drawLine(0f, 7f, w, 7f, paint)
                canvas.drawLine(0f, h - 7f, w, h - 7f, paint)
                canvas.drawLine(7f, 0f, 7f, h, paint)
                canvas.drawLine(w - 7f, 0f, w - 7f, h, paint)
                paint.strokeWidth = 8f
                val x = progress * w
                canvas.drawCircle(x, 7f, 6f, paint)
                canvas.drawCircle(w - x, h - 7f, 6f, paint)
            }
        }
    }

    companion object {
        fun attach(activity: android.app.Activity): MayraVoiceLightOverlay {
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            val old = root.findViewWithTag<MayraVoiceLightOverlay>("mayra_voice_light")
            if (old != null) return old
            val view = MayraVoiceLightOverlay(activity).apply {
                tag = "mayra_voice_light"
                visibility = GONE
                isClickable = false
                isFocusable = false
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            root.addView(view, FrameLayout.LayoutParams(-1, -1))
            return view
        }
    }
}
