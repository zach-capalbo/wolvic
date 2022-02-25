package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.ContentBlocking;
import com.igalia.wolvic.browser.api.IDisplay;
import com.igalia.wolvic.browser.api.IRuntime;
import com.igalia.wolvic.browser.api.ISession;
import com.igalia.wolvic.browser.api.ISessionSettings;
import com.igalia.wolvic.browser.api.ISessionState;
import com.igalia.wolvic.browser.api.MediaSession;
import org.mozilla.geckoview.GeckoSession;

public class SessionImpl implements ISession {
    private @NonNull GeckoSession mSession;
    private ISessionSettings mSettings;
    private ISession.ContentDelegate mContentDelegate;
    private ISession.SelectionActionDelegate mSelectionActionDelegate;
    private MediaSession.Delegate mMediaSessionDelegate;

    public SessionImpl(@Nullable ISessionSettings settings) {
        if (settings == null) {
            mSession = new GeckoSession();
        } else {
            mSession = new GeckoSession(((SettingsImpl)settings).getGeckoSettings());
        }
        mSettings = new SettingsImpl(mSession.getSettings());
    }

    public @NonNull GeckoSession getGeckoSession() {
        return mSession;
    }

    @Override
    public void loadUri(@NonNull String uri, int flags) {
        mSession.load(new GeckoSession.Loader()
                .uri(uri)
                .flags(toGeckoFlags(flags)));
    }

    @Override
    public void loadData(@NonNull byte[] data, String mymeType) {
        mSession.load(new GeckoSession.Loader()
                .data(data, mymeType));
    }

    @Override
    public void reload(int flags) {
        mSession.reload(toGeckoFlags(flags));
    }

    @Override
    public void stop() {
        mSession.stop();
    }

    @Override
    public void setActive(boolean active) {
        mSession.setActive(active);
    }

    @Override
    public void setFocused(boolean focused) {
        mSession.setFocused(focused);
    }

    @Override
    public void open(@NonNull IRuntime runtime) {
        mSession.open(((RuntimeImpl)runtime).getGeckoRuntime());
    }

    @Override
    public boolean isOpen() {
        return mSession.isOpen();
    }

    @Override
    public void close() {
        mSession.close();
    }

    @Override
    public void goBack(boolean userInteraction) {
        mSession.goBack(userInteraction);
    }

    @Override
    public void goForward(boolean userInteraction) {
        mSession.goBack(userInteraction);
    }

    @Override
    public void gotoHistoryIndex(int index) {
        mSession.gotoHistoryIndex(index);
    }

    @Override
    public void purgeHistory() {
        mSession.purgeHistory();
    }

    @NonNull
    @Override
    public ISessionSettings getSettings() {
        return mSettings;
    }

    @Override
    public void exitFullScreen() {
        mSession.exitFullScreen();
    }

    @NonNull
    @Override
    public IDisplay acquireDisplay() {
        return new DisplayImpl(mSession.acquireDisplay());
    }

    @Override
    public void releaseDisplay(@NonNull IDisplay display) {
        mSession.releaseDisplay(((DisplayImpl)display).getGeckoDisplay());
    }

    @Override
    public void restoreState(@NonNull ISessionState state) {
        mSession.restoreState(((SessionStateImpl)state).getGeckoState());
    }

    @Override
    public void setContentDelegate(@Nullable ContentDelegate delegate) {
        if (mContentDelegate == delegate) {
            return;
        }
        mContentDelegate = delegate;
        if (mContentDelegate == null) {
            mSession.setContentDelegate(null);
        } else {
            mSession.setContentDelegate(new ContentDelegateImpl(mContentDelegate, this));
        }
    }

    @Nullable
    @Override
    public ContentDelegate getContentDelegate() {
        return mContentDelegate;
    }

    @Override
    public void setProgressDelegate(@Nullable ProgressDelegate delegate) {

    }

    @Nullable
    @Override
    public ProgressDelegate getProgressDelegate() {
        return null;
    }

    @Override
    public void setNavigationDelegate(@Nullable NavigationDelegate delegate) {

    }

    @Nullable
    @Override
    public NavigationDelegate getNavigationDelegate() {
        return null;
    }

    @Override
    public void setScrollDelegate(@Nullable ScrollDelegate delegate) {

    }

    @Nullable
    @Override
    public ScrollDelegate getScrollDelegate() {
        return null;
    }

    @Override
    public void setHistoryDelegate(@Nullable HistoryDelegate delegate) {

    }

    @Nullable
    @Override
    public HistoryDelegate getHistoryDelegate() {
        return null;
    }

    @Override
    public void setContentBlockingDelegate(@Nullable ContentBlocking.Delegate delegate) {

    }

    @Nullable
    @Override
    public ContentBlocking.Delegate getContentBlockingDelegate() {
        return null;
    }

    @Override
    public void setPromptDelegate(@Nullable PromptDelegate delegate) {

    }

    @Nullable
    @Override
    public PromptDelegate getPromptDelegate() {
        return null;
    }

    @Override
    public void setSelectionActionDelegate(@Nullable ISession.SelectionActionDelegate delegate) {
        if (mSelectionActionDelegate == delegate) {
            return;
        }
        mSelectionActionDelegate = delegate;
        if (mContentDelegate == null) {
            mSession.setSelectionActionDelegate(null);
        } else {
            mSession.setSelectionActionDelegate(new SelectionActionDelegateImpl(mSelectionActionDelegate, this));
        }
    }

    @Override
    public void setMediaSessionDelegate(@Nullable MediaSession.Delegate delegate) {
        if (mMediaSessionDelegate == delegate) {
            return;
        }
        mMediaSessionDelegate = delegate;
        if (delegate == null) {
            mSession.setMediaSessionDelegate(null);
        } else {
            mSession.setMediaSessionDelegate(new MediaSessionDelegateImpl(this, delegate));
        }
    }

    @Nullable
    @Override
    public MediaSession.Delegate getMediaSessionDelegate() {
        return mMediaSessionDelegate;
    }

    @Nullable
    @Override
    public ISession.SelectionActionDelegate getSelectionActionDelegate() {
        return mSelectionActionDelegate;
    }


    private int toGeckoFlags(@LoadFlags int flags) {
        int result = 0;
        if ((flags & ISession.LOAD_FLAGS_NONE) != 0) {
            result |= GeckoSession.LOAD_FLAGS_NONE;
        }
        if ((flags & ISession.LOAD_FLAGS_BYPASS_CACHE) != 0) {
            result |= GeckoSession.LOAD_FLAGS_BYPASS_CACHE;
        }
        if ((flags & ISession.LOAD_FLAGS_BYPASS_PROXY) != 0) {
            result |= GeckoSession.LOAD_FLAGS_BYPASS_PROXY;
        }
        if ((flags & ISession.LOAD_FLAGS_EXTERNAL) != 0) {
            result |= GeckoSession.LOAD_FLAGS_EXTERNAL;
        }
        if ((flags & ISession.LOAD_FLAGS_ALLOW_POPUPS) != 0) {
            result |= GeckoSession.LOAD_FLAGS_ALLOW_POPUPS;
        }
        if ((flags & ISession.LOAD_FLAGS_BYPASS_CLASSIFIER) != 0) {
            result |= GeckoSession.LOAD_FLAGS_BYPASS_CLASSIFIER;
        }
        return result;
    }
}
