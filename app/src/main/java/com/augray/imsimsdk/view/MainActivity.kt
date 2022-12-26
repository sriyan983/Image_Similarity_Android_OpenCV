package com.augray.imsimsdk.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.augray.imsimsdk.R
import com.augray.imsimsdk.adapters.CustomAdapter
import com.augray.imsimsdk.viewmodel.ItemsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.opencv.android.OpenCVLoader
import org.opencv.features2d.SIFT
import utils.Dialog
import utils.OnDialogPressListenerInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), OnDialogPressListenerInterface {
    lateinit var addFAB: FloatingActionButton
    var TAG = "MainActivity"

    val APP_TAG = "ImSimSDKApp"
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    var adapter: CustomAdapter? = null

    var currentFolderName: String? = null
    var currentFileCount: Int = 0

    var clickCount: Int = -1

    var cameraResultLauncher: ActivityResultLauncher<Intent>? = null

    lateinit var dialogUtil: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSupportActionBar()?.setTitle("ImSimSDK");
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(false);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);

        dialogUtil = Dialog()

        addFAB = findViewById(R.id.idFABAdd)
        addFAB.setOnClickListener {
            Toast.makeText(this@MainActivity, "Add clicked..", Toast.LENGTH_LONG).show()

            currentFolderName = SimpleDateFormat("yyyyMMddHHmmss").format(Date());
            currentFileCount = 0

            //onLaunchCamera()
            dialogUtil.showDialog(this, this)
        }

        if (OpenCVLoader.initDebug()) {
            Log.d("LOADED", "success")

            createFolderRecyclerView()

            cameraResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                    if (result.resultCode == RESULT_OK) {
                        //val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                        Log.d("FILEPATH:", photoFile!!.absolutePath)
                        Toast.makeText(this, "Success taking picture", Toast.LENGTH_SHORT).show()

                        if (currentFileCount < clickCount - 1) {
                            currentFileCount++
                            Handler().postDelayed({
                                onLaunchCamera()
                            }, 1000)

                        }

                    } else { // Result was a failure
                        Toast.makeText(this, "Error taking picture", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Log.d("LOADED", "failed")
        }
    }

    override fun onResume() {
        super.onResume()

        if (adapter != null) {
            createFolderRecyclerView()
            adapter?.notifyDataSetChanged();
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (cameraResultLauncher != null) {
            cameraResultLauncher?.unregister()
        }
    }

    private fun onLaunchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFileUri("${currentFileCount}_img.jpg")
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
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + currentFolderName), "")

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    private fun getDB(file: File): ArrayList<Uri> {
        val mResFiles: Array<File> = file.listFiles()
        val uriArrayList = ArrayList<Uri>()
        var fileUri: Uri
        for (f in mResFiles) {
            Log.i(TAG, f.getName())
            // Use the FileProvider to get a content URI
            try {
                fileUri = FileProvider.getUriForFile(
                    this, "com.augray.imsimsdk.fileprovider",
                    f
                )
                // add current file uri to the list
                Log.d(TAG, fileUri.toString())
                uriArrayList.add(fileUri)
            } catch (e: Exception) {
                Log.e(TAG, "Error")
            }
        }
        return uriArrayList
    }

    private fun createFolderRecyclerView() {
        var list: ArrayList<Uri> =
            getDB(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), ""))

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        val data = ArrayList<ItemsViewModel>()
        for (item in list) {
            data.add(
                ItemsViewModel(
                    R.drawable.ic_baseline_folder_24,
                    item?.lastPathSegment.toString()
                )
            )
        }

        if (adapter == null) {
            recyclerview.layoutManager = LinearLayoutManager(this)
            adapter = CustomAdapter()
            recyclerview.adapter = adapter

            adapter?.onItemClick = { uri ->
                Log.d("TAG", uri.toString())
                Toast.makeText(this, "uri tapped - $uri.toString()", Toast.LENGTH_SHORT).show()

                val intent =
                    Intent(
                        this@MainActivity,
                        com.augray.imsimsdk.view.ImageGridActivity::class.java
                    )
                intent.putExtra("id", uri.lastPathSegment)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //finish()
                startActivity(intent)
            }
        }
        adapter?.mList = data
        adapter?.uris = list
    }

    override fun okClick(numberOfImages: Int) {
        clickCount = numberOfImages
        Toast.makeText(this, "Entered number of images $numberOfImages", Toast.LENGTH_SHORT).show()

        onLaunchCamera()
    }

    override fun cancelClick() {
        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
    }
}