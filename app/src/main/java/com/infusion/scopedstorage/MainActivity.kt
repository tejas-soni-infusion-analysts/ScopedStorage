package com.infusion.scopedstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //        val resolver = requireContext().contentResolver
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "iiissd")
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
//        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//
//        val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
//        var newFiler = File(FileUriUtils.getRealPath(requireContext(),uri!!))
//        if (newFiler.exists().not()){
//            newFiler.mkdirs()
//        }

//        val c = ContextWrapper(requireActivity())
//        c.filesDir.absolutePath

//        var f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+File.separator+"testing")
//        if(!f.exists()){
//            f.mkdirs()
//        } else {
//            f.delete()
//        }

//        val file =  File(requireContext().filesDir.absolutePath+File.separator+"maili")
//        if (file.exists().not()){
//            file.mkdirs()
//        }

//                val file =  File(requireContext().getExternalFilesDir(null)!!.absolutePath+File.separator+"maili")
//        if (file.exists().not()){
//            file.mkdirs()
//        }

        val file =  File(Environment.getRootDirectory()
            .absolutePath+File.separator+"maili")
        if (file.exists().not()){
            file.mkdirs()
        }
    }
}