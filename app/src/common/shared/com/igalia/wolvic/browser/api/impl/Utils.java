package com.igalia.wolvic.browser.api.impl;
import com.igalia.wolvic.browser.api.Image;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;

public class Utils {
    static Image fromGeckoImage(final org.mozilla.geckoview.Image img) {
        return size -> new ResultImpl<>(img.getBitmap(size));
    }

    static GeckoResult<AllowOrDeny> map(GeckoResult<com.igalia.wolvic.browser.api.AllowOrDeny> res) {
        return res.map(value -> value == com.igalia.wolvic.browser.api.AllowOrDeny.ALLOW ? AllowOrDeny.ALLOW : AllowOrDeny.DENY);
    }


}
