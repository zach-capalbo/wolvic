package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;

import com.igalia.wolvic.browser.api.ISession;

import org.mozilla.geckoview.GeckoSession;

class ScrollDelegateImpl implements GeckoSession.ScrollDelegate {
    private ISession.ScrollDelegate mDelegate;
    private SessionImpl mSession;

    public ScrollDelegateImpl(ISession.ScrollDelegate delegate, SessionImpl session) {
        mDelegate = delegate;
        mSession = session;
    }

    @Override
    public void onScrollChanged(@NonNull GeckoSession session, int scrollX, int scrollY) {
        mDelegate.onScrollChanged(mSession, scrollX, scrollY);
    }
}
