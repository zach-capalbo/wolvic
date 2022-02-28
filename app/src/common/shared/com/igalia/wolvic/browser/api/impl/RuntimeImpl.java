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
        cb.setAntiTracking(ContentBlockingDelegateImpl.toGeckoAntitracking(settings.getAntiTracking()));
        cb.setCookieBehavior(ContentBlockingDelegateImpl.toGeckoCookieBehavior(settings.getCookieBehavior()));
        cb.setCookieBehaviorPrivateMode(ContentBlockingDelegateImpl.toGeckoCookieBehavior(settings.getCookieBehavior()));
        cb.setCookieLifetime(ContentBlockingDelegateImpl.toGeckoCookieLifetime(settings.getCookieLifetime()));
        cb.setSafeBrowsing(ContentBlockingDelegateImpl.toGeckoSafeBrowsing(settings.getSafeBrowsing()));
        cb.setEnhancedTrackingProtectionLevel(ContentBlockingDelegateImpl.toGeckoEtpLevel(settings.getEnhancedTrackingProtectionLevel()));
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

    static int toGeckoColorScheme(@RuntimeSettings.ColorScheme int flags) {
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
