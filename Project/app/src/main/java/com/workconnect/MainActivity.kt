package com.workconnect

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val backStack = ArrayDeque<Screen>()
    private var currentScreen = Screen.Splash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWebView()
        show(Screen.Splash, addToBackStack = false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (currentScreen == Screen.Splash) {
                show(Screen.PhoneAuthentication)
            }
        }, SPLASH_DELAY_MS)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    return if (url == "#" || url.startsWith("file:///android_asset/stitch/")) {
                        false
                    } else {
                        false
                    }
                }

                override fun onPageFinished(view: WebView, url: String) {
                    injectNavigationHooks()
                }
            }

            addJavascriptInterface(ScreenBridge(), "WorkConnectAndroid")
        }

        setContentView(webView)
    }

    private fun show(screen: Screen, addToBackStack: Boolean = true) {
        if (addToBackStack && screen != currentScreen) {
            backStack.addLast(currentScreen)
        }

        currentScreen = screen
        webView.loadUrl("file:///android_asset/stitch/${screen.folder}/code.html")
    }

    private fun injectNavigationHooks() {
        webView.evaluateJavascript(
            """
            (() => {
              if (window.__workConnectHooksInstalled) return;
              window.__workConnectHooksInstalled = true;

              const clickableSelector = [
                'button',
                'a',
                '[role="button"]',
                'input[type="button"]',
                'input[type="submit"]',
                '.cursor-pointer',
                '[onclick]',
                '[data-icon]'
              ].join(',');

              const textOf = (node) => {
                const icon = node.querySelector?.('.material-symbols-outlined')?.textContent || node.dataset?.icon || '';
                const text = (
                  node.innerText ||
                  node.value ||
                  node.getAttribute?.('aria-label') ||
                  node.getAttribute?.('placeholder') ||
                  ''
                ).trim();
                return `${'$'}{icon} ${'$'}{text}`.replace(/\s+/g, ' ').trim();
              };

              const actionableFromPath = (event) => {
                for (const node of event.composedPath()) {
                  if (!node || node === document || node === window || !node.matches) continue;
                  if (node.matches(clickableSelector)) return node;

                  const text = textOf(node).toLowerCase();
                  if (
                    text.includes('view details') ||
                    text.includes('book now') ||
                    text.includes('view profile') ||
                    text.includes('write review') ||
                    text.includes('chat') ||
                    text.includes('earnings') ||
                    text.includes('profile setup')
                  ) {
                    return node;
                  }
                }
                return null;
              };

              const sendTap = (label, event) => {
                if (!label) return false;
                const handled = window.WorkConnectAndroid.onTap(label, window.location.href);
                if (handled && event) {
                  event.preventDefault();
                  event.stopPropagation();
                }
                return handled;
              };

              document.addEventListener('click', (event) => {
                const target = event.target.closest(clickableSelector) || event.target.closest('nav a') || actionableFromPath(event);
                if (!target) return;
                sendTap(textOf(target), event);
              }, true);

              document.addEventListener('submit', (event) => {
                const form = event.target;
                const submitter = event.submitter;
                const label = textOf(submitter) || `submit ${'$'}{document.title}`;
                sendTap(label, event);
              }, true);
            })();
            """.trimIndent(),
            null
        )
    }

    override fun onBackPressed() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
            webView.loadUrl("file:///android_asset/stitch/${currentScreen.folder}/code.html")
        } else {
            super.onBackPressed()
        }
    }

    inner class ScreenBridge {
        @JavascriptInterface
        fun onTap(label: String, url: String): Boolean {
            val next = resolveNavigation(label, url) ?: return false
            runOnUiThread { show(next) }
            return true
        }

        @JavascriptInterface
        fun toast(message: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resolveNavigation(rawLabel: String, url: String): Screen? {
        val label = rawLabel.lowercase()
        val screen = Screen.fromAssetUrl(url)

        return when {
            "workconnect splash" in label -> Screen.PhoneAuthentication
            "get started" in label && screen == Screen.CustomerProfileSetup -> Screen.SetupComplete
            "get started" in label -> Screen.PhoneAuthentication
            "send otp" in label || "continue" in label && screen == Screen.PhoneAuthentication -> Screen.OtpVerification
            "verify" in label && screen == Screen.OtpVerification -> Screen.CustomerProfileSetup
            "complete setup" in label || "save profile" in label && screen == Screen.CustomerProfileSetup -> Screen.SetupComplete
            "go to dashboard" in label || "start exploring" in label -> Screen.CustomerDashboard
            "arrow_back" in label || label == "back" -> null

            "customer" in label && "dashboard" in label -> Screen.CustomerDashboard
            "worker" in label && "dashboard" in label -> Screen.WorkerDashboard
            label == "home" || label.endsWith(" home") -> if (isWorkerScreen(screen)) Screen.WorkerDashboard else Screen.CustomerDashboard

            "electric_bolt" in label || "plumbing" in label || "carpenter" in label || "cleaning_services" in label || "imagesearch_roller" in label || "construction" in label -> Screen.WorkersByCategory
            "search" in label || "view all" in label || "categories" in label -> Screen.WorkersByCategory
            "filter" in label -> Screen.WorkersByCategory
            "view details" in label || "details" in label && "booking" !in label -> Screen.WorkerDetails
            "view profile" in label -> Screen.WorkerDetails
            "book now" in label || label == "book" || "book worker" in label -> Screen.BookWorker
            "confirm booking" in label || "request booking" in label || "schedule booking" in label -> Screen.BookingDetailsSummary
            "pay" in label || "confirm" in label && screen == Screen.BookingDetailsSummary -> Screen.MyBookings

            "bookings" in label || "my bookings" in label -> if (isWorkerScreen(screen)) Screen.WorkerBookingsFullList else Screen.MyBookings
            "assignment" in label -> if (isWorkerScreen(screen)) Screen.WorkerBookingsFullList else Screen.MyBookings
            "accept" in label || "booking details" in label -> Screen.BookingDetailsSummary

            "saved" in label || "bookmark" in label -> Screen.SavedWorkers
            "notifications" in label || "notification" in label -> Screen.Notifications
            "chat" in label || "message" in label || "send message" in label -> Screen.ChatWithWorker
            "submit review" in label -> Screen.MyBookings
            "review" in label && "write" in label -> Screen.WriteReview
            "all reviews" in label || "reviews" in label -> Screen.AllReviewsList

            "profile" in label || "person" in label -> if (isWorkerScreen(screen)) Screen.WorkerProfileViewEdit else Screen.CustomerProfile
            "edit profile" in label && isWorkerScreen(screen) -> Screen.WorkerProfileViewEdit
            "payments" in label || "stats" in label || "earnings" in label -> Screen.WorkerStatsEarnings

            ("next" in label || "arrow_forward" in label) && screen == Screen.WorkerSetupStep1 -> Screen.WorkerSetupStep2
            ("next" in label || "next step" in label) && screen == Screen.WorkerSetupStep2 -> Screen.WorkerSetupStep3
            ("next" in label || "arrow_forward" in label) && screen == Screen.WorkerSetupStep3 -> Screen.WorkerSetupStep4
            ("finish" in label || "submit" in label || "check_circle" in label || "activate" in label) && screen == Screen.WorkerSetupStep4 -> Screen.WorkerDashboard
            "profile setup" in label || "setup profile" in label -> Screen.WorkerSetupStep1

            else -> null
        }
    }

    private fun isWorkerScreen(screen: Screen): Boolean {
        return screen in setOf(
            Screen.WorkerDashboard,
            Screen.WorkerBookingsFullList,
            Screen.WorkerProfileViewEdit,
            Screen.WorkerSetupStep1,
            Screen.WorkerSetupStep2,
            Screen.WorkerSetupStep3,
            Screen.WorkerSetupStep4,
            Screen.WorkerStatsEarnings
        )
    }

    private enum class Screen(val folder: String) {
        AllReviewsList("all_reviews_list"),
        BookingDetailsSummary("booking_details_summary"),
        BookWorker("book_worker"),
        ChatWithWorker("chat_with_worker"),
        CustomerDashboard("customer_dashboard"),
        CustomerProfile("customer_profile"),
        CustomerProfileSetup("customer_profile_setup"),
        MyBookings("my_bookings"),
        Notifications("notifications"),
        OtpVerification("otp_verification"),
        PhoneAuthentication("phone_authentication"),
        SavedWorkers("saved_workers"),
        SetupComplete("setup_complete"),
        Splash("splash_screen"),
        WorkersByCategory("workers_by_category"),
        WorkerBookingsFullList("worker_bookings_full_list"),
        WorkerDashboard("worker_dashboard"),
        WorkerDetails("worker_details"),
        WorkerProfileViewEdit("worker_profile_view_edit"),
        WorkerSetupStep1("worker_setup_step_1"),
        WorkerSetupStep2("worker_setup_step_2"),
        WorkerSetupStep3("worker_setup_step_3"),
        WorkerSetupStep4("worker_setup_step_4"),
        WorkerStatsEarnings("worker_stats_earnings"),
        WriteReview("write_review");

        companion object {
            fun fromAssetUrl(url: String): Screen {
                return entries.firstOrNull { "/${it.folder}/" in url } ?: Splash
            }
        }
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1600L
    }
}
