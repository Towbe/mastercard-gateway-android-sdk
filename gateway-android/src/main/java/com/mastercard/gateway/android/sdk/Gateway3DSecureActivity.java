package com.mastercard.gateway.android.sdk;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;
import java.util.Set;


public class Gateway3DSecureActivity extends AppCompatActivity implements Gateway3DSecureView {

    /**
     * The HTML used to initialize the WebView. Should be the HTML content returned from the Gateway
     * during the Check 3DS Enrollment call
     */
    public static final String EXTRA_HTML = "com.mastercard.gateway.android.HTML";

    /**
     * An OPTIONAL title to display in the toolbar for this activity
     */
    public static final String EXTRA_TITLE = "com.mastercard.gateway.android.TITLE";

    /**
     * The ACS Result data after performing 3DS
     */
    public static final String EXTRA_ACS_RESULT = "com.mastercard.gateway.android.ACS_RESULT";


    Toolbar toolbar;
    WebView webView;

    Gateway3DSecurePresenter presenter = new Gateway3DSecurePresenter();

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3dsecure);

        // init toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> cancel());

        // init web view
        webView = findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(buildWebViewClient());

        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();

        super.onDestroy();
    }

    @Override
    public String getDefaultTitle() {
        return getString(R.string.gateway_3d_secure_authentication);
    }

    @Override
    public String getExtraTitle() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString(EXTRA_TITLE);
        }

        return null;
    }

    @Override
    public String getExtraHtml() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString(EXTRA_HTML);
        }

        return null;
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void setWebViewHtml(String html) {
        webView.loadData(html, "text/html", "utf-8");
    }

    @Override
    public void loadWebViewUrl(Uri uri) {
        webView.loadUrl(uri.toString());
    }

    @Override
    public void intentToEmail(Uri uri) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setData(uri);

        startActivity(emailIntent);
    }

    @Override
    public void cancel() {
        onBackPressed();
    }

    @Override
    public void complete(String acsResult) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ACS_RESULT, acsResult);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    WebViewClient buildWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                presenter.webViewUrlChanges(Uri.parse(url));
                return true;
            }
        };
    }
}
