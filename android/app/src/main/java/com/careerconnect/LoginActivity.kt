package com.careerconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnExit = findViewById<Button>(R.id.btnExit)

        // Navigate to Dashboard / Main on Login click
        btnSubmit?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Close app on Exit click
        btnExit?.setOnClickListener {
            finishAffinity()
        }
    }
}
