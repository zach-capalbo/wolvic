package com.igalia.wolvic.browser.api.impl;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.ISession;

import org.mozilla.geckoview.GeckoSession;

import java.util.Collection;

/* package */ class SelectionActionDelegateImpl implements GeckoSession.SelectionActionDelegate {
    private ISession.SelectionActionDelegate mDelegate;
    private ISession mSession;

    public SelectionActionDelegateImpl(ISession.SelectionActionDelegate delegate, ISession session) {
        this.mDelegate = delegate;
        this.mSession = session;
    }

    /* package */ class SelectionImpl implements ISession.SelectionActionDelegate.Selection {
        GeckoSession.SelectionActionDelegate.Selection mSelection;

        public SelectionImpl(Selection selection) {
            this.mSelection = selection;
        }

        @Override
        public int flags() {
            return mSelection.flags;
        }

        @NonNull
        @Override
        public String text() {
            return mSelection.text;
        }

        @Nullable
        @Override
        public RectF clientRect() {
            return mSelection.clientRect;
        }

        @NonNull
        @Override
        public Collection<String> availableActions() {
            return mSelection.availableActions;
        }

        @Override
        public boolean isActionAvailable(@NonNull String action) {
            return mSelection.isActionAvailable(action);
        }

        @Override
        public void execute(@NonNull String action) {
            mSelection.execute(action);
        }
    }

    @Override
    public void onShowActionRequest(@NonNull GeckoSession session, @NonNull Selection selection) {
        mDelegate.onShowActionRequest(mSession, new SelectionImpl(selection));
    }

    @Override
    public void onHideAction(@NonNull GeckoSession session, int reason) {
        mDelegate.onHideAction(mSession, reason);
    }
}
