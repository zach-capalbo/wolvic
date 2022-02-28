package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.ISession;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;

import java.util.Arrays;

class PermissionDelegateImpl implements GeckoSession.PermissionDelegate {
    SessionImpl mSession;
    ISession.PermissionDelegate mDelegate;

    PermissionDelegateImpl(ISession.PermissionDelegate delegate, SessionImpl session) {
        mSession = session;
        mDelegate = delegate;
    }

    @Override
    public void onAndroidPermissionsRequest(@NonNull GeckoSession session, @Nullable String[] permissions, @NonNull Callback callback) {
        mDelegate.onAndroidPermissionsRequest(mSession, permissions, new ISession.PermissionDelegate.Callback() {
            @Override
            public void grant() {
                callback.grant();
            }

            @Override
            public void reject() {
                callback.reject();
            }
        });
    }

    @Nullable
    @Override
    public GeckoResult<Integer> onContentPermissionRequest(@NonNull GeckoSession session, @NonNull ContentPermission perm) {
        return ResultImpl.from(mDelegate.onContentPermissionRequest(mSession, fromGeckoContentPermission(perm)));
    }

    @Override
    public void onMediaPermissionRequest(@NonNull GeckoSession session, @NonNull String uri, @Nullable MediaSource[] video, @Nullable MediaSource[] audio, @NonNull MediaCallback callback) {
        mDelegate.onMediaPermissionRequest(mSession, uri, fromGeckoMediaSources(video), fromGeckoMediaSources(audio), new ISession.PermissionDelegate.MediaCallback() {
            @Override
            public void grant(@Nullable String video, @Nullable String audio) {
                callback.grant(video, audio);
            }

            @Override
            public void grant(@Nullable ISession.PermissionDelegate.MediaSource video, @Nullable ISession.PermissionDelegate.MediaSource audio) {
                callback.grant(toGeckoMediaSource(video), toGeckoMediaSource(audio));
            }

            @Override
            public void reject() {
                callback.reject();
            }
        });
    }

    static ISession.PermissionDelegate.ContentPermission fromGeckoContentPermission(@NonNull ContentPermission perm) {
        return new ISession.PermissionDelegate.ContentPermission(
                perm.uri, perm.thirdPartyOrigin, perm.privateMode, fromGeckoPermission(perm.permission), fromGeckoPermValue(perm.value), perm.contextId
        );
    }

    private static @ISession.Permission int fromGeckoPermission(int permType) {
        switch (permType) {
            case PERMISSION_GEOLOCATION:
                return ISession.PermissionDelegate.PERMISSION_GEOLOCATION;
            case PERMISSION_DESKTOP_NOTIFICATION:
                return ISession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION;
            case PERMISSION_PERSISTENT_STORAGE:
                return ISession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE;
            case PERMISSION_XR:
                return ISession.PermissionDelegate.PERMISSION_XR;
            case PERMISSION_AUTOPLAY_INAUDIBLE:
                return ISession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE;
            case PERMISSION_AUTOPLAY_AUDIBLE:
                return ISession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE;
            case PERMISSION_MEDIA_KEY_SYSTEM_ACCESS:
                return ISession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS;
            case PERMISSION_TRACKING:
                return ISession.PermissionDelegate.PERMISSION_TRACKING;
            case PERMISSION_STORAGE_ACCESS:
                return ISession.PermissionDelegate.PERMISSION_STORAGE_ACCESS;
        }

        throw new RuntimeException("Unhandled permission type");
    }

    private static @ISession.PermissionDelegate.ContentPermission.Value int fromGeckoPermValue(int valueType) {
        switch (valueType) {
            case ContentPermission.VALUE_ALLOW:
                return ISession.PermissionDelegate.ContentPermission.VALUE_ALLOW;
            case ContentPermission.VALUE_PROMPT:
                return ISession.PermissionDelegate.ContentPermission.VALUE_PROMPT;
            case ContentPermission.VALUE_DENY:
                return ISession.PermissionDelegate.ContentPermission.VALUE_DENY;
        }

        throw new RuntimeException("Unhandled permission value");
    }


    private ISession.PermissionDelegate.MediaSource[] fromGeckoMediaSources(@Nullable MediaSource[] sources) {
        if (sources == null) {
            return null;
        }
        return Arrays.stream(sources).map(this::fromGeckoMediaSource).toArray(ISession.PermissionDelegate.MediaSource[]::new);
    }

    private static class MediaSourceImpl extends ISession.PermissionDelegate.MediaSource {
        private MediaSource geckoMediaSource;

        public MediaSourceImpl(@NonNull String id, @NonNull String rawId, @Nullable String name, int source, int type, MediaSource geckoMediaSource) {
            super(id, rawId, name, source, type);
            this.geckoMediaSource = geckoMediaSource;
        }
    }

    private MediaSourceImpl fromGeckoMediaSource(@NonNull MediaSource geckoSource) {
        int source = 0;
        int type = 0;


        switch (geckoSource.source) {
            case MediaSource.SOURCE_AUDIOCAPTURE:
                source = ISession.PermissionDelegate.MediaSource.SOURCE_AUDIOCAPTURE;
                break;
            case MediaSource.SOURCE_CAMERA:
                source = ISession.PermissionDelegate.MediaSource.SOURCE_CAMERA;
                break;
            case MediaSource.SOURCE_MICROPHONE:
                source = ISession.PermissionDelegate.MediaSource.SOURCE_MICROPHONE;
                break;
            case MediaSource.SOURCE_OTHER:
                source = ISession.PermissionDelegate.MediaSource.SOURCE_OTHER;
                break;
            case MediaSource.SOURCE_SCREEN:
                source = ISession.PermissionDelegate.MediaSource.SOURCE_SCREEN;
                break;
        }

        switch (geckoSource.type) {
            case MediaSource.TYPE_AUDIO:
                type = ISession.PermissionDelegate.MediaSource.TYPE_AUDIO;
                break;
            case MediaSource.TYPE_VIDEO:
                type = ISession.PermissionDelegate.MediaSource.TYPE_VIDEO;
                break;
        }


        return new MediaSourceImpl(geckoSource.id, geckoSource.rawId, geckoSource.name, source, type, geckoSource);
    }

    private static @Nullable MediaSource toGeckoMediaSource(@Nullable ISession.PermissionDelegate.MediaSource source) {
        if (source == null) {
            return null;
        }

        return ((MediaSourceImpl) source).geckoMediaSource;
    }
}
