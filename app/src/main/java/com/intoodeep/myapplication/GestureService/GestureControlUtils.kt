package com.intoodeep.myapplication.GestureService
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.view.accessibility.AccessibilityEvent
import android.graphics.Path

class GestureControlUtil{
    private fun doRightThenDownDrag() {
        val dragRightPath = Path().apply {
            moveTo(200f, 200f)
            lineTo(400f, 200f)
        }
        val dragRightDuration = 500L // 0.5 second

        // The starting point of the second path must match
        // the ending point of the first path.
        val dragDownPath = Path().apply {
            moveTo(400f, 200f)
            lineTo(400f, 400f)
        }
        val dragDownDuration = 500L
        val rightThenDownDrag = GestureDescription.StrokeDescription(
            dragRightPath,
            0L,
            dragRightDuration,
            true
        ).apply {
            continueStroke(dragDownPath, dragRightDuration, dragDownDuration, false)
        }
    }
}