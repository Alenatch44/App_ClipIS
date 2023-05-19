package org.pytorch.helloworld

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.*
import kotlin.collections.ArrayList

class Text2ImageActivity : AppCompatActivity() {
    private val data: MutableList<Bean> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_show)
        supportActionBar!!.hide()
        val sharedata = application as ShareData
        val intent = intent
        val text = intent.getStringExtra("text")
        val Top = intent.getIntArrayExtra("Index")
        val TextSimilarity = sharedata.similarity

        // This finds the size of the data to be displayed based on the TextSimilarity array
        var Showsize = 0
        for (i in TextSimilarity!!.indices) {
            if (TextSimilarity[i] < 0.001) {
                Showsize = i
                break
            }
            Log.d(TAG, Showsize.toString())
        }
        if (Showsize < 10) {
            Showsize = 10
        }

        //bean.image = sharedata.imageSet[0]
        // This creates a Bean object for each image and adds it to the data list
        for (i in 0 until Showsize) {
            val bean = Bean()
            bean.image = sharedata.imageSet[Top!![i]]
            data.add(bean)
        }

        // This sets up the RecyclerView and its adapter
        val manager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = findViewById<RecyclerView>(R.id.list)
        val myAdapter = MyAdapter(data, this)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = manager
    }

    // This is a companion object that holds the constant TAG for logging purposes
    companion object {
        private const val TAG = "My App"
    }
}