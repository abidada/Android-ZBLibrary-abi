/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package zblibrary.demo.activity;

import static zblibrary.demo.util.PermissionUtil.PERMISSIONS_LOCATION;
import static zblibrary.demo.util.PermissionUtil.REQUEST_EXTERNAL_STORAGE;
import static zblibrary.demo.util.PermissionUtil.REQUEST_GEOLOCATION;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebBackForwardList;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;
import zblibrary.demo.R;
import zblibrary.demo.util.Constant;
import zblibrary.demo.util.h5.JsApi;
import zblibrary.demo.util.h5.JsEchoApi;
import zuo.biao.library.base.BaseActivity;
import zuo.biao.library.interfaces.OnBottomDragListener;
import zuo.biao.library.util.CommonUtil;

/**
 * 联系人资料界面
 *
 * @author Lemon
 */
public class MyWebViewActivity extends BaseActivity implements OnClickListener, OnLongClickListener, OnBottomDragListener {
    public static final String TAG = "MyWebViewActivity";
    DWebView dwebView;
    //WebView dwebView;

    /**
     * 启动这个Activity的Intent
     *
     * @param context
     * @return
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, MyWebViewActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_main, this);
        //功能归类分区方法，必须调用<<<<<<<<<<
        initView();
        initData();
        initEvent();
        //功能归类分区方法，必须调用>>>>>>>>>>
    }

    @Override
    public void initView() {
        // 初始化webview
        initWebView();
        initButtons();
    }

    private static final int DISABLE_ALPHA = 120;
    private static final int ENABLE_ALPHA = 255;
    private static final int FILE_CHOOSER_REQUEST = 100;
    private long mClickBackTime = 0;
    private static final String mHomeUrl = "file:///android_asset/webpage/homePage.html";

    private ValueCallback<Uri[]> mFilePathCallback;
    private GeolocationPermissionsCallback mGeolocationCallback;
    private String locationPermissionUrl;

    /**
     * webview选择文件
     *
     * @param isMulti 是否是多选
     */
    private void openFileChooseProcess(boolean isMulti) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("*/*");
        if (isMulti) {
            Log.e(TAG, "putExtra");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(Intent.createChooser(intent, "FileChooser"), FILE_CHOOSER_REQUEST);
    }

