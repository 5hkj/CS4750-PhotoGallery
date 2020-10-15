package com.hyunkyung.cs4750.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hyunkyung.cs4750.photogallery.api.FlickrFetchr

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }
}