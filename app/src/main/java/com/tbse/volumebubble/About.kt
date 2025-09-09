package com.tbse.volumebubble

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.play_game_button)
            .setOnClickListener {
                startActivity(Intent(this, GameActivity::class.java))
            }
    }

}
