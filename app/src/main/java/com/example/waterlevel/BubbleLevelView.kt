package com.example.waterlevel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.math.sqrt

class BubbleLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#455A64")
    }

    private val centerCrossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#78909C")
    }

    private val levelZonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1A4CAF50")
    }

    private val levelZoneBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#4CAF50")
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bubbleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#ECEFF1")
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.parseColor("#30455A64")
    }

    // Tilt in graden, max ±90
    var tiltX: Float = 0f
        set(value) {
            field = value.coerceIn(-90f, 90f)
            invalidate()
        }

    var tiltY: Float = 0f
        set(value) {
            field = value.coerceIn(-90f, 90f)
            invalidate()
        }

    private val levelThreshold = 2.5f // graden tolerantie voor "waterpas"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 20f
        val bubbleRadius = radius * 0.12f
        val levelZoneRadius = radius * 0.15f

        // Achtergrond cirkel
        canvas.drawCircle(cx, cy, radius, backgroundPaint)

        // Grid lijnen
        val gridStep = radius / 4f
        for (i in 1..3) {
            canvas.drawCircle(cx, cy, gridStep * i, gridPaint)
        }
        canvas.drawLine(cx - radius, cy, cx + radius, cy, gridPaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, gridPaint)

        // Groene zone in het midden
        canvas.drawCircle(cx, cy, levelZoneRadius, levelZonePaint)
        canvas.drawCircle(cx, cy, levelZoneRadius, levelZoneBorderPaint)

        // Buitenste ring
        canvas.drawCircle(cx, cy, radius, outerRingPaint)

        // Kruis in het midden
        val crossSize = 12f
        canvas.drawLine(cx - crossSize, cy, cx + crossSize, cy, centerCrossPaint)
        canvas.drawLine(cx, cy - crossSize, cx, cy + crossSize, centerCrossPaint)

        // Bereken bubble positie op basis van tilt
        // Tilt van 0° = center, tilt van 45° = rand
        val maxTiltForEdge = 45f
        val normalizedX = (tiltX / maxTiltForEdge).coerceIn(-1f, 1f)
        val normalizedY = (tiltY / maxTiltForEdge).coerceIn(-1f, 1f)

        // Zorg dat bubble binnen de cirkel blijft
        val maxBubbleRadius = radius - bubbleRadius - 4f
        var bubbleX = cx + normalizedX * maxBubbleRadius
        var bubbleY = cy - normalizedY * maxBubbleRadius

        // Clip naar cirkelrand
        val dist = sqrt((bubbleX - cx) * (bubbleX - cx) + (bubbleY - cy) * (bubbleY - cy))
        if (dist > maxBubbleRadius) {
            val scale = maxBubbleRadius / dist
            bubbleX = cx + (bubbleX - cx) * scale
            bubbleY = cy + (bubbleY - cy) * scale
        }

        // Bepaal kleur op basis van of het waterpas is
        val isLevel = isSurfaceLevel()
        bubblePaint.color = if (isLevel) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")

        // Teken bubble
        canvas.drawCircle(bubbleX, bubbleY, bubbleRadius, bubblePaint)
        canvas.drawCircle(bubbleX, bubbleY, bubbleRadius, bubbleBorderPaint)

        // Glans effect op bubble
        val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(80, 255, 255, 255)
        }
        canvas.drawCircle(
            bubbleX - bubbleRadius * 0.25f,
            bubbleY - bubbleRadius * 0.25f,
            bubbleRadius * 0.4f,
            shinePaint
        )
    }

    fun isSurfaceLevel(): Boolean {
        return Math.abs(tiltX) <= levelThreshold && Math.abs(tiltY) <= levelThreshold
    }
}
