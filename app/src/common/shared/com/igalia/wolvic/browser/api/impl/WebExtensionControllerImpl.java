package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.IResult;
import com.igalia.wolvic.browser.api.ISession;
import com.igalia.wolvic.browser.api.WebExtensionController;
import com.igalia.wolvic.browser.components.GeckoWebExtension;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;

import java.util.List;
import java.util.stream.Collectors;

import mozilla.components.concept.engine.webextension.WebExtension;

class WebExtensionControllerImpl implements WebExtensionController {
    GeckoRuntime mRuntime;
    org.mozilla.geckoview.WebExtensionController mController;
    PromptDelegate mPromptDelegate;
    DebuggerDelegate mDebuggerDelegate;

    public WebExtensionControllerImpl(GeckoRuntime aRuntime) {
        mRuntime = aRuntime;
        mController = mRuntime.getWebExtensionController();
    }

    @Nullable
    @Override
    public PromptDelegate getPromptDelegate() {
        return mPromptDelegate;
    }

    @Override
    public void setPromptDelegate(@Nullable PromptDelegate delegate) {
        mPromptDelegate = delegate;
        if (delegate == null) {
            mController.setPromptDelegate(null);
            return;
        }

        mController.setPromptDelegate(new org.mozilla.geckoview.WebExtensionController.PromptDelegate() {
            @Nullable
            @Override
            public GeckoResult<AllowOrDeny> onInstallPrompt(@NonNull org.mozilla.geckoview.WebExtension extension) {
                return Utils.map(ResultImpl.from(mPromptDelegate.onInstallPrompt(new GeckoWebExtension(extension, mRuntime))));
            }

            @Nullable
            @Override
            public GeckoResult<AllowOrDeny> onUpdatePrompt(@NonNull org.mozilla.geckoview.WebExtension currentlyInstalled, @NonNull org.mozilla.geckoview.WebExtension updatedExtension, @NonNull String[] newPermissions, @NonNull String[] newOrigins) {
                return Utils.map(ResultImpl.from(mPromptDelegate.onUpdatePrompt(new GeckoWebExtension(currentlyInstalled, mRuntime),
                        new GeckoWebExtension(updatedExtension, mRuntime), newPermissions, newOrigins)));
            }
        });
    }

    @Override
    public void setDebuggerDelegate(@NonNull DebuggerDelegate delegate) {
        mDebuggerDelegate = delegate;
        mController.setDebuggerDelegate(new org.mozilla.geckoview.WebExtensionController.DebuggerDelegate() {
            @Override
            public void onExtensionListUpdated() {
                mDebuggerDelegate.onExtensionListUpdated();
            }
        });
    }

    @NonNull
    @Override
    public IResult<WebExtension> install(@NonNull String uri) {
        return map(mController.install(uri));
    }

    @NonNull
    @Override
    public IResult<WebExtension> setAllowedInPrivateBrowsing(@NonNull WebExtension extension, boolean allowed) {
        return map(mController.setAllowedInPrivateBrowsing(toGeckoExtension(extension), allowed));
    }

    @NonNull
    @Override
    public IResult<WebExtension> installBuiltIn(@NonNull String uri) {
        return map(mController.installBuiltIn(uri));
    }

    @NonNull
    @Override
    public IResult<WebExtension> ensureBuiltIn(@NonNull String uri, @Nullable String id) {
        return map(mController.ensureBuiltIn(uri, id));
    }

    @NonNull
    @Override
    public IResult<Void> uninstall(@NonNull WebExtension extension) {
        return new ResultImpl<>(mController.uninstall(toGeckoExtension(extension)));
    }

    @NonNull
    @Override
    public IResult<WebExtension> enable(@NonNull WebExtension extension, int source) {
        return map(mController.enable(toGeckoExtension(extension), source));
    }

    @NonNull
    @Override
    public IResult<WebExtension> disable(@NonNull WebExtension extension, int source) {
        return map(mController.disable(toGeckoExtension(extension), source));
    }

    @NonNull
    @Override
    public IResult<List<WebExtension>> list() {
        return new ResultImpl<>(mController.list().map(list -> list.stream()
                .map(ext -> new GeckoWebExtension(ext, mRuntime))
                .collect(Collectors.toList())));
    }

    @NonNull
    @Override
    public IResult<WebExtension> update(@NonNull WebExtension extension) {
        return map(mController.update(toGeckoExtension(extension)));
    }

    @Override
    public void setTabActive(@NonNull ISession session, boolean active) {
        mController.setTabActive(((SessionImpl) session).getGeckoSession(), active);
    }

    private org.mozilla.geckoview.WebExtension toGeckoExtension(@NonNull WebExtension ext) {
        return ((GeckoWebExtension) ext).getNativeExtension();
    }

    private IResult<WebExtension> map(GeckoResult<org.mozilla.geckoview.WebExtension> ext) {
        return new ResultImpl<>(ext.map(value -> new GeckoWebExtension(value, mRuntime)));
    }
}
