package com.example.myapplication.screen

import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.myapplication.R
import com.example.myapplication.custom.GraphicOverlay
import com.example.myapplication.detector.FaceDetectorProcessor
import com.example.myapplication.preference.BitmapUtils
import com.example.myapplication.preference.PreferenceUtils
import com.example.myapplication.preference.VisionImageProcessor
import com.example.myapplication.singleton.SwitchStateBuilder
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max

class PreviewImageActivity : AppCompatActivity() {
    private lateinit var ivPreview: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnDone: ImageView

    private var imageUri: Uri? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var graphicOverlaySecond: GraphicOverlay? = null
    private var imageMaxWidth = 0
    private var selectedSize: String? = SIZE_SCREEN
    private var imageMaxHeight = 0
    private var isLandScape = false
    private var imageProcessor: VisionImageProcessor? = null

    private val targetedWidthHeight: Pair<Int, Int>
        get() {
            val targetWidth: Int
            val targetHeight: Int
            when (selectedSize) {
                SIZE_SCREEN -> {
                    targetWidth = imageMaxWidth
                    targetHeight = imageMaxHeight
                }
                SIZE_640_480 -> {
                    targetWidth = if (isLandScape) 640 else 480
                    targetHeight = if (isLandScape) 480 else 640
                }
                SIZE_1024_768 -> {
                    targetWidth = if (isLandScape) 1024 else 768
                    targetHeight = if (isLandScape) 768 else 1024
                }
                else -> throw IllegalStateException("Unknown size")
            }
            return Pair(targetWidth, targetHeight)
        }

    override fun onResume() {
        SwitchStateBuilder.isDetectLiveCamera = false
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchStateBuilder.isDetectLiveCamera = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        initView()
        listener()
        isLandScape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (savedInstanceState != null) {
            imageMaxWidth =
                savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight =
                savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
            selectedSize =
                savedInstanceState.getString(KEY_SELECTED_SIZE)
        }
        val rootView = findViewById<View>(R.id.root_view)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    imageMaxWidth = rootView.width
                    imageMaxHeight = rootView.height
                    if (SIZE_SCREEN == selectedSize) {
                        tryReloadAndDetectedImage()
                    }
                }
            })
    }

    private fun initView() {
        imageUri = intent.data
        ivPreview = findViewById(R.id.iv_preview_image)
        btnBack = findViewById(R.id.btn_back)
        btnDone = findViewById(R.id.btn_done)
        graphicOverlay = findViewById(R.id.graphic_overlay)
        graphicOverlaySecond = findViewById(R.id.graphic_overlay_second)
    }

    private fun listener() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
//            val bmp = BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri)!!
//            val stream = ByteArrayOutputStream()
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val byteArray: ByteArray = stream.toByteArray()
            val intent = Intent(this, ResultActivity::class.java)
//            intent.putExtra("image", byteArray)
            startActivity(intent)
        }

        val faceDetectorOptions =
            PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)
        imageProcessor =
            FaceDetectorProcessor(this, faceDetectorOptions)
    }

    private fun tryReloadAndDetectedImage() {
        try {
            if (imageUri == null) return
            if (SIZE_SCREEN == selectedSize && imageMaxWidth == 0) return

            val imageBitmap = BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?: return

            graphicOverlay!!.clear()
            graphicOverlaySecond!!.clear()

            val targetedSize = targetedWidthHeight

            val scaleFactor = max(
                imageBitmap.width.toFloat() / targetedSize.first.toFloat(),
                imageBitmap.height.toFloat() / targetedSize.second.toFloat()
            )
            val resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.width / scaleFactor).toInt(),
                (imageBitmap.height / scaleFactor).toInt(),
                true
            )
            ivPreview.setImageBitmap(resizedBitmap)
            Log.e(TAG, "tryReloadAndDetectedImage: ")
            if (imageProcessor != null) {
                graphicOverlay!!.setImageSourceInfo(
                    resizedBitmap.width, resizedBitmap.height, /* isFlipped= */false
                )
                val secondBm = (AppCompatResources.getDrawable(this, R.drawable.result_not_crop) as BitmapDrawable).bitmap
                graphicOverlaySecond!!.setImageSourceInfo(
                    secondBm.width, secondBm.height, /* isFlipped= */false
                )

                imageProcessor!!.processBitmap(resizedBitmap, graphicOverlay)

                imageProcessor!!.processBitmap(secondBm, graphicOverlaySecond)
            } else {
                Log.e(
                    TAG,
                    "Null imageProcessor, please check adb logs for imageProcessor creation error"
                )
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Error retrieving saved image"
            )
            imageUri = null
        }
    }

    companion object {
        private const val SIZE_SCREEN = "w:screen"
        private const val SIZE_1024_768 = "w:1024"
        private const val SIZE_640_480 = "w:640"
        private const val TAG = "PreviewImageActivity"

        private const val KEY_IMAGE_URI = "com.google.mlkit.vision.demo.KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH = "com.google.mlkit.vision.demo.KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT = "com.google.mlkit.vision.demo.KEY_IMAGE_MAX_HEIGHT"
        private const val KEY_SELECTED_SIZE = "com.google.mlkit.vision.demo.KEY_SELECTED_SIZE"
    }

    fun getCropImage(width: Int, height: Int, path: Path, bitmapMain: Bitmap): Bitmap {
        val resultingImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultingImage)

        val paint = Paint()
        paint.isAntiAlias = true

        canvas.drawPath(path, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmapMain, 0f, 0f, paint)

        return resultingImage
    }
}