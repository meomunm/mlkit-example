package com.example.myapplication.preference;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.myapplication.custom.GraphicOverlay;
import com.example.myapplication.custom.GraphicOverlay.Graphic;

public class CameraImageGraphic extends Graphic {
    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
    }
}
