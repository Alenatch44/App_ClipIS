package org.pytorch.helloworld

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import org.pytorch.helloworld.MyAdapter.MyViewHolder
import java.io.File
import java.io.FileOutputStream

class MyAdapter(private val data: List<Bean>?, private val context: Context) :
    RecyclerView.Adapter<MyViewHolder>() {

    // This method creates a new instance of MyViewHolder and returns it.
    // It also inflates the layout for the view holder.
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = View.inflate(context, R.layout.list_item, null)
        return MyViewHolder(view)
    }

    // This method binds the data to the views of the view holder.
    override fun onBindViewHolder(@NonNull holder: MyViewHolder, position: Int) {
        // Set the image for the ImageView using the data from the List<Bean>.
        holder.im.setImageBitmap(data!![position].image)
    }

    // This method returns the number of items in the data List<Bean>.
    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    // This is the view holder class for the RecyclerView.
    inner class MyViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val im: ImageView
        private val LL: View

        // This method sets up the views in the view holder.
        init {
            im = itemView.findViewById(R.id.im)
            LL = itemView.findViewById(R.id.LL)

            // This sets an OnClickListener on the ImageView.
            // When the ImageView is clicked, it gets the bitmap from the ImageView
            // and shares it using the shareImageandText method.
            im.setOnClickListener {
                val bitmapDrawable = im.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                shareImageandText(bitmap)
            }
        }

        // This method shares the image and text using an intent.
        private fun shareImageandText(bitmap: Bitmap) {
            val uri = getmageToShare(bitmap)
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
            intent.type = "image/*"
            itemView.context.startActivity(Intent.createChooser(intent, "Share Via"))
        }

        // This method gets the image Uri to be shared.
        private fun getmageToShare(bitmap: Bitmap): Uri? {
            val imagefolder = File(context.cacheDir, "images")
            var uri: Uri? = null
            try {
                imagefolder.mkdirs()
                val file = File(imagefolder, "Image")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                uri = FileProvider.getUriForFile(context, "com.alena.shareimage.fileprovider", file)
            } catch (e: Exception) {
                Toast.makeText(context, "" + e.message, Toast.LENGTH_LONG).show()
            }
            return uri
        }
    }
}