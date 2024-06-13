package com.fahim.diceroller

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.fahim.diceroller.ui.theme.DiceRollerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceRollerTheme {
                DiceRollerApp()
            }
        }
    }
}

@Preview
@Composable
fun DiceRollerApp() {
    RollDiceImage(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )

}

@Composable
fun RollDiceImage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var result by remember { mutableStateOf(1) }
    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
    var isRolling by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.dice_roll)
    }

    LaunchedEffect(key1 = isRolling) {
        if (isRolling) {
            coroutineScope.launch {
                launch {
                    try {
                        mediaPlayer.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                launch {
                    rotation.animateTo(
                        targetValue = 1000f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1.2f,
                        animationSpec = InfiniteRepeatableSpec(
                            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }
        } else {
            rotation.snapTo(0f)
            scale.snapTo(1f)
            withContext(Dispatchers.IO) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    try {
                        mediaPlayer.prepareAsync() // Now you can prepare it
                    } catch (e: IOException) {
                        Log.e("MediaPlayerError", "Error preparing media player", e)

                    } catch (e: IllegalStateException) {
                        Log.e("MediaPlayerError", "Error preparing media player", e)

                    }
                }

            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .clickable(indication = null, // Disable the visual feedback
                    interactionSource = remember { MutableInteractionSource() })
                {
                    if (isRolling) return@clickable // Prevent rolling when already rolling
                    isRolling = true
                    coroutineScope.launch {
                        delay(1000)  // Duration of the rolling animation
                        result = (1..6).random()
                        isRolling = false
                    }
                },
            painter = painterResource(imageResource),
            contentDescription = result.toString()
        )
    }

}