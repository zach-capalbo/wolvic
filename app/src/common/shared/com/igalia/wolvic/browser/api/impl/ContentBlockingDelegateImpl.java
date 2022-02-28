package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoSession;

class ContentBlockingDelegateImpl implements ContentBlocking.Delegate {
    com.igalia.wolvic.browser.api.ContentBlocking.Delegate mDelegate;
    SessionImpl mSession;

    public ContentBlockingDelegateImpl(com.igalia.wolvic.browser.api.ContentBlocking.Delegate delegate, SessionImpl session) {
        mDelegate = delegate;
        mSession = session;
    }


    @Override
    public void onContentBlocked(@NonNull GeckoSession session, @NonNull ContentBlocking.BlockEvent event) {
        mDelegate.onContentBlocked(mSession, fromGeckoBlockEvent(event));
    }

    @Override
    public void onContentLoaded(@NonNull GeckoSession session, @NonNull ContentBlocking.BlockEvent event) {
        mDelegate.onContentLoaded(mSession, fromGeckoBlockEvent(event));
    }

    static com.igalia.wolvic.browser.api.ContentBlocking.BlockEvent fromGeckoBlockEvent(@NonNull ContentBlocking.BlockEvent event) {
        return new com.igalia.wolvic.browser.api.ContentBlocking.BlockEvent(
          event.uri, fromGeckoAntiTracking(event.getAntiTrackingCategory()), fromGeckoSafeBrowsing(event.getSafeBrowsingCategory()),
                fromGeckoCookieBehavior(event.getCookieBehaviorCategory()), event.isBlocking()
        );
    }

    static int fromGeckoAntiTracking(int flags) {
        switch (flags) {
            case ContentBlocking.AntiTracking.AD:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.AD;
            case ContentBlocking.AntiTracking.ANALYTIC:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.ANALYTIC;
            case ContentBlocking.AntiTracking.CONTENT:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.CONTENT;
            case ContentBlocking.AntiTracking.CRYPTOMINING:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.CRYPTOMINING;
            case ContentBlocking.AntiTracking.DEFAULT:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.DEFAULT;
            case ContentBlocking.AntiTracking.FINGERPRINTING:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.FINGERPRINTING;
            case ContentBlocking.AntiTracking.NONE:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.NONE;
            case ContentBlocking.AntiTracking.SOCIAL:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.SOCIAL;
            case ContentBlocking.AntiTracking.STP:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.STP;
            case ContentBlocking.AntiTracking.STRICT:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.STRICT;
            case ContentBlocking.AntiTracking.TEST:
                return com.igalia.wolvic.browser.api. ContentBlocking.AntiTracking.TEST;
        }

        throw new RuntimeException("Unreachable code");
    }

    static int toGeckoAntitracking(@com.igalia.wolvic.browser.api.ContentBlocking.CBAntiTracking int flags) {
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

    static int fromGeckoSafeBrowsing(int flags) {
        switch (flags) {
            case ContentBlocking.SafeBrowsing.DEFAULT:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.DEFAULT;
            case ContentBlocking.SafeBrowsing.HARMFUL:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.HARMFUL;
            case ContentBlocking.SafeBrowsing.MALWARE:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.MALWARE;
            case ContentBlocking.SafeBrowsing.NONE:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.NONE;
            case ContentBlocking.SafeBrowsing.PHISHING:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.PHISHING;
            case ContentBlocking.SafeBrowsing.UNWANTED:
                return com.igalia.wolvic.browser.api.ContentBlocking.SafeBrowsing.UNWANTED;
        }

        throw new RuntimeException("Unreachable code");
    }

    static int toGeckoSafeBrowsing(@com.igalia.wolvic.browser.api.ContentBlocking.CBSafeBrowsing int flags) {
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

    static int fromGeckoCookieBehavior(int flags) {
        switch (flags) {
            case ContentBlocking.CookieBehavior.ACCEPT_ALL:
                return com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_ALL;
            case ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY:
                return com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY;
            case .ContentBlocking.CookieBehavior.ACCEPT_NONE:
                return com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_NONE;
            case ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS:
                return com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS;
            case ContentBlocking.CookieBehavior.ACCEPT_VISITED:
                return com.igalia.wolvic.browser.api.ContentBlocking.CookieBehavior.ACCEPT_VISITED;
        }

        throw new RuntimeException("Unreachable code");
    }

    static int toGeckoCookieBehavior(@com.igalia.wolvic.browser.api.ContentBlocking.CBCookieBehavior int flags) {
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

    static int toGeckoEtpLevel(@com.igalia.wolvic.browser.api.ContentBlocking.CBEtpLevel int flags) {
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

    static int toGeckoCookieLifetime(@com.igalia.wolvic.browser.api.ContentBlocking.CBCookieLifetime int flags) {
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
}
