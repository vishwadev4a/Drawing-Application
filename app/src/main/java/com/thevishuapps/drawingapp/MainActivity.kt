package com.thevishuapps.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var drawingview:DrawingView?=null
    var defaultcolor:Int=0
    val opengallerylauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode== RESULT_OK && result.data!=null){
            val imageground:ImageView=findViewById(R.id.iv_background)
            imageground.setImageURI(result.data?.data)
        }
    }
    private var mimagebuttoncurrentpaint:ImageButton?=null
    val requestPermission:ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions->
            permissions.entries.forEach{
                val permissionname=it.key
                val isgranted=it.value
                if(isgranted){
                    val pickintent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    opengallerylauncher.launch(pickintent)
                }
                else{
                    if(permissionname==Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this,"Not granted",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val undo:ImageButton=findViewById(R.id.ib_undo)
        val undoo:ImageButton=findViewById(R.id.ib_undoo)
        undoo.setOnClickListener{
            drawingview?.undoo()
        }
        drawingview=findViewById(R.id.drawing_view)
        drawingview?.setsizeforbrush(20.toFloat())
        val linearlayoutpaintcolors=findViewById<LinearLayout>(R.id.ll_paint_colors)
        mimagebuttoncurrentpaint=linearlayoutpaintcolors[1] as ImageButton
        mimagebuttoncurrentpaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallete_pressed))
        val ib_brush:ImageButton=findViewById(R.id.ib_brush)
        val ibgallery:ImageButton=findViewById(R.id.ib_gallery)
        ibgallery.setOnClickListener{
            requeststoragepermission()
        }
        defaultcolor=ContextCompat.getColor(this,R.color.black)
        ib_brush.setOnClickListener{
            showbrushsizechooserdialog()
        }
        undo.setOnClickListener {
            drawingview?.onclickundo()
        }
        var colorpickbtn=findViewById<ImageButton>(R.id.colorpick)
        colorpickbtn.setOnClickListener{
            // Kotlin Code
            ColorPickerDialog
                .Builder(this)        				// Pass Activity Instance
                .setTitle("Pick Theme")           	// Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                .setDefaultColor(R.color.black)     // Pass Default Color
                .setColorListener { color, colorHex ->
                    // Handle Color Selection
                    drawingview?.setcolor(colorHex)
                }
                .show()
        }
        var canvascolor:ImageButton=findViewById(R.id.ib_canvas)
        canvascolor.setOnClickListener{
            ColorPickerDialog.Builder(this).setTitle("Choose Canvas Color").setColorShape(ColorShape.SQAURE).setDefaultColor(R.color.white).setColorListener{
                color,colorHex->
                var img=findViewById<DrawingView>(R.id.drawing_view)
                img.setBackgroundColor(Color.parseColor(colorHex))
            }
                .show()
        }
        val ibsave:ImageButton=findViewById(R.id.ib_save)
        ibsave.setOnClickListener{
            if(isreadstorageallowed()){
                showprogressdialog()
                lifecycleScope.launch{
                    val fldrawingview:FrameLayout=findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(fldrawingview))
                }
            }
        }
    }
    private fun getBitmapFromView(view: View):Bitmap {

        //Define a bitmap with the same size as the view.
        // CreateBitmap : Returns a mutable bitmap with the specified width and height
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun isreadstorageallowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result== PackageManager.PERMISSION_GRANTED
    }
    private fun requeststoragepermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE
            )){
            showrationaldialog("Drawing App ","Drawing App needs to Access Your External Storage")
        }
        else{
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            )
        }
    }
    private fun showrationaldialog(
        title:String,message:String
    ){
        val builder:AlertDialog.Builder=AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
            .setPositiveButton("Cancel"){
                dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
    var customprogressdialog:Dialog?=null
    private fun showprogressdialog() {
        customprogressdialog = Dialog(this)
        customprogressdialog?.setContentView(R.layout.dilaog_custom_progress)
        customprogressdialog!!.show()
    }
    private fun cancelprogressdialog(){
        if(customprogressdialog!=null){
            customprogressdialog?.dismiss()
            customprogressdialog=null
        }
    }
    private fun shareImage(result:String){

        /*MediaScannerConnection provides a way for applications to pass a
        newly created or downloaded media file to the media scanner service.
        The media scanner service will read metadata from the file and add
        the file to the media content provider.
        The MediaScannerConnectionClient provides an interface for the
        media scanner service to return the Uri for a newly scanned file
        to the client of the MediaScannerConnection class.*/

        /*scanFile is used to scan the file when the connection is established with MediaScanner.*/
        MediaScannerConnection.scanFile(
            this@MainActivity, arrayOf(result), null
        ) { path, uri ->
            // This is used for sharing the image after it has being stored in the storage.
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                uri
            ) // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
            shareIntent.type =
                "image/png" // The MIME type of the data being handled by this intent.
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share"
                )
            )// Activity Action: Display an activity chooser,
            // allowing the user to pick what they want to before proceeding.
            // This can be used as an alternative to the standard activity picker
            // that is displayed by the system when you try to start an activity with multiple possible matches,
            // with these differences in behavior:
        }
        // END
    }
    private fun showbrushsizechooserdialog(){
        val brushDialog= Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")
        val smallbtn=brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val medbtn=brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val numpick=brushDialog.findViewById<NumberPicker>(R.id.numpick)
        numpick.minValue=0
        numpick.maxValue=100
        smallbtn.setOnClickListener({
            drawingview?.setsizeforbrush(10.toFloat())
            brushDialog.dismiss()
        })
        medbtn.setOnClickListener{
            drawingview?.setsizeforbrush(20.toFloat())
            brushDialog.dismiss()
        }
        val larbtn=brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        larbtn.setOnClickListener{
            drawingview?.setsizeforbrush(30.toFloat())
            brushDialog.dismiss()
        }
        numpick.setOnValueChangedListener { picker, oldVal, newVal ->
            drawingview?.setsizeforbrush(newVal.toFloat())
        }
        brushDialog.show()
    }

    // TODO(Step 2 : A method to save the image.)
    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */

                    val f = File(getExternalFilesDir(null), "Drawings" + (System.currentTimeMillis()/1000) + ".jpg")
                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fo =
                        FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                    //We switch from io to ui thread to show a toast
                    runOnUiThread {
                        cancelprogressdialog()
                        if (!result.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully :$result",
                                Toast.LENGTH_SHORT
                            ).show()
                            //shareImage(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }
    private suspend fun  shareBitmapFile(mBitmap : Bitmap?):String{
        var result = ""

        withContext(Dispatchers.IO){
            if(mBitmap != null){
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                    val fName = File(externalCacheDir, "MyPaint_" + (System.currentTimeMillis()/1000) + ".jpg")

                    val fOutputStream = FileOutputStream(fName)
                    fOutputStream.write(bytes.toByteArray())
                    fOutputStream.close()
                    result = fName.absolutePath

                    runOnUiThread {
                        cancelprogressdialog()
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity, "Sharing $result",Toast.LENGTH_LONG ).show()
                        }else{
                            Toast.makeText(this@MainActivity, "File sharing failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }catch(e: Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result
    }
    fun paintclicked(view:View){
        if(view!==mimagebuttoncurrentpaint){
            val imagebutton=view as ImageButton
            val colortag=imagebutton.tag.toString()
            drawingview?.setcolor(colortag)
            imagebutton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallete_pressed))
            mimagebuttoncurrentpaint?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))
            mimagebuttoncurrentpaint=view
        }
    }
    }