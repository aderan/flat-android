package io.agora.flat.util

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

fun ViewGroup.inflate(@LayoutRes resource: Int, root: ViewGroup, attachToRoot: Boolean): View {
    return LayoutInflater.from(context).inflate(resource, root, attachToRoot)
}

fun View.renderTo(rect: Rect) {
    val layoutParams = layoutParams as FrameLayout.LayoutParams
    layoutParams.width = rect.width()
    layoutParams.height = rect.height()
    layoutParams.leftMargin = rect.left
    layoutParams.topMargin = rect.top
    this.layoutParams = layoutParams
}

fun ImageView.loadAvatarAny(data: Any) {
    val request = ImageRequest.Builder(context)
        .data(data)
        .target(this)
        .apply {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        .build()
    context.imageLoader.enqueue(request)
}

fun View.getViewRect(anchorView: View): Rect {
    val array = IntArray(2)
    getLocationOnScreen(array)

    val arrayP = IntArray(2)
    anchorView.getLocationOnScreen(arrayP)

    return Rect(
        array[0] - arrayP[0],
        array[1] - arrayP[1],
        array[0] - arrayP[0] + this.width,
        array[1] - arrayP[1] + this.height
    )
}

