package org.pytorch.helloworld

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.utils.Oscillator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xiasuhuei321.loadingdialog.view.LoadingDialog
import org.pytorch.*
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var ImageFeatureSet: Array<FloatArray?>
    var mSharedPref: SharedPreferences? = null
    var mSharedPref_image: SharedPreferences? = null
    private val ImageSet: MutableList<Bitmap?> = ArrayList()
    private var OriginImageSet: MutableList<File> = ArrayList()
    private val OriginImageSetToPath: List<File> = ArrayList()
    private val OriginImagePath: MutableList<String> = ArrayList()
    private var IsCheckPhotoUpload = false
    private var IsCheckSavePath = false
    private var IsCheckLoadFailed = false
    private var startDateStr: String? = null
    private var endDateStr: String? = null
    private var progressBar: ProgressBar? = null

    // Thread class for loading images
    inner class MyThread_load_image(start: Int, end: Int) : Thread() {
        private var start = 0
        private var end = 100

        init {
            this.start = start
            this.end = end
        }

        override fun run() {
            // Loop through image range and load each image
            for (i in start until end) {
                try {
                    // Load bitmap image from file path
                    val bitmap = BitmapFactory.decodeFile(OriginImageSet[i].path, getBitmapOption(4))
                    // Add bitmap to ImageSet
                    ImageSet.add(bitmap)
                } catch (e: OutOfMemoryError) {
                    // Catch out of memory exception
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        // Get the ShareData instance
        val sharedata = application as ShareData

        // Create and show the loading dialog
        val ld_suc = LoadingDialog(this)
        ld_suc.setLoadingText("Loading")
            .setSuccessText("Images fully loaded!") // Show text when the images are successfully loaded
            .setFailedText("Failed to load images")
            .show()

        // Get the ProgressBar and EditText views
        val progressBar = findViewById(R.id.progress_Bar_1) as ProgressBar
        val editText = findViewById<View>(R.id.textinput) as EditText
        editText.isEnabled = false // Disable the EditText

        // Check if originImagePath is null
        if (sharedata.originImagePath == null) {
            val ImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AI/"
            val Imagefile = File(ImagePath)
            val fileSet = Imagefile.listFiles()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy")

            // Iterate through each file in the file set
            for (file in fileSet) {
                if (file.name.endsWith(".jpg") || file.name.endsWith(".png")) {
                    val lastModified = file.lastModified()
                    val date = Date(lastModified)
                    startDateStr = sharedata.startData
                    endDateStr = sharedata.endData
                    if (startDateStr == null || endDateStr == null) {
                        OriginImageSet.add(file)
                        OriginImagePath.add(file.absolutePath)
                        Log.d("", "URI path: " + Uri.fromFile(file))
                        IsCheckSavePath = true
                    } else {
                        var startDate: Date? = null
                        startDate = try {
                            dateFormat.parse(startDateStr)
                        } catch (e: ParseException) {
                            throw RuntimeException(e)
                        }
                        var endDate: Date? = null
                        endDate = try {
                            dateFormat.parse(endDateStr)
                        } catch (e: ParseException) {
                            throw RuntimeException(e)
                        }
                        if (date.after(startDate) && date.before(endDate)) {
                            OriginImageSet.add(file)
                            OriginImagePath.add(file.absolutePath)
                            val formattedDate = dateFormat.format(date)
                            Log.d(
                                "",
                                "File name: " + file.name + "File path: " + file.absolutePath + " File creation date: " + formattedDate
                            )
                        }
                        if (OriginImageSet.size == 0) {
                            val toast = Toast.makeText(
                                this@MainActivity,
                                "There are no images in the specified time period, please change the time period",
                                Toast.LENGTH_LONG
                            )
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                            val intent = Intent(this, ChoiceActivity::class.java)
                            val options = ActivityOptions.makeCustomAnimation(
                                this,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                            startActivity(intent, options.toBundle())
                        }
                    }
                    Log.d("", "URI path: " + Uri.fromFile(file))
                }
            }
            if (IsCheckSavePath) {
                save_image_path()
            }
            if (OriginImageSet.size != 0) {
                IsCheckPhotoUpload = true
                IsCheckLoadFailed = true
            }
        } else {
            IsCheckLoadFailed = true
            OriginImageSet = addImagesToList(sharedata.originImagePath, OriginImageSet)
        }

        if (IsCheckLoadFailed) {
            // Create a thread group to load images
            val myLoadImageThread = Generate_ThreadGroup(8, OriginImageSet.size)
            // Start the threads to load images
            for (i in myLoadImageThread) {
                i!!.start()
            }
            try {
                // Wait for all threads to finish loading images
                for (i in myLoadImageThread) {
                    i!!.join()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // Create a copy of the loaded images to share
            val ShareImageSet: List<Bitmap?> = ArrayList(ImageSet)
            sharedata.imageSet = ShareImageSet
            var module_image: Module? = null
            val module_text: Module
            try {
                // Load PyTorch models for image and text encoding
                module_image = LiteModuleLoader.load(assetFilePath(this, "image_encode.ptl"))
                module_text = LiteModuleLoader.load(assetFilePath(this, "tiny_text_encode.ptl"))
                sharedata.setModule_image(module_image)
                sharedata.module_text = module_text
            } catch (e: IOException) {
                Log.e("PytorchHelloWorld", "Error reading assets", e)
                finish()
            }
            // Hide the progress bar
            progressBar.setVisibility(View.GONE)
            if (IsCheckPhotoUpload) {
                // Prepare image features for similarity calculation
                var t_tensor: Tensor? = null
                ImageFeatureSet = arrayOfNulls(ImageSet.size)
                for (i in ImageSet.indices) {
                    // Resize the image to fit the input tensor dimensions
                    ImageSet[i] = resizeImage(ImageSet[i], INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT)
                    // Convert the image to a float tensor
                    t_tensor = TensorImageUtils.bitmapToFloat32Tensor(
                        ImageSet[i],
                        TORCHVISION_NORM_MEAN_RGB,
                        TORCHVISION_NORM_STD_RGB,
                        MemoryFormat.CHANNELS_LAST
                    )
                    // Encode the image tensor to a feature tensor
                    t_tensor = module_image!!.forward(IValue.from(t_tensor)).toTensor()
                    ImageFeatureSet[i] = t_tensor.dataAsFloatArray
                }
                // Hide the progress bar
                progressBar.setVisibility(View.GONE)
                // Notify the user that the images have been loaded successfully
                ld_suc.loadSuccess()
                if (IsCheckSavePath) {
                    // Save the encoded image features to a file
                    save()
                }
                sharedata.imageSetFeature = ImageFeatureSet
            } else {
                // Notify the user that the images have been loaded successfully
                ld_suc.loadSuccess()
            }
            try {
                // Initialize Simple_tokenize with a Tokenizer object that reads from "vocab.txt"
                val Simple_tokenize = Tokenizer(assetFilePath(this, "vocab.txt"))
                // Enable editing of the editText field
                editText.isEnabled = true
                // Set an action listener to editText for when the user inputs text and presses the search button
                editText.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        // Get the text entered by the user
                        val text = editText.text.toString()
                        try {
                            // Tokenize the text using the Simple_tokenize Tokenizer
                            val allToken = tokenize(text, 77, Simple_tokenize)
                            // Convert the tokenized text into a tensor
                            val textTensor = Tensor.fromBlob(allToken, longArrayOf(1, 77))
                            // Get the text feature tensor by passing the text tensor to the text module
                            val a = sharedata.module_text
                            val Textfeature =
                                a!!.forward(IValue.from(textTensor)).toTensor().dataAsFloatArray
                            // Calculate the similarity of the text feature to the feature of each image
                            val TextSimilarity =
                                ConSimilarity(sharedata.imageSetFeature, Textfeature)
                            // Sort the image indices by their similarity to the text feature
                            val Index = Arraysort(TextSimilarity)
                            // Save the calculated similarity scores to the shared data
                            sharedata.similarity = TextSimilarity
                            // Create an intent to start the Text2ImageActivity and pass it the text and sorted indices
                            val intent = Intent()
                            intent.putExtra("text", text)
                            intent.putExtra("Index", Index)
                            intent.setClass(this@MainActivity, Text2ImageActivity::class.java)
                            startActivity(intent)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    }
                    false
                }
            } catch (e: IOException) {
                // If there is an exception while initializing Simple_tokenize, print the stack trace
                e.printStackTrace()
            }
    }else {
            ld_suc.loadFailed()
        }}


    // This function takes in a text string, context length, and a tokenizer and returns an integer array of tokenized text
    // The start token and end token are retrieved from the tokenizer
    // The text is encoded using the tokenizer and then added to the integer array of tokens
    // The final integer array is returned
    fun tokenize(text: String, context_length: Int, Simple_tokenize: Tokenizer?): IntArray {
        val startToken: Int = Tokenizer.Companion.encoder.get("")!!
        val endToken: Int = Tokenizer.Companion.encoder.get("")!!
        val textToken: IntArray = Tokenizer.Companion.encode(text)
        val allToken = IntArray(context_length)
        for (i in 0 until context_length) {
            if (i == 0) {
                allToken[i] = startToken
            } else if (i < textToken.size + 1 && i > 0) {
                allToken[i] = textToken[i - 1]
            } else if (i == textToken.size + 1) {
                allToken[i] = endToken
            }
        }
        return allToken
    }

    // This function takes in a list of image file paths and adds them to a mutable list of image files
    // If the file exists, it is added to the list
    // The final mutable list of image files is returned
    private fun addImagesToList(
        imagePathList: List<String?>?,
        imageFileList: MutableList<File>
    ): MutableList<File> {
        for (imagePath in imagePathList!!) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageFileList.add(imageFile)
            }
        }
        return imageFileList
    }

    fun save() {
        // Retrieve SharedPreferences object
        mSharedPref = getSharedPreferences("myPrefs", MODE_PRIVATE)
        // Retrieve Editor object
        val mEditor = mSharedPref?.edit()
        // Initialize Gson object with custom FloatArrayAdapter
        val gson = GsonBuilder()
            .registerTypeAdapter(FloatArray::class.java, FloatArrayAdapter())
            .create()
        // Convert ImageFeatureSet to JSON string using Gson
        val json = gson.toJson(ImageFeatureSet)
        // Save JSON string to SharedPreferences
        mEditor?.putString("photo_urls", json)
        mEditor?.apply()
    }

    fun save_image_path() {
        // Retrieve SharedPreferences object
        mSharedPref_image = getSharedPreferences("myPrefs_images_path", MODE_PRIVATE)
        // Retrieve Editor object
        val mEditor = mSharedPref_image?.edit()
        // Initialize Gson object
        val gson = Gson()
        // Convert OriginImagePath to JSON string using Gson
        val json = gson.toJson(OriginImagePath)
        // Save JSON string to SharedPreferences
        mEditor?.putString("images_path", json)
        mEditor?.apply()
    }

    private fun getBitmapOption(inSampleSize: Int): BitmapFactory.Options {
        // Request garbage collection
        System.gc()
        // Initialize BitmapFactory.Options object
        val options = BitmapFactory.Options()
        // Set inJustDecodeBounds and inPurgeable options to true
        options.inJustDecodeBounds = true
        options.inPurgeable = true
        // Set inSampleSize option
        options.inSampleSize = inSampleSize
        // Set inJustDecodeBounds to false
        options.inJustDecodeBounds = false
        return options
    }

    // This function generates an array of MyThread_load_image objects based on the given number of threads and image size.
    // Each MyThread_load_image object represents a portion of the image to be loaded and processed.
    fun Generate_ThreadGroup(num_thread: Int, imageSize: Int): Array<MyThread_load_image?> {
        val myThread = arrayOfNulls<MyThread_load_image>(num_thread)
        val basesize = imageSize / num_thread
        val lastsize = imageSize % num_thread
        for (i in myThread.indices) {
            // Distribute the workload evenly across the threads except for the last thread which may have more work to do.
            if (i != myThread.size - 1) {
                myThread[i] = MyThread_load_image(i * basesize, (i + 1) * basesize)
            } else {
                myThread[i] = MyThread_load_image(i * basesize, (i + 1) * basesize + lastsize)
            }
        }
        return myThread
    }

    // This is a companion object that contains utility functions and constants used throughout the application.
    companion object {
        // These arrays contain the mean and standard deviation values used for normalization of RGB image tensors.
        var TORCHVISION_NORM_MEAN_RGB = floatArrayOf(0.481f, 0.458f, 0.408f)
        var TORCHVISION_NORM_STD_RGB = floatArrayOf(0.269f, 0.261f, 0.276f)
        var INPUT_TENSOR_WIDTH = 224
        var INPUT_TENSOR_HEIGHT = 224

        // This function returns the file path of an asset given its name.
        // If the file doesn't exist, it is created by copying the asset file to the app's internal storage.
        @kotlin.jvm.Throws(IOException::class)
        fun assetFilePath(context: Context, assetName: String?): String {
            val file = File(context.filesDir, assetName)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
            context.assets.open(assetName!!).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        }

        // This function calculates the cosine similarity between the image and text features.
        fun ConSimilarity(ImageFeatures: Array<FloatArray?>?, TextFeatures: FloatArray): FloatArray {
            val Similarity = FloatArray(ImageFeatures!!.size)
            for (i in ImageFeatures.indices) {
                for (j in ImageFeatures[i]!!.indices) {
                    Similarity[i] += ImageFeatures[i]!![j] * TextFeatures[j]
                }
            }
            var sum = 0f
            for (i in Similarity.indices) {
                Similarity[i] = Math.exp((10 * Similarity[i]).toDouble()).toFloat()
                sum += Similarity[i]
            }
            for (i in Similarity.indices) {
                Similarity[i] /= sum
            }
            return Similarity
        }
        // This function takes in a FloatArray as input and returns an IntArray after sorting the indices of the input array in descending order
        fun Arraysort(arr: FloatArray): IntArray {
            var temp: Float
            var index: Int
            val k = arr.size
            val Index = IntArray(k)
            for (i in 0 until k) {
                Index[i] = i
            }
            for (i in arr.indices) {
                for (j in 0 until arr.size - i - 1) {
                    if (arr[j] < arr[j + 1]) {
                        temp = arr[j]
                        arr[j] = arr[j + 1]
                        arr[j + 1] = temp
                        index = Index[j]
                        Index[j] = Index[j + 1]
                        Index[j + 1] = index
                    }
                }
            }
            return Index
        }

        // This function takes in a Bitmap object, along with width and height values, and returns a resized Bitmap object
        fun resizeImage(bitmap: Bitmap?, w: Int, h: Int): Bitmap {
            val width = bitmap!!.width
            val height = bitmap.height
            val scaleWidth = w.toFloat() / width
            val scaleHeight = h.toFloat() / height
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        }
    }
}