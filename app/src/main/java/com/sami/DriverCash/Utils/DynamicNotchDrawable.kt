package com.sami.DriverCash.Utils

import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log // Importación para Logs

class DynamicNotchDrawable(backgroundColorInput: Int) : Drawable() {

    private val TAG = "NotchDebug_Drawable"

    private val paint = Paint().apply {
        // Restaurar el color original y el estilo de relleno
        color = backgroundColorInput // Usar el color pasado al constructor
        style = Paint.Style.FILL     // Rellenar la forma
        isAntiAlias = true
    }

    private val path = Path()

    private var selectedItemCenterX: Float = -1f
    private var notchWidth: Float = 180f
    private var notchHeight: Float = 30f

    init {
        Log.d(TAG, "Initialized with backgroundColor: $backgroundColorInput")
    }

    fun updateNotchProperties(centerX: Float, newNotchWidth: Float, newNotchHeight: Float) {
        Log.d(TAG, "updateNotchProperties called with centerX: $centerX, newNotchWidth: $newNotchWidth, newNotchHeight: $newNotchHeight")
        var needsRedraw = false
        if (selectedItemCenterX != centerX) {
            Log.d(TAG, "selectedItemCenterX changed from $selectedItemCenterX to $centerX")
            selectedItemCenterX = centerX
            needsRedraw = true
        }
        if (notchWidth != newNotchWidth) {
            Log.d(TAG, "notchWidth changed from $notchWidth to $newNotchWidth")
            notchWidth = newNotchWidth
            needsRedraw = true
        }
        if (notchHeight != newNotchHeight) {
            Log.d(TAG, "notchHeight changed from $notchHeight to $newNotchHeight")
            notchHeight = newNotchHeight
            needsRedraw = true
        }

        if (needsRedraw) {
            Log.d(TAG, "Needs redraw, calling preparePath() and invalidateSelf()")
            preparePath()
            invalidateSelf()
        } else {
            Log.d(TAG, "No property changes, redraw not triggered by updateNotchProperties.")
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        Log.d(TAG, "onBoundsChange called: bounds = $bounds")
        preparePath()
    }

    // --- ESTA ES LA FUNCIÓN preparePath() RESTAURADA ---
    private fun preparePath() {
        path.reset()
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        // Asegurarnos de que el paint tenga el color y estilo correctos para la montaña
        // Esto es redundante si el init del paint ya lo hace, pero por seguridad:
        // paint.color = backgroundColorInput // Ya se establece en el constructor y se usa
        paint.style = Paint.Style.FILL

        val topBaselineY = notchHeight // La "línea base" superior de la barra, desde donde sube la montaña

        Log.d(TAG, "preparePath (Restored): drawableWidth=$width, drawableHeight=$height, topBaselineY=$topBaselineY")
        Log.d(TAG, "preparePath (Restored): current selectedItemCenterX=$selectedItemCenterX, notchWidth=$notchWidth, notchHeight=$notchHeight")

        if (width == 0f || height == 0f) {
            Log.w(TAG, "preparePath (Restored): Width or Height is 0, path not drawn.")
            return
        }

        path.moveTo(0f, topBaselineY)

        if (selectedItemCenterX >= 0 && notchWidth > 0 && notchHeight > 0) {
            Log.d(TAG, "preparePath (Restored): Drawing notch path.")
            val notchStartX = (selectedItemCenterX - notchWidth / 2).coerceIn(0f, width)
            val notchEndX = (selectedItemCenterX + notchWidth / 2).coerceIn(0f, width)
            Log.d(TAG, "preparePath (Restored): notchStartX=$notchStartX, notchEndX=$notchEndX, mountainTopY (control point Y)=0f")

            path.lineTo(notchStartX, topBaselineY)
            // La curva cuadrática: el punto de control Y (0f) define la cima de la montaña.
            // Si notchHeight es, por ejemplo, 30f, topBaselineY es 30f. La montaña sube desde Y=30f hasta Y=0f.
            path.quadTo(selectedItemCenterX, 0f, notchEndX, topBaselineY)
            path.lineTo(width, topBaselineY)
        } else {
            Log.d(TAG, "preparePath (Restored): Drawing simple top line (no notch). selectedItemCenterX=$selectedItemCenterX, notchWidth=$notchWidth, notchHeight=$notchHeight")
            path.lineTo(width, topBaselineY)
        }

        path.lineTo(width, height) // Línea inferior derecha
        path.lineTo(0f, height)    // Línea inferior izquierda
        path.close() // Cierra el path volviendo al punto inicial (0f, topBaselineY) si es necesario, o al último moveTo.
        Log.d(TAG, "preparePath (Restored): Path closed.")
    }
    // --- FIN DE LA FUNCIÓN preparePath() RESTAURADA ---


    override fun draw(canvas: Canvas) {
        Log.d(TAG, "draw called. Drawing path.")
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        Log.d(TAG, "setAlpha called: $alpha")
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        Log.d(TAG, "setColorFilter called.")
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
