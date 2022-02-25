package com.igalia.wolvic.browser.api;

import android.content.Context;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.igalia.wolvic.browser.api.impl.RuntimeImpl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IRuntime {

    class ClearFlags {
        /**
         * Cookies.
         */
        public static final long COOKIES = 1 << 0;

        /**
         * Network cache.
         */
        public static final long NETWORK_CACHE = 1 << 1;

        /**
         * Image cache.
         */
        public static final long IMAGE_CACHE = 1 << 2;

        /**
         * DOM storages.
         */
        public static final long DOM_STORAGES = 1 << 4;

        /**
         * Auth tokens and caches.
         */
        public static final long AUTH_SESSIONS = 1 << 5;

        /**
         * Site permissions.
         */
        public static final long PERMISSIONS = 1 << 6;

        /**
         * All caches.
         */
        public static final long ALL_CACHES = NETWORK_CACHE | IMAGE_CACHE;

        /**
         * All site settings (permissions, content preferences, security settings, etc.).
         */
        public static final long SITE_SETTINGS = 1 << 7 | PERMISSIONS;

        /**
         * All site-related data (cookies, storages, caches, permissions, etc.).
         */
        public static final long SITE_DATA =
                1 << 8 | COOKIES | DOM_STORAGES | ALL_CACHES | PERMISSIONS | SITE_SETTINGS;

        /**
         * All data.
         */
        public static final long ALL = 1 << 9;
    }

    @Retention(RetentionPolicy.SOURCE)
    @LongDef(
            flag = true,
            value = {
                    ClearFlags.COOKIES,
                    ClearFlags.NETWORK_CACHE,
                    ClearFlags.IMAGE_CACHE,
                    ClearFlags.DOM_STORAGES,
                    ClearFlags.AUTH_SESSIONS,
                    ClearFlags.PERMISSIONS,
                    ClearFlags.ALL_CACHES,
                    ClearFlags.SITE_SETTINGS,
                    ClearFlags.SITE_DATA,
                    ClearFlags.ALL
            })
            /* package */ @interface StorageControllerClearFlags {}

    static IRuntime create(@NonNull Context context, @NonNull RuntimeSettings settings) {
        return new RuntimeImpl(context, settings);
    }

    RuntimeSettings getSettings();
    void updateTackingProtection(@NonNull ContentBlocking.Settings settings);


    /**
     * Clear data for all hosts.
     *
     * <p>Note: Any open session may re-accumulate previously cleared data. To ensure that no
     * persistent data is left behind, you need to close all sessions prior to clearing data.
     *
     * @param flags Combination of {@link ClearFlags}.
     * @return A {@link IResult} that will complete when clearing has finished.
     */
    @UiThread
    @NonNull IResult<Void> clearData(final @StorageControllerClearFlags long flags);


    /**
     * Returns a WebExtensionController for this GeckoRuntime.
     *
     * @return an instance of {@link WebExtensionController}.
     */
    @UiThread
    @NonNull
    WebExtensionController getWebExtensionController();

}
