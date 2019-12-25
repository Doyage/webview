package com.webviewtest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity: AppCompatActivity() {

//    private val url = "http://www.androidexample.com/media/webview/details.html"
//    private val url = "https://imgur.com/upload"
    private val url = "https://moving-taxi-jxwmi.run.goorm.io/moving_taxi/";
//    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
//    private val FILECHOOSER_RESULTCODE = 1

    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCapturedImageURI: Uri? = null
    private var mCameraPhotoPath: String? = null
    private var INPUT_FILE_REQUEST_CODE = 1
    private var FILECHOOSER_RESULTCODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the web view settings instance
        val settings = web_view.settings

        // Enable java script in web view
        settings.javaScriptEnabled = true

        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)

        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true


        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true  // api 26
        }
        //settings.pluginState = WebSettings.PluginState.ON
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.mediaPlaybackRequiresUserGesture = false

        // More optional settings, you can enable it by yourself
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.loadWithOverviewMode = true
        settings.allowContentAccess = true
        settings.setGeolocationEnabled(true)
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccess = true

        // WebView settings
        web_view.fitsSystemWindows = true

        /*
            if SDK version is greater of 19 then activate hardware acceleration
            otherwise activate software acceleration
        */
        web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null)


//         Set web view client
        web_view.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                toast("Page loading.")
            }

            override fun onPageFinished(view: WebView, url: String) {
                toast("Page loaded: ${view.title}")
            }
        }

        web_view.loadUrl(url);
        web_view.webChromeClient = MyWebChromeClient();
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? { // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    internal inner class MyWebChromeClient : WebChromeClient() {
        override fun onShowFileChooser(webView:WebView, filePathCallback:ValueCallback<Array<Uri>>, fileChooserParams:FileChooserParams):Boolean {
            if (mFilePathCallback != null) {
                mFilePathCallback = null
            }

            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(packageManager) != null) { // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) { // Error occurred while creating the File
//                    Log.e(Common.TAG, "Unable to create Image File", ex)
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {

                    Log.d("1111", Uri.fromFile(photoFile).toString());

                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                } else {
                    takePictureIntent = null
                }
            }

            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "image/*"

            val intentArray: Array<Intent?>
            intentArray = takePictureIntent.let { arrayOf(it) } ?: arrayOfNulls<Intent>(0)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)

            return true
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == FILECHOOSER_RESULTCODE) {
//            mFilePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data))
//            mFilePathCallback = null
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
//            var results: Array<Uri>!
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) { // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        var results: Array<Uri> = arrayOf(Uri.parse(mCameraPhotoPath))
                        mFilePathCallback?.onReceiveValue(results)
                        mFilePathCallback = null
                        return
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
//                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            mFilePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            mFilePathCallback = null
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Log.d("111", "111")
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return
                }
                var result: Uri? = null
                try {
                    result = if (resultCode != Activity.RESULT_OK) {
                        null
                    } else { // retrieve from the private variable if the intent is null
                        if (data == null) mCapturedImageURI else data.data
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "activity :$e",
                        Toast.LENGTH_LONG
                    ).show()
                }

//                mUploadMessage.onReceiveValue(result)
                mFilePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
                mUploadMessage = null
            } else {

                Log.d("222", "222")
            }
        }
        return
    }

    // Method to show app exit dialog
    private fun showAppExitDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Please confirm")
        builder.setMessage("No back history found, want to exit the app?")
        builder.setCancelable(true)

        builder.setPositiveButton("Yes", { _, _ ->
            // Do something when user want to exit the app
            // Let allow the system to handle the event, such as exit the app
            super@MainActivity.onBackPressed()
        })

        builder.setNegativeButton("No", { _, _ ->
            // Do something when want to stay in the app
            toast("thank you.")
        })

        // Create the alert dialog using alert dialog builder
        val dialog = builder.create()

        // Finally, display the dialog when user press back button
        dialog.show()
    }

    // Handle back button press in web view
    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            // If web view have back history, then go to the web view back history
            web_view.goBack()
        }
    }
}

// Extension function to show toast message
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
