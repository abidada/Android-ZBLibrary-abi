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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;
import zblibrary.demo.DEMO.DemoMainActivity;
import zblibrary.demo.R;
import zblibrary.demo.model.User;
import zblibrary.demo.util.Constant;
import zblibrary.demo.util.MenuUtil;
import zblibrary.demo.util.h5.JsApi;
import zblibrary.demo.util.h5.JsEchoApi;
import zblibrary.demo.view.UserView;
import zuo.biao.library.base.BaseActivity;
import zuo.biao.library.base.BaseModel;
import zuo.biao.library.base.BaseView.OnDataChangedListener;
import zuo.biao.library.interfaces.OnBottomDragListener;
import zuo.biao.library.interfaces.OnHttpResponseListener;
import zuo.biao.library.manager.CacheManager;
import zuo.biao.library.ui.BottomMenuView;
import zuo.biao.library.ui.BottomMenuView.OnBottomMenuItemClickListener;
import zuo.biao.library.ui.BottomMenuWindow;
import zuo.biao.library.ui.EditTextInfoActivity;
import zuo.biao.library.ui.TextClearSuit;
import zuo.biao.library.ui.WebViewActivity;
import zuo.biao.library.util.CommonUtil;
import zuo.biao.library.util.JSON;
import zuo.biao.library.util.Log;
import zuo.biao.library.util.StringUtil;

/**
 * 联系人资料界面
 *
 * @author Lemon
 */
public class MyWebViewActivity extends BaseActivity implements OnClickListener, OnLongClickListener, OnBottomDragListener {
    public static final String TAG = "MyWebViewActivity";


    DWebView dwebView;

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
        setContentView(R.layout.activity_web_view, this);
        //功能归类分区方法，必须调用<<<<<<<<<<
        initView();
        initData();
        initEvent();
        //功能归类分区方法，必须调用>>>>>>>>>>
    }

    @Override
    public void initView() {
        dwebView = findView(R.id.webview);
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.addJavascriptObject(new JsEchoApi(), "echo");

        // set debug mode
        DWebView.setWebContentsDebuggingEnabled(true);
//        dwebView.addJavascriptObject(new JsApi(this.getActivity() ), null);
//        dwebView.addJavascriptObject(new JsEchoApi(), "echo");
//        dwebView.loadUrl("https://test-b-fat.pingan.com.cn/orionApp/hybirdH5/index.html");
        dwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView webView, int i, String s, String s1) {
                super.onReceivedError(webView, i, s, s1);
            }

            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                System.out.println("webView = " + webView + ", webResourceRequest = " + webResourceRequest + ", webResourceError = " + webResourceError);
                super.onReceivedError(webView, webResourceRequest, webResourceError);
            }

            @Override
            public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
                System.out.println("webView = " + webView + ", webResourceRequest = " + webResourceRequest + ", webResourceResponse = " + webResourceResponse);
                super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                System.out.println("webView = " + webView + ", sslErrorHandler = " + sslErrorHandler + ", sslError = " + sslError);
                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }
        });
        dwebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                System.out.println("webView =   " + consoleMessage.message());
                System.out.println("webView =   " + consoleMessage.lineNumber());
                System.out.println("webView =   " + consoleMessage.toString());
                return super.onConsoleMessage(consoleMessage);
            }
        });
//        dwebView.loadUrl("http://22.6.144.116:8899/"); //wifi: pab-app
//        dwebView.loadUrl("https://test-b-fat.pingan.com.cn/orionApp/hybirdH5/index.html");
//        dwebView.loadUrl("192.168.0.112:886");
        dwebView.loadUrl("192.168.0.112:887");
        dwebView.loadUrl("https://dz.xcxgy.cn/forum.php?mod=index&mobile=2");


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


//        dWebView.loadUrl("www.baidu.com");
    }

    @Override
    public void initData() {
    }

    @Override
    public void initEvent() {
        findView(R.id.llAboutWeibo, this).setOnLongClickListener(this);
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