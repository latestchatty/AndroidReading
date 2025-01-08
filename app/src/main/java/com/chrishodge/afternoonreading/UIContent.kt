package com.chrishodge.afternoonreading.ui

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.chrishodge.afternoonreading.BuildConfig
import com.chrishodge.afternoonreading.R
import com.chrishodge.afternoonreading.ThreadsClient
import com.chrishodge.afternoonreading.ThreadsScreen
import com.chrishodge.afternoonreading.ThreadsViewModel

/*
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AllThreads (channels: List<Threads>){
    Scaffold() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black))
                .wrapContentSize(Alignment.TopStart)
        ) {
            /*
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 25.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\uD83C\uDF3F  Plants in Cosmetics",
                        style = MaterialTheme.typography.h1
                    )
                }
            }
            */
            items(channels){ channel ->
                Column {
                    Card(
                        border = BorderStroke(1.dp,Color.Gray.copy(0.1f)),
                        modifier = Modifier.padding(8.dp),
                        backgroundColor = Color.Gray.copy(0.1f)) {
                        Column {
                            Row(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Author",
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.orange),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Left,
                                    fontSize = 20.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "4 days ago",
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray.copy(0.75f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right,
                                    fontSize = 20.sp,
                                    maxLines = 1
                                )
                            }
                            Row(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = channel.name,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Left,
                                    fontSize = 22.sp,
                                    maxLines = 3
                                )
                            }
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = 47.toString() + " Replies",
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray.copy(0.75f),
                                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                                    textAlign = TextAlign.Left,
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                )
                                Box {
                                    MinimalDropdownMenu()
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
*/

@Composable
fun ChatScreen() {
    val apiKey = BuildConfig.API_KEY
    println("API Key: $apiKey")

    val threadsClient = ThreadsClient("$apiKey")
    val viewModel = ThreadsViewModel(threadsClient, "https://canary.discord.com/api/v9/guilds/81835925105020928/threads/active")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black))
            .wrapContentSize(Alignment.Center)
    ) {
        ThreadsScreen(viewModel)
        /*
        Text(
            text = "Home View",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
        */
        // AllThreads(channelList)
    }
}

@Composable
fun AccountScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Account",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Settings",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Composable
fun MinimalDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Hide Thread") },
                onClick = { /* Do something... */ }
            )
            DropdownMenuItem(
                text = { Text("Report") },
                onClick = { /* Do something... */ }
            )
        }
}