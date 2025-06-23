package com.example.uas_laptop_price_prediction_221351134

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardTentang: CardView = findViewById(R.id.cardTentang)
        val cardFitur: CardView = findViewById(R.id.cardFitur)
        val cardModel: CardView = findViewById(R.id.cardModel)
        val cardSimulasi: CardView = findViewById(R.id.cardSimulasi)

        cardTentang.setOnClickListener {
            val intent = Intent(this, TentangActivity::class.java)
            startActivity(intent)
        }

        cardFitur.setOnClickListener {
            val intent = Intent(this, FiturActivity::class.java)
            startActivity(intent)
        }

        cardModel.setOnClickListener {
            val intent = Intent(this, ModelActivity::class.java)
            startActivity(intent)
        }

        cardSimulasi.setOnClickListener {
            val intent = Intent(this, SimulasiActivity::class.java)
            startActivity(intent)
        }
    }
}
