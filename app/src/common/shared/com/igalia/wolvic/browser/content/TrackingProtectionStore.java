package com.igalia.wolvic.browser.content;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.igalia.wolvic.R;
import com.igalia.wolvic.VRBrowserActivity;
import com.igalia.wolvic.browser.SettingsStore;
import com.igalia.wolvic.browser.api.ContentBlocking;
import com.igalia.wolvic.browser.api.IRuntime;
import com.igalia.wolvic.browser.engine.Session;
import com.igalia.wolvic.db.SitePermission;
import com.igalia.wolvic.ui.viewmodel.SitePermissionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class TrackingProtectionStore implements DefaultLifecycleObserver,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public interface TrackingProtectionListener {
        default void onExcludedTrackingProtectionChange(@NonNull String url, boolean excluded, boolean isPrivate) {};
        default void onTrackingProtectionLevelUpdated(int level) {};
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mContext.getString(R.string.settings_key_tracking_protection_level))) {
            setTrackingProtectionLevel(SettingsStore.getInstance(mContext).getTrackingProtectionLevel());
        }
    }

    private Context mContext;
    private IRuntime mRuntime;
    private Lifecycle mLifeCycle;
    private SitePermissionViewModel mViewModel;
    private List<TrackingProtectionListener> mListeners;
    private SharedPreferences mPrefs;
    private List<SitePermission> mSitePermissions;
    private boolean mIsFirstUpdate;

    public TrackingProtectionStore(@NonNull Context context,
                                   @NonNull IRuntime runtime) {
        mContext = context;
        mRuntime = runtime;
        mListeners = new ArrayList<>();
        mSitePermissions = new ArrayList<>();
        mIsFirstUpdate = true;

        mLifeCycle = ((VRBrowserActivity) context).getLifecycle();
        mLifeCycle.addObserver(this);

        mViewModel = new SitePermissionViewModel(((Application)context.getApplicationContext()));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        setTrackingProtectionLevel(SettingsStore.getInstance(mContext).getTrackingProtectionLevel());
    }

    public void addListener(@NonNull TrackingProtectionListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull TrackingProtectionListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        mViewModel.getAll(SitePermission.SITE_PERMISSION_TRACKING).observeForever(mSitePermissionObserver);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        mViewModel.getAll(SitePermission.SITE_PERMISSION_TRACKING).removeObserver(mSitePermissionObserver);
    }

    private Observer<List<SitePermission>> mSitePermissionObserver = new Observer<List<SitePermission>>() {
        @Override
        public void onChanged(List<SitePermission> sitePermissions) {
            if (sitePermissions != null) {
                mSitePermissions = sitePermissions;
                mIsFirstUpdate = false;
            }
        }
    };

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mLifeCycle.removeObserver(this);
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void contains(@NonNull Session session, Function<Boolean, Void> onResult) {
        onResult.apply(false);
    }

    public void fetchAll(Function<List<SitePermission>, Void> onResult) {
        final List<SitePermission> list = new ArrayList<>();
        onResult.apply(list);
    }

    public void add(@NonNull Session session) {
        mListeners.forEach(listener -> listener.onExcludedTrackingProtectionChange(
                session.getCurrentUri(),
                true,
                session.isPrivateMode()));
        saveExceptions();
    }

    public void remove(@NonNull Session session) {
        mListeners.forEach(listener -> listener.onExcludedTrackingProtectionChange(
                session.getCurrentUri(),
                false,
                session.isPrivateMode()));
        saveExceptions();
    }

    public void remove(@NonNull SitePermission permission) {
        mListeners.forEach(listener -> listener.onExcludedTrackingProtectionChange(
                permission.url,
                false,
                false));
        saveExceptions();
    }

    public void removeAll() {
    }

    private void saveExceptions() {
    }

    private void setTrackingProtectionLevel(int level) {
        ContentBlocking.Settings settings = mRuntime.getSettings().getContentBlocking();
        TrackingProtectionPolicy policy = TrackingProtectionPolicy.recommended();
        if (mRuntime != null) {
            switch (level) {
                case ContentBlocking.EtpLevel.NONE:
                    policy = TrackingProtectionPolicy.none();
                    break;
                case ContentBlocking.EtpLevel.DEFAULT:
                    policy = TrackingProtectionPolicy.recommended();
                    break;
                case ContentBlocking.EtpLevel.STRICT:
                    policy = TrackingProtectionPolicy.strict();
                    break;
            }

            settings.setEnhancedTrackingProtectionLevel(level);
            settings.setStrictSocialTrackingProtection(policy.shouldBlockContent());
            settings.setAntiTracking(policy.getAntiTrackingPolicy());
            settings.setCookieBehavior(policy.getCookiePolicy());

            mRuntime.updateTackingProtection(settings);

            mListeners.forEach(listener -> listener.onTrackingProtectionLevelUpdated(level));
        }
    }

    public static TrackingProtectionPolicy getTrackingProtectionPolicy(Context mContext) {
        int level = SettingsStore.getInstance(mContext).getTrackingProtectionLevel();
        switch (level) {
            case ContentBlocking.EtpLevel.NONE:
                return TrackingProtectionPolicy.none();
            case ContentBlocking.EtpLevel.DEFAULT:
                return TrackingProtectionPolicy.recommended();
            case ContentBlocking.EtpLevel.STRICT:
                return TrackingProtectionPolicy.strict();
        }

        return TrackingProtectionPolicy.recommended();
    }

}
