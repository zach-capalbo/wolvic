package com.igalia.wolvic.browser.api.impl;

import android.graphics.Bitmap;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.igalia.wolvic.browser.api.IDisplay;
import com.igalia.wolvic.browser.api.IResult;
import org.mozilla.geckoview.GeckoDisplay;

/* package */ class DisplayImpl implements IDisplay {
    GeckoDisplay mDisplay;

    public DisplayImpl(GeckoDisplay display) {
        this.mDisplay = display;
    }

    /* package */ GeckoDisplay getGeckoDisplay() {
        return  mDisplay;
    }


    @Override
    public void surfaceChanged(@NonNull Surface surface, int width, int height) {
        mDisplay.surfaceChanged(surface, width, height);
    }

    @Override
    public void surfaceChanged(@NonNull Surface surface, int left, int top, int width, int height) {
        mDisplay.surfaceChanged(surface, left, top, width, height);
    }

    @Override
    public void surfaceDestroyed() {
        mDisplay.surfaceDestroyed();
    }

    @NonNull
    @Override
    public IResult<Bitmap> capturePixels() {
        return new ResultImpl<>(mDisplay.capturePixels());
    }

    @NonNull
    @Override
    public IResult<Bitmap> capturePixelsWithAspectPreservingSize(int width) {
        return new ResultImpl<>(mDisplay.screenshot().aspectPreservingSize(500).capture());
    }
}
