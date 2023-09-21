package com.bluetech.vidown.data.repos

import com.bluetech.vidown.data.db.dao.MediaDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepo @Inject constructor(var mediaDao: MediaDao){

}