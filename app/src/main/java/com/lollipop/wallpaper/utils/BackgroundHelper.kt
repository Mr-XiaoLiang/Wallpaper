package com.lollipop.wallpaper.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.dialog.HeaderToast
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object BackgroundHelper {

    private const val PICTURE_DIR = "BACKGROUND"
    private const val PICTURE_NAME = "BACKGROUND"

    private var globalLastChangeFlag = 0L

    private fun createRequestPictureIntent(): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
    }

    private fun parseActivityResult(intent: Intent?): Uri? {
        return intent?.data
    }

    fun createActivityResultContract(): ActivityResultContract<Unit, Uri> {
        return ResultContract()
    }

    fun read(context: Context): BackgroundReader {
        return BackgroundReader(context)
    }

    fun write(context: Activity): BackgroundWriter {
        return BackgroundWriter(context)
    }

    class BackgroundReader(private val context: Context) {

        var bitmap: Bitmap? = null
            private set

        private var backgroundFlag = 0L

        val needReload: Boolean
            get() {
                return backgroundFlag == globalLastChangeFlag
            }

        fun load() {
            synchronized(BackgroundHelper::class) {
                bitmap = null
                val filesDir = context.filesDir
                val pictureDir = File(filesDir, PICTURE_DIR)
                if (!pictureDir.exists()) {
                    return
                }
                val pictureFile = File(pictureDir, PICTURE_NAME)
                if (!pictureFile.exists()) {
                    return
                }
                bitmap = BitmapFactory.decodeFile(pictureFile.path)
                backgroundFlag = globalLastChangeFlag
            }
        }

        fun clean() {
            bitmap = null
            backgroundFlag = 0L
        }
    }

    class BackgroundWriter(private val context: Activity) {

        fun getActivityResultCallback(): ActivityResultCallback<Uri> {
            return ResultCallback(this)
        }

        fun delete() {
            HeaderToast.show(context, context.getString(R.string.start_delete))
            doAsync({
                onUI {
                    HeaderToast.show(context, context.getString(R.string.delete_fail))
                }
            }) {
                File(File(context.filesDir, PICTURE_DIR), PICTURE_NAME).delete()
                globalLastChangeFlag = System.currentTimeMillis()
                onUI {
                    HeaderToast.show(context, context.getString(R.string.delete_successful))
                }
            }
        }

        fun onActivityResult(data: Uri) {
            showLoading()
            doAsync({
                onUI {
                    hideLoading(false)
                }
            }) {
                savePicture(data) { successful ->
                    onUI {
                        hideLoading(successful)
                    }
                }
            }
        }

        private fun showLoading() {
            HeaderToast.show(context, context.getString(R.string.start_loading))
            // TODO
        }

        private fun hideLoading(successful: Boolean) {
            HeaderToast.show(
                context,
                context.getString(
                    if (successful) {
                        R.string.end_loading_successful
                    } else {
                        R.string.end_loading_fail
                    }
                )
            )
            // TODO
        }

        /**
         * 保存图片信息
         */
        private fun savePicture(uri: Uri, onEnd: (successful: Boolean) -> Unit) {
            // 图片数据，如果没有就返回
            if (uri == Uri.EMPTY) {
                onEnd(false)
                return
            }
            // 获取屏幕大小，尽可能的获取和屏幕一样大的图片
            val screenSize = getScreenSize()
            // 获取不到屏幕尺寸就算了吧
            if (screenSize.isEmpty) {
                onEnd(false)
                return
            }
            val screenWidth = screenSize.width
            val screenHeight = screenSize.height
            val option = BitmapFactory.Options()
            // 只获取尺寸
            option.inJustDecodeBounds = true
            // 解析图片
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                option
            )
            // 计算采样比例
            val srcWidth = option.outWidth
            val srcHeight = option.outHeight
            // 向下取整找到长宽的采样率，尽量大一点
            val widthRatio = (srcWidth * 1F / screenWidth).toInt()
            val heightRatio = (srcHeight * 1F / screenHeight).toInt()
            // 取最小值，但是不能小于1
            val ratio = max(min(widthRatio, heightRatio), 1)
            // 去掉只获取尺寸的flag
            option.inJustDecodeBounds = false
            // 设置采样率
            option.inSampleSize = ratio
            // 获取图片
            val bitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                option
            )
            if (bitmap == null) {
                onEnd(false)
                return
            }
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            if (bitmapWidth != screenWidth || bitmapHeight != screenHeight) {
                val newBitmap =
                    Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(newBitmap)
                val matrix = Matrix()
                val wRatio = screenWidth * 1F / bitmapWidth
                val hRatio = screenHeight * 1F / bitmapHeight
                val matrixScale = max(wRatio, hRatio)
                matrix.setScale(matrixScale, matrixScale)
                val offsetX = (matrixScale * bitmapWidth - screenWidth) * 0.5F * -1
                val offsetY = (matrixScale * bitmapHeight - screenHeight) * 0.5F * -1
                matrix.postTranslate(offsetX, offsetY)
                canvas.drawBitmap(bitmap, matrix, null)
                bitmap.recycle()
                saveBitmap(newBitmap)
                newBitmap.recycle()
            } else {
                saveBitmap(bitmap)
                bitmap.recycle()
            }
            onEnd(true)
        }

        /**
         * 保存图片
         */
        private fun saveBitmap(bitmap: Bitmap) {
            val filesDir = context.filesDir
            val pictureDir = File(filesDir, PICTURE_DIR)
            if (pictureDir.exists()) {
                if (pictureDir.isFile) {
                    pictureDir.delete()
                }
            } else {
                pictureDir.mkdirs()
            }
            val pictureFile = File(pictureDir, PICTURE_NAME)
            val outputStream = FileOutputStream(pictureFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            globalLastChangeFlag = System.currentTimeMillis()
        }

        /**
         * 获取屏幕尺寸
         */
        private fun getScreenSize(): ScreenSize {
            val out = Point()
            context.windowManager.defaultDisplay.getRealSize(out)
            if (out.x < 100 || out.y < 100) {
                if (versionThen(Build.VERSION_CODES.R)) {
                    val bounds = context.windowManager.maximumWindowMetrics.bounds
                    out.x = bounds.width()
                    out.y = bounds.height()
                } else {
                    val displayMetrics = context.resources.displayMetrics
                    out.y = displayMetrics.heightPixels
                    out.x = displayMetrics.widthPixels
                }
            }
            return ScreenSize(out.x, out.y)
        }
    }


    private class ScreenSize(
        val width: Int,
        val height: Int
    ) {

        val isEmpty: Boolean
            get() {
                return width < 100 || height < 100
            }

    }

    private class ResultContract : ActivityResultContract<Unit, Uri>() {
        override fun createIntent(context: Context, input: Unit?): Intent {
            return createRequestPictureIntent()
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return parseActivityResult(intent) ?: Uri.EMPTY
        }
    }

    private class ResultCallback(
        private val backgroundHelper: BackgroundWriter
    ) : ActivityResultCallback<Uri> {

        override fun onActivityResult(result: Uri?) {
            result ?: return
            backgroundHelper.onActivityResult(result)
        }

    }

}