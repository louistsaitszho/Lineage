package io.github.louistsaitszho.lineage.model

import android.content.Context
import io.github.louistsaitszho.lineage.attributes.VideoAttribute
import io.github.louistsaitszho.lineage.model.attributes.ModuleAttribute
import io.github.louistsaitszho.lineage.model.poko.JsonApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*

/**
 * Implementation of DataCenter class using
 * - Retrofit (that is already in a wrapper)
 * - TODO local storage (Realm or SQLite or ObjectBox or whatever works best)
 *
 * Created by Louis Tsai on 27.08.17.
 */
class DataCenterImpl(context: Context?) : DataCenter {
    private val apiWrapper = LineageApiWrapperImpl()
    private val preferenceStorage = PreferenceStorageImpl(context)

    /**
     * TODO retrieve data from local storage
     * Get a list of videos from a callback
     * @param callback will be trigger once the data is ready (or failed)
     * @return a Cancelable object so you can cancel operation if you want to (memory leak, prevent
     * loading when the activity is dead, etc.)
     */
    override fun getVideos(moduleId: String, callback: DataListener<MutableList<Video>>): Cancelable? {
        val call = apiWrapper.getVideo(moduleId)
        val cancelable = Cancelable {
            //TODO some kind of flag to indicate "Don't even try to return anything back"?
            if (call.isExecuted)
                call.cancel()
            true
        }

        call.enqueue(object : Callback<JsonApiResponse<VideoAttribute>> {
            override fun onResponse(call: Call<JsonApiResponse<VideoAttribute>>?, response: Response<JsonApiResponse<VideoAttribute>>?) {
                //todo check errors
                Timber.d(response.toString())
                if (response?.body() != null && response.body()?.data != null) {
                    val outputList = ArrayList<Video>()
                    Timber.d(response.body().toString())
                    response.body()?.data?.forEach {
                        Timber.d(it.toString())
                        outputList.add(Video(it))
                    }
                    if (call?.isCanceled == false)
                        callback.onSuccess(DataListener.SOURCE_REMOTE, outputList)
                }
            }

            override fun onFailure(call: Call<JsonApiResponse<VideoAttribute>>?, t: Throwable?) {
                Timber.e(t)
                if (call?.isCanceled == false)
                    callback.onFailure(Throwable("API returns error when you are trying to fetch videos", t))
            }
        })
        return cancelable
    }

    override fun getModules(callback: DataListener<MutableList<Module>>): Cancelable {
        val call = apiWrapper.getModules()
        val cancelable = Cancelable {
            if (call.isExecuted)
                call.cancel()
            true
        }

        call.enqueue(object : Callback<JsonApiResponse<ModuleAttribute>> {
            override fun onResponse(call: Call<JsonApiResponse<ModuleAttribute>>?, response: Response<JsonApiResponse<ModuleAttribute>>?) {
                if (response?.errorBody() != null) {
                    //todo i need something to handle standard networking error and stuff
                    Timber.e(Throwable("user encounter networking error when calling GET modules: $response."))
                } else if (response?.body()?.error != null && response.body()?.error?.isNotEmpty() == true) {
                    //todo this will be the response specific error (and it's handling)
                }
                //technically error and body can exist together, that's why this is not a else if. Whoever calls it should decide what to do instead
                if (response?.body()?.data != null) {
                    val outputList = ArrayList<Module>()
                    Timber.d(response.body().toString())
                    response.body()?.data?.forEach {
                        outputList.add(Module(it))
                    }
                    if (call?.isCanceled == false)
                        callback.onSuccess(DataListener.SOURCE_REMOTE, outputList)
                }
            }

            override fun onFailure(call: Call<JsonApiResponse<ModuleAttribute>>?, t: Throwable?) {
                Timber.e(t)
                if (call?.isCanceled == false)
                    callback.onFailure(Throwable("API returns error when you are trying to fetch modules", t))
            }
        })

        return cancelable
    }

    /**
     * TODO temporarily return hard code school key
     */
    override fun getSchoolCodeLocally(callback: DataListener<String>): Cancelable? {
        callback.onSuccess(DataListener.SOURCE_LOCAL, "123")
//        if (preferenceStorage.hasSchoolKey()) {
//            callback.onSuccess(DataListener.SOURCE_LOCAL, preferenceStorage.getSchoolKey())
//        } else {
//            callback.onFailure(Throwable("No school key"))
//        }
        return null
    }

    override fun close() {
        //TODO realm/SQLite/object box/whatever
    }
}
