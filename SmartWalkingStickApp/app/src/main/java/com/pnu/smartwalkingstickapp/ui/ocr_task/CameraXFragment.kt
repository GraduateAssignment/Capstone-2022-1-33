package com.pnu.smartwalkingstickapp.ui.ocr_task

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentCameraXBinding
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class CameraXFragment : Fragment() {
    private lateinit var binding: FragmentCameraXBinding

    private lateinit var _viewFinder: PreviewView
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture

    private lateinit var safeContext: Context

    private lateinit var outputDirectory: File

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private lateinit var textToSpeech: TextToSpeech

    private lateinit var detector: ObjectDetector

    private lateinit var whereFrom: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraXBinding.inflate(inflater, container, false)
        _viewFinder = binding.viewFinder
        outputDirectory = getOutputDirectory()

        textToSpeech = TextToSpeech(safeContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                // TODO: 한글 패치
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS","해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }
            }
        })
        whereFrom = arguments?.getString("feature").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(val cameraPermission = ContextCompat.checkSelfPermission(safeContext, Manifest.permission.CAMERA)){
            PackageManager.PERMISSION_GRANTED ->  startCamera()
            else -> requestPermission()
        }
        if(whereFrom == "detect") {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.5f)
                .build()
            detector = ObjectDetector.createFromFileAndOptions(
                safeContext, // the application context
                "model.tflite", // must be same as the filename in assets folder
                options
            )
        }
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.CAMERA), 99)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(safeContext)
        val cameraExecutor = ContextCompat.getMainExecutor(safeContext)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(_viewFinder.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                // enable the following line if RGBA output is needed.
                // .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            if (whereFrom == "detect"){
                imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                    val image = TensorImage.fromBitmap(imageProxy.toBitmap())
                    val results = detector.detect(image)
                    val people = debugPrint(results).toString()
                    playTextToSpeech(people)
                    Toast.makeText(safeContext, people, Toast.LENGTH_SHORT).show()
                    imageProxy.close()
                })
            }
            else {
                imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    // insert your code here.
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                        val result = recognizer.process(image)
                            .addOnSuccessListener {
                                for (block in it.textBlocks) {
                                    playTextToSpeech(block.text)
                                    Toast.makeText(safeContext, block.text, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Log.d("Fail", "!!")
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                })
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)
            } catch(exception: Exception) {
                Log.e(TAG, "Use case binding failed", exception)
            }
        }, ContextCompat.getMainExecutor(safeContext))
    }
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create timestamped output file to hold the image
        val photoFile = File(outputDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(safeContext), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(safeContext, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
        })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }

    private fun playTextToSpeech(text: String){
        Log.d("Speech:", text)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun debugPrint(results : List<Detection>): Int {
        var peopleNum = 0
        for ((i, obj) in results.withIndex()) {

            for ((j, category) in obj.categories.withIndex()) {
                if (category.label == "person") {
                    peopleNum++
                }
            }
        }
        return peopleNum
//        Log.v("Check:", results.size.toString())
//        for ((i, obj) in results.withIndex()) {
//            val box = obj.boundingBox
//
//            Log.v(TAG, "Detected object: ${i} ")
//            Log.v(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")
//
//            for ((j, category) in obj.categories.withIndex()) {
//                Log.v(TAG, "    Label $j: ${category.label}")
//                val confidence: Int = category.score.times(100).toInt()
//                Log.v(TAG, "    Confidence: ${confidence}%")
//            }
//        }
    }
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}