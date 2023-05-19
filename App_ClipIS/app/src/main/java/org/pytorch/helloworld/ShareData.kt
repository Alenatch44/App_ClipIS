package org.pytorch.helloworld

import android.app.Application
import android.graphics.Bitmap
import androidx.collection.CircularArray
import org.pytorch.Module
import java.io.File

class ShareData : Application() {
    var bitmapLoad: Bitmap? = null
    var getOriginImagePath: CircularArray<Any>? = null

    //public String[] getOriginImagePath;
    var imageSet: List<Bitmap?> = ArrayList()
    lateinit var imageSetFeature: Array<FloatArray?>
    var module_text: Module? = null
    var module_image: Module? = null
    lateinit var similarity: FloatArray
    var originImagePath: MutableList<File>? = null
    var startData: String? = null
    var endData: String? = null

    override fun onCreate() {
        super.onCreate()
        imageSet = ArrayList()
    }
}