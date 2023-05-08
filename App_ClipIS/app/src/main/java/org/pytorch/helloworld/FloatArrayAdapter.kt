package org.pytorch.helloworld

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

class FloatArrayAdapter : TypeAdapter<FloatArray>() {
    // Overrides the write method of TypeAdapter to write a FloatArray as JSON
    // out: JsonWriter object for writing JSON
    // value: FloatArray to be written as JSON
    @kotlin.jvm.Throws(IOException::class)
    override fun write(out: JsonWriter, value: FloatArray) {
        out.beginArray()
        for (i in value.indices) {
            out.beginObject()
            out.name("index").value(i.toLong())
            out.name("value").value(value[i].toDouble())
            out.endObject()
        }
        out.endArray()
    }

    // Overrides the read method of TypeAdapter to read a FloatArray from JSON
    // in: JsonReader object for reading JSON
    // returns: FloatArray object read from JSON
    @kotlin.jvm.Throws(IOException::class)
    override fun read(`in`: JsonReader): FloatArray {
        val list: MutableList<Float> = ArrayList()
        `in`.beginArray()
        while (`in`.hasNext()) {
            `in`.beginObject()
            var index = -1
            var value = 0f
            while (`in`.hasNext()) {
                val name = `in`.nextName()
                if (name == "index") {
                    index = `in`.nextInt()
                } else if (name == "value") {
                    value = `in`.nextDouble().toFloat()
                }
            }
            `in`.endObject()
            if (index >= 0) {
                while (list.size <= index) {
                    list.add(0f)
                }
                list[index] = value
            }
        }
        `in`.endArray()
        // Convert the list of Floats to a FloatArray
        val result = FloatArray(list.size)
        for (i in list.indices) {
            result[i] = list[i]
        }
        return result
    }
}
