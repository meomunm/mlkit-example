package com.example.myapplication.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.custom.CameraSource
import com.example.myapplication.custom.CameraSourcePreview
import com.example.myapplication.custom.GraphicOverlay
import com.example.myapplication.custom.ShapeOvalCustom
import com.example.myapplication.detector.CallbackInitSuccess
import com.example.myapplication.detector.FaceDetectorProcessor
import com.example.myapplication.preference.PreferenceUtils
import com.example.myapplication.singleton.SwitchStateBuilder
import com.google.mlkit.vision.face.Face
import java.io.IOException
import java.util.ArrayList

class MainActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    CompoundButton.OnCheckedChangeListener {
    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var customShapeOval: ShapeOvalCustom? = null

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    override fun onResume() {
        SwitchStateBuilder.isDetectLiveCamera = true
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchStateBuilder.isDetectLiveCamera = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vision_live_preview)
        preview = findViewById(R.id.preview_view)
        if (preview == null) {
            Log.d(TAG, "Preview is null")
        }

        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)

        if (allPermissionsGranted()) {
            createCameraSource(FACE_DETECTION_TAKE_PICTURE)
        } else {
            runtimePermissions
        }

        findViewById<ImageView>(R.id.btn_insert_photo).setOnClickListener { view: View ->
            Log.e(TAG, "onCreate: btn_insert_photo ")
            startChooseImageIntentForResult()
        }
    }

    private fun startChooseImageIntentForResult() {
        Log.e(TAG, "onCreate: startChooseImageIntentForResult")

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_CODE_CHOOSE_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            //start preview activity
            val intent = Intent(this, PreviewImageActivity::class.java)
            intent.data = data!!.data
            startActivity(intent)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    private fun createCameraSource(model: String) {

        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay)
        }
        try {
            when (model) {
                FACE_DETECTION_TAKE_PICTURE -> {
                    Log.i(TAG, "Using Face Detector Processor")
                    val faceDetectorOptions =
                        PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)
                    cameraSource!!.setMachineLearningFrameProcessor(
                        FaceDetectorProcessor(this, faceDetectorOptions)
                    )
                }
                else -> Log.e(TAG, "Unknown model: $model")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: $model", e)
            Toast.makeText(
                applicationContext, "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(
                    this,
                    permission
                )
            ) {
                return false
            }
        }
        return true
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
            } else {
                cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
            }
        }
        preview?.stop()
        startCameraSource()
    }

    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }



    companion object {
        private const val REQUEST_CODE_CHOOSE_IMAGE = 1002
        private const val FACE_DETECTION_TAKE_PICTURE = "Get a pictures"

        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUESTS = 1
        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}