package org.pytorch.helloworld

import android.app.Application
import android.graphics.Bitmap
import androidx.collection.CircularArray
import org.pytorch.Module

class ShareData : Application() {
    var getOriginImagePath: CircularArray<Any>? = null

    //public String[] getOriginImagePath;
    var imageSet: List<Bitmap?> = ArrayList()
    lateinit var imageSetFeature: Array<FloatArray?>
    private var module_image: Module? = null
    var module_text: Module? = null
    lateinit var similarity: FloatArray
    private val ImagesList: List<*>? = null
    var originImagePath: List<String>? = null
    var startData: String? = null
    var endData: String? = null
    fun setModule_image(module_image: Module?) {
        this.module_image = module_image
    }

    override fun onCreate() {
        super.onCreate()
        imageSet = ArrayList()
    }
}