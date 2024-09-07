package com.example.myfitness

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myfitness.ui.theme.MyFitnessTheme
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())

        setContent {
            MyFitnessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(null, 0)
                    focusManager.clearFocus()
                }
            }

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.my_fitness_icon),
                contentDescription = "my_fitness_icon",
                modifier = Modifier.size(130.dp).padding(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("MyFitness", fontSize = 32.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Timer("Overall Workout Timer")
        Spacer(modifier = Modifier.height(32.dp))
        Timer("High Intensity Timer")
        Spacer(modifier = Modifier.height(32.dp))
        BMRCalculator()
    }
}

@Composable
fun Timer(timerLabel: String) {
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var totalSeconds by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }

    var timerVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var job: Job? = remember { null }

    fun convertToHMS(seconds: Long): Triple<Int, Int, Int> {
        val h = (seconds / 3600).toInt()
        val m = ((seconds % 3600) / 60).toInt()
        val s = (seconds % 60).toInt()
        return Triple(h, m, s)
    }

    fun stopTimer() {
        job?.cancel()
        running = false
        paused = true
    }

    fun resetTimer() {
        stopTimer()
        totalSeconds = 0
        hours = ""
        minutes = ""
        seconds = ""
        paused = false
        timerVisible = false
    }

    fun startTimer() {
        if (!paused) {
            val h = hours.toIntOrNull() ?: 0
            val m = minutes.toIntOrNull() ?: 0
            val s = seconds.toIntOrNull() ?: 0
            totalSeconds = (h * 3600 + m * 60 + s).toLong()
        }

        if (totalSeconds == 0L) {
            return
        }

        job?.cancel()

        job = coroutineScope.launch {
            while (totalSeconds > 0 && running) {
                delay(1000)
                totalSeconds -= 1

                if (totalSeconds == 0L) {
                    resetTimer()
                    return@launch
                }
            }

            running = false
        }
        running = true
        paused = false
        timerVisible = true
    }

    val (h, m, s) = convertToHMS(totalSeconds)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(timerLabel, fontSize = 24.sp, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (!timerVisible) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Hours", fontSize = 16.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("Minutes", fontSize = 16.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = seconds,
                    onValueChange = { seconds = it },
                    label = { Text("Seconds", fontSize = 16.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        Box {
            if (timerVisible) {
                Text(
                    text = buildString {
                        if (h > 0) {
                            append("%d:".format(h))
                            append("%02d:".format(m))
                        } else if (m > 0) {
                            append("%d:".format(m))
                        }

                        if (h > 0 || m > 0) {
                            append("%02d".format(s))
                        } else {
                            append("%d".format(s))
                        }
                    },
                    fontSize = 48.sp, color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { if (running) stopTimer() else startTimer() },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (running) "Stop" else "Start", fontSize = 18.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = ::resetTimer,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset", fontSize = 18.sp, color = Color.Black)
            }
        }
    }
}

data class BMRInput(
    val weight: Double?,
    val height: Double?,
    val age: Int?,
    val gender: String?
)

fun calculateBMR(input: BMRInput): Double? {
    input.weight?.let { w ->
        input.height?.let { h ->
            input.age?.let { a ->
                return when (input.gender) {
                    "Male" -> 88.36 + (13.4 * w) + (4.8 * h) - (5.7 * a)
                    "Female" -> 447.6 + (9.2 * w) + (3.1 * h) - (4.3 * a)
                    else -> null
                }
            }
        }
    }
    return null
}

@Composable
fun BMRCalculator() {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var bmr by remember { mutableStateOf<Double?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text("BMR Calculator", fontSize = 24.sp, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
            Text("Male", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
            Text("Female", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            Button(
                onClick = {
                    val bmrInput = BMRInput(
                        weight = weight.toDoubleOrNull(),
                        height = height.toDoubleOrNull(),
                        age = age.toIntOrNull(),
                        gender = gender
                    )
                    bmr = calculateBMR(bmrInput)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Calculate", fontSize = 18.sp, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bmr != null) {
            Text(text = "Your BMR: ${bmr!!.roundToInt()} calories/day", fontSize = 24.sp, color = Color.White)
        }
    }
}