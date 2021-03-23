using System.Collections;
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
        _texture = InitializeTexture2D();
        if (_texture == null) return;
        _plugIn.RegisterTexture(_texture);
        if (renderTexture is null) return;
        StartCoroutine(BlitRenderTexture());
    }

    private Texture2D InitializeTexture2D()
    {
        if (renderTexture != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init render texture");
            return CreateTexture2D(renderTexture.width, renderTexture.height);
        }

        var material = GetComponent<Renderer>()?.material;
        if (material != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init material texture");
            var texture = CreateTexture2D(1024, 1024);
            material.mainTexture = texture;
            return texture;
        }

        var rawImage = GetComponent<RawImage>();
        if (rawImage != null)
        {
            Debug.Log("[TextureRendererOnAndroid] init raw image texture");
            var texture = CreateTexture2D(1024, 1024);
            rawImage.texture = texture;
            return texture;
        }

        Debug.LogError("[TextureRendererOnAndroid] Texture not found.");
        return null;
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

    private IEnumerator BlitRenderTexture()
    {
        while (true)
        {
            renderTexture.enableRandomWrite = true;
            RenderTexture.active = renderTexture;
            Graphics.Blit(_texture, renderTexture);
            yield return null;
        }

        // ReSharper disable once IteratorNeverReturns
    }
}