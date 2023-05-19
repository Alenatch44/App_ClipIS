package org.pytorch.helloworld

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.xiasuhuei321.loadingdialog.view.LoadingDialog
import org.pytorch.IValue
import org.pytorch.MemoryFormat
import org.pytorch.Tensor
import org.pytorch.helloworld.MainActivity.Companion.INPUT_TENSOR_HEIGHT
import org.pytorch.helloworld.MainActivity.Companion.INPUT_TENSOR_WIDTH
import org.pytorch.helloworld.MainActivity.Companion.TORCHVISION_NORM_MEAN_RGB
import org.pytorch.helloworld.MainActivity.Companion.TORCHVISION_NORM_STD_RGB
import org.pytorch.helloworld.MainActivity.Companion.resizeImage
import org.pytorch.torchvision.TensorImageUtils


interface ImageProcessingListener {
    fun onImageProcessingComplete()
}

class ImageProcessingTask(private val ImageSet: MutableList<Bitmap?>, private val sharedata: ShareData, private val listener: ImageProcessingListener) :
    AsyncTask<Void, Void, Void>() {
    private lateinit var ImageFeatureSet: Array<FloatArray?>


    override fun doInBackground(vararg params: Void?): Void? {
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
            t_tensor = sharedata.module_image!!.forward(IValue.from(t_tensor)).toTensor()
            ImageFeatureSet[i] = t_tensor.dataAsFloatArray
        }
        // Save the encoded image features to a file

        return null
    }

    override fun onPostExecute(result: Void?) {
        // Set the image set feature in the shared data
        sharedata.imageSetFeature = ImageFeatureSet
        Log.d("", "Значение ImageFeatureSet: $ImageFeatureSet")
        listener.onImageProcessingComplete()
    }

}