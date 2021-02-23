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

    var boxFace: Pair<Float, Float> = getSizeEdge(
        getPositionEdge(EdgeBoxBorder.LEFT, face.allContours),
        getPositionEdge(EdgeBoxBorder.RIGHT, face.allContours),
        getPositionEdge(EdgeBoxBorder.BOTTOM, face.allContours),
        getPositionEdge(EdgeBoxBorder.TOP, face.allContours)
    )

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
        Log.e(TAG, "onSuccess: size: Canvasss")
        // Decide color based on face ID
        val colorID =
            if (face.trackingId == null) 0 else abs(face.trackingId!! % NUM_COLORS)

        canvas.drawRect(
            getPositionEdge(EdgeBoxBorder.LEFT, face.allContours),
            getPositionEdge(EdgeBoxBorder.TOP, face.allContours),
            getPositionEdge(EdgeBoxBorder.RIGHT, face.allContours),
            getPositionEdge(EdgeBoxBorder.BOTTOM, face.allContours),
            boxPaints[colorID]
        )

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
    }

    private fun getPositionEdge(edge: EdgeBoxBorder, listFaceContour: List<FaceContour>): Float {
        when (edge) {
            EdgeBoxBorder.TOP -> {
                var min = Float.MAX_VALUE
                listFaceContour.forEach { faceContour ->
                    faceContour.points.minBy {
                        it.y
                    }?.let {
                        min = if (min > it.y) {
                            it.y
                        } else {
                            min
                        }
                    }
                }
                return min
            }
            EdgeBoxBorder.LEFT -> {
                var min = Float.MAX_VALUE
                listFaceContour.forEach { faceContour ->
                    faceContour.points.minBy {
                        it.x
                    }?.let {
                        min = if (min > it.x) {
                            it.x
                        } else {
                            min
                        }
                    }
                }
                return min
            }
            EdgeBoxBorder.BOTTOM -> {
                var max = 0.0f
                listFaceContour.forEach { faceContour ->
                    faceContour.points.maxBy {
                        it.y
                    }?.let {
                        max = if (max < it.y) {
                            it.y
                        } else {
                            max
                        }
                    }
                }
                return max
            }
            EdgeBoxBorder.RIGHT -> {
                var max = 0.0f
                listFaceContour.forEach { faceContour ->
                    faceContour.points.maxBy {
                        it.x
                    }?.let {
                        max = if (max < it.x) {
                            it.x
                        } else {
                            max
                        }
                    }
                }
                return max
            }
        }
    }

    // Return width height of box => scale box user fit box model
    private fun getSizeEdge(left: Float, right: Float, bottom: Float, top: Float): Pair<Float, Float> {
        // 2 image -> 4 size edge
        // w = x1 - x
        // h = y1 - y
        Log.e(TAG, "getSizeEdge: -----> $right ----> $top")
        val width = right - left
        val height = bottom - top
        return Pair(first = width, second = height)
    }

    companion object {
        private const val TAG = "FaceGraphic"
        private const val FACE_POSITION_RADIUS = 8.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val ID_Y_OFFSET = 40.0f
        private const val BOX_STROKE_WIDTH = 5.0f
        private const val NUM_COLORS = 10
        private val COLORS =
            arrayOf (
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

enum class EdgeBoxBorder {
    TOP, LEFT, RIGHT, BOTTOM
}
