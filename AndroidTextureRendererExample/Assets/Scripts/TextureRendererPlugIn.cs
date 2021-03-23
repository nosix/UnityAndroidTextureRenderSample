using System;
using System.Runtime.InteropServices;
using UnityEngine;

// The Script Execution Order must be before TextureRendererOnAndroid.
public class TextureRendererPlugIn : MonoBehaviour
{
    private AndroidJavaObject _androidPlugInInstance;

    [DllImport("unity-texture-renderer")]
    private static extern IntPtr GetInitContextEventFunc();

    [DllImport("unity-texture-renderer")]
    private static extern void RegisterTexture(IntPtr texturePtr, int width, int height);

    private void Start()
    {
        InitializeAndroidApi();
        CallOnInitContextEvent();
    }

    private void InitializeAndroidApi()
    {
        if (_androidPlugInInstance == null)
        {
            _androidPlugInInstance = new AndroidJavaObject("com.example.unity.texture.TextureRendererPlugIn");
        }
    }

    private void OnDestroy()
    {
        _androidPlugInInstance?.Call("destroy");
        _androidPlugInInstance = null;
    }

    private void CallOnInitContextEvent()
    {
        GL.IssuePluginEvent(GetInitContextEventFunc(), 0);
    }

    public void RegisterTexture(Texture texture)
    {
        RegisterTexture(texture.GetNativeTexturePtr(), texture.width, texture.height);
    }
}