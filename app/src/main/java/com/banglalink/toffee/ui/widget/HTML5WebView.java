package com.banglalink.toffee.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.banglalink.toffee.R;

public class HTML5WebView extends WebView {

    private Context                             mContext;
    private MyWebChromeClient                   mWebChromeClient;
    private View                                mCustomView;
    private FrameLayout                         mCustomViewContainer;
    private WebChromeClient.CustomViewCallback  mCustomViewCallback;

    private FrameLayout                         mLayout;

    static final String LOGTAG = "HTML5WebView";
    private VelBoxProgressDialog progressDialog;

    private void init(Context context) {
        mContext = context;
        progressDialog = new VelBoxProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.show();
        Activity a = (Activity) mContext;

        mLayout = new FrameLayout(context);

        FrameLayout mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(a).inflate(R.layout.custom_screen, null);
        FrameLayout mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
        mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);

        mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

        // Configure the webview
        WebSettings s = getSettings();
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setJavaScriptEnabled(true);
        mWebChromeClient = new MyWebChromeClient();
        setWebChromeClient(mWebChromeClient);
        setWebViewClient(new Html5WebViewClient());

        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        s.setDomStorageEnabled(true);

        mContentView.addView(this);
    }

    public HTML5WebView(Context context) {
        super(context);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FrameLayout getLayout() {
        return mLayout;
    }

    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((mCustomView == null) && canGoBack()){
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
        {
            //Log.i(LOGTAG, "here in on ShowCustomView");
            HTML5WebView.this.setVisibility(View.GONE);

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomViewContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null)
                return;

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();

            HTML5WebView.this.setVisibility(View.VISIBLE);
            HTML5WebView.this.goBack();
        }


        @Override
        public void onReceivedTitle(WebView view, String title) {
            ((Activity) mContext).setTitle(title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
           if(newProgress > 30){
               progressDialog.dismiss();
           }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }


    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
}
