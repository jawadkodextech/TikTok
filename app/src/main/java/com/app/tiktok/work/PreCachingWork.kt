package com.app.tiktok.work

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.tiktok.app.MyApp
import com.app.tiktok.utils.Constants
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll

private const val TAG = "PreCachingService"

class PreCachingService(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private var cachingJob: Deferred<Unit?>? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private val simpleCache = MyApp.simpleCache


    override suspend fun doWork(): Result = coroutineScope {
        val dtSorucFact = DefaultHttpDataSource.Factory()
        dtSorucFact.setUserAgent(
            Util.getUserAgent(
                applicationContext,
                "exo"
            )
        )

        cacheDataSourceFactory = CacheDataSource.Factory()
        cacheDataSourceFactory?.setCache(simpleCache!!)//setCache(simpleCache)
        cacheDataSourceFactory?.setUpstreamDataSourceFactory(dtSorucFact)
//        simpleCache!!,
//        DefaultHttpDataSourceFactory(
//            Util.getUserAgent(
//                applicationContext,
//                "exo"
//            )
//        )
        val dataList = inputData.getStringArray(Constants.KEY_STORIES_LIST_DATA)

        val jobs = dataList?.map { data ->
            async {
                val dataUri = Uri.parse(data)
//                DataSpec(dataUri, 0, (500 * 1024).toLong(), null)
                val dataSpec = DataSpec.Builder()
                dataSpec.setUri(dataUri)
                dataSpec.setPosition(0)
                dataSpec.setLength((500 * 1024).toLong())
                val dataSource: DataSource =
                    DefaultDataSourceFactory(
                        applicationContext,
                        Util.getUserAgent(
                            applicationContext,
                            "exo"
                        )
                    ).createDataSource()

                preloadVideo(
                    cacheDataSourceFactory?.createDataSource(),
                    dataSpec.build(),
                    simpleCache,
                    dataSource
                ) { requestLength: Long, bytesCached: Long, newBytesCached: Long ->
                    val downloadPercentage = (bytesCached * 100.0
                            / requestLength)
                    Log.d(TAG, "downloadPercentage: $downloadPercentage")
                }
            }
        }
        jobs?.joinAll()
        Result.success()
    }

    private fun preloadVideo(
        cachedt: CacheDataSource?,
        dataSpec: DataSpec?,
        cache: Cache?,
        upstream: DataSource?,
        progressListener: CacheWriter.ProgressListener?
    ) {
        Log.d(TAG, "preloadVideo")
        try {
//            CacheU
            val cw = CacheWriter(
                cachedt!!,
                dataSpec!!,
                ByteArray(CacheWriter.DEFAULT_BUFFER_SIZE_BYTES),
                progressListener
            )
            cw.cache()
//            CacheDataSource.Factory()
//                .cache.cac
//            SimpleCache.
//            CacheUtil.cache(
//                dataSpec,
//                cache,
//                CacheUtil.DEFAULT_CACHE_KEY_FACTORY,
//                upstream,
//                progressListener,
//                null
//            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}