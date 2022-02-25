package com.igalia.wolvic.browser.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.IResult;

import org.mozilla.geckoview.GeckoResult;

import java.util.Objects;

public class ResultImpl<T> implements IResult<T> {
    GeckoResult<T> mGeckoResult;

    public ResultImpl() {
        this.mGeckoResult = new GeckoResult<>();
    }

    public ResultImpl(GeckoResult<T> mGeckoResult) {
        this.mGeckoResult = mGeckoResult;
    }

    public static <T> GeckoResult<T> from(IResult<T> res) {
        return ((ResultImpl<T>)(res)).mGeckoResult;
    }
    public interface Mapper<T, U> {
        @Nullable
        T map(@Nullable U val);
    }


    @Override
    public void complete(@Nullable T value) {
        mGeckoResult.complete(value);
    }

    @Override
    public void completeExceptionally(@NonNull Throwable exception) {
        mGeckoResult.completeExceptionally(exception);
    }

    @NonNull
    @Override
    public <U> IResult<U> then(@NonNull OnValueListener<T, U> valueListener) {
        return new ResultImpl<>(mGeckoResult.then(aValue ->
                ((ResultImpl<U>) Objects.requireNonNull(valueListener.onValue(aValue))).mGeckoResult));
    }

    @NonNull
    @Override
    public <U> IResult<U> exceptionally(@NonNull OnExceptionListener<U> exceptionListener) {
        return new ResultImpl<>(mGeckoResult.exceptionally(throwable ->
                ((ResultImpl<U>) Objects.requireNonNull(exceptionListener.onException(throwable))).mGeckoResult));
    }

    @NonNull
    @Override
    public <U> IResult<U> then(@Nullable OnValueListener<T, U> valueListener, @Nullable OnExceptionListener<U> exceptionListener) {
        GeckoResult.OnValueListener<T,U> geckoValue = null;
        GeckoResult.OnExceptionListener<U> geckoException = null;
        if (valueListener != null) {
            geckoValue = aValue -> ((ResultImpl<U>) Objects.requireNonNull(valueListener.onValue(aValue))).mGeckoResult;
        }
        if (exceptionListener != null) {
            geckoException = throwable -> ((ResultImpl<U>) Objects.requireNonNull(exceptionListener.onException(throwable))).mGeckoResult;
        }

        return new ResultImpl<>(mGeckoResult.then(geckoValue, geckoException));
    }

    @NonNull
    @Override
    public IResult<Boolean> cancel() {
        return new ResultImpl<>(mGeckoResult.cancel());
    }

    @Override
    public void setCancellationDelegate(@Nullable CancellationDelegate delegate) {
        if (delegate == null) {
            mGeckoResult.setCancellationDelegate(null);
            return;
        }

        mGeckoResult.setCancellationDelegate(new GeckoResult.CancellationDelegate() {
            @NonNull
            @Override
            public GeckoResult<Boolean> cancel() {
                return ResultImpl.from(delegate.cancel());
            }
        });
    }
}
