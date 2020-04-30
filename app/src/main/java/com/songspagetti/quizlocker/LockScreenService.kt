package com.songspagetti.quizlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder

class LockScreenService : Service() {

    var receiver : ScreenOffReceiver? = null

    private val ANDROID_CHANNEL_ID = "com.songspagetti.quizlocker"
    private val NOTIFICATION_ID = 9999

    // 서비스가 최초 생성될때 콜백함수
    override fun onCreate() {
        super.onCreate()
        // 브로드캐스트 리시버가 null 인경우에만 런타임에 등록
        if(receiver == null){
            receiver = ScreenOffReceiver()
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            registerReceiver(receiver, filter)
        }
    }
    // 서비스를 호출하는 클라이언트가 startService() 함수를 호출할때마다 불리는 콜백함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            super.onStartCommand(intent, flags, startId)
            if(intent != null){
                if(intent.action == null){
                    //서비스가 최초 실행이 아닌 경우 onCreate가 불리지 않을 수 있음.
                    // 이 경우 receiver 가 null 이면 새로 생성하고 등록한다.
                    if(receiver == null){
                        receiver = ScreenOffReceiver()
                        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                        registerReceiver(receiver, filter)
                    }
                }
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                //오레오 버전 이상부터 백그라운드 서비스가 계속 실행되지 않도록 제한을 하기 때문에 계속 실행되어야 하는 서비스는 포어그라운드 서비스를 사용해야 함.
                // 포어그라운드 서비스는 반드시 '알림'을 사용자에게 띄워 어떤 앱이 백그라운드에서 계속 작업중인지 알 수 있고, 불필요하다 생각할 경우 서비스를 중지할 수 있도록 해야 함.
                //상단 알림 채널 생성 (임의의 id, 알람이름, 중요도)
                val channel = NotificationChannel(ANDROID_CHANNEL_ID, "MyService", NotificationManager.IMPORTANCE_DEFAULT)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                //Notification 서비스 객체를 가져옴
                //Android 8.0 이상에서 알림을 제공하려면 먼저 NotificationChannel 인스턴스를 createNotificationChannel()에 전달하여 앱의 알림 채널 을 시스템에 등록해야 합니다.
                val manager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                // Register the channel with the system
                manager.createNotificationChannel(channel)

                //Notification 알림 객체 생성
                val builder = Notification.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("SmartTracker Running")

                val notification = builder.build()
                // Notification  알림과 함께 포어그라운드 서비스 시작
                startForeground(NOTIFICATION_ID, notification)

            }
            // @@@ 오레오 미만 모델 대상 노티피케이션 추가하자! @@@

            return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스가 종료될 때 브로드캐스트 리시버 등록도 해제
        if(receiver != null){
            unregisterReceiver(receiver)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
            return null
    }
}
