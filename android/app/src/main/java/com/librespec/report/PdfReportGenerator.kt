package com.librespec.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateReport(
        context: Context, 
        materialName: String, 
        confidence: Float, 
        ambientTemp: Float
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("LibreSpec Kinetic Report", 50f, 50f, paint)
        
        paint.textSize = 16f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        canvas.drawText("Date: ${dateFormat.format(Date())}", 50f, 100f, paint)
        canvas.drawText("Matched Material: $materialName", 50f, 130f, paint)
        canvas.drawText("Confidence (DDTW): ${String.format("%.1f%%", confidence)}", 50f, 160f, paint)
        canvas.drawText("Ambient Temperature: ${String.format("%.1f°C", ambientTemp)}", 50f, 190f, paint)
        
        // In a full implementation, we would draw the Vico Chart bitmap onto this canvas
        // canvas.drawBitmap(chartBitmap, 50f, 250f, null)
        
        document.finishPage(page)
        
        val directory = File(context.getExternalFilesDir(null), "Reports")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "Report_${System.currentTimeMillis()}.pdf")
        
        try {
            document.writeTo(FileOutputStream(file))
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}
