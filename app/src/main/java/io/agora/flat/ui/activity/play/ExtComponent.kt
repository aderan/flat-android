package io.agora.flat.ui.activity.play

import android.os.Build
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import io.agora.flat.R
import io.agora.flat.databinding.ComponentExtensionBinding
import io.agora.flat.util.showToast
import kotlinx.coroutines.flow.collect

/**
 * display common loading, toast, dialog
 */
class ExtComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
) : BaseComponent(activity, rootView) {
    companion object {
        val TAG = ExtComponent::class.simpleName
    }

    private lateinit var extensionBinding: ComponentExtensionBinding

    private val viewModel: ExtensionViewModel by activity.viewModels()

    override fun onCreate(owner: LifecycleOwner) {
        // injectApi()
        initView()
        // initListener()
        observeState()
    }

    private fun initView() {
        extensionBinding = ComponentExtensionBinding.inflate(activity.layoutInflater, rootView, true)
    }

    private fun observeState() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                showLoading(it.loading)
                if (it.error != null) {
                    activity.showToast(it.error.message)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        extensionBinding.loadingLayout.isVisible = show
        if (show) {
            extensionBinding.loadingView.load(R.raw.loading, gifImageLoader) {
                crossfade(true)
            }
        }
    }

    private val gifImageLoader = ImageLoader.Builder(activity).apply {
        componentRegistry {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder(activity))
            } else {
                add(GifDecoder())
            }
        }
    }.build()
}
