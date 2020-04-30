package com.songspagetti.quizlocker

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_quiz_locker.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class QuizLockerActivity : AppCompatActivity() {

    var quiz:JSONObject? = null

    //getPreferences(int mode) : 별도의 파일명을 지정하지 않기 때문에 자동으로 액티비티 이름의 파일 내에 저장되는데,
    // 예를 들어서 TestActivity에서 getPreferences() 함수로 SharedPreferences를 획득하면 TextActivity.xml에 저장된다.
    // 해당 액티비티만의 저장 공간이므로 다른 액티비티에서는 데이터를 이용할 수 없다.

    //getSharedPreferences(String name, int mode) :특정 이름을 가진 SharedPreferences를 생성, 주로 애플리케이션 전체에서 사용
    // 파일명에 대한 정보를 매개변수로 지정하기 때문에 해당 이름으로 XML파일을 만든다.
    // 다른 액티비티나 컴포넌트들이 데이터를 공유할 수 있다. 데이터가 많은데 이를 각각의 파일로 나누어 구분하여 저장하고자 할 때 주로 이용된다.

    // 정답 횟수 저장 SharedPreference // lazy를 통해 Activity 초기화 이후에 wrongAnswer 라는 이름의 SharedPreference 객체 반환
    val wrongAnswerPref by lazy { getSharedPreferences("wrongAnswer", Context.MODE_PRIVATE) }
    // MODE_PRIVATE :  자기 앱 내에서 사용, 외부 앱에서 접근 불가.
    // MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE : 외부 앱에서 읽기, 쓰기 가
    // 오답 횟수 저장 SharedPreference
    val correctAnswerPref by lazy{ getSharedPreferences("correctAnswer", Context.MODE_PRIVATE)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면보다 상단에 위치하기 위한 설정 조정. 버전별로 사용법이 다르기 때문에 버전에 따라 적용한다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            //잠금화면에서 보여지도록 설정
            setShowWhenLocked(true)
            // 잠금 해제(터치 가능하게)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)

        }else{

            //잠금화면에서 보여지도록 설정
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            // 기본 잠금화면을 해제
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        }
        // 화면을 켜진 상태로 유지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_quiz_locker)

        // 퀴즈 데이터를 가져온다.
        val json = assets.open("capital.json").reader().readText()
        val quizArray = JSONArray(json)
        //퀴즈를 선택한다.
        quiz = quizArray.getJSONObject(Random().nextInt(quizArray.length()))

        quizLabel.text = quiz?.getString("question")
        choice1.text = quiz?.getString("choice1")
        choice2.text = quiz?.getString("choice2")

        // 정답과 오답 횟수의 텍스트 뷰 초기화
        val id = quiz?.getInt("id").toString()?: ""
        correctCountLabel.text = "정답횟수: ${correctAnswerPref.getInt(id, 0)}"  // 빌드 후 첫 출력 : 0  sharedPreference 내 파라미터가 언제 생성되는거지?
        wrongCountLabel.text = "오답횟수: ${wrongAnswerPref.getInt(id, 0)}" // 빌드 후 첫 출력 : 0



        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when{

                    progress > 95 -> {
                        leftImageView.setImageResource(R.drawable.padlock)
                        rightImageView.setImageResource(R.drawable.unlock)
                    }
                    progress < 5 -> {
                        leftImageView.setImageResource(R.drawable.unlock)
                        rightImageView.setImageResource(R.drawable.padlock)
                    }
                    else -> {
                        leftImageView.setImageResource(R.drawable.padlock)
                        rightImageView.setImageResource(R.drawable.padlock)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            // 터치 조작을 끝낸 경우 불리는 함수
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?:50
                when{
                    progress > 95 -> checkChoice(quiz?.getString("choice2") ?: "")
                    progress < 5 -> checkChoice(quiz?.getString("choice1") ?: "")
                    else -> seekBar?.progress = 50
                }
            }

            fun checkChoice(choice: String){
                quiz?.let{
                    when{
                        // choice 의 텍스트가 정답 텍스트와 같으면 Activity 종료
                        choice == it.getString("answer") -> {
                            val id = it.getInt("id").toString()
                            var count = correctAnswerPref.getInt(id,0)
                            count++
                            correctAnswerPref.edit().putInt(id, count).apply() // id 가 아니라 한가지로 해야한다.
                            correctCountLabel.text = "정답횟수: ${count}"

                            finish()
                        }
                        else -> {
                            val id = it.getInt("id").toString()
                            var count = wrongAnswerPref.getInt(id, 0)
                            count++
                            wrongAnswerPref.edit().putInt(id, count).apply()
                            wrongCountLabel.text = "오답횟수: ${count}"


                            leftImageView.setImageResource(R.drawable.padlock)
                            rightImageView.setImageResource(R.drawable.padlock)
                            seekBar?.progress = 50

                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if(Build.VERSION.SDK_INT >= 26){
                                // 1초동안 100의 세기(최고 255) 로 1회 진동
                                vibrator.vibrate(VibrationEffect.createOneShot(1000, 100))
                            }else {
                                // 1초동안
                                vibrator.vibrate(1000)
                            }
                        }
                    }
                }
            }
        })





    }
}
