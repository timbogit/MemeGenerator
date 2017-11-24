package com.timschmelmer.memegenerator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

private val PHOTO_INTENT_REQUEST_CODE = 10
private val EXTERNAL_STORAGE_PER_REQUEST_CODE = 20

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val shareButton = findViewById<Button>(R.id.button_share)
        shareButton.setOnClickListener( { view ->

            sharePhoto()
        })
    }

    private fun sharePhoto() {
        createCompositeImage()
        createShareIntent()

    }

    private fun createShareIntent() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val sharedFile = File(cacheDir, "images/image.png")
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@MainActivity,
                "com.timmschmelmer.fileprovider", sharedFile))
        shareIntent.type = "image/png"
        startActivity(shareIntent)
    }

    private fun createCompositeImage() {
        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout_meme)
        frameLayout.setDrawingCacheEnabled(true)
        val bitmap = frameLayout.drawingCache
        val sharedFile = File(cacheDir, "images")
        sharedFile.mkdirs()

        val stream = FileOutputStream(sharedFile.toString() + "/image.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        frameLayout.setDrawingCacheEnabled(false)
        frameLayout.destroyDrawingCache()
    }

    fun pickPhotoFromGallery(view: View) {
        Log.d("MainActivity", "I am in pickPhotoFromGallery")
        requestPermission()
    }

    private fun createPhotoIntent() {
        val photoIntent = Intent(Intent.ACTION_PICK)

        val photoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val photoUri = Uri.parse(photoDirectory.path)
        photoIntent.setDataAndType(photoUri, "image/*")

        startActivityForResult(photoIntent, PHOTO_INTENT_REQUEST_CODE)
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PER_REQUEST_CODE)

        } else {
            createPhotoIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            EXTERNAL_STORAGE_PER_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    createPhotoIntent()

                } else {
                    Toast.makeText(this, "Gallery Permission Denied :(", Toast.LENGTH_SHORT)
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PHOTO_INTENT_REQUEST_CODE) {
                val photoUri = data?.data
                val imageView = findViewById<ImageView>(R.id.image_view_meme)
                Picasso.with(this@MainActivity).load(photoUri).into(imageView)
            }
        }
    }
}
