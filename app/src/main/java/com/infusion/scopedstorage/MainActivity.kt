package com.infusion.scopedstorage

import android.Manifest
import android.R.attr.mimeType
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infusion.scopedstorage.databinding.ActivityMainBinding
import com.infusion.scopedstorage.databinding.DialogImageBinding
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpClickListener()
    }

    private fun setUpClickListener() {
        binding.btnMediaStore.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "MediaStore")
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )

                val uri: Uri? =
                    resolver.insert(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                        contentValues
                    )
                uri?.let {
                    val path = FileUriUtils.getRealPath(this, uri)
                    val newFile = File(path ?: "")
                    if (newFile.exists().not()) {
                        newFile.mkdirs()
                    }
                    setPath(path ?: "Something went wrong")
                } ?: run { toast() }

            } else {
                val newFile =
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + "MediaStore")
                if (newFile.exists().not()) {
                    if (newFile.mkdirs()) {
                        setPath(newFile.absolutePath)
                    } else toast()
                } else
                    setPath(newFile.absolutePath)
            }
        }

        binding.btnInPackage.setOnClickListener {
            val path = externalMediaDirs[0].absolutePath + File.separator + "WrapperDir"
            val newFile = File(path)
            if (newFile.exists().not()) {
                if (newFile.mkdirs()) {
                    setPath(path)
                } else {
                    toast()
                }
            } else setPath(path)

        }

        binding.btnFilesDirectory.setOnClickListener {
            val path = filesDir.absolutePath + File.separator + "FileDir"
            val newFile = File(path)
            if (newFile.exists().not()) {
                if (newFile.mkdirs()) {
                    setPath(path)
                } else {
                    toast()
                }
            } else setPath(path)
        }

        binding.btnExternalFilesDirectory.setOnClickListener {
            val path = getExternalFilesDir(null)?.absolutePath + File.separator + "ExternalDir"
            val newFile = File(path)
            if (newFile.exists().not()) {
                if (newFile.mkdirs()) {
                    setPath(path)
                } else {
                    toast()
                }
            } else setPath(path)
        }

        binding.btnInExternalDirectory.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) {
                checkForFullAccess()
            } else {
                requestPermission()

            }
        }
    }

    private fun storeFile(fullPath: String) {
        try {
            val path = fullPath + File.separator + "Demo.jpg"
            val newFile = File(path)
            binding.tvPath.text = "Created at: $path"
            if (newFile.exists().not()) {
                val inputStream = resources.openRawResource(R.raw.image)
                val outputStream = FileOutputStream(newFile)
                val data = ByteArray(inputStream.available())
                inputStream.read(data)
                outputStream.write(data)
                inputStream.close()
                outputStream.close()
                showImageDialog(path)
            } else showImageDialog(path)
        } catch (e: Exception) {
            toast(e.localizedMessage ?: "Something went wrong")
        }
    }

    private fun showImageDialog(path: String) {
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)
        val binding = DialogImageBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        binding.tvClose.setOnClickListener {
//            File(path).delete()
            dialog.dismiss()
        }
        Glide.with(dialog.context).load(path).into(binding.ivImage)
        dialog.show()
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private fun checkForFullAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                createDirectory()
            }
        } else {
            createDirectory()
        }

    }

    private fun createDirectory() {
        val path =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "ScopedStorage"
        val newFile = File(path)
        if (newFile.exists().not()) {
            if (newFile.mkdirs()) {
                setPath(path)
            } else {
                toast("Permission not available")
            }
        } else setPath(path)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var count = 0
            var totalPermission = 0
            val rejectPermission = arrayListOf<String>()
            for (entry in result.entries) {
                totalPermission++
                val permissionName = entry.key
                if (entry.value) {
                    count++
                    if (count == result.entries.size) {
                        checkForFullAccess()
                    }
                } else {
                    rejectPermission.add(permissionName)
                    if (totalPermission == result.entries.size) {
                        showDialog()
                    }
                }
            }

        }

    private fun showDialog() {
        MaterialAlertDialogBuilder(this, 0)
            .apply {
                setTitle("Permission Required")
                setMessage("Please accept permission to let us save file on your device.")
                setPositiveButton("Ok") { _, _ ->
//                    requestPermission()
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this@MainActivity, "Please try again", Toast.LENGTH_SHORT).show()
                }
                setCancelable(false)
                create()
                show()
            }
    }

    private fun setPath(path: String) {
        storeFile(path)
    }

    private fun toast(message: String = "Something went wrong") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