    public static boolean verifyLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_GEOLOCATION);
            return false;
        }
        return true;
    }

    private void initWebChromeClient() {
        final Context context = this;
        final Activity activity = this;
        dwebView.setWebChromeClient(new WebChromeClient() {
            /**
             * 具体接口使用细节请参考文档：
             * https://x5.tencent.com/docs/webview.html
             * 或 Android WebKit 官方：
             * https://developer.android.com/reference/android/webkit/WebChromeClient
             */

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.i(TAG, "onProgressChanged, newProgress:" + newProgress + ", view:" + view);
                changGoForwardButton(view);
            }

            @Override
            public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
                new AlertDialog.Builder(context).setTitle("JS弹窗Override")
                        .setMessage(message)
                        .setPositiveButton("OK", (dialogInterface, i) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView webView, String url, String message, JsResult result) {
                new AlertDialog.Builder(context).setTitle("JS弹窗Override")
                        .setMessage(message)
                        .setPositiveButton("OK", (dialogInterface, i) -> result.confirm())
                        .setNegativeButton("Cancel", (dialogInterface, i) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsBeforeUnload(WebView webView, String url, String message, JsResult result) {
                new AlertDialog.Builder(context).setTitle("页面即将跳转")
                        .setMessage(message)
                        .setPositiveButton("OK", (dialogInterface, i) -> result.confirm())
                        .setNegativeButton("Cancel", (dialogInterface, i) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView webView, String url, String message, String defaultValue, JsPromptResult result) {
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                new AlertDialog.Builder(context).setTitle("JS弹窗Override")
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton("OK", (dialogInterface, i) -> result.confirm(input.getText().toString()))
                        .setCancelable(false)
                        .show();
                return true;
            }

            /**
             * Return value usage see FILE_CHOOSE_REQUEST in
             * {@link MyWebViewActivity#onActivityResult(int, int, Intent)}
             */
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                Log.i(TAG, "openFileChooser: " + fileChooserParams.getMode());
                mFilePathCallback = filePathCallback;
                openFileChooseProcess(fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissionsCallback geolocationPermissionsCallback) {
                if (verifyLocationPermissions(activity)) {
                    geolocationPermissionsCallback.invoke(origin, true, false);
                } else {
                    locationPermissionUrl = origin;
                    mGeolocationCallback = geolocationPermissionsCallback;
                }
            }
        });
    }

    /**
     * 初始化webview
     */
    private void initWebView() {
        //完全仿造D:\code\android\X5\X5Demo\app\src\main\java\com\tencent\tbs\demo\feature\BaseWebViewActivity.java
        //x5官方BaseWebViewActivity逻辑
        //dwebView = findView(R.id.webview);
        dwebView = new DWebView(context);
        ViewGroup mContainer = findViewById(R.id.webViewContainer);
        mContainer.addView(dwebView);

        WebSettings webSetting = dwebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowFileAccess(false);
        webSetting.setSupportZoom(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setDomStorageEnabled(true);

        initWebViewClient();
        initWebChromeClient();
        initJavaScriptInterface();
        // set debug mode

        dwebView.setWebContentsDebuggingEnabled(true);
        // 排版适应屏幕
        // textZoom:100表示正常，120表示文字放大1.2倍
        dwebView.getSettings().setTextZoom(80);
        //该方法可以获取当前文字大小
        Log.i(TAG, String.valueOf(dwebView.getSettings().getTextZoom()));

        //始终无图：所有图片都不显示
        //dwebView.getSettings().setLoadsImagesAutomatically(false);
        //dwebView.getSettings().setBlockNetworkImage(true);

        //2.4 夜间模式
        // enable:true(日间模式)，enable：false（夜间模式）
        //mWebView.getSettingsExtension().setDayOrNight(eanble);

        //2.5 form保存及自动填充
        // 是否记录并提示用户填充对应form元素，内核默认是true
        //dwebView.getSettings().setSaveFormData(true);

//        dwebView.loadUrl("http://22.6.144.116:8899/"); //wifi: pab-app
//        dwebView.loadUrl("https://test-b-fat.pingan.com.cn/orionApp/hybirdH5/index.html");
//        dwebView.loadUrl("192.168.0.112:886");
//        dwebView.loadUrl("192.168.0.112:887");
        dwebView.loadUrl("https://zhongyi666.top");

//        test  temp
//        dwebView.callHandler("addValue", new Object[]{3, 4}, new OnReturnValue<Integer>() {
//            @Override
//            public void onValue(Integer retValue) {
//                showToast(retValue);
//            }
//        });

//        test temp
//        dwebView.callHandler("addValue", new Object[]{3, 4}, new OnReturnValue<Integer>() {
//            @Override
//            public void onValue(Integer retValue) {
//                System.out.println("retValue = " + retValue);
//                 showToast(retValue);
//            }
//        });
    }

    private void initJavaScriptInterface() {
        final Activity context = this;
        // dwebView.addJavascriptObject(new JsApi(this.getActivity() ), null);
        //dwebView.addJavascriptObject(new JsEchoApi(), "echo");
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.addJavascriptObject(new JsEchoApi(), "echo");

        //另一种添加接口方式,参考写法
        //dwebView.addJavascriptInterface(new WebViewJavaScriptFunction() {
        //    @Override
        //    public void onJsFunctionCalled(String tag) {
        //
        //    }
        //
        //    @JavascriptInterface
        //    public void openQRCodeScan() {
        //        new IntentIntegrator(context).initiateScan();
        //    }
        //
        //    @JavascriptInterface
        //    public void openDebugX5() {
        //        dwebView.loadUrl("http://debugx5.qq.com");
        //    }
        //
        //    @JavascriptInterface
        //    public void openWebkit() {
        //        startActivity(new Intent(context, SystemWebViewActivity.class));
        //    }
        //
        //
        //}, "Android");
    }

    private void initWebViewClient() {
        dwebView.setWebViewClient(new WebViewClient() {

            //对于url打开会回调该接口 方式2
            //@Override
            //public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            //    return super.shouldOverrideUrlLoading(webView, s);
            //}

            //对于url打开会回调该接口  方式1推荐
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                return super.shouldOverrideUrlLoading(webView, webResourceRequest);
                //return true;//返回true，表示拦截,webview不在进行事件操作
            }

            /**
             * 具体接口使用细节请参考文档：
             * https://x5.tencent.com/docs/webview.html
             * 或 Android WebKit 官方：
             * https://developer.android.com/reference/android/webkit/WebChromeClient
             */

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "onPageStarted, view:" + view + ", url:" + url);
                //mUrlEditText.setText(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished, view:" + view + ", url:" + url);
                changGoForwardButton(view);
            }

            @Override
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "onReceivedError: " + errorCode
                        + ", description: " + description
                        + ", url: " + failingUrl);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                if (webResourceRequest.getUrl().toString().contains("debugdebug")) {
                    InputStream in = null;
                    Log.i("AterDebug", "shouldInterceptRequest");
                    try {
                        in = new FileInputStream(new File("/sdcard/1.png"));
                    } catch (Exception e) {

                    }

                    return new WebResourceResponse("image/*", "utf-8", in);
                } else {
                    return super.shouldInterceptRequest(webView, webResourceRequest);
                }

            }
        });
    }


    private ImageButton mBackBtn;
    private ImageButton mForwardBtn;
    private EditText mUrlEditText;

    /**导航栏左侧的侧边栏的父容器*/
    private DrawerLayout mDrawerLayout;
    //导航视图
    private NavigationView mNavigationView;
    private void initButtons() {
        final Context context = this.getApplicationContext();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mBackBtn = findViewById(R.id.btn_back);
        mBackBtn.setImageAlpha(DISABLE_ALPHA);
        mBackBtn.setEnabled(false);
        mBackBtn.setOnClickListener(view -> {
            if (dwebView != null && dwebView.canGoBack()) {
                dwebView.goBack();
            }
        });

        mForwardBtn = findViewById(R.id.btn_forward);
        mForwardBtn.setEnabled(false);
        mForwardBtn.setImageAlpha(DISABLE_ALPHA);
        mForwardBtn.setOnClickListener(view -> {
            if (dwebView != null && dwebView.canGoForward()) {
                dwebView.goForward();
            }
        });

        findViewById(R.id.btn_more).setOnClickListener(this::showPopupMenu);
        findViewById(R.id.btn_reload).setOnClickListener(view -> {
            if (dwebView != null) {
                dwebView.reload();
            }
        });
        findViewById(R.id.btn_exit).setOnClickListener(view -> finish());

        mUrlEditText = findViewById(R.id.urlEdit);
        mUrlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String url = mUrlEditText.getEditableText().toString();
                if (!url.contains("://") && !url.startsWith("javascript:")) {
                    url = "https://" + url;
                }
                dwebView.loadUrl(url);
                if (mUrlEditText != null) {
                    mUrlEditText.clearFocus();
                }
            }
            return true;
        });
        mUrlEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        findViewById(R.id.urlLoad).setOnClickListener(v->{
            if (dwebView != null) {
                String url = mUrlEditText.getEditableText().toString();
                if (!url.contains("://") && !url.startsWith("javascript:")) {
                    url = "https://" + url;
                }
                dwebView.loadUrl(url);
            }
            if (mUrlEditText != null) {
                mUrlEditText.clearFocus();
            }
        });
    }

    private void changGoForwardButton(WebView view) {
        try {
            if (view.canGoBack()) {
                mBackBtn.setImageAlpha(ENABLE_ALPHA);
                mBackBtn.setEnabled(true);
            } else {
                mBackBtn.setImageAlpha(DISABLE_ALPHA);
                mBackBtn.setEnabled(false);
            }
            if (view.canGoForward()) {
                mForwardBtn.setImageAlpha(ENABLE_ALPHA);
                mForwardBtn.setEnabled(true);
            } else {
                mForwardBtn.setImageAlpha(DISABLE_ALPHA);
                mForwardBtn.setEnabled(false);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Exception: " + t);
        }
    }

    private void showPopupMenu(View view) {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        this.showToast("设置");

        //PopupMenu popupMenu = new PopupMenu(this, view);
        //// 获取布局文件
        //popupMenu.getMenuInflater().inflate(R.menu.website_menu, popupMenu.getMenu());
        //popupMenu.show();
        //final Activity fContext = this;
        //// 通过上面这几行代码，就可以把控件显示出来了
        //popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        //
        //    @SuppressLint("NonConstantResourceId")
        //    @Override
        //    public boolean onMenuItemClick(MenuItem item) {
        //        // 控件每一个item的点击事件
        //        String url = "";
        //        switch (item.getItemId()) {
        //            case R.id.debugtbs:
        //                url = "http://debugtbs.qq.com";
        //                break;
        //            case R.id.debugx5:
        //                url = "http://debugx5.qq.com";
        //                break;
        //            case R.id.qrcode:
        //                //new IntentIntegrator(fContext).initiateScan();
        //                break;
        //            default:
        //                url = mHomeUrl;
        //                break;
        //        }
        //        if (dwebView != null && !"".equals(url)) {
        //            dwebView.loadUrl(url);
        //        }
        //        return true;
        //    }
        //});
    }

    /* Don't care about the Base UI Logic below ^_^ */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            initWebView();
        }

        if (mGeolocationCallback != null && requestCode == REQUEST_GEOLOCATION) {
            boolean allow = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            mGeolocationCallback.invoke(locationPermissionUrl, allow, false);
            mGeolocationCallback = null;
            locationPermissionUrl = "";
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dwebView != null && dwebView.canGoBack()) {
                WebBackForwardList webBackForwardList = dwebView.copyBackForwardList();
                //dwebView.goBackOrForward(-1);
                dwebView.goBack();
                changGoForwardButton(dwebView);
                return true;
            }
            long currentTime = System.currentTimeMillis();
            // 3秒内连按两次后退按钮，退出应用
            if (currentTime - mClickBackTime < 3000) {
//                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                mClickBackTime = currentTime;
            }

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (mFilePathCallback != null) {
                    if (data != null && data.getClipData() != null) {
                        //有选择多个文件
                        int count = data.getClipData().getItemCount();
                        Log.i(TAG, "url count ：  " + count);
                        Uri[] uris = new Uri[count];
                        int currentItem = 0;
                        while (currentItem < count) {
                            Uri fileUri = data.getClipData().getItemAt(currentItem).getUri();
                            uris[currentItem] = fileUri;
                            currentItem = currentItem + 1;
                        }
                        mFilePathCallback.onReceiveValue(uris);
                    } else {
                        Uri result = data == null ? null : data.getData();
                        Log.e(TAG, "" + result);
                        mFilePathCallback.onReceiveValue(new Uri[]{result});
                    }
                    mFilePathCallback = null;
                }
            } else {
                //没有选择任何文件,直接选择返回:
                //模拟一个空的uri,回调回去,才能状态设置初始化,才能继续重新选择文件
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    //mFilePathCallback.onReceiveValue(new Uri[]{Uri.parse("")});
                    //content://com.android.providers.media.documents/document/image%3A31104
                    mFilePathCallback = null;
                }
            }
        } else {
            // zxing扫描
            //IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            //if (result != null) {
            //    if (result.getContents() == null) {
            //        Toast.makeText(this, "扫描结果为空", Toast.LENGTH_SHORT).show();
            //    } else {
            //        String str = result.getContents();
            //        if (dwebView != null) {
            //            dwebView.loadUrl(str);
            //        }
            //    }
            //}
        }
    }

    @Override
    protected void onDestroy() {
        if (dwebView != null) {
            dwebView.destroy();
        }
        super.onDestroy();
    }


    @Override
    public void initData() {
    }

    @Override
    public void initEvent() {
//        findView(R.id.llAboutWeibo, this).setOnLongClickListener(this);
        this.showToast("Android原生toast");
    }

    //系统自带监听方法<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    @Override
    public void onDragBottom(boolean rightToLeft) {
        if (rightToLeft) {
            return;
        }

        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAboutWeibo:
                dwebView.callHandler("addValue", new Object[]{3, 4}, new OnReturnValue<Integer>() {
                    @Override
                    public void onValue(Integer retValue) {
                        startActivity(AboutActivity.createIntent(MyWebViewActivity.this));
                        showToast(retValue);
                    }
                });
//                toActivity(WebViewActivity.createIntent(context, "博客", Constant.APP_OFFICIAL_BLOG));
                break;
            default:
                break;
        }
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {

            case R.id.llAboutWeibo:
                CommonUtil.copyText(context, Constant.APP_OFFICIAL_BLOG);
                return true;
            default:
                break;
        }
        return false;


        //生命周期、onActivityResult>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        //Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        //内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


        //内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    }

}