package com.hyunkyung.cs4750.photogallery

import android.app.Application
import androidx.lifecycle.*
import com.hyunkyung.cs4750.photogallery.api.FlickrFetchr

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    val galleryItemLiveData: LiveData<List<GalleryItem>>
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
    get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreference.getStoredQuery(app)
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank())
                flickrFetchr.fetchPhotos()
            else
                flickrFetchr.searchPhotos(searchTerm)
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreference.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}