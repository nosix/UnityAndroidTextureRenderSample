using UnityEngine;
using UnityEngine.UI;

public class TextureRendererOnAndroid : MonoBehaviour
{
    [SerializeField] private RenderTexture renderTexture;

    private TextureRendererPlugIn _plugIn;
    private Texture2D _texture;

    private void Start()
    {
        _plugIn = FindObjectOfType<TextureRendererPlugIn>();
        InitializeTexture2D();
    }

    private void InitializeTexture2D()
    {
        if (_plugIn == null)
        {
            Debug.LogError("[TextureRendererOnAndroid] WebViewPlugIn component is not found");
            return;
        }

        if (renderTexture != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init render texture");
            _texture = CreateTexture2D(renderTexture.width, renderTexture.height);
            _plugIn.RegisterTexture(_texture);
            return;
        }

        var material = GetComponent<Renderer>()?.material;
        if (material != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init material texture");
            _texture = CreateTexture2D(1024, 1024);
            material.mainTexture = _texture;
            _plugIn.RegisterTexture(_texture);
            return;
        }

        var rawImage = GetComponent<RawImage>();
        if (rawImage != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init raw image texture");
            _texture = CreateTexture2D(1024, 1024);
            rawImage.texture = _texture;
            _plugIn.RegisterTexture(_texture);
            return;
        }

        Debug.LogError("[TextureRendererOnAndroid] Texture not found.");
    }

    private static Texture2D CreateTexture2D(int width, int height)
    {
        Debug.Log($"[TextureRendererOnAndroid] Texture2D size is ({width}, {height})");
        var texture = new Texture2D(width, height, TextureFormat.ARGB32, false)
        {
            filterMode = FilterMode.Point
        };
        texture.Apply();
        return texture;
    }

    private void Update()
    {
        BlitRenderTexture();
    }

    private void BlitRenderTexture()
    {
        if (renderTexture is null) return;
        renderTexture.enableRandomWrite = true;
        RenderTexture.active = renderTexture;
        Graphics.Blit(_texture, renderTexture);
    }
}