package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.db.dao.MediaDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepo @Inject constructor(var mediaDao: MediaDao){

}