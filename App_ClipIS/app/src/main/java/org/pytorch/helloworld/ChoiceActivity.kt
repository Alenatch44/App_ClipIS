package org.pytorch.helloworld

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChoiceActivity : AppCompatActivity() {
    // Declaring some private properties of the class with default values
    private var progressBar: ProgressBar? = null
    private var mSharedPref: SharedPreferences? = null
    private var mSharedPref_path: SharedPreferences? = null
    private var selectedDatesTextView: TextView? = null
    private var mStartDate: Long? = null
    private var mEndDate: Long? = null
    private var sharedata: ShareData? = null
    private var Image_path: List<String>? = null
    private var startDateString: String? = null
    private var endDateString: String? = null

    // Declaring some private properties of the class with initial empty lists
    private val ImageSet: List<Bitmap> = ArrayList()
    private val OriginImageSet: List<File> = ArrayList()
    private val OriginImagePath: List<String> = ArrayList()

    // Declaring two nullable Button properties of the class
    var save_button: Button? = null
    var load_button: Button? = null

    // The onCreate method of the activity starts here
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)

        // Hides the default action bar
        supportActionBar!!.hide()

        // Finds the progress bar view from the layout and assigns it to the progressBar property
        progressBar = findViewById(R.id.progress_Bar_1)

        // Checks if the app has permission to write to external storage and requests it if necessary
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            if (ActivityCompat.checkSelfPermission(
                    this@ChoiceActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this@ChoiceActivity,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    100
                )
            }
        }

        // Assigns the application's ShareData instance to the sharedata property
        sharedata = application as ShareData

        // Finds the save and load buttons from the layout and assigns them to the respective properties
        save_button = findViewById<View>(R.id.button_save) as Button
        load_button = findViewById<View>(R.id.button_load) as Button

        // Enables the save button
        save_button!!.isEnabled = true

        // Finds the select dates button and selected dates text view from the layout and assigns them to properties
        val selectDatesButton = findViewById<View>(R.id.select_dates_button) as Button
        selectedDatesTextView = findViewById(R.id.selected_dates_text_view)

        // Create material date picker
        val calendarConstraintBuilder = CalendarConstraints.Builder()
        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTitleText("SELECT A DATE")
        materialDateBuilder.setCalendarConstraints(calendarConstraintBuilder.build())
        val materialDatePicker: MaterialDatePicker<*> = materialDateBuilder.build()

        // Show date picker on select dates button click
        selectDatesButton.setOnClickListener {
            materialDatePicker.show(
                supportFragmentManager,
                "MATERIAL_DATE_PICKER"
            )
        }

        // Handle date picker selection
        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            mStartDate = (selection as Pair<Long?, Long?>).first
            mEndDate = selection.second
            val startDate = Date(mStartDate!!)
            val endDate = Date(mEndDate!!)
            val dateFormat = SimpleDateFormat("dd.MM.yyyy")
            startDateString = dateFormat.format(startDate)
            endDateString = dateFormat.format(endDate)
            selectedDatesTextView?.setText("Фотографии будут загружены с $startDateString по $endDateString")
            sharedata!!.startData = startDateString
            sharedata!!.endData = endDateString
            save_button!!.isEnabled = false
        }
    }

    fun save(view: View?) {
        // Show progress bar while loading data
        progressBar!!.visibility = View.VISIBLE

        // Get shared preferences
        mSharedPref = getSharedPreferences("myPrefs", MODE_PRIVATE)
        mSharedPref_path = getSharedPreferences("myPrefs_images_path", MODE_PRIVATE)
        sharedata = application as ShareData

        // Get photo urls and image paths from shared preferences if they exist
        val json = mSharedPref?.getString("photo_urls", "")
        val json_path = mSharedPref_path?.getString("images_path", "")

        // If photo urls and image paths are found, load them into the app and start MainActivity
        if (mSharedPref_path != null && mSharedPref_path!!.contains("images_path") && json != null && json_path != null) {
            val gson = GsonBuilder()
                .registerTypeAdapter(FloatArray::class.java, FloatArrayAdapter())
                .create()
            val Image = gson.fromJson(json, Array<FloatArray?>::class.java)
            sharedata!!.imageSetFeature = Image
            val gson_path = Gson()
            val type_path = object : TypeToken<List<String?>?>() {}.type
            Image_path = gson_path.fromJson<List<String>>(json_path, type_path)
            sharedata!!.originImagePath = Image_path
            val intent = Intent(this, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
            Toast.makeText(this, "Данные загружены", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Данные не были сохранены ранее", Toast.LENGTH_SHORT).show()
        }
    }

    fun load(view: View?) {
        // Show progress bar while loading data
        progressBar!!.visibility = View.VISIBLE

        // Start MainActivity
        val intent = Intent(this, MainActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        startActivity(intent, options.toBundle())
        Toast.makeText(this, "Данные загружаются", Toast.LENGTH_SHORT).show()
    }
}