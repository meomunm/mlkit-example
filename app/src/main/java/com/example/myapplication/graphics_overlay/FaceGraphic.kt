package com.example.myapplication.graphics_overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import com.example.myapplication.custom.GraphicOverlay
import com.example.myapplication.singleton.SwitchStateBuilder
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.abs

class FaceGraphic constructor(overlay: GraphicOverlay?, private val face: Face) :
    GraphicOverlay.Graphic(overlay) {
    private val facePositionPaint: Paint
    private val numColors = COLORS.size
    private val idPaints = Array(numColors) { Paint() }
    private val boxPaints = Array(numColors) { Paint() }
    private val labelPaints = Array(numColors) { Paint() }

    init {
        val selectedColor = Color.WHITE
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
        for (i in 0 until numColors) {
            idPaints[i] = Paint()
            idPaints[i].color = COLORS[i][0]
            idPaints[i].textSize =
                ID_TEXT_SIZE
            boxPaints[i] = Paint()
            boxPaints[i].color = COLORS[i][1]
            boxPaints[i].style = Paint.Style.STROKE
            boxPaints[i].strokeWidth =
                BOX_STROKE_WIDTH
            labelPaints[i] = Paint()
            labelPaints[i].color = COLORS[i][1]
            labelPaints[i].style = Paint.Style.FILL
        }
    }

    /** Draws the face annotations for position on the supplied canvas.  */
    override fun draw(canvas: Canvas) {
        // Draws a circle at the position of the detected face, with the face's track id below.
        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        // Calculate positions.
        val left = x - scale(face.boundingBox.width() / 2.0f)
        val top = y - scale(face.boundingBox.height() / 2.0f)
        val right = x + scale(face.boundingBox.width() / 2.0f)
        val bottom = y + scale(face.boundingBox.height() / 2.0f)

        // Decide color based on face ID
        val colorID =
            if (face.trackingId == null) 0 else abs(face.trackingId!! % NUM_COLORS)

        Log.e(TAG, "HungTG => draw: $left, $top, $right, $bottom,")
        // Draw shape box border face
        canvas.drawRect(left, top, right, bottom, boxPaints[colorID])
        // Draws all face contours (dots)
        Log.e(TAG, "draw: size {${face.allContours.size}}")

        if (SwitchStateBuilder.isDetectLiveCamera) {
            for (contour in face.allContours) {
                for (point in contour.points) {
                    canvas.drawPoint(translateX(point.x), translateY(point.y), facePositionPaint)
                }
            }
        } else {
            for (contour in face.allContours) {
                if (contour.faceContourType == FaceContour.FACE) {
                    for (i in contour.points.indices) {
                        if (i != 0) {
                            canvas.drawLine(
                                (contour.points[i - 1].x),
                                (contour.points[i - 1].y),
                                (contour.points[i].x),
                                (contour.points[i].y),
                                facePositionPaint
                            )
                        } else {
                            canvas.drawLine(
                                (contour.points[0].x),
                                (contour.points[0].y),
                                (contour.points[contour.points.size - 1].x),
                                (contour.points[contour.points.size - 1].y),
                                facePositionPaint
                            )
                        }
                    }
                }
            }
        }
        // TODO: crop face to bitmap
    }

    companion object {
        private const val TAG = "FaceGraphic"
        private const val FACE_POSITION_RADIUS = 8.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val ID_Y_OFFSET = 40.0f
        private const val BOX_STROKE_WIDTH = 5.0f
        private const val NUM_COLORS = 10
        private val COLORS =
            arrayOf(
                intArrayOf(Color.BLACK, Color.WHITE),
                intArrayOf(Color.WHITE, Color.MAGENTA),
                intArrayOf(Color.BLACK, Color.LTGRAY),
                intArrayOf(Color.WHITE, Color.RED),
                intArrayOf(Color.WHITE, Color.BLUE),
                intArrayOf(Color.WHITE, Color.DKGRAY),
                intArrayOf(Color.BLACK, Color.CYAN),
                intArrayOf(Color.BLACK, Color.YELLOW),
                intArrayOf(Color.WHITE, Color.BLACK),
                intArrayOf(Color.BLACK, Color.GREEN)
            )
    }
}
