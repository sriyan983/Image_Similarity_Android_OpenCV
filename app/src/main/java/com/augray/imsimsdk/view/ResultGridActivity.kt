package com.augray.imsimsdk.view

import android.R.attr.bitmap
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
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
import java.io.FileOutputStream
import java.util.*


class ResultGridActivity : AppCompatActivity(), AsyncCompareTask.AsyncResponse {
    lateinit var addFAB: FloatingActionButton

    lateinit var courseRV: RecyclerView
    lateinit var courseRVAdapter: ImageGridAdapter
    lateinit var courseList: ArrayList<ImageViewModel>
    lateinit var asyncTaskList: ArrayList<AsyncCompareTask>

    lateinit var imgToRecognize: Bitmap

    lateinit var dialog: ProgressDialog

    var TAG: String = "ResultGridActivity"

    var processCount = 0


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() === android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)

        getSupportActionBar()?.setTitle("Similarity Results");
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(false);

        addFAB = findViewById(R.id.idFABAdd)
        addFAB.setOnClickListener {
            Toast.makeText(this@ResultGridActivity, "Compare clicked..", Toast.LENGTH_LONG).show()
            compare()
        }

        val fileID = Uri.parse(intent.extras?.getString("id"))
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + fileID), "")

        Log.d("RResultGridActivity", fileID.toString())

        courseList = ArrayList()
        asyncTaskList = ArrayList()

        var testFile: File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/test/input_img.jpg"), "")
        var fileUri: Uri
        try {
            fileUri = FileProvider.getUriForFile(this, "com.augray.imsimsdk.fileprovider",
                testFile

            )
            Log.d(TAG, fileUri.toString())
            if (testFile.exists()) {
                imgToRecognize = BitmapFactory.decodeFile(testFile.absolutePath)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error")
        }

        createTasks(File(mediaStorageDir.path))

        courseRV = findViewById(R.id.idRVCourses)

        val layoutManager = GridLayoutManager(this, 2)

        courseRV.layoutManager = layoutManager

        courseRVAdapter = ImageGridAdapter(courseList, this)
        courseRV.adapter = courseRVAdapter

        runTasks()

        dialog = ProgressDialog.show(
            this, "",
            "Loading. Please wait...", true
        )
    }

    private fun runTasks() {
        for(t in asyncTaskList) {
            runOnUiThread {
                t.execute()
            }
        }
    }

    private fun createTasks(file: File) {
        val mResFiles: Array<File> = file.listFiles()
        var fileUri: Uri
        for (f in mResFiles) {
            Log.i(TAG, f.getName())
            try {
                fileUri = FileProvider.getUriForFile(this, "com.augray.imsimsdk.fileprovider",
                    f
                )
                if (f.exists()) {
                    val imgBitmap = BitmapFactory.decodeFile(f.absolutePath)
                    val task = AsyncCompareTask(this, this)
                    task.objToRecognize = imgToRecognize
                    task.fileID = f.name
                    task.scene = imgBitmap
                    task.minDistance = 100.0
                    task.maxDistance = 0.0
                    Log.i(TAG, "Comparing")
                    //task.execute()
                    asyncTaskList.add(task)

                }

                // add current file uri to the list
                Log.d(TAG, fileUri.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error")
            }
        }
    }

    private fun compare() {
        var imgScene = courseList.get(1).imgBitmap
        if (imgToRecognize != null && imgScene != null) {
            Log.i(TAG, "Scaling bitmaps")
            //imgToRecognize = Bitmap.createScaledBitmap(imgToRecognize, 100, 100, true);
            //imgScene = Bitmap.createScaledBitmap(imgScene, 100, 100, true);
            runOnUiThread {
                //ivPicture.setImageBitmap(imgToRecognize)
                //ivScene.setImageBitmap(imgScene)
                val task = AsyncCompareTask(this, this)
                task.objToRecognize = imgToRecognize
                task.scene = imgScene
                task.minDistance = 100.0
                task.maxDistance = 0.0
                Log.i(TAG, "Comparing")
                task.execute()
            }
        } else {
            Log.i(TAG, "Unable to compare")
        }
    }

    override fun processFinish(result: Bitmap?, fileID: String?) {
        Log.d(TAG, "file processed: ${fileID}")
        processCount++
        courseList.add(ImageViewModel(fileID!!, result!!))
        courseRVAdapter.notifyDataSetChanged()

        saveBitmap(processCount, result)

        if (processCount == asyncTaskList.count())
        {
            dialog.cancel()
        }
    }

    fun saveBitmap(count: Int, bitmap: Bitmap) {
        var dir: File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/result"), "")
        var testFile: File = File(dir.path + File.separator + "${count}_img.jpg")
        var fileUri: Uri
        try {
            fileUri = FileProvider.getUriForFile(this, "com.augray.imsimsdk.fileprovider",
                testFile

            )
            val stream =
                FileOutputStream(testFile) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error")
        }
    }

}