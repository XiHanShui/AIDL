package com.oct.aidl

import android.os.Bundle
import android.os.SharedMemory
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.oct.aidl.client.AIDLClient
import com.oct.aidl.client.ClientManager
import com.oct.aidl.client.IResponseCallback
import java.time.Duration
import java.util.UUID
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ClientManager.instance.init(applicationContext,true, Duration.ofSeconds(10))

    }

    private val aidlClient  by lazy { AIDLClient( "com.oct.test","com.oct.service") }

    fun test(view: View) {


        val elapsedRealtimeNanos = System.currentTimeMillis()
        val toString = UUID.randomUUID().toString()
        Log.e("MainActivity","${System.currentTimeMillis()-elapsedRealtimeNanos}")
        thread {
            for (i in 0 until 1){
                thread {
//                    Thread.sleep(1000)
                   aidlClient.request(Request(params = "1111",
                        ), cb =object :IResponseCallback(){
                       override fun callback(result: Response) {

                       }



                   })

                }
            }


        }

    }


}