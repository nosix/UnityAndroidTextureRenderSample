package com.example.unity.texture

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES32
import android.util.Log
import android.util.Size
import android.view.Surface
import java.io.Closeable
import javax.microedition.khronos.opengles.GL10

/**
 * EGLContext associated with the thread that draws offscreen textures
 *
 * Constructor and all methods must be called in the same thread.
 */
class OffscreenEGLContext(shareContext: EGLContext) : Closeable {

    companion object {
        private val PBUFFER_SIZE = Size(1, 1)
    }

    private val mDisplay: EGLDisplay
    private val mContext: EGLContext
    private val mSurface: EGLSurface

    private var isClosed: Boolean = false

    init {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            throw IllegalStateException("eglGetDisplay failed")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw IllegalStateException("eglInitialize failed")
        }
        Log.d(LogTag.CONTEXT, "EGL version: ${version.joinToString(".")}")
        val configAttributes = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 0,
            EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_NONE,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, 1, numConfigs, 0)) {
            throw IllegalStateException("eglChooseConfig failed")
        }
        if (numConfigs[0] <= 0 || configs[0] == null) {
            throw IllegalStateException("no EGLContext")
        }
        val contextAttributes = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        val context =
            EGL14.eglCreateContext(display, configs[0], shareContext, contextAttributes, 0)
        if (context == EGL14.EGL_NO_CONTEXT) {
            throw IllegalStateException("eglCreateContext failed")
        }
        val pbufferAttributes = intArrayOf(
            EGL14.EGL_WIDTH, PBUFFER_SIZE.width,
            EGL14.EGL_HEIGHT, PBUFFER_SIZE.height,
            EGL14.EGL_NONE
        )
        val surface = EGL14.eglCreatePbufferSurface(display, configs[0], pbufferAttributes, 0)
        if (surface == EGL14.EGL_NO_SURFACE) {
            throw IllegalStateException("eglCreatePbufferSurface failed")
        }
        mDisplay = display
        mContext = context
        mSurface = surface
    }

    fun createOffscreenTexture(textureId: Int, width: Int, height: Int): Texture {
        return run {
            val offscreenTextureId = createOffscreenGLTexture()
            val surfaceTexture = SurfaceTexture(offscreenTextureId)
            surfaceTexture.setDefaultBufferSize(width, height)
            Texture(textureId, width, height, offscreenTextureId, surfaceTexture)
        }
    }

    private fun createOffscreenGLTexture(): Int {
        val textureId = IntArray(1)
        GLES20.glGenTextures(textureId.size, textureId, 0)
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw IllegalStateException(error.toErrorString())
        }
        return textureId[0]
    }

    fun createSurface(texture: Texture, onFrameAvailable: (SurfaceTexture) -> Unit): Surface {
        // TODO remove run?
        return run {
            val surfaceTexture = texture.offscreenSurfaceTexture
            surfaceTexture.setOnFrameAvailableListener(onFrameAvailable)
            Surface(surfaceTexture)
        }
    }

    fun updateTexImage(texture: Texture) {
        run {
            val surfaceTexture = texture.offscreenSurfaceTexture
            val offscreenTextureId = texture.offscreenTextureId
            surfaceTexture.updateTexImage()
            Log.d(
                LogTag.CONTEXT,
                "${GLES20.glIsTexture(offscreenTextureId)}, ${GLES20.glIsTexture(texture.textureId)}"
            )
            GLES32.glCopyImageSubData(
                offscreenTextureId,
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                0, 0, 0, 0,
                texture.textureId,
                GLES20.GL_TEXTURE_2D,
                0, 0, 0, 0,
                texture.width, texture.height, 1
            )
            val error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                Log.w(LogTag.CONTEXT, error.toErrorString())
            }
        }
    }

    private inline fun <T> run(action: OffscreenEGLContext.() -> T): T {
        if (!EGL14.eglMakeCurrent(mDisplay, mSurface, mSurface, mContext)) {
            throw IllegalStateException("eglMakeCurrent failed")
        }
        try {
            return this.action()
        } finally {
            // Don't bind context to thread for a long time
            EGL14.eglMakeCurrent(
                mDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        }
    }

    @Synchronized
    override fun close() {
        if (isClosed) return
        isClosed = true
        EGL14.eglDestroySurface(mDisplay, mSurface)
        EGL14.eglDestroyContext(mDisplay, mContext)
        EGL14.eglReleaseThread()
        EGL14.eglTerminate(mDisplay)
    }

    private fun Int.toErrorString(): String = when (this) {
        GL10.GL_NO_ERROR -> "no error"
        GL10.GL_INVALID_ENUM -> "invalid enum"
        GL10.GL_INVALID_VALUE -> "invalid value"
        GL10.GL_INVALID_OPERATION -> "invalid operation"
        GL10.GL_STACK_OVERFLOW -> "stack overflow"
        GL10.GL_STACK_UNDERFLOW -> "stack underflow"
        GL10.GL_OUT_OF_MEMORY -> "out of memory"
        else -> "unknown"
    }

    class Texture(
        val textureId: Int,
        val width: Int,
        val height: Int,
        val offscreenTextureId: Int,
        val offscreenSurfaceTexture: SurfaceTexture
    ) : Closeable {
        var updateDelayMillis: Long = 100

        override fun close() {
            offscreenSurfaceTexture.release()
            GLES20.glDeleteTextures(1, intArrayOf(offscreenTextureId), 0)
        }
    }

}