package ru.threedplatforma.sdksample

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

infix fun <T : Any?> Boolean.then(variable: T): T? {
    return if (this) variable else null
}

fun Context.copyToClipboard(message: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("modelId", message)
    clipboard.setPrimaryClip(clip)
}

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}