package app.daxstream.online;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://dax-stream-dan.floot.app/";
    private static final String HOST = "dax-stream-dan.floot.app";
    private FrameLayout root;
    private WebView browser;
    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;
    private int initialOrientation;
    private int initialUiFlags;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(5, 5, 6));
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        browser = new WebView(this);
        browser.setBackgroundColor(Color.BLACK);
        browser.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebSettings s = browser.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(browser, true);

        browser.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (!request.isForMainFrame()) return false;
                if ("https".equalsIgnoreCase(u.getScheme()) && HOST.equalsIgnoreCase(u.getHost())) return false;
                if ("https".equalsIgnoreCase(u.getScheme()) || "http".equalsIgnoreCase(u.getScheme())) {
                    openExternally(new Intent(Intent.ACTION_VIEW, u));
                    return true;
                }
                if ("intent".equalsIgnoreCase(u.getScheme())) {
                    try { openExternally(Intent.parseUri(u.toString(), Intent.URI_INTENT_SCHEME)); } catch (Exception ignored) { }
                    return true;
                }
                openExternally(new Intent(Intent.ACTION_VIEW, u));
                return true;
            }
        });

        browser.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreenView != null) { callback.onCustomViewHidden(); return; }
                fullscreenView = view;
                fullscreenCallback = callback;
                initialOrientation = getRequestedOrientation();
                initialUiFlags = getWindow().getDecorView().getSystemUiVisibility();
                browser.setVisibility(View.INVISIBLE);
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            @Override public void onHideCustomView() { closeFullscreen(); }
        });

        root.addView(browser, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        browser.loadUrl(APP_URL);
    }

    private void openExternally(Intent intent) {
        try { startActivity(intent); }
        catch (ActivityNotFoundException ignored) { }
        catch (SecurityException ignored) { }
    }

    private void closeFullscreen() {
        if (fullscreenView == null) return;
        root.removeView(fullscreenView);
        fullscreenView = null;
        browser.setVisibility(View.VISIBLE);
        setRequestedOrientation(initialOrientation);
        getWindow().getDecorView().setSystemUiVisibility(initialUiFlags);
        if (fullscreenCallback != null) {
            fullscreenCallback.onCustomViewHidden();
            fullscreenCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (fullscreenView != null) { closeFullscreen(); return; }
        if (browser.canGoBack()) { browser.goBack(); return; }
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (browser != null) {
            root.removeView(browser);
            browser.destroy();
            browser = null;
        }
        super.onDestroy();
    }
}
