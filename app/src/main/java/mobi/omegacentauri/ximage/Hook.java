package mobi.omegacentauri.ximage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XResources;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    final Hook.Data persistentData = new Hook.Data();
    static final String blockDomains[] = {
            "adsense.com",
            "adblade.com",
            "207.net",
            "247realmedia.com",
            "2mdn.net",
            "2o7.net",
            "33across.com",
            "abmr.net",
            "adbrite.com",
            "adbureau.net",
            "adchemy.com",
            "addthis.com",
            "addthisedge.com",
            "admeld.com",
            "admob.com",
            "adsonar.com",
            "advertising.com",
            "afy11.net",
            "aquantive.com",
            "atdmt.com",
            "atwola.com",
            "channelintelligence.com",
            "cmcore.com",
            "coremetrics.com",
            "crowdscience.com",
            "decdna.net",
            "decideinteractive.com",
            "doubleclick.com",
            "doubleclick.net",
            "esomniture.com",
            "fimserve.com",
            "flingwebads.com",
            "foxnetworks.com",
            "googleadservices.com",
            "googlesyndication.com",
            "google-analytics.com",
            "gravity.com",
            "hitbox.com",
            "imiclk.com",
            "imrworldwide.com",
            "insightexpress.com",
            "insightexpressai.com",
            "intellitxt.com",
            "invitemedia.com",
            "leadback.com",
            "lindwd.net",
            "mookie1.com",
            "myads.com",
            "netconversions.com",
            "nexac.com",
            "nextaction.net",
            "nielsen-online.com",
            "offermatica.com",
            "omniture.com",
            "omtrdc.net",
            "pm14.com",
            "quantcast.com",
            "quantserve.com",
            "realmedia.com",
            "revsci.net",
            "rightmedia.com",
            "rmxads.com",
            "ru4.com",
            "rubiconproject.com",
            "samsungadhub.com",
            "scorecardresearch.com",
            "sharethis.com",
            "shopthetv.com",
            "acoda.net",
            "targetingmarketplace.com",
            "themig.com",
            "trendnetcloud.com",
            "yieldmanager.com",
            "yieldmanager.net",
            "yldmgrimg.net",
            "youknowbest.com",
            "yumenetworks.com",
    };


    @SuppressLint("NewApi")
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("ximage handleLoadPackage "+lpparam.packageName);
        XSharedPreferences prefs = new XSharedPreferences(Options.class.getPackage().getName(), Options.PREFS);
        persistentData.detectBlock = prefs.getBoolean(Options.PREF_DETECT_BLOCK, false);
        XposedBridge.log("ximage: det block "+persistentData.detectBlock);
        hookWebKitKiller(lpparam);
    }



    private void hookWebKitKiller(LoadPackageParam lpparam) {
        final String packageName = lpparam.packageName;

        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadUrl",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleURL(param, "loadUrl");
                    }
                });
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "postUrl",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleURL(param, "loadUrl");
                    }
                });
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadDataWithBaseURL",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleURL(param, "loadUrl");
                    }
                });
        XposedBridge.log("loadData");
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadData",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleURL(param, "loadUrl");
                    }
                });
    }

    private void handleURL(XC_MethodHook.MethodHookParam param, String method) {
        if (method.toLowerCase().endsWith("url")) {
            String url = (String)param.args[0];
            if (url == null)
                return;
            XposedBridge.log("ximage: "+method+" "+url.substring(0,Math.min(160,url.length())));
            if (persistentData.detectBlock) {
                WebView wv = (WebView) param.thisObject;
                WebSettings ws = wv.getSettings();
                if (ws.getUserAgentString().equals("DISABLED!")) {
                    XposedBridge.log("ximage: already disabled");
                    param.setResult(null);
                    return;
                }
                Uri uri= Uri.parse(url);
                String host = uri.getHost();
                if (host == null)
                    return;
                host = host.toLowerCase();
                boolean block = false;
                for (int i=0;i<blockDomains.length;i++) {
                    if (host.equals(blockDomains[i]) || host.endsWith("."+blockDomains[i])) {
                        block = true;
                        break;
                    }
                }
                if (block) {
                    XposedBridge.log("ximage: blocking!");
                    ws.setUserAgentString("DISABLED!");
                    param.setResult(null);
                    return;
                }
                XposedBridge.log("not blocking");
                return;
            }
        }
        param.setResult(null);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }


    class Data {
        public boolean detectBlock;
    }
}