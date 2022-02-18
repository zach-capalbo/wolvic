package com.igalia.wolvic.browser.api.impl;

import org.mozilla.geckoview.GeckoRuntime;

public class IRuntimeImpl {
    private GeckoRuntime mRuntime;


    GeckoRuntime getGeckoRuntime() {
        return  mRuntime;
    }
}
