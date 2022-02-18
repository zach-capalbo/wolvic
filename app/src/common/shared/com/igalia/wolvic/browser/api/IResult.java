package com.igalia.wolvic.browser.api;

import android.os.Handler;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.impl.ResultImpl;

public interface IResult<T> {
    /**
     * Construct a empty result with pending state.
     */
    static @NonNull <U> IResult<U> create() {
        return new ResultImpl<>();
    }

    /**
     * Construct a result that is completed with the specified value.
     *
     * @param value The value used to complete the newly created result.
     * @param <U> Type for the result.
     * @return The completed {@link IResult}
     */
    static @NonNull <U> IResult<U> fromValue(@Nullable final U value) {
        final IResult<U> result = new ResultImpl<>();
        result.complete(value);
        return result;
    }

    /**
     * Construct a result that is completed with the specified {@link Throwable}. May not be null.
     *
     * @param error The exception used to complete the newly created result.
     * @param <T> Type for the result if the result had been completed without exception.
     * @return The completed {@link IResult}
     */
    static @NonNull <T> IResult<T> fromException(@NonNull final Throwable error) {
        final IResult<T> result = new ResultImpl<>();
        result.completeExceptionally(error);
        return result;
    }

    /** @return a {@link IResult} that resolves to {@link AllowOrDeny#DENY} */
    @AnyThread
    @NonNull
    public static IResult<AllowOrDeny> deny() {
        return IResult.fromValue(AllowOrDeny.DENY);
    }

    /** @return a {@link IResult} that resolves to {@link AllowOrDeny#ALLOW} */
    @AnyThread
    @NonNull
    public static IResult<AllowOrDeny> allow() {
        return IResult.fromValue(AllowOrDeny.ALLOW);
    }


    /**
     * Complete the result with the specified value. IllegalStateException is thrown if the result is
     * already complete.
     *
     * @param value The value used to complete the result.
     * @throws IllegalStateException If the result is already completed.
     */
    void complete(final @Nullable T value);


    /**
     * Complete the result with the specified {@link Throwable}. IllegalStateException is thrown if
     * the result is already complete.
     *
     * @param exception The {@link Throwable} used to complete the result.
     * @throws IllegalStateException If the result is already completed.
     */
    void completeExceptionally(@NonNull final Throwable exception);

    /**
     * Convenience method for {@link #then(IResult.OnValueListener, IResult.OnExceptionListener)}.
     *
     * @param valueListener An instance of {@link IResult.OnValueListener}, called when the {@link
     *     IResult} is completed with a value.
     * @param <U> Type of the new result that is returned by the listener.
     * @return A new {@link IResult} that the listener will complete.
     */
    @NonNull <U> IResult<U> then(@NonNull final IResult.OnValueListener<T, U> valueListener);


    /**
     * Convenience method for {@link #then(IResult.OnValueListener, IResult.OnExceptionListener)}.
     *
     * @param exceptionListener An instance of {@link IResult.OnExceptionListener}, called when the {@link
     *     IResult} is completed with an {@link Exception}.
     * @param <U> Type of the new result that is returned by the listener.
     * @return A new {@link IResult} that the listener will complete.
     */
     @NonNull <U> IResult<U> exceptionally(@NonNull final IResult.OnExceptionListener<U> exceptionListener);


    /**
     * An interface used to deliver values to listeners of a {@link IResult}
     *
     * @param <T> Type of the value delivered via {@link #onValue(Object)}
     * @param <U> Type of the value for the result returned from {@link #onValue(Object)}
     */
    public interface OnValueListener<T, U> {
        /**
         * Called when a {@link IResult} is completed with a value. Will be called on the same
         * thread where the IResult was created or on the {@link Handler} provided via {@link
         * #withHandler(Handler)}.
         *
         * @param value The value of the {@link IResult}
         * @return Result used to complete the next result in the chain. May be null.
         * @throws Throwable Exception used to complete next result in the chain.
         */
        @AnyThread
        @Nullable
        IResult<U> onValue(@Nullable T value) throws Throwable;
    }

    public interface OnExceptionListener<V> {
        /**
         * Called when a {@link IResult} is completed with an exception. Will be called on the same
         * thread where the IResult was created or on the {@link Handler} provided via {@link
         * #withHandler(Handler)}.
         *
         * @param exception Exception that completed the result.
         * @return Result used to complete the next result in the chain. May be null.
         * @throws Throwable Exception used to complete next result in the chain.
         */
        @AnyThread
        @Nullable
        IResult<V> onException(@NonNull Throwable exception) throws Throwable;
    }
}
