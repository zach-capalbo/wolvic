package com.igalia.wolvic.browser;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.igalia.wolvic.PlatformActivity;
import com.igalia.wolvic.R;
import com.igalia.wolvic.browser.api.IResult;
import com.igalia.wolvic.browser.api.ISession;
import com.igalia.wolvic.browser.engine.Session;
import com.igalia.wolvic.browser.engine.SessionState;
import com.igalia.wolvic.browser.engine.SessionStore;
import com.igalia.wolvic.db.SitePermission;
import com.igalia.wolvic.ui.viewmodel.SitePermissionViewModel;
import com.igalia.wolvic.ui.widgets.WidgetManagerDelegate;
import com.igalia.wolvic.ui.widgets.WindowWidget;
import com.igalia.wolvic.ui.widgets.dialogs.PermissionWidget;
import com.igalia.wolvic.utils.SystemUtils;
import com.igalia.wolvic.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionDelegate implements ISession.PermissionDelegate, WidgetManagerDelegate.PermissionListener {

    static final int PERMISSION_REQUEST_CODE = 1143;

    static final String LOGTAG = SystemUtils.createLogtag(PermissionDelegate.class);
    private Context mContext;
    private int mParentWidgetHandle;
    private WidgetManagerDelegate mWidgetManager;
    private ISession.PermissionDelegate.Callback mCallback;
    private PermissionWidget mPermissionWidget;
    private SitePermissionViewModel mSitePermissionModel;
    private List<SitePermission> mSitePermissions;

    public interface PlatformLocationOverride {
        void onLocationGranted(Session session);
    }
    public static PlatformLocationOverride sPlatformLocationOverride;

    public PermissionDelegate(Context aContext, WidgetManagerDelegate aWidgetManager) {
        mContext = aContext;
        mWidgetManager = aWidgetManager;
        mWidgetManager.addPermissionListener(this);
        SessionStore.get().setPermissionDelegate(this);
        mSitePermissionModel = new SitePermissionViewModel((Application)aContext.getApplicationContext());
        mSitePermissionModel.getAll().observeForever(mSitePermissionObserver);
    }

    public void setParentWidgetHandle(int aHandle) {
        mParentWidgetHandle = aHandle;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE || mCallback == null) {
            return;
        }

        boolean granted = true;
        for (int result: grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        if (granted) {
            mCallback.grant();
        } else {
            mCallback.reject();
        }
    }

    public void handlePermission(final String aUri, final PermissionWidget.PermissionType aType, final Callback aCallback) {
        if (mPermissionWidget == null) {
            mPermissionWidget = new PermissionWidget(mContext);
            mWidgetManager.addWidget(mPermissionWidget);
        }

        mPermissionWidget.showPrompt(aUri, aType, aCallback);
    }

    private Observer<List<SitePermission>> mSitePermissionObserver = sites -> {
        mSitePermissions = sites;
    };

    IResult<Integer> handleWebXRPermission(ISession aISession, ContentPermission perm) {
        Session session = SessionStore.get().getSession(aISession);
        if (session == null || !SettingsStore.getInstance(mContext).isWebXREnabled()) {
            return IResult.fromValue(ContentPermission.VALUE_DENY);
        }
        final String domain = UrlUtils.getHost(perm.uri);

        @Nullable SitePermission site = null;
        if (mSitePermissions != null) {
            site = mSitePermissions.stream()
                    .filter(sitePermission -> sitePermission.category == SitePermission.SITE_PERMISSION_WEBXR &&
                            sitePermission.url.equalsIgnoreCase(domain))
                    .findFirst().orElse(null);
        }

        if (site == null) {
            session.setWebXRState(SessionState.WEBXR_ALLOWED);
            return IResult.fromValue(ContentPermission.VALUE_ALLOW);
        } else {
            session.setWebXRState(SessionState.WEBXR_BLOCKED);
            return IResult.fromValue(ContentPermission.VALUE_DENY);
        }
    }

    public void release() {
        mSitePermissionModel.getAll().removeObserver(mSitePermissionObserver);
        mWidgetManager.removePermissionListener(this);
        SessionStore.get().setPermissionDelegate(null);
        mCallback = null;
        mContext = null;
        mWidgetManager = null;
    }

    @Override
    public void onAndroidPermissionsRequest(ISession aSession, String[] permissions, Callback aCallback) {
        Log.d(LOGTAG, "onAndroidPermissionsRequest: " + Arrays.toString(permissions));
        ArrayList<String> missingPermissions = new ArrayList<>();
        ArrayList<String> filteredPermissions = new ArrayList<>();
        for (String permission: permissions) {
            if (PlatformActivity.filterPermission(permission)) {
                Log.d(LOGTAG, "Skipping permission: " + permission);
                filteredPermissions.add(permission);
                continue;
            }
            Log.d(LOGTAG, "permission = " + permission);
            if (mContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.size() == 0) {
            if (filteredPermissions.size() == 0) {
                Log.d(LOGTAG, "Android permissions granted");
                aCallback.grant();
            } else {
                Log.d(LOGTAG, "Android permissions rejected");
                aCallback.reject();
            }
        } else {
            Log.d(LOGTAG, "Request Android permissions: " + missingPermissions);
            mCallback = aCallback;
            ((Activity)mContext).requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public IResult<Integer> onContentPermissionRequest(ISession aSession, ContentPermission perm) {
        Log.d(LOGTAG, "onContentPermissionRequest: " + perm.uri + " " + perm.permission);
        if (perm.permission == PERMISSION_XR) {
            return handleWebXRPermission(aSession, perm);
        }

        if (perm.permission == PERMISSION_AUTOPLAY_INAUDIBLE) {
            // https://hacks.mozilla.org/2019/02/firefox-66-to-block-automatically-playing-audible-video-and-audio/
            return IResult.fromValue(ContentPermission.VALUE_ALLOW);
        } else if(perm.permission == PERMISSION_AUTOPLAY_AUDIBLE) {
            if (SettingsStore.getInstance(mContext).isAutoplayEnabled()) {
                return IResult.fromValue(ContentPermission.VALUE_ALLOW);
            } else {
                return IResult.fromValue(ContentPermission.VALUE_DENY);
            }
        }

        PermissionWidget.PermissionType type;
        if (perm.permission == PERMISSION_DESKTOP_NOTIFICATION) {
            type = PermissionWidget.PermissionType.Notification;
        } else if (perm.permission == PERMISSION_GEOLOCATION) {
            type = PermissionWidget.PermissionType.Location;
        } else if (perm.permission == PERMISSION_MEDIA_KEY_SYSTEM_ACCESS) {
            final IResult<Integer> result = IResult.create();
            WindowWidget windowWidget = mWidgetManager.getFocusedWindow();
            Runnable enableDrm = () -> {
                Session session = SessionStore.get().getSession(aSession);
                if (SettingsStore.getInstance(mContext).isDrmContentPlaybackEnabled()) {
                    if (session != null) {
                        session.setDrmState(SessionState.DRM_ALLOWED);
                    }
                    result.complete(ContentPermission.VALUE_ALLOW);
                } else {
                    if (session != null) {
                        session.setDrmState(SessionState.DRM_BLOCKED);
                    }
                    result.complete(ContentPermission.VALUE_DENY);
                }
            };
            if (SettingsStore.getInstance(mContext).isDrmContentPlaybackSet()) {
                enableDrm.run();

            } else {
                windowWidget.showFirstTimeDrmDialog(enableDrm);
            }
            windowWidget.setDrmUsed(true);
            return result;
        } else {
            Log.e(LOGTAG, "onContentPermissionRequest unknown permission: " + perm.permission);
            return IResult.fromValue(ContentPermission.VALUE_DENY);
        }

        final IResult<Integer> result = IResult.create();
        handlePermission(perm.uri, type, new Callback() {
            @Override
            public void grant() {
                result.complete(ContentPermission.VALUE_ALLOW);
                if (type == PermissionWidget.PermissionType.Location && sPlatformLocationOverride != null) {
                    sPlatformLocationOverride.onLocationGranted(SessionStore.get().getSession(aSession));
                }
            }

            @Override
            public void reject() {
                result.complete(ContentPermission.VALUE_DENY);
            }
        });
        return result;
    }

    @Override
    public void onMediaPermissionRequest(ISession aSession, String aUri, MediaSource[] aVideo, MediaSource[] aAudio, final MediaCallback aMediaCallback) {
        Log.d(LOGTAG, "onMediaPermissionRequest: " + aUri);

        final MediaSource video = aVideo != null ? aVideo[0] : null;
        final MediaSource audio = aAudio != null ? aAudio[0] : null;
        PermissionWidget.PermissionType type;
        if (video != null && audio != null) {
            type = PermissionWidget.PermissionType.CameraAndMicrophone;
        } else if (video != null) {
            type = PermissionWidget.PermissionType.Camera;
        } else if (audio != null) {
            type = PermissionWidget.PermissionType.Microphone;
        } else {
            aMediaCallback.reject();
            return;
        }

        ISession.PermissionDelegate.Callback callback = new ISession.PermissionDelegate.Callback() {
            @Override
            public void grant() {
                aMediaCallback.grant(video, audio);
            }

            @Override
            public void reject() {
                aMediaCallback.reject();
            }
        };

        // Temporary fix for https://bugzilla.mozilla.org/show_bug.cgi?id=1621380
        if ((type == PermissionWidget.PermissionType.Camera ||
                type == PermissionWidget.PermissionType.CameraAndMicrophone)) {
            callback.reject();
            return;
        }

        handlePermission(aUri, type, callback);
    }

    public boolean isPermissionGranted(@NonNull String permission) {
        return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    // Handle app permissions that Gecko doesn't handle itself yet
    public void onAppPermissionRequest(final ISession aSession, String aUri, final String permission, final Callback callback) {
        Log.d(LOGTAG, "onAppPermissionRequest: " + aUri);

        // If the permission is already granted we just grant
        if (mContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {

            // Check if we support a rationale for that permission
            PermissionWidget.PermissionType type = null;
            if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                type = PermissionWidget.PermissionType.ReadExternalStorage;
            }

            if (type != null) {
                // Show rationale
                handlePermission(mContext.getString(R.string.app_name), type, new Callback() {
                    @Override
                    public void grant() {
                        onAndroidPermissionsRequest(aSession, new String[]{permission}, callback);
                    }

                    @Override
                    public void reject() {
                        if (callback != null) {
                            callback.reject();
                        }
                    }
                });

            } else {
                // Let Android handle the permission request
                onAndroidPermissionsRequest(aSession, new String[]{permission}, callback);
            }

        } else {
            if (callback != null) {
                callback.grant();
            }
        }
    }
    public void addPermissionException(@NonNull String uri, @SitePermission.Category int category) {
        @Nullable SitePermission site = mSitePermissions.stream()
                .filter((item) -> item.category == category && item.url.equals(uri))
                .findFirst().orElse(null);

        if (site == null) {
            site = new SitePermission(uri, "", category);
            mSitePermissions.add(site);
        }
        mSitePermissionModel.insertSite(site);

        // Reload URIs with the same domain
        for (WindowWidget window: mWidgetManager.getWindows().getCurrentWindows()) {
            Session session = window.getSession();
            if (uri.equalsIgnoreCase(UrlUtils.getHost(session.getCurrentUri()))) {
                session.reload(ISession.LOAD_FLAGS_BYPASS_CACHE);
            }
        }

    }

    public void removePermissionException(String uri, @SitePermission.Category int category) {
        @Nullable SitePermission site = mSitePermissions.stream()
                .filter((item) -> item.category == category && item.url.equals(uri))
                .findFirst().orElse(null);

        mSitePermissions.removeIf(sitePermission -> sitePermission.url.equals(uri));
        if (site != null) {
            mSitePermissionModel.deleteSite(site);
        }

        // Reload URIs with the same domain
        for (WindowWidget window: mWidgetManager.getWindows().getCurrentWindows()) {
            Session session = window.getSession();
            if (uri.equalsIgnoreCase(UrlUtils.getHost(session.getCurrentUri()))) {
                session.reload(ISession.LOAD_FLAGS_BYPASS_CACHE);
            }
        }

    }
}
