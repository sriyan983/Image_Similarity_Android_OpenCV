package com.augray.imsimsdk.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.augray.imsimsdk.R
import com.augray.imsimsdk.adapters.ImageGridAdapter
import com.augray.imsimsdk.viewmodel.ImageViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import utils.AsyncCompareTask
import java.io.File
import java.util.*

class ImageGridActivity : AppCompatActivity() {
    lateinit var addFAB: FloatingActionButton

    lateinit var courseRV: RecyclerView
    lateinit var courseRVAdapter: ImageGridAdapter
    lateinit var courseList: ArrayList<ImageViewModel>

    var TAG: String = "ImageGridActivity"

    var cameraResultLauncher: ActivityResultLauncher<Intent>? = null
    var photoFile: File? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() === android.R.id.home) {
            finish()
            //            val intent = Intent(applicationContext, MainActivity::class.java)
            //            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            //            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)

        getSupportActionBar()?.setTitle("Dataset");
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(false);

        val fileID = Uri.parse(intent.extras?.getString("id"))
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + fileID), "")
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    //val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                    Log.d("FILEPATH:", photoFile!!.absolutePath)
                    Toast.makeText(this, "Success taking picture", Toast.LENGTH_SHORT).show()

                    val intent =
                        Intent(
                            this@ImageGridActivity,
                            com.augray.imsimsdk.view.ResultGridActivity::class.java
                        )
                    intent.putExtra("id", fileID.lastPathSegment)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //finish()
                    startActivity(intent)
                }

            }

        addFAB = findViewById(R.id.idFABAdd)
        addFAB.setOnClickListener {
            // on below line we are displaying a toast message.
            Toast.makeText(this@ImageGridActivity, "Compare clicked..", Toast.LENGTH_LONG).show()
            //compare()

            onLaunchCamera()

        }

        Log.d("ImageGridActivity", fileID.toString())

        courseList = ArrayList()
        getImages(File(mediaStorageDir.path))

        courseRV = findViewById(R.id.idRVCourses)

        val layoutManager = GridLayoutManager(this, 2)
        courseRV.layoutManager = layoutManager
        courseRVAdapter = ImageGridAdapter(courseList, this)
        courseRV.adapter = courseRVAdapter

        courseRVAdapter.notifyDataSetChanged()
    }

    private fun onLaunchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFileUri("input_img.jpg")
        if (photoFile != null) {
            val fileProvider: Uri =
                FileProvider.getUriForFile(this, "com.augray.imsimsdk.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (intent.resolveActivity(packageManager) != null) {
                cameraResultLauncher?.launch(intent)
            }
        }
    }

    private fun getPhotoFileUri(fileName: String): File {
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + "test"), "")

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    private fun getImages(file: File) {
        val mResFiles: Array<File> = file.listFiles()
        var fileUri: Uri
        for (f in mResFiles) {
            Log.i(TAG, f.getName())
            // Use the FileProvider to get a content URI
            try {
                fileUri = FileProvider.getUriForFile(
                    this, "com.augray.imsimsdk.fileprovider",
                    f
                )
                if (f.exists()) {
                    // on below line we are creating an image bitmap variable
                    // and adding a bitmap to it from image file.
                    val imgBitmap = BitmapFactory.decodeFile(f.absolutePath)
                    // on below line we are setting bitmap to our image view.
                    courseList.add(ImageViewModel(f.name, imgBitmap))
                }

                // add current file uri to the list
                Log.d(TAG, fileUri.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error")
            }
        }
    }

}