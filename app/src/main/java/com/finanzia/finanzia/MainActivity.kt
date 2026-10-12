package com.finanzia.finanzia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finanzia.finanzia.datos.Repositorios
import com.finanzia.finanzia.navegacion.FinanziaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanziaApp(auth = Repositorios.auth, onboarding = Repositorios.onboarding)
        }
    }
}
