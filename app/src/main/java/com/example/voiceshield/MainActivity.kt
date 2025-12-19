package com.example.voiceshield

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voiceshield.ui.theme.VoiceShieldTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var mediaRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceShieldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VoiceShieldHome(
                        modifier = Modifier.padding(innerPadding),
                        startRecording = { startRecording() },
                        stopRecording = { stopRecording() }
                    )
                }
            }
        }
    }

    private fun startRecording() {
        val outputFile = File(cacheDir, "voiceshield_demo.3gp")

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
    }
}

@Composable
fun VoiceShieldHome(
    modifier: Modifier = Modifier,
    startRecording: () -> Unit = {},
    stopRecording: () -> Unit = {}
) {
    val context = LocalContext.current
    var isProtecting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val msg = if (isGranted) {
            "Mic permission granted (demo)"
        } else {
            "Mic permission denied"
        }
        android.widget.Toast
            .makeText(
                context,
                msg,
                android.widget.Toast.LENGTH_SHORT
            )
            .show()
    }

    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }

            val cursor: Cursor? =
                context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            selectedFileName =
                if (cursor != null && cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else {
                    uri.lastPathSegment
                }
            cursor?.close()
        }
    }

    val scamKeywords = listOf(
        "OTP",
        "UPI PIN",
        "ATM PIN",
        "Aadhaar number",
        "bank login",
        "screen share",
        "refund link"
    )

    // Animation state for title
    var startAnim by remember { mutableStateOf(false) }

    val titleScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = tween(durationMillis = 800),
        label = "titleScale"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "titleAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

    // Gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A148C),
                        Color(0xFF7C4DFF)
                    )
                )
            )
            .padding(12.dp)
    ) {
        // Card on top
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated title section – slightly lower and styled
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .graphicsLayer { alpha = titleAlpha }
                        .scale(titleScale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VOICE SHIELD",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color(0xFF0D47A1),
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Scam Call Bodyguard",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF303F9F),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Listens and warns before you get trapped",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5E35B1),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Bottom content
                Column {
                    Button(
                        onClick = {
                            if (!isProtecting) {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                                startRecording()
                            } else {
                                stopRecording()
                            }
                            isProtecting = !isProtecting
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isProtecting)
                                Color(0xFFD32F2F)
                            else
                                Color(0xFF6200EE),
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isProtecting) "Stop Protection" else "Start Protection")
                    }

                    Button(
                        onClick = {
                            android.widget.Toast
                                .makeText(
                                    context,
                                    "Warning: This call is dangerous. Do not share OTP. Cut the call. (demo)",
                                    android.widget.Toast.LENGTH_LONG
                                )
                                .show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6D00),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Simulate Scam Call")
                    }

                    Button(
                        onClick = {
                            audioPicker.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0288D1),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Check Recorded Call")
                    }

                    Text(
                        text = if (isProtecting)
                            "Protection is ON (demo – mic recording running)."
                        else
                            "Protection is OFF (demo – not listening).",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color(0xFF424242)
                    )

                    if (selectedFileName != null) {
                        Text(
                            text = "Selected recording: $selectedFileName",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color(0xFF1565C0)
                        )
                    }

                    Text(
                        text = "Scam keywords to watch for:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp),
                        color = Color(0xFF6A1B9A)
                    )

                    scamKeywords.forEach { word ->
                        Text(
                            text = "• $word",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceShieldHomePreview() {
    VoiceShieldTheme {
        VoiceShieldHome()
    }
}
