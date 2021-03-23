package com.example.unity.texture

import android.opengl.EGL14
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TextureRendererPlugIn {

    private val mNativeApi = NativeApi()
    private val mRendererScope = CoroutineScope(Dispatchers.Main)

    private var mEGLContext: OffscreenEGLContext? = null

    init {
        Log.d(LogTag.PLUG_IN, "[${Thread.currentThread().name}] construct")
        mNativeApi.initialize()
        mNativeApi.onInitContext = this::onInitContext
        mNativeApi.onRegister = this::onRegister
    }

    fun destroy() {
        Log.d(LogTag.PLUG_IN, "[${Thread.currentThread().name}] destroy")
        mRendererScope.cancel()
        mEGLContext?.close()
        mNativeApi.release()
    }

    private fun onInitContext() {
        Log.d(LogTag.PLUG_IN, "[${Thread.currentThread().name}] onInitContext")
        val unityContext = EGL14.eglGetCurrentContext()
        if (unityContext == EGL14.EGL_NO_CONTEXT) {
            throw IllegalStateException("Unity EGLContext is invalid")
        }
        mRendererScope.launch {
            mEGLContext = OffscreenEGLContext(unityContext)
        }
    }

    private fun onRegister(texturePtr: Int, width: Int, height: Int) {
        val context = checkNotNull(mEGLContext) { "EGLContext is not initialized" }
        mRendererScope.launch {
            val texture = context.createSurfaceTexture(texturePtr, width, height)
            var isFrameAvailable = false
            val surface = context.createSurface(texture) {
                isFrameAvailable = true
            }
            try {
                val drawer = CircleDrawer(width, height)
                drawer.draw(surface)
                while (true) {
                    if (!isFrameAvailable) {
                        delay(texture.updateDelayMillis)
                        continue
                    }
                    isFrameAvailable = false
                    context.updateTexImage(texture)
                    drawer.draw(surface)
                    delay(texture.updateDelayMillis)
                }
            }
            finally {
                surface.release()
                texture.release()
            }
        }
    }
}