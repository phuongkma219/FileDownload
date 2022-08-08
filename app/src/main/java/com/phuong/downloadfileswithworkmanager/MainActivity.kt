package com.phuong.downloadfileswithworkmanager

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.phuong.downloadfileswithworkmanager.Constants.KEY_FILE_NAME
import com.phuong.downloadfileswithworkmanager.Constants.KEY_FILE_TYPE
import com.phuong.downloadfileswithworkmanager.Constants.KEY_FILE_URI
import com.phuong.downloadfileswithworkmanager.Constants.KEY_FILE_URL
import com.phuong.downloadfileswithworkmanager.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding :ActivityMainBinding
    private  lateinit var workManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        workManager = WorkManager.getInstance(this)

        binding.btnDownload.setOnClickListener {
            if (Utils.storagePermissionGrant(this)){
                startDownloadingFile()

            }
            else{
                requestStoragePermission()
            }
        }
        binding.btnZip.setOnClickListener {
            val backupDBPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            backupDBPath.mkdirs()
            val target = File(
                backupDBPath,
                "Imagephone.png"
            )
            val s = arrayOfNulls<String>(1)
            s[0] = target.absolutePath
            val zip = ZipManager()
            zip.zip(s,  backupDBPath.absolutePath + "/demo1.zip")
        }
        binding.btnUnZip.setOnClickListener {
            val backupDBPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val zip = ZipManager()
           zip.unpackZip(backupDBPath.absolutePath +"/","demo.zip")

        }
        binding.btnSetWallpaper.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "choose image"), 22)
        }

    }
    private fun startDownloadingFile(
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()
        val data = Data.Builder()

        data.apply {
            putString(KEY_FILE_NAME, "img_avatar.png")
            putString(KEY_FILE_URL, "https://www.w3schools.com/howto/img_avatar.png")
            putString(KEY_FILE_TYPE, "PNG")
        }

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(FileDownloadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()

        workManager.enqueue(oneTimeWorkRequest)

        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this) { info ->
                info?.let {
                    when (it.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val uri = it.outputData.getString(KEY_FILE_URI)
                            uri?.let {
                                binding.btnOpenFile.text = "Open File"
                                binding.btnOpenFile.visibility = View.VISIBLE
                                binding.btnOpenFile.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(uri.toUri(), "image/png")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Can't open Pdf",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                        WorkInfo.State.FAILED -> {

                            binding.btnOpenFile.text = "Download in failed"
                        }
                        WorkInfo.State.RUNNING -> {
                            binding.btnOpenFile.text = "Download in progress.."
                        }
                        else -> {

                        }
                    }
                }
            }
    }
    private fun requestStoragePermission() {
        resultLauncher.launch(
            Utils.getStoragePermissions()
        )

    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (Utils.storagePermissionGrant(this)
            ) {
                startDownloadingFile()
            } else {
                Utils.showAlertPermissionNotGrant(binding.root, this)
            }
        }
    private fun setWallpaper(url: String){
        Glide.with(this)
            .asBitmap()
            .load(Uri.parse(url))
            .into(object : CustomTarget<Bitmap>(){
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val wallpaperManager : WallpaperManager = WallpaperManager.getInstance(this@MainActivity)
                    wallpaperManager.setBitmap(resource,null,false,
                        WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }

            })
    }
}