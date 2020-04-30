package com.songspagetti.quizlocker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val fragment  = MyPreferenceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.preferenceContent, fragment).commit()

        initButton.setOnClickListener {
            initAnswerCount()
        }

    }

    fun initAnswerCount(){

        val correctAnswerPref = getSharedPreferences("correctAnswer", Context.MODE_PRIVATE)
        val wrongAnswerPref = getSharedPreferences("wrongAnswer", Context.MODE_PRIVATE)

        correctAnswerPref.edit().clear().apply()
        wrongAnswerPref.edit().clear().apply()
    }

    // PreferenceFragmentCompat() : UI 에서 변경된 사항을 바로 저장할 수 있도록 하는 SharedPreference 생성할 수 있음
    class MyPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.pref, rootKey)
            // 퀴즈 종류 요약정보에 현재 선택된 항목을 보여주는 코드
            val categoryPref = findPreference<Preference>("category") as MultiSelectListPreference
            categoryPref.summary = categoryPref.values.joinToString(", ") // 이 기능이 작동을 안하네?
            // 환경설정 정보값이 변경될때에 요약정보를 변경하도록 리스너 등록
            categoryPref.setOnPreferenceChangeListener { preference, newValue ->
                // newValue 파라미터가 HashSet 으로 캐스팅이 실패하면 리턴
                val newValueSet = newValue as? HashSet<*>
                    ?: return@setOnPreferenceChangeListener true
                //선택된 퀴즈종류로 요약정보 보여줌
                categoryPref.summary = newValue.joinToString(", ")

                true
            }
            // 퀴즈 잠금화면 사용 스위치 객체 가져옴
            val useLockScreenPref = findPreference<Preference>("useLockScreen") as SwitchPreference
            //클릭되었을때의 이벤트 리스너
            useLockScreenPref.setOnPreferenceClickListener{
                when{
                    //퀴즈 잠금화면 사용이 체크된 경우 LockScreenService 실행
                    useLockScreenPref.isChecked -> {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            activity!!.startForegroundService(Intent(activity, LockScreenService::class.java))
                        } else{
                            activity!!.startService(Intent(activity, LockScreenService::class.java))
                        }
                    }

                    else -> activity!!.stopService(Intent(activity, LockScreenService::class.java))
                }
                true
            }

            // 앱이 시작되었을때 이미 퀴즈잠금화면 사용이 체크되어있으면 서비스 실행
            if(useLockScreenPref.isChecked){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    activity!!.startForegroundService(Intent(activity, LockScreenService::class.java))
                } else {
                    activity!!.startService(Intent(activity, LockScreenService::class.java))
                }
            }


        }
    }
}



















