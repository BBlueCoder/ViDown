package com.bluetech.vidown.core.workers

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.bluetech.vidown.data.db.entities.DownloadStatus
import com.bluetech.vidown.utils.formatSizeToReadableFormat
import com.bluetech.vidown.workers.DownloadFile
import com.google.common.truth.Truth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.cert.TrustAnchor

class DownloadFileTest {

    private lateinit var context: Context
    private val downloadUrl =  "https://rr2---sn-p5h-uobe.googlevideo.com/videoplayback?expire=1694722114&ei=4hMDZZ2CHdmEp-oPj9-2-A8&ip=196.64.156.9&id=o-AOr9LK0tqk6Vf7Z3xdm6AQ_L5o4VkZ4Wdnf3EbUqpR0A&itag=140&source=youtube&requiressl=yes&mh=1F&mm=31%2C29&mn=sn-p5h-uobe%2Csn-p5h-gc5y&ms=au%2Crdu&mv=m&mvi=2&pl=21&gcr=ma&initcwndbps=528750&spc=UWF9f0u681rfNGseWC_PLAgJNpKpcPz2b79vt4PMiA&vprv=1&svpuc=1&mime=audio%2Fmp4&ns=Tew27QMjDy1YBGgYQ6mqdBYP&gir=yes&clen=3509814&dur=216.827&lmt=1694149892244439&mt=1694699868&fvip=3&keepalive=yes&fexp=24007246%2C51000011&c=WEB&txp=4532434&n=sGA2liRv1dWx8Q&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cgcr%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhANq20OHqeZt2B1LAer-MiN7CbLNe0wQGdScd-314Zf8dAiB8B2FtBh6g-tdFjRBQF-O6ndDSWx_bLnbJM4osCeQ2ow%3D%3D&sig=AOq0QJ8wRgIhALgLlkWBTnORfgQBLLUQJ3pDCcdAH2bYxNdHLBZGnuPxAiEAoAX8xhZud8L97Tc7hqzudG3hi8Qkhb9IH7bh1imvjMs="

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun downloadFile_Test() = runBlocking {
        val rootPath = context.filesDir.path
        val fileName = "fileNameTest"
        var savedDownloadedSize: Long = 1818624
        var size: Long = 0

        DownloadFile(rootPath, downloadUrl, fileName, savedDownloadedSize)()
            .onCompletion {
                println("********** downloadedSize = $savedDownloadedSize")
                Truth.assertThat(savedDownloadedSize).isEqualTo(File(rootPath,fileName).length())
            }
            .collect {
                println("************ downloading ${it.downloadSizeInBytes.formatSizeToReadableFormat()}")
                savedDownloadedSize = it.downloadSizeInBytes
                size = it.sizeInBytes
            }
    }

    @Test
    fun downloadFile_exceptionTest() = runBlocking {
        val invalidRootPath = "***"
        val fileName = "fileNameTest"

        try{
            DownloadFile(invalidRootPath,downloadUrl,fileName,0)()
                .onCompletion {
                    println("**************** complete $it")
                }.collect{
                }
        }catch (ex : Exception){
            Truth.assertThat(ex).isNotNull()
        }


    }
}