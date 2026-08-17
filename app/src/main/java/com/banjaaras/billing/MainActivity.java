package com.banjaaras.billing;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Bill Gernator — WebView shell around the billing web app (assets/bill.html).
 *
 * Rewarded ads: the HTML calls window.AndroidAds.showRewardedAd() through the
 * JS bridge below. Coin balance and sender name live entirely inside the
 * WebView's own localStorage (handled in bill.html) — this class only needs
 * to load/show the ad and notify the page when a reward is earned.
 *
 * The Rewarded ad unit ID below is your real AdMob unit
 * (ca-app-pub-4344006394945876/1082487651). Real ads will now be requested,
 * so avoid tapping "watch ad" repeatedly yourself during testing — Google
 * can flag accounts for invalid traffic from developer self-clicks. Use a
 * separate test device registered in the AdMob console, or Google's test
 * unit ID, while actively testing.
 */
public class MainActivity extends AppCompatActivity {

    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-4344006394945876/1082487651"; // Real Rewarded ad unit ID

    private WebView webView;
    private RewardedAd rewardedAd;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> loadRewardedAd());

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // required for localStorage (coins, sender name)
        settings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidAdsBridge(), "AndroidAds");
        webView.loadUrl("file:///android_asset/bill.html");

        setContentView(webView);
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                rewardedAd = null;
            }
        });
    }

    /** Bridge exposed to the WebView as window.AndroidAds */
    private class AndroidAdsBridge {

        @JavascriptInterface
        public void showRewardedAd() {
            runOnUiThread(() -> {
                if (rewardedAd == null) {
                    Toast.makeText(MainActivity.this, "Ad not ready yet, please try again in a moment.", Toast.LENGTH_SHORT).show();
                    webView.evaluateJavascript("window.onRewardedAdFailed && window.onRewardedAdFailed();", null);
                    loadRewardedAd(); // try to have one ready for next time
                    return;
                }

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        loadRewardedAd(); // preload the next ad
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        rewardedAd = null;
                        webView.evaluateJavascript("window.onRewardedAdFailed && window.onRewardedAdFailed();", null);
                        loadRewardedAd();
                    }
                });

                rewardedAd.show(MainActivity.this, (RewardItem rewardItem) -> {
                    // User earned the reward — tell the web page to add coins.
                    runOnUiThread(() ->
                        webView.evaluateJavascript("window.onRewardedAdEarned && window.onRewardedAdEarned();", null)
                    );
                });
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
