package com.neutron.transcrypt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.neutron.transcrypt.ui.state.MessageUiState
import com.neutron.transcrypt.ui.theme.Purple40
import com.neutron.transcrypt.ui.theme.TranscryptTheme
import com.neutron.transcrypt.ui.theme.White500
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val messagesList = mutableStateListOf<MessageUiState>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranscryptTheme(darkTheme = false, dynamicColor = true) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // brand name
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Create references for the composables to constrain
                        val (title, messageList, inputBox) = createRefs()
                        BrandTitle(Modifier.constrainAs(title) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        })
                        Conversation(
                            Modifier
                                .constrainAs(messageList) {
                                    top.linkTo(title.bottom)
                                    bottom.linkTo(inputBox.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    width = Dimension.matchParent
                                    height = Dimension.fillToConstraints
                                }
                        )
                        InputBox(Modifier.constrainAs(inputBox) {
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                            height = Dimension.wrapContent
                        })
                    }
                }
            }
        }
        listOfDummyMessages()
    }

    @Composable
    fun Conversation(modifier: Modifier) {
        val lazyColumnListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true,
            state = lazyColumnListState,
            reverseLayout = false
        ) {
            coroutineScope.launch {
                if (lazyColumnListState.isScrollInProgress.not() && messagesList.size > 0) {
                    lazyColumnListState.animateScrollToItem(messagesList.size - 1)
                }
            }
            items(messagesList) {
                MessageCard(message = it)
            }

        }
    }

    @Composable
    fun MessageCard(message: MessageUiState) {
        val icon = Icons.Filled
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if (message.isTranslated) Arrangement.Start else Arrangement.End
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isTranslated) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        White500
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .wrapContentWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (message.isTranslated) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier.size(18.dp),
                            onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = icon.ContentCopy,
                                contentDescription = "copy",
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun BrandTitle(constrainAs: Modifier) {
        Surface(
            shadowElevation = 8.dp,
            modifier = constrainAs
                .fillMaxWidth()
                .height(58.dp)
                .background(color = White500)
                .wrapContentHeight(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = getString(R.string.brand_name_title),
                    Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun InputBox(constrainAs: Modifier) {
        var text by rememberSaveable { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        Row(
            modifier = constrainAs
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1.0f),
                value = text,
                onValueChange = {
                    text = it
                },
                label = { Text(text = getString(R.string.input_message)) }
            )
            IconButton(
                modifier = Modifier
                    .wrapContentWidth(),
                onClick = {
                    messagesList.apply {
                        if (text.isNotEmpty()) {
                            add(MessageUiState(text.trim()))
                            lifecycleScope.launch {
                                delay(500L)
                                add(MessageUiState("theek hai! bhai", true))
                            }
                        }
                    }
                })
            {
                Icon(
                    modifier = Modifier
                        .clip(CircleShape),
                    imageVector = Icons.Filled.Send,
                    contentDescription = "send",
                    tint = Purple40
                )
            }
        }
    }

    // todo : delete later
    private fun listOfDummyMessages() {
        val list = mutableListOf(
            MessageUiState("bhai me 2 min me aa raha hu", false),
            MessageUiState("theek hai! bhai", true),
            MessageUiState("bhai me 2 min me aa raha hu", false),
            MessageUiState("theek hai! bhai", true),
            MessageUiState("bhai me 2 min me aa raha hu", false),
            MessageUiState("theek hai! bhai", true),
            MessageUiState("bhai me 2 min me aa raha hu", false),
            MessageUiState("theek hai! bhai", true),
            MessageUiState("bhai me 2 min me aa raha hu", false),
            MessageUiState("theek hai! bhai", true)
        )
//        messagesList.addAll(list)
    }
}