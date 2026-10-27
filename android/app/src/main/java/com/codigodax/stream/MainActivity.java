package com.codigodax.stream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/** DAX Stream para Android. Site HTTPS na Hostinger, sem senhas embutidas no APK. */
public class MainActivity extends Activity {
    private static final String HOME = "https://stream.codigodax.com/";
    private FrameLayout root;
    private WebView web;
    private ProgressBar progress;
    private View customVideo;
    private WebChromeClient.CustomViewCallback customVideoCallback;

    private static final String PIP_JS =
        "(function(){" +
        "if(window.__daxAndroidInjected)return;window.__daxAndroidInjected=true;" +
        "var css=document.createElement('style');" +
        "css.textContent='html.dax-android-pip #modal{display:flex!important;position:fixed!important;inset:0!important;padding:0!important;background:black!important;z-index:999999!important}'+" +
        "'html.dax-android-pip #modal .panel{width:100vw!important;height:100vh!important;max-height:none!important;border:0!important;background:#000!important}'+" +
        "'html.dax-android-pip .panelhead,html.dax-android-pip #episodes,html.dax-android-pip .dax-controls,html.dax-android-pip .dax-player-top,html.dax-android-pip .dax-alt{display:none!important}'+" +
        "'html.dax-android-pip #modal .body,html.dax-android-pip #mediaWrap{margin:0!important;padding:0!important;width:100%!important;height:100%!important;display:flex!important;align-items:center!important;background:#000!important}'+" +
        "'html.dax-android-pip #mediaWrap video{width:100%!important;height:100%!important;max-height:100vh!important;object-fit:contain!important}';" +
        "document.head.appendChild(css);" +
        "document.addEventListener('click',function(e){" +
        "var button=e.target.closest('.dax-mini-toggle,[aria-label=\"Picture-in-Picture nativo\"],[aria-label=\"Abrir mini-player dentro da página\"]');" +
        "if(!button)return;" +
        "e.preventDefault();e.stopImmediatePropagation();" +
        "if(window.DaxAndroid)window.DaxAndroid.pip();" +
        "},true);" +
        "})();";

    @Override @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8,9,14));
        getWindow().setNavigationBarColor(Color.rgb(8,9,14));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(8,9,14));
        setContentView(root);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(8,9,14));
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        root.addView(web, new FrameLayout.LayoutParams(-1,-1));
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress, new FrameLayout.LayoutParams(-1,5,Gravity.TOP));

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        // Compatibilidade com provedores que redirecionam mídia HTTPS para HTTP.
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.addJavascriptInterface(new AndroidBridge(),"DaxAndroid");

        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView v,String url,Bitmap icon) {
                progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView v,String url) {
                progress.setVisibility(View.GONE);
                if(url.startsWith(HOME) || url.startsWith("https://stream.codigodax.com"))
                    v.evaluateJavascript(PIP_JS,null);
                CookieManager.getInstance().flush();
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String host = u.getHost();
                if (host!=null && (host.equals("stream.codigodax.com") || host.endsWith(".codigodax.com")))
                    return false;
                if (request.isForMainFrame()) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,u));
                    } catch(Exception ex) {
                        Toast.makeText(MainActivity.this,"Link não disponível",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
            @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError err) {
                if(r.isForMainFrame())
                    Toast.makeText(MainActivity.this,"Falha de conexão. Verifique sua internet.",Toast.LENGTH_LONG).show();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v,int pct) {
                progress.setProgress(pct);
                if(pct==100)progress.setVisibility(View.GONE);
            }
            @Override public void onShowCustomView(View v,CustomViewCallback cb) {
                if(customVideo!=null){cb.onCustomViewHidden();return;}
                customVideo=v;customVideoCallback=cb;
                web.setVisibility(View.GONE);
                root.addView(v,new FrameLayout.LayoutParams(-1,-1));
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            @Override public void onHideCustomView(){closeFullScreen();}
        });
        if(state!=null)web.restoreState(state);
        else web.loadUrl(HOME);
    }

    private final class AndroidBridge {
        @JavascriptInterface public void pip() {
            runOnUiThread(()->{
                try {
                    enterPictureInPictureMode(new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16,9)).build());
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Mini-player indisponível neste aparelho",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override public void onPictureInPictureModeChanged(boolean active,Configuration configuration) {
        super.onPictureInPictureModeChanged(active,configuration);
        if(web!=null)web.evaluateJavascript(
            "document.documentElement.classList.toggle('dax-android-pip',"+(active?"true":"false")+");",null);
    }

    private void closeFullScreen(){
        if(customVideo==null)return;
        root.removeView(customVideo);customVideo=null;
        web.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if(customVideoCallback!=null)customVideoCallback.onCustomViewHidden();
        customVideoCallback=null;
    }

    @Override public void onBackPressed(){
        if(customVideo!=null)closeFullScreen();
        else if(web.canGoBack())web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        super.onSaveInstanceState(out);web.saveState(out);
    }
    @Override protected void onPause(){
        CookieManager.getInstance().flush();super.onPause();
    }
    @Override protected void onDestroy(){
        if(web!=null){web.destroy();web=null;}super.onDestroy();
    }
}
