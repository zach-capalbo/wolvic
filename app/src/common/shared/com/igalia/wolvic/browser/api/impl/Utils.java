package com.igalia.wolvic.browser.api.impl;
import com.igalia.wolvic.browser.api.Image;

public class Utils {
    static Image fromGeckoImage(final org.mozilla.geckoview.Image img) {
        return size -> new ResultImpl<>(img.getBitmap(size));
    }
}
