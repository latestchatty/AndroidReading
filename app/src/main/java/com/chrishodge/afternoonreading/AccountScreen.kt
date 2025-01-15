package com.chrishodge.afternoonreading.ui

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrishodge.afternoonreading.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "LoginWebView"

// Extension function to inject JavaScript
fun WebView.injectJavaScript(script: String, callback: ((String?) -> Unit)? = null) {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script) { result ->
                callback?.invoke(result)
            }
        } else {
            // For older versions, fall back to loadUrl
            loadUrl("javascript:$script")
            callback?.invoke(null)
        }
        Log.d(TAG, "JavaScript injection completed")
    }
}

@Composable
fun AccountScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val webViewModel = remember { LoginWebViewModel() }
        LoginWebView(
            viewModel = webViewModel,
            shrink = false,
            shrunkShowingQR = false
        )
    }
}

class LoginWebViewModel : ViewModel() {
    private val _link = MutableStateFlow("https://discord.com/login")
    val link: StateFlow<String> = _link.asStateFlow()

    private val _didFinishLoading = MutableStateFlow(false)
    val didFinishLoading: StateFlow<Boolean> = _didFinishLoading.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    fun updateToken(newToken: String?) {
        viewModelScope.launch {
            _token.emit(newToken)
            Log.d(TAG, "Token updated: ${newToken?.take(10)}...")
        }
    }

    fun updatePageTitle(title: String) {
        viewModelScope.launch {
            _pageTitle.emit(title)
            Log.d(TAG, "Page title updated: $title")
        }
    }

    fun updateLoadingState(isLoading: Boolean) {
        viewModelScope.launch {
            _didFinishLoading.emit(isLoading)
            Log.d(TAG, "Loading state updated: $isLoading")
        }
    }
}

@Composable
fun LoginWebView(
    viewModel: LoginWebViewModel,
    shrink: Boolean,
    shrunkShowingQR: Boolean
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "LoginWebView disposed")
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                Log.d(TAG, "Creating new WebView instance")

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadsImagesAutomatically = true
                    setSupportMultipleWindows(true)
                    javaScriptCanOpenWindowsAutomatically = true
                    Log.d(TAG, "WebView settings configured")
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d(TAG, "Page load started: $url")
                        viewModel.updateLoadingState(false)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "Page load finished: $url")
                        view?.title?.let { viewModel.updatePageTitle(it) }
                        viewModel.updateLoadingState(true)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        Log.e(TAG, "WebView Error: ${error?.description}, Error Code: ${error?.errorCode}")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString()
                        Log.d(TAG, "Attempting to load URL: $url")
                        return if (url == viewModel.link.value || url?.contains("newassets.hcaptcha.com") == true) {
                            false // Let the WebView handle the URL
                        } else {
                            Log.d(TAG, "Blocked URL loading: $url")
                            true // Block the URL from loading
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                        Log.d(TAG, "WebView Console: ${message.message()}")
                        return true
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        Log.d(TAG, "Loading progress: $newProgress%")
                        super.onProgressChanged(view, newProgress)
                    }
                }

                addJavascriptInterface(
                    WebViewJavaScriptInterface(viewModel),
                    "AndroidInterface"
                )

                // Inject the custom JavaScript
                val js = """
                    // Clear localStorage
                    localStorage.clear();
                    
                    // Keep original references
                    let originalLocalStorage = localStorage;
                    let originalConsoleLog = console.log;
                    
                    // Delete window localStorage
                    delete window.localStorage;
                    
                    // Logging function
                    const log = (operation, message) => {
                        originalConsoleLog('%c[LocalStorage Shim]%c', 'color:green;font-weight:700', '', operation, message);
                    };
                    
                    // Storage operations
                    const setItem = (key, value) => {
                        log('SET', key + ' <- ' + value);
                        if (key === 'token') {
                            AndroidInterface.onTokenReceived(JSON.parse(value));
                        }
                        originalLocalStorage[key] = value;
                    };
                    
                    const getItem = (key) => {
                        log('GET', key + ' -> ' + originalLocalStorage[key]);
                        return originalLocalStorage[key];
                    };
                    
                    // Create new localStorage proxy
                    window.localStorage = new Proxy({}, {
                        get(target, name) {
                            if (name === 'setItem') return setItem;
                            if (name === 'getItem') return getItem;
                            if (name === 'removeItem') return (key) => {
                                log('DELETE', key);
                                originalLocalStorage.removeItem(key);
                            };
                            return getItem(name);
                        },
                        set(target, key, value) {
                            setItem(key, value);
                        }
                    });
                    
                    // Style modifications
                    window.onload = () => {
                        document.querySelectorAll('#app-mount div[class^="characterBackground-"] > *:not(div)').forEach(e => e.remove());
                        
                        const style = document.createElement('style');
                        style.innerHTML = `
                            form[class*="authBox-"]::before, section[class*="authBox-"]::before {
                                content: unset;
                            }
                            form[class*="authBox-"], section[class*="authBox-"] {
                                background-color: rgba(0, 0, 0, .7)!important;
                                -webkit-backdrop-filter: blur(24px) saturate(140%);
                                border-radius: ${if (shrink) "0" else "12"}px;
                                ${if (shrink) "padding: 1rem;" else ""}
                            }
                            .theme-dark {
                                --input-background: rgba(0, 0, 0, .25)!important;
                            }
                            div[class^="select-"] > div > div:nth-child(2) {
                                background-color: var(--input-background)!important;
                            }
                            ${if (shrunkShowingQR) """
                                .qr-only div[class*="centeringWrapper-"]>div {
                                    flex-direction: column;
                                }
                                .qr-only div[class*="mainLoginContainer-"]>div:nth-child(2) {
                                    display: none;
                                }
                                .qr-only div[class*="qrLogin-"] {
                                    display: block!important;
                                    margin-top: 24px;
                                }
                            """ else ""}
                            span[class^='needAccount'], span[class*=' needAccount'] {
                                display: none !important;
                            }
                            button[class^='smallRegisterLink'], button[class*=' smallRegisterLink'] {
                                display: none !important;
                            }
                        `;
                        document.body.appendChild(style);
                    };
                """.trimIndent()

                // Use the new injectJavaScript extension function
                injectJavaScript(js) { result ->
                    Log.d(TAG, "JavaScript injection result: $result")
                }

                Log.d(TAG, "Starting to load URL: ${viewModel.link.value}")
                loadUrl(viewModel.link.value)
            }
        },
        update = { webView ->
            Log.d(TAG, "WebView update callback triggered")
            // Handle any updates if needed
        }
    )
}

class WebViewJavaScriptInterface(private val viewModel: LoginWebViewModel) {
    @JavascriptInterface
    fun onTokenReceived(token: String) {
        Log.d(TAG, "Token received from JavaScript")
        viewModel.updateToken(token)
    }
}