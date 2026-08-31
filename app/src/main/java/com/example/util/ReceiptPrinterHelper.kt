package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.data.local.OrderEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptPrinterHelper {

    enum class PrintLayout {
        THERMAL_RECEIPT_POS, // 58mm/80mm POS style roll
        STANDARD_A4_INVOICE  // Full A4/Letter official gaming invoice
    }

    /**
     * Triggers Android Native PrintManager to print or export the receipt as PDF.
     */
    fun printOrderReceipt(
        context: Context,
        order: OrderEntity,
        layout: PrintLayout = PrintLayout.THERMAL_RECEIPT_POS,
        onComplete: (() -> Unit)? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val jobName = "FreeFire_Receipt_${order.orderId}"
        val adapter = OrderReceiptPrintAdapter(context, order, layout, onComplete)

        val printAttributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(
                if (layout == PrintLayout.THERMAL_RECEIPT_POS) {
                    PrintAttributes.MediaSize.ISO_A6
                } else {
                    PrintAttributes.MediaSize.ISO_A4
                }
            )
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, adapter, printAttributes)
    }

    /**
     * Share text receipt via system intent.
     */
    fun shareReceiptText(context: Context, order: OrderEntity) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
        val text = buildString {
            appendLine("══════════════════════════════════════")
            appendLine("    🔥 FREE FIRE TOP-UP OFFICIAL RECEIPT 🔥")
            appendLine("══════════════════════════════════════")
            appendLine("Status: SUCCESSFUL / INSTANT DELIVERED ⚡")
            appendLine("Order ID: ${order.orderId}")
            appendLine("Transaction Ref: ${order.transactionRef}")
            appendLine("Date: $dateStr")
            appendLine("--------------------------------------")
            appendLine("PLAYER DETAILS:")
            appendLine("Nickname: ${order.playerNickname}")
            appendLine("Player UID: ${order.playerId}")
            appendLine("Server Region: ${order.serverRegion}")
            appendLine("--------------------------------------")
            appendLine("PURCHASE DETAILS:")
            appendLine("Item: ${order.itemName}")
            if (order.diamondCount > 0) {
                appendLine("Diamonds: ${order.diamondCount} + ${order.bonusDiamonds} Bonus 💎")
            }
            appendLine("Payment Gateway: ${order.paymentMethod}")
            appendLine("Processing Fee: $0.00 (Zero Fee Capped)")
            if (order.discountAmount > 0) {
                appendLine("Promo Discount: -$${order.discountAmount} (${order.promoCodeApplied ?: ""})")
            }
            appendLine("TOTAL PAID: $${order.price}")
            appendLine("══════════════════════════════════════")
            appendLine("Verification QR Code Payload:")
            appendLine(generateReceiptVerificationString(order))
            appendLine("══════════════════════════════════════")
            appendLine("Thank you for choosing Free Fire Official Top-Up!")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Free Fire Receipt - ${order.orderId}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Free Fire Receipt")
        if (context !is Activity) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    /**
     * Builds the string encoded in the receipt's dynamic QR code.
     */
    fun generateReceiptVerificationString(order: OrderEntity): String {
        return "FF-RECEIPT|OID:${order.orderId}|UID:${order.playerId}|NAME:${order.playerNickname}|AMT:$${order.price}|DIAMONDS:${order.diamondCount + order.bonusDiamonds}|TXN:${order.transactionRef}|FEE:0.00|STATUS:OK"
    }

    private class OrderReceiptPrintAdapter(
        private val context: Context,
        private val order: OrderEntity,
        private val layout: PrintLayout,
        private val onComplete: (() -> Unit)? = null
    ) : PrintDocumentAdapter() {

        private var pdfDocument: PdfDocument? = null
        private val pageWidth = if (layout == PrintLayout.THERMAL_RECEIPT_POS) 380 else 595
        private val pageHeight = if (layout == PrintLayout.THERMAL_RECEIPT_POS) 680 else 842

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }

            val info = PrintDocumentInfo.Builder("Receipt_${order.orderId}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()

            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                return
            }

            pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument!!.startPage(pageInfo)

            renderReceiptCanvas(page.canvas, order, layout)

            pdfDocument!!.finishPage(page)

            try {
                destination?.fileDescriptor?.let { fd ->
                    FileOutputStream(fd).use { outStream ->
                        pdfDocument!!.writeTo(outStream)
                    }
                }
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                onComplete?.invoke()
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
            } finally {
                pdfDocument?.close()
                pdfDocument = null
            }
        }

        private fun renderReceiptCanvas(canvas: Canvas, order: OrderEntity, layout: PrintLayout) {
            val isThermal = layout == PrintLayout.THERMAL_RECEIPT_POS

            // Canvas Background
            val bgPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // Header Banner
            val headerPaint = Paint().apply {
                color = if (isThermal) android.graphics.Color.DKGRAY else android.graphics.Color.parseColor("#E65100")
                style = Paint.Style.FILL
            }
            val headerHeight = if (isThermal) 60f else 80f
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), headerHeight, headerPaint)

            // Header Text
            val titlePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = if (isThermal) 16f else 22f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("FREE FIRE OFFICIAL TOP-UP", pageWidth / 2f, if (isThermal) 34f else 46f, titlePaint)

            val subTitlePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FFF3E0")
                textSize = if (isThermal) 10f else 12f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("TAX INVOICE & DIGITAL RECEIPT (0% FEE)", pageWidth / 2f, if (isThermal) 48f else 66f, subTitlePaint)

            var curY = headerHeight + 25f

            val labelPaint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = if (isThermal) 10f else 12f
                isAntiAlias = true
            }

            val valuePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = if (isThermal) 10f else 12f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))

            fun drawLine(label: String, value: String, highlightColor: Int? = null) {
                canvas.drawText(label, 20f, curY, labelPaint)
                if (highlightColor != null) {
                    val oldColor = valuePaint.color
                    valuePaint.color = highlightColor
                    canvas.drawText(value, pageWidth - 20f, curY, valuePaint)
                    valuePaint.color = oldColor
                } else {
                    canvas.drawText(value, pageWidth - 20f, curY, valuePaint)
                }
                curY += if (isThermal) 16f else 20f
            }

            fun drawDivider() {
                val divPaint = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    strokeWidth = 1f
                }
                canvas.drawLine(20f, curY, pageWidth - 20f, curY, divPaint)
                curY += if (isThermal) 12f else 16f
            }

            // Order Metadata
            drawLine("Order ID:", order.orderId)
            drawLine("Transaction Ref:", order.transactionRef)
            drawLine("Date & Time:", dateStr)
            drawLine("Delivery Status:", "DELIVERED ⚡", android.graphics.Color.parseColor("#2E7D32"))

            drawDivider()

            // Player Info
            drawLine("Player Nickname:", order.playerNickname)
            drawLine("Player UID:", order.playerId)
            drawLine("Server Region:", order.serverRegion)

            drawDivider()

            // Item Details
            drawLine("Item Name:", order.itemName)
            if (order.diamondCount > 0) {
                drawLine("Diamonds Delivered:", "${order.diamondCount + order.bonusDiamonds} 💎 (${order.diamondCount} + ${order.bonusDiamonds} Bonus)")
            }
            drawLine("Payment Gateway:", order.paymentMethod)
            drawLine("Processing Fee:", "$0.00 (Zero-Fee Capped)", android.graphics.Color.parseColor("#2E7D32"))

            if (order.discountAmount > 0) {
                drawLine("Promo Discount:", "-$${order.discountAmount} (${order.promoCodeApplied ?: ""})", android.graphics.Color.parseColor("#C62828"))
            }

            drawDivider()

            // Total Amount
            val totalLabelPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = if (isThermal) 14f else 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val totalValPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#E65100")
                textSize = if (isThermal) 16f else 22f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            canvas.drawText("TOTAL PAID:", 20f, curY + 6f, totalLabelPaint)
            canvas.drawText("$${order.price}", pageWidth - 20f, curY + 6f, totalValPaint)
            curY += if (isThermal) 28f else 36f

            drawDivider()

            // Generate & Draw Dynamic QR Code
            val qrString = generateReceiptVerificationString(order)
            val qrSize = if (isThermal) 130 else 170
            val qrBitmap = QRCodeGenerator.generateQrBitmap(
                content = qrString,
                size = qrSize,
                foregroundColor = android.graphics.Color.BLACK,
                backgroundColor = android.graphics.Color.WHITE
            )

            val qrLeft = (pageWidth - qrSize) / 2f
            canvas.drawBitmap(qrBitmap, qrLeft, curY, null)
            curY += qrSize + 12f

            // QR caption
            val qrCaptionPaint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = if (isThermal) 8f else 10f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Scan QR to verify authentic top-up receipt", pageWidth / 2f, curY, qrCaptionPaint)
            curY += 12f

            // Footer note
            val footerPaint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = if (isThermal) 7f else 9f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Free Fire 100% Direct Delivery • Zero Processing Fee Guarantee", pageWidth / 2f, curY, footerPaint)
        }
    }
}
