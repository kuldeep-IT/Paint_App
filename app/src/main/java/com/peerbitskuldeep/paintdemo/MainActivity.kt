package com.peerbitskuldeep.paintdemo

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_dialog_brush_size.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.Permission

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(10.toFloat())

        mImageButtonCurrentPaint = ll_paint_colors[2] as ImageButton

        //for the clicked color background change
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        gallery.setOnClickListener {

            if (isReadStorageAllowed())
            {
//                Toast.makeText(this,"Pick Photo", Toast.LENGTH_SHORT).show()

                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(pickPhotoIntent, GALLERY)
            }
            else
            {
                requestStoragePermission()
//                Toast.makeText(this,"Give permission", Toast.LENGTH_SHORT).show()
            }

        }

        //undo
        undo.setOnClickListener {

            drawing_view.onClickUndo()

        }

        //save
        save.setOnClickListener {

//            saveMediaToStorage(getBitmapFromView(fl_drawing_view_container))
//            saveToGallery(this, getBitmapFromView(fl_drawing_view_container),"Peerbits")
            saveImage(getBitmapFromView(fl_drawing_view_container),this@MainActivity, "Paint")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK)
        {
            if (requestCode == GALLERY)
            {
                try {
                    if (data!!.data != null)
                    {
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    }
                    else{
                        Toast.makeText(this,"Error to get image", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun showBrushSizeChooserDialog()
    {
        val brushDialog = Dialog(this).apply {

            setContentView(R.layout.custom_dialog_brush_size)
            setTitle("Select brush size:")

        }

        brushDialog.btnSmallBrush.setOnClickListener {

            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.btnMediumBrush.setOnClickListener {

            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.btnLargeBrush.setOnClickListener {

            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }

    fun paintClicked(view: View) {

        // selected ->  mImageButtonCurrentPaint = ll_paint_colors[2] as ImageButton
         if (view != mImageButtonCurrentPaint)
         {
             val imgBtn = view as ImageButton

             val colorTag = imgBtn.tag.toString()
             drawing_view.setColor(colorTag)

             imgBtn.setImageDrawable(
                 ContextCompat.getDrawable(
                     this,
                     R.drawable.pallet_pressed
                 )
             )

             mImageButtonCurrentPaint?.setImageDrawable(
                 ContextCompat.getDrawable(
                     this,
                     R.drawable.pallet_normal
                 )
             )
             mImageButtonCurrentPaint = view
         }

    }

    private fun requestStoragePermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()
        ))
        {
            Toast.makeText(this, "Need permission to add a background Image!", Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,
                "Permissions granted!",
                Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(this,
                "Permissions Denied!",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun isReadStorageAllowed(): Boolean
    {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap
    {
        val returnedBitmap = Bitmap.createBitmap(view.width,
            view.height
        ,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedBitmap)

        //background image
        val bgDrawable = view.background

        //background image is selected
        if (bgDrawable != null)
        {
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap

    }

   /* private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
           Toast.makeText(this,"Image saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }*/

   /*fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }*/

    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}