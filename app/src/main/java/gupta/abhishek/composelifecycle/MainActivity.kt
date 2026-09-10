package gupta.abhishek.composelifecycle

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import gupta.abhishek.composelifecycle.ui.theme.ComposeLifecycleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLifecycleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Screen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable

fun Screen(modifier: Modifier) {

// Compose Lifecycle


//    Activity
//    ↓
//    setContent { }
//    ↓
//    Composable
//    ↓
//    Composition
//    ↓
//    Recomposition

    val lifecycleOwner = LocalLifecycleOwner.current


    DisposableEffect(lifecycleOwner) {


        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    Log.d("LifecycleObserver", "ON_CREATE")
                }

                Lifecycle.Event.ON_START -> {
                    Log.d("LifecycleObserver", "ON_START")
                }

                Lifecycle.Event.ON_RESUME -> {
                    Log.d("LifecycleObserver", "ON_RESUME")
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("LifecycleObserver", "ON_PAUSE")
                }

                Lifecycle.Event.ON_STOP -> {
                    Log.d("LifecycleObserver", "ON_STOP")
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("LifecycleObserver", "ON_DESTROY")
                }

                Lifecycle.Event.ON_ANY -> {
                    Log.d("LifecycleObserver", "ON_ANY")
                }
            }

        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }

    }



    var showDialog by remember{
        mutableStateOf(false)
    }
    Column(
      modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(onClick = {
            Log.d("LifecycleObserver", "Button clicked")
            showDialog= true
        }) {
            Text("Show Alert Dialog Box")
        }



    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("Confirmation")
            },
            text = {
                Text("Do you want to continue?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        Log.d("Dialog", "Accepted")
                    }
                ) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        Log.d("Dialog", "Cancelled")
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
