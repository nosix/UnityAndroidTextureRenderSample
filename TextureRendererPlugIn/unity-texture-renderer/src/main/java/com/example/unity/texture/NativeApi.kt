package com.example.unity.texture

class NativeApi {

    var onInitContext: () -> Unit = {}
    var onRegister: (texturePtr: Int, width: Int, height: Int) -> Unit = { _, _, _ -> }

    external fun initialize()
    external fun release()

    fun initContext() {
        onInitContext()
    }

    fun registerTexture(texturePtr: Int, width: Int, height: Int) {
        onRegister(texturePtr, width, height)
    }

    companion object {
        init {
            System.loadLibrary("unity-texture-renderer")
        }
    }
}