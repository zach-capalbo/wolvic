package com.igalia.wolvic.browser.api.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.igalia.wolvic.browser.api.IResult;
import com.igalia.wolvic.browser.api.IRuntime;
import com.igalia.wolvic.browser.api.RuntimeSettings;
import com.igalia.wolvic.browser.api.WebExtensionController;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.StorageController;

public class RuntimeImpl implements IRuntime {
    private GeckoRuntime mRuntime;
    private RuntimeSettings mSettings;
    private WebExtensionControllerImpl mWebExtensionController;

    public RuntimeImpl(Context ctx, RuntimeSettings settings) {
        GeckoRuntimeSettings.Builder builder = new GeckoRuntimeSettings.Builder();
        builder.crashHandler(settings.getCrashHandler())
                .aboutConfigEnabled(settings.isAboutConfigEnabled())
                .allowInsecureConnections((int)settings.getAllowInsecureConenctions())
                .contentBlocking(new ContentBlocking.Settings.Builder()
                        .antiTracking(toGeckoAntitracking(settings.getContentBlocking().getAntiTracking()))
                        .enhancedTrackingProtectionLevel(toGeckoEtpLevel(settings.getContentBlocking().getEnhancedTrackingProtectionLevel()))
                        .cookieBehavior(toGeckoCookieBehavior(settings.getContentBlocking().getCookieBehavior()))
                        .cookieBehaviorPrivateMode(toGeckoCookieBehavior(settings.getContentBlocking().getCookieBehaviorPrivate()))
                        .cookieLifetime(toGeckoCookieLifetime(settings.getContentBlocking().getCookieLifetime()))
                        .safeBrowsing(toGeckoSafeBrowsing(settings.getContentBlocking().getSafeBrowsing()))
                        .build())
                .displayDensityOverride(settings.getDisplayDensityOverride())
                .remoteDebuggingEnabled(settings.isRemoteDebugging())
                .displayDpiOverride(settings.getDisplayDpiOverride())
                .screenSizeOverride(settings.getScreenWidthOverride(), settings.getScreenHeightOverride())
                .inputAutoZoomEnabled(settings.isInputAutoZoomEnabled())
                .doubleTapZoomingEnabled(settings.isDoubleTapZoomingEnabled())
                .debugLogging(settings.isDoubleTapZoomingEnabled())
                .consoleOutput(settings.isConsoleOutputEnabled())
                .loginAutofillEnabled(settings.isAutofillLoginsEnabled())
                .configFilePath(settings.getConfigFilePath())
                .javaScriptEnabled(settings.isJavaScriptEnabled())
                .glMsaaLevel(settings.getGlMsaaLevel())
                .webManifest(settings.isWebManifestEnabled())
                .pauseForDebugger(settings.isPauseForDebuggerEnabled())
                .preferredColorScheme(toGeckoColorScheme(settings.getPreferredColorScheme()));

        mRuntime = GeckoRuntime.create(ctx, builder.build());
        mSettings = settings;
        mWebExtensionController = new WebExtensionControllerImpl(mRuntime);
    }

    GeckoRuntime getGeckoRuntime() {
        return  mRuntime;
    }


    @Override
    public RuntimeSettings getSettings() {
        return mSettings;
    }

    @Override
    public void updateTackingProtection(@NonNull com.igalia.wolvic.browser.api.ContentBlocking.Settings settings) {
        ContentBlocking.Settings cb =  mRuntime.getSettings().getContentBlocking();
        cb.setAntiTracking(toGeckoAntitracking(settings.getAntiTracking()));
        cb.setCookieBehavior(toGeckoCookieBehavior(settings.getCookieBehavior()));
        cb.setCookieBehaviorPrivateMode(toGeckoCookieBehavior(settings.getCookieBehavior()));
        cb.setCookieLifetime(toGeckoCookieLifetime(settings.getCookieLifetime()));
        cb.setSafeBrowsing(toGeckoSafeBrowsing(settings.getSafeBrowsing()));
        cb.setEnhancedTrackingProtectionLevel(toGeckoEtpLevel(settings.getEnhancedTrackingProtectionLevel()));
    }

    @NonNull
    @Override
    public IResult<Void> clearData(long flags) {
        return new ResultImpl<>(mRuntime.getStorageController().clearData(toGeckoStorageFlags(flags)));
    }

    @NonNull
    @Override
    public WebExtensionController getWebExtensionController() {
        return mWebExtensionController;
    }

    private int toGeckoAntitracking(@com.igalia.wolvic.browser.api.ContentBlocking.CBAntiTracking int flags) {
        switch (flags) {
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.AD:
                return ContentBlocking.AntiTracking.AD;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.ANALYTIC:
                return ContentBlocking.AntiTracking.ANALYTIC;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.CONTENT:
                return ContentBlocking.AntiTracking.CONTENT;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.CRYPTOMINING:
                return ContentBlocking.AntiTracking.CRYPTOMINING;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.DEFAULT:
                return ContentBlocking.AntiTracking.DEFAULT;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.FINGERPRINTING:
                return ContentBlocking.AntiTracking.FINGERPRINTING;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.NONE:
                return ContentBlocking.AntiTracking.NONE;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.SOCIAL:
                return ContentBlocking.AntiTracking.SOCIAL;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.STP:
                return ContentBlocking.AntiTracking.STP;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.STRICT:
                return ContentBlocking.AntiTracking.STRICT;
            case com.igalia.wolvic.browser.api.ContentBlocking.AntiTracking.TEST:
                return ContentBlocking.AntiTracking.TEST;
        }

        throw new RuntimeException("Unreachable code");
    }

    private int toGeckoSafeBrowsing(@com.igalia.wolvic.browser.api.ContentBlocking.CBSafeBrowsing int flags) {
        switch (flags) {
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.DEFAULT:
                return ContentBlocking.SafeBrowsing.DEFAULT;
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.HARMFUL:
                return ContentBlocking.SafeBrowsing.HARMFUL;
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.MALWARE:
                return ContentBlocking.SafeBrowsing.MALWARE;
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.NONE:
                return ContentBlocking.SafeBrowsing.NONE;
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.PHISHING:
                return ContentBlocking.SafeBrowsing.PHISHING;
            case com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.UNWANTED:
                return ContentBlocking.SafeBrowsing.UNWANTED;
        }

        throw new RuntimeException("Unreachable code");
    }

    private int toGeckoCookieBehavior(@com.igalia.wolvic.browser.api.ContentBlocking.CBCookieBehavior int flags) {
        switch (flags) {
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_ALL:
                return ContentBlocking.CookieBehavior.ACCEPT_ALL;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY:
                return ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_NONE:
                return ContentBlocking.CookieBehavior.ACCEPT_NONE;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS:
                return ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_VISITED:
                return ContentBlocking.CookieBehavior.ACCEPT_VISITED;
        }

        throw new RuntimeException("Unreachable code");
    }

    private int toGeckoEtpLevel(@com.igalia.wolvic.browser.api.ContentBlocking.CBEtpLevel int flags) {
        switch (flags) {
            case com.igalia.wolvic.browser.api.ContentBlocking.EtpLevel.DEFAULT:
                return ContentBlocking.EtpLevel.DEFAULT;
            case com.igalia.wolvic.browser.api.ContentBlocking.EtpLevel.NONE:
                return ContentBlocking.EtpLevel.NONE;
            case com.igalia.wolvic.browser.api.ContentBlocking.EtpLevel.STRICT:
                return ContentBlocking.EtpLevel.STRICT;
        }

        throw new RuntimeException("Unreachable code");
    }

    private int toGeckoCookieLifetime(@com.igalia.wolvic.browser.api.ContentBlocking.CBCookieLifetime int flags) {
        switch (flags) {
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieLifetime.DAYS:
                return ContentBlocking.CookieLifetime.DAYS;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieLifetime.NORMAL:
                return ContentBlocking.CookieLifetime.NORMAL;
            case com.igalia.wolvic.browser.api.ContentBlocking.CookieLifetime.RUNTIME:
                return ContentBlocking.CookieLifetime.RUNTIME;
        }

        throw new RuntimeException("Unreachable code");
    }

    private int toGeckoColorScheme(@RuntimeSettings.ColorScheme int flags) {
        switch (flags) {
            case RuntimeSettings.COLOR_SCHEME_DARK:
                return GeckoRuntimeSettings.COLOR_SCHEME_DARK;
            case RuntimeSettings.COLOR_SCHEME_LIGHT:
                return GeckoRuntimeSettings.COLOR_SCHEME_LIGHT;
            case RuntimeSettings.COLOR_SCHEME_SYSTEM:
                return GeckoRuntimeSettings.COLOR_SCHEME_SYSTEM;
        }

        throw new RuntimeException("Unreachable code");
    }

    private long toGeckoStorageFlags(@IRuntime.StorageControllerClearFlags long flags) {
        long res = 0;
        if ((flags & ClearFlags.COOKIES) != 0) {
            res |= StorageController.ClearFlags.COOKIES;
        }
        if ((flags & ClearFlags.NETWORK_CACHE) != 0) {
            res |= StorageController.ClearFlags.NETWORK_CACHE;
        }
        if ((flags & ClearFlags.IMAGE_CACHE) != 0) {
            res |= StorageController.ClearFlags.IMAGE_CACHE;
        }
        if ((flags & ClearFlags.DOM_STORAGES) != 0) {
            res |= StorageController.ClearFlags.DOM_STORAGES;
        }
        if ((flags & ClearFlags.AUTH_SESSIONS) != 0) {
            res |= StorageController.ClearFlags.AUTH_SESSIONS;
        }
        if ((flags & ClearFlags.PERMISSIONS) != 0) {
            res |= StorageController.ClearFlags.PERMISSIONS;
        }
        if ((flags & ClearFlags.SITE_DATA) != 0) {
            res |= StorageController.ClearFlags.SITE_DATA;
        }
        if ((flags & ClearFlags.ALL_CACHES) != 0) {
            res |= StorageController.ClearFlags.ALL_CACHES;
        }
        if ((flags & ClearFlags.ALL) != 0) {
            res |= StorageController.ClearFlags.ALL;
        }

        return res;
    }
}
