package com.igalia.wolvic.browser.api;
import androidx.annotation.AnyThread;

/**
 * Used by a ContentDelegate to indicate what action to take on a slow script event.
 *
 * @see ISession.ContentDelegate#onSlowScript(ISession,String)
 */
@AnyThread
public enum SlowScriptResponse {
    STOP,
    CONTINUE;
}
