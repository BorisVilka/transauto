package org.transauto.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.transauto.main.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView
    private var mUploadMessage: ValueCallback<Uri>? = null
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
        webView = binding.webView
        if(savedInstanceState!=null)
            webView.restoreState(savedInstanceState.getBundle("webViewState")!!)
        else {
            var cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView,true)
            var settings = webView.settings
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setGeolocationEnabled(true)
            settings.allowContentAccess = true
            settings.safeBrowsingEnabled = true
            settings.blockNetworkLoads = false
            settings.blockNetworkImage = false
            settings.safeBrowsingEnabled = true
            settings.loadWithOverviewMode = true
            settings.setSupportMultipleWindows(false)
            settings.offscreenPreRaster = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setAppCacheEnabled(true)
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.javaScriptEnabled = true
            settings.databasePath = "/data/data/"+this.packageName +"/databases/";
            settings.domStorageEnabled = true;

            webView.webChromeClient = object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    if(uploadMessage!=null) {
                        uploadMessage!!.onReceiveValue(null)
                        uploadMessage = null
                    }

                    uploadMessage = filePathCallback

                    val intent = fileChooserParams!!.createIntent()

                    try {
                        startActivityForResult(intent,100)
                    } catch (e: Exception) {
                        uploadMessage = null;
                        return false
                    }

                    return true
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    var transport = resultMsg!!.obj as WebView.WebViewTransport
                    transport.webView = webView
                    resultMsg.sendToTarget()
                    return true
                }
            }
            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if(webView.url.toString() == "https://crm.ta-7.ru/driver-dashboard" || !webView.canGoBack() || webView.url.toString()=="https://crm.ta-7.ru/") {
                        binding.toolbar.visibility = View.GONE
                    } else {
                        binding.toolbar.visibility = View.VISIBLE
                    }
                    CookieManager.getInstance().flush()
                    CookieSyncManager.createInstance(applicationContext).sync()
                }


                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    Log.d("TAG",request!!.url.toString());
                    if(webView.url.toString() == "https://crm.ta-7.ru/driver-dashboard") {
                        binding.toolbar.visibility = View.GONE
                    } else {
                        binding.toolbar.visibility = View.VISIBLE
                    }
                    if(request!!.url.toString().contains("tg:")) {
                        try {
                            var intent = Intent(Intent.ACTION_VIEW,Uri.parse(request!!.url.toString()));
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {

                        }
                    }
                    else if(request!!.url.toString().contains("tel:")) {
                        try {
                            var intent = Intent(Intent.ACTION_VIEW,Uri.parse(request!!.url.toString()));
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {

                        }
                    }
                    else {
                        view!!.loadUrl(request!!.url.toString())
                        binding.button.visibility = View.VISIBLE
                    }
                    return true
                }
            }
            //Log.d("TAG",getSharedPreferences("prefs", MODE_PRIVATE).getString("url","")!!)
            binding.webView.loadUrl("https://crm.ta-7.ru/")
            binding.button.setOnClickListener {
                if(binding.webView.canGoBack()) {
                    while(webView.url.toString() != "https://crm.ta-7.ru/driver-dashboard" && binding.webView.canGoBack()) binding.webView.goBack()
                    if(webView.url.toString() == "https://crm.ta-7.ru/driver-dashboard") {
                        binding.toolbar.visibility = View.GONE
                    } else {
                        binding.toolbar.visibility = View.VISIBLE
                    }
                } else {
                    binding.button.visibility = View.INVISIBLE
                }
            }
            binding.textView.setOnClickListener {
                if(binding.webView.canGoBack()) {
                    while(webView.url.toString() != "https://crm.ta-7.ru/driver-dashboard" && binding.webView.canGoBack()) binding.webView.goBack()
                     if(webView.url.toString() == "https://crm.ta-7.ru/driver-dashboard") {
                        binding.toolbar.visibility = View.GONE
                    } else {
                        binding.toolbar.visibility = View.VISIBLE
                    }
                } else {
                    binding.button.visibility = View.INVISIBLE
                }
            }
        }
    }
    override fun onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack()
            Log.d("TAG",webView.url.toString()+"|||")
            if(webView.url.toString() == "https://crm.ta-7.ru/driver-dashboard") {
                binding.toolbar.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE
            }
        }
        else super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            val result =
                if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } else Toast.makeText(applicationContext, "Failed to Upload Image", Toast.LENGTH_LONG)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var bundle = Bundle()
        webView.saveState(bundle)
        outState.putBundle("webViewState",bundle)
    }
}