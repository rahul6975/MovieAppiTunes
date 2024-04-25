package com.rahul.movieappitunes.network.paginationChecker

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
import java.util.Arrays
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class PagingRequestHelper
    (private val mRetryService: Executor) {
    private val mLock = Any()

    @GuardedBy("mLock")
    private val mRequestQueues =
        arrayOf(
            RequestQueue(RequestType.INITIAL),
            RequestQueue(RequestType.BEFORE),
            RequestQueue(RequestType.AFTER)
        )
    internal val mListeners = CopyOnWriteArrayList<Listener>()

    @AnyThread
    fun addListener(listener: Listener): Boolean {
        return mListeners.add(listener)
    }

    fun removeListener(listener: Listener): Boolean {
        return mListeners.remove(listener)
    }


    @AnyThread
    fun runIfNotRunning(type: RequestType, request: Request): Boolean {
        val hasListeners = !mListeners.isEmpty()
        var report: StatusReport? = null
        synchronized(mLock) {
            val queue = mRequestQueues[type.ordinal]
            if (queue.mRunning != null) {
                return false
            }
            queue.mRunning = request
            queue.mStatus = Status.RUNNING
            queue.mFailed = null
            queue.mLastError = null
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }

        report?.run {
            dispatchReport(this)
        }

        val wrapper = RequestWrapper(request, this, type)
        wrapper.run()
        return true
    }

    @GuardedBy("mLock")
    private fun prepareStatusReportLocked(): StatusReport {
        val errors = arrayOf(
            mRequestQueues[0].mLastError,
            mRequestQueues[1].mLastError,
            mRequestQueues[2].mLastError
        )

        return StatusReport(
            getStatusForLocked(RequestType.INITIAL),
            getStatusForLocked(RequestType.BEFORE),
            getStatusForLocked(RequestType.AFTER),
            errors
        )
    }

    @GuardedBy("mLock")
    private fun getStatusForLocked(type: RequestType): Status {
        return mRequestQueues[type.ordinal].mStatus
    }

    @AnyThread
    @VisibleForTesting
    internal fun recordResult(wrapper: RequestWrapper, throwable: Throwable?) {
        var report: StatusReport? = null
        val success = throwable == null
        val hasListeners = !mListeners.isEmpty()
        synchronized(mLock) {
            val queue = mRequestQueues[wrapper.mType.ordinal]
            queue.mRunning = null
            queue.mLastError = throwable
            if (success) {
                queue.mFailed = null
                queue.mStatus = Status.SUCCESS
            } else {
                queue.mFailed = wrapper
                queue.mStatus = Status.FAILED
            }
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }

        report?.run { dispatchReport(this) }

    }

    private fun dispatchReport(report: StatusReport) {
        for (listener in mListeners) {
            listener.onStatusChange(report)
        }
    }


    fun retryAllFailed(): Boolean {
        val toBeRetried = arrayOfNulls<RequestWrapper>(RequestType.values().size)
        var retried = false
        synchronized(mLock) {
            for (i in 0 until RequestType.values().size) {
                toBeRetried[i] = mRequestQueues[i].mFailed
                mRequestQueues[i].mFailed = null
            }
        }
        for (failed in toBeRetried) {
            if (failed != null) {
                failed.retry(mRetryService)
                retried = true
            }
        }
        return retried
    }

    internal class RequestWrapper(
        val mRequest: Request,
        val mHelper: PagingRequestHelper,
        val mType: RequestType
    ) : Runnable {
        override fun run() {
            mRequest.invoke(Callback(this, mHelper))
        }

        fun retry(service: Executor) {
            service.execute { mHelper.runIfNotRunning(mType, mRequest) }
        }
    }

    class Callback internal constructor(
        private val mWrapper: RequestWrapper,
        private val mHelper: PagingRequestHelper
    ) {
        private val mCalled = AtomicBoolean()

        fun recordSuccess() {
            if (mCalled.compareAndSet(false, true)) {
                mHelper.recordResult(mWrapper, null)
            } else {
                throw IllegalStateException(
                    "already called recordSuccess or recordFailure"
                )
            }
        }


        fun recordFailure(throwable: Throwable) {
            if (mCalled.compareAndSet(false, true)) {
                mHelper.recordResult(mWrapper, throwable)
            } else {
                throw IllegalStateException(
                    "already called recordSuccess or recordFailure"
                )
            }
        }
    }

    class StatusReport internal constructor(

        val initial: Status,

        val before: Status,

        val after: Status,
        private val mErrors: Array<Throwable?>
    ) {

        fun hasRunning(): Boolean {
            return (initial == Status.RUNNING
                    || before == Status.RUNNING
                    || after == Status.RUNNING)
        }


        fun hasError(): Boolean {
            return (initial == Status.FAILED
                    || before == Status.FAILED
                    || after == Status.FAILED)
        }

        fun getErrorFor(type: RequestType): Throwable? {
            return mErrors[type.ordinal]
        }

        override fun toString(): String {
            return ("StatusReport{"
                    + "initial=" + initial
                    + ", before=" + before
                    + ", after=" + after
                    + ", mErrors=" + Arrays.toString(mErrors)
                    + '}'.toString())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as StatusReport?
            if (initial != that!!.initial) return false
            if (before != that.before) return false
            return if (after != that.after) false else Arrays.equals(mErrors, that.mErrors)
        }

        override fun hashCode(): Int {
            var result = initial.hashCode()
            result = 31 * result + before.hashCode()
            result = 31 * result + after.hashCode()
            result = 31 * result + Arrays.hashCode(mErrors)
            return result
        }
    }

    /**
     * Listener interface to get notified by request status changes.
     */
    interface Listener {
        /**
         * Called when the status for any of the requests has changed.
         *
         * @param report The current status report that has all the information about the requests.
         */
        fun onStatusChange(report: StatusReport)
    }

    /**
     * Represents the status of a Request for each [RequestType].
     */
    enum class Status {
        /**
         * There is current a running request.
         */
        RUNNING,

        /**
         * The last request has succeeded or no such requests have ever been run.
         */
        SUCCESS,

        /**
         * The last request has failed.
         */
        FAILED
    }

    /**
     * Available request types.
     */
    enum class RequestType {
        /**
         * Corresponds to an initial request made to a [DataSource] or the empty state for
         * a [BoundaryCallback][androidx.paging.PagedList.BoundaryCallback].
         */
        INITIAL,

        /**
         * Corresponds to the `loadBefore` calls in [DataSource] or
         * `onItemAtFrontLoaded` in
         * [BoundaryCallback][androidx.paging.PagedList.BoundaryCallback].
         */
        BEFORE,

        /**
         * Corresponds to the `loadAfter` calls in [DataSource] or
         * `onItemAtEndLoaded` in
         * [BoundaryCallback][androidx.paging.PagedList.BoundaryCallback].
         */
        AFTER
    }

    internal inner class RequestQueue(val mRequestType: RequestType) {
        var mFailed: RequestWrapper? = null
        var mRunning: Request? = null
        var mLastError: Throwable? = null
        var mStatus = Status.SUCCESS
    }
}


/**
 * Runner class that runs a request tracked by the [PagingRequestHelper].
 *
 *
 * When a request is invoked, it must call one of [Callback.recordFailure]
 * or [Callback.recordSuccess] once and only once. This call
 * can be made any time. Until that method call is made, [PagingRequestHelper] will
 * consider the request is running.
 */
typealias Request = ((PagingRequestHelper.Callback) -> Unit)