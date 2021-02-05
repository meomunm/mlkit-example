package com.example.myapplication.screen

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R

const val PIC_CROP: Int = 1

class ResultActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ResultActivity"
    }

    private lateinit var ivResult: ImageView
    private lateinit var originalBmp: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        initView()
//        cropImageTest()
        customMatrixImage()

    }

    private fun cropImageTest() {
        ivResult.setImageBitmap(Bitmap.createBitmap(originalBmp, 309, 289, 869, 979))

    }

    private fun customMatrixImage() {
        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val cropBitmap = Bitmap.createBitmap((ivResult.drawable as BitmapDrawable).bitmap,  0, 0, 500, 800, matrix, true)
        ivResult.setImageBitmap(cropBitmap)

    }

    private fun initView() {
        ivResult = findViewById(R.id.iv_result)
//        val byteArray = intent.getByteArrayExtra("image")
//        originalBmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        Log.e(TAG, "initView: ${ivResult.imageMatrix}")
    }

    private fun cropImage() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.result)
        val mutableBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val bitmap2 = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val polyCanvas = Canvas(bitmap2)
//        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.strokeWidth = 9f

        val path = Path()

        path.moveTo(150f, 0f)
        path.lineTo(230f, 120f)
        path.lineTo(290f, 160f)
        path.lineTo(150f, 170f)
        path.lineTo(70f, 200f)
        path.lineTo(150f, 0f)
        polyCanvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        polyCanvas.drawBitmap(mutableBitmap, 0f, 0f, paint)

        ivResult.setImageBitmap(bitmap2)

//        val uri2bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, bitmap2))
//        } else {
//            MediaStore.Images.Media.getBitmap(this.contentResolver, bitmap2)
//        }
//        performCrop(uri2bitmap)
    }

    private fun performCrop(picUri: Uri) {
        val cropIntent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(picUri, "image/*")
        cropIntent.setDataAndType(picUri, "image/*")
        cropIntent.putExtra("crop", true)
        cropIntent.putExtra("aspectX", 1)
        cropIntent.putExtra("aspectY", 1)
        cropIntent.putExtra("outputX", 128)
        cropIntent.putExtra("outputY", 128)
        cropIntent.putExtra("return-data", true)
        startActivityForResult(cropIntent, PIC_CROP)
    }
}