package edu.stanford.spezi.contact

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import edu.stanford.spezi.ui.StringResource

fun ContactOption.Companion.call(number: String) =
    ContactOption(
        image = Icons.Default.Call,
        title = StringResource(R.string.contact_call),
        action = { context ->
            runCatching {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }
                context.startActivity(intent)
            }.onFailure {
                logger.e(it) { "Failed to open intent for phone call to `$number`." }
            }
        }
    )
