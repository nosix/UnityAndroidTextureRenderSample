#include <jni.h>
#include <string>
#include <android/log.h>

#include <IUnityInterface.h>
#include <IUnityGraphics.h>

#define LOG_TAG "TextureRendererPlugIn"

#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ----- Unity Plugin API

void initContext();

void registerTexture(void *texturePtr, int width, int height);

static void UNITY_INTERFACE_API
OnInitContextEvent(int eventID) {
    initContext();
}

extern "C" UnityRenderingEvent UNITY_INTERFACE_EXPORT UNITY_INTERFACE_API
GetInitContextEventFunc() {
    return OnInitContextEvent;
}

extern "C" void UNITY_INTERFACE_EXPORT UNITY_INTERFACE_API
RegisterTexture(void *texturePtr, int width, int height) {
    registerTexture(texturePtr, width, height);
}

// ----- JNI

static JavaVM *s_JavaVM;
static jobject s_NativeApiObject = nullptr;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    s_JavaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_unity_texture_NativeApi_initialize(JNIEnv *env, jobject obj) {
    s_NativeApiObject = env->NewGlobalRef(obj);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_unity_texture_NativeApi_release(JNIEnv *env, jobject obj) {
    env->DeleteGlobalRef(s_NativeApiObject);
    s_NativeApiObject = nullptr;
}

// ----- functions

void initContext() {
    JNIEnv *env;
    bool isAttached = false;

    if (!s_NativeApiObject) {
        LOG_E("Not initialized");
        return;
    }

    if (s_JavaVM->GetEnv((void **) &env, JNI_VERSION_1_6) < 0) {
        if (s_JavaVM->AttachCurrentThread(&env, nullptr) < 0) {
            LOG_E("Can't attach current thread");
            return;
        }
        isAttached = true;
    }

    jclass cls = env->GetObjectClass(s_NativeApiObject);
    if (!cls) {
        if (isAttached)
            s_JavaVM->DetachCurrentThread();
        LOG_E("Can't get class of NativeApiObject");
        return;
    }

    jmethodID method = env->GetMethodID(cls, "initContext", "()V");
    if (!method) {
        if (isAttached)
            s_JavaVM->DetachCurrentThread();
        LOG_E("Can't get method of NativeApiObject");
        return;
    }

    env->CallVoidMethod(s_NativeApiObject, method);

    if (isAttached)
        s_JavaVM->DetachCurrentThread();
}

void registerTexture(void *texturePtr, int width, int height) {
    JNIEnv *env;
    bool isAttached = false;

    if (!s_NativeApiObject) {
        LOG_E("Not initialized");
        return;
    }

    if (s_JavaVM->GetEnv((void **) &env, JNI_VERSION_1_6) < 0) {
        if (s_JavaVM->AttachCurrentThread(&env, nullptr) < 0) {
            LOG_E("Can't attach current thread");
            return;
        }
        isAttached = true;
    }

    jclass cls = env->GetObjectClass(s_NativeApiObject);
    if (!cls) {
        if (isAttached)
            s_JavaVM->DetachCurrentThread();
        LOG_E("Can't get class of NativeApiObject");
        return;
    }

    jmethodID method = env->GetMethodID(cls, "registerTexture", "(III)V");
    if (!method) {
        if (isAttached)
            s_JavaVM->DetachCurrentThread();
        LOG_E("Can't get method of NativeApiObject");
        return;
    }

    env->CallVoidMethod(s_NativeApiObject, method, (int) (size_t) texturePtr, width, height);

    if (isAttached)
        s_JavaVM->DetachCurrentThread();
}

// https://docs.unity3d.com/Manual/NativePluginInterface.html
// https://github.com/Unity-Technologies/NativeRenderingPlugin