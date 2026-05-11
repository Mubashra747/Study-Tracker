package com.studytracker.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.studytracker.R

class TimerActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var timeLeftMs: Long = 25 * 60 * 1000L

    private lateinit var tvDisplay: TextView
    private lateinit var btnStartPause: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        val toolbar = findViewById<Toolbar>(R.id.timerToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        tvDisplay   = findViewById(R.id.tvTimerDisplay)
        btnStartPause = findViewById(R.id.btnStartPause)

        // Mode selector
        findViewById<RadioGroup>(R.id.rgTimerMode).setOnCheckedChangeListener { _, id ->
            cancelTimer()
            timeLeftMs = when (id) {
                R.id.rbWork       -> 25 * 60 * 1000L
                R.id.rbShortBreak ->  5 * 60 * 1000L
                R.id.rbLongBreak  -> 15 * 60 * 1000L
                else              -> 25 * 60 * 1000L
            }
            updateDisplay(timeLeftMs)
        }

        btnStartPause.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }

        findViewById<TextView>(R.id.btnReset).setOnClickListener {
            cancelTimer()
            updateDisplay(timeLeftMs)
        }

        updateDisplay(timeLeftMs)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftMs, 1000) {
            override fun onTick(ms: Long) {
                timeLeftMs = ms
                updateDisplay(ms)
            }
            override fun onFinish() {
                isRunning = false
                btnStartPause.text = getString(R.string.timer_start)
            }
        }.start()
        isRunning = true
        btnStartPause.text = getString(R.string.timer_pause)
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        btnStartPause.text = getString(R.string.timer_start)
    }

    private fun cancelTimer() {
        timer?.cancel()
        isRunning = false
        btnStartPause.text = getString(R.string.timer_start)
    }

    private fun updateDisplay(ms: Long) {
        val min = (ms / 1000) / 60
        val sec = (ms / 1000) % 60
        tvDisplay.text = String.format("%02d:%02d", min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
