# UnityAndroidTextureRenderSample

This is a sample to draw on Unity Texture with Android Plugin.

## Verified environment

- Emulator
  - Android 9.0 (Google Play), API 28, x86
  - Android 11.0 (Google Play), API 30, x86
- Device
  - Android 8.1, API 26, QUALCOMM SDM845 for arm64 (Nreal Computing Unit)
  - Android 10, API 29, Galaxy S9 (SCV38)

## Usage

1. Open TextureRendererPlugIn project on Android Studio
2. Publish AAR
  ```shell
  ./gradlew publishToMavenLocal
  ```
3. Open AndroidTextureRendererExample project on Unity Editor
4. Switch Platform to Android
5. Select Run Device
6. Build and Run

## Abstract

- This will generate an EGLContext for the thread that draws with the Android Plugin.
- The generated EGLContext shares Unity's EGLContext.
- The Texture target passed by Unity can be TEXTURE_2D or TEXTUER_EXTERNAL_OES.
  - In the verification environment,
    - the target in the emulator is TEXTUER_EXTERNAL_OES
    - the target in the device is TEXTURE_2D
- If the target is TEXTURE_2D, draw it on the offscreen texture and then copy it to the Unity texture.
- If the target is TEXTUER_EXTERNAL_OES, draw directly on the Unity texture.
