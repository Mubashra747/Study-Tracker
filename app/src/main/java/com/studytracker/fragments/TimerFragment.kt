package com.studytracker.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.studytracker.R
import java.util.Locale

class TimerFragment : Fragment() {

    private lateinit var tvTimerDisplay: TextView
    private lateinit var btnStartPause: TextView
    private lateinit var btnReset: TextView
    private lateinit var rgTimerMode: RadioGroup

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private var timeLeftInMillis: Long = 25 * 60 * 1000L // Default 25 minutes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        tvTimerDisplay = view.findViewById(R.id.tvTimerDisplay)
        btnStartPause = view.findViewById(R.id.btnStartPause)
        btnReset = view.findViewById(R.id.btnReset)
        rgTimerMode = view.findViewById(R.id.rgTimerMode)

        updateCountDownText()

        btnStartPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        rgTimerMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWork -> setTimer(25)
                R.id.rbShortBreak -> setTimer(5)
                R.id.rbLongBreak -> setTimer(15)
            }
        }

        return view
    }

    private fun setTimer(minutes: Int) {
        pauseTimer()
        timeLeftInMillis = minutes * 60 * 1000L
        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                btnStartPause.text = getString(R.string.timer_start)
            }
        }.start()

        timerRunning = true
        btnStartPause.text = getString(R.string.timer_pause)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        btnStartPause.text = getString(R.string.timer_start)
    }

    private fun resetTimer() {
        pauseTimer()
        // Reset based on current selection
        val checkedId = rgTimerMode.checkedRadioButtonId
        when (checkedId) {
            R.id.rbWork -> setTimer(25)
            R.id.rbShortBreak -> setTimer(5)
            R.id.rbLongBreak -> setTimer(15)
            else -> setTimer(25)
        }
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimerDisplay.text = timeLeftFormatted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
