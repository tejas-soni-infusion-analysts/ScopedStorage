package com.infusion.scopedstorage

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.os.storage.StorageManager
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
    private val filesArray = arrayListOf<ImageModel>()
    private val imageArray = arrayOf(R.raw.image,R.raw.image2,R.raw.image3)


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
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separator + "Videosss"
                )

                val uri: Uri? =
                    resolver.insert(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                        contentValues
                    )
                uri?.let {
                    val path = FileUriUtils.getRealPath(this, uri)
                    val newFile = File(path ?: "")
                    filesArray.clear()
                    filesArray.add(ImageModel(path = newFile.absolutePath, imageId = imageArray[0]))
                    val inputStream = resources.openRawResource(imageArray[0])
                    resolver.openOutputStream(uri)
                    val outputStream = FileOutputStream(newFile)
                    val data = ByteArray(inputStream.available())
                    inputStream.read(data)
                    outputStream.write(data)
                    inputStream.close()
                    outputStream.close()
                    showImageDialog()
//                     if (newFile.exists().not()) {
//                         newFile.mkdirs()
//                     }
//                     setPath(path ?: "Something went wrong")
                } ?: run { toast() }

            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED
                ) {
                    val newFile =
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + "MediaStore")
                    if (newFile.exists().not()) {
                        if (newFile.mkdirs()) {
                            setPath(newFile.absolutePath)
                        } else toast()
                    } else
                        setPath(newFile.absolutePath)
                } else {
                    requestPermission()

                }
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

        binding.btnOpenWhatsapp.setOnClickListener {
            if (Build.VERSION.SDK_INT > 29 && contentResolver.persistedUriPermissions.size <= 0) {
                val createOpenDocumentTreeIntent =
                    (getSystemService(STORAGE_SERVICE) as StorageManager).primaryStorageVolume.createOpenDocumentTreeIntent()
                val replace =
                    createOpenDocumentTreeIntent.getParcelableExtra<Parcelable>("android.provider.extra.INITIAL_URI")
                        .toString().replace("/root/", "/document/")
                createOpenDocumentTreeIntent.putExtra(
                    "android.provider.extra.INITIAL_URI", Uri.parse(
                        "$replace%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
                    )
                )
                startActivityForResult(createOpenDocumentTreeIntent, 2001)
            } else {
                toast("Fetch files directly from folder")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            data?.data?.let {
                if (it.toString().contains(".Statuses")) {
                    contentResolver
                        .takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (Build.VERSION.SDK_INT > 29) {
                        // Load files
                            toast("Permission accepted fetch files")
                        return
                    }
                    return
                }
            }
        }
    }
    private fun storeFile(fullPath: String) {
        val path = fullPath + File.separator
        filesArray.clear()
        for (i in 0..2){
            filesArray.add(ImageModel(path = path+"Demo$i.jpg", imageId=imageArray[i]))
        }
        try {
            filesArray.forEachIndexed { index, imageModel ->
                val newFile = File(imageModel.path)
                binding.tvPath.text = "Created at: $fullPath"
                if (newFile.exists().not()) {
                    val inputStream = resources.openRawResource(imageModel.imageId)
                    val outputStream = FileOutputStream(newFile)
                    val data = ByteArray(inputStream.available())
                    inputStream.read(data)
                    outputStream.write(data)
                    inputStream.close()
                    outputStream.close()
                    if (index == 2) {
                        showImageDialog()
                    }
                } else {
                    if (index == 2) {
                        showImageDialog()
                    }
                }
            }
            } catch (e: Exception) {
                toast(e.localizedMessage ?: "Something went wrong")

        }
    }

    private fun showImageDialog() {
        val dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)
        val binding = DialogImageBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        binding.tvClose.setOnClickListener {
//            File(path).delete()
            dialog.dismiss()
        }
        try {
            Glide.with(dialog.context).load(filesArray[0].path).into(binding.ivImage)
            Glide.with(dialog.context).load(filesArray[1].path).into(binding.ivImage2)
            Glide.with(dialog.context).load(filesArray[2].path).into(binding.ivImage3)
        }catch (e: Exception){}
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

//    private fun getExistingImageUriOrNullQ(): Uri? {
//        var imageUri : Uri? = null
//        val projection = arrayOf(
//            MediaStore.MediaColumns._ID,
//            MediaStore.MediaColumns.DISPLAY_NAME,   // unused (for verification use only)
//            MediaStore.MediaColumns.RELATIVE_PATH,  // unused (for verification use only)
//        )
//        // take note of the / after OLArt
//        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH}='${Environment.DIRECTORY_DOWNLOADS}' AND ${MediaStore.MediaColumns.DISPLAY_NAME}='MediaStore'"
//
//
//        contentResolver.query(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
//            projection, selection, null, null ).use { c ->
//            if (c != null && c.count >= 1) {
//
//                print("has cursor result")
//                c.moveToFirst().let {
//
//                    val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID) )
//                    val displayName = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME) )
//                    val relativePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH) )
////                    lastModifiedDate = c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED) )
//
//                    imageUri = ContentUris.withAppendedId(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  id)
//
////                    print("image uri update $displayName $relativePath $imageUri ($lastModifiedDate)")
//
//                    return imageUri
//                }
//            }
//        }
//        print("image not created yet")
//        return null
//    }

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
