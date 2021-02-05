package com.example.myapplication.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class ShapeOvalCustom(context: Context) : View(context) {
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint =  Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.GRAY
        val oval1 = RectF(0f, 0f, 250f, 250f)
        canvas?.drawOval(oval1, paint)
    }
}