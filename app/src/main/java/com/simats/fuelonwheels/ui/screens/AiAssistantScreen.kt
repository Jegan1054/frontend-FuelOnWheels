package com.simats.fuelonwheels.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

// --- 1. Data Model ---
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

// --- 2. ViewModel Logic ---
class AiViewModel : ViewModel() {

    val chatMessages = mutableStateListOf<ChatMessage>()
    var isLoading by mutableStateOf(false)

    // Replace with your actual Gemini API key
    private val apiKey = "AIzaSyBovs2qgIzQJTRiiq3Cq09Fwzm2H0imBfk"

    // Validate and use an available model name
    private val modelName = "gemini-2.5-flash"

    // GenerativeModel instance
    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey,
        systemInstruction = content {
            text(
                "You are the Fuel on Wheels AI assistant. " +
                        "Your Name is Fuelbot. " +
                        "Only answer questions related to fuel delivery, app features, pricing, " +
                        "or orders. If a user asks about unrelated topics, politely reply that you can only assist with Fuel on Wheels queries."
            )
        }
    )

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return

        chatMessages.add(ChatMessage(userPrompt, true))
        isLoading = true

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(userPrompt)
                val replyText = response.text ?: "No response"
                chatMessages.add(ChatMessage(replyText, false))
            } catch (e: Exception) {
                chatMessages.add(ChatMessage("Error: ${e.localizedMessage}", false))
            } finally {
                isLoading = false
            }
        }
    }
}

// --- 3. UI Components ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: AiViewModel = viewModel()) {
    var textFieldValue by remember { mutableStateOf("") }

    // Handle system back press
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FuelOn Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.chatMessages) { message ->
                    ChatBubble(message)
                }
                if (viewModel.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about fuel delivery...") },
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(
                    onClick = {
                        if (textFieldValue.isNotBlank()) {
                            viewModel.sendMessage(textFieldValue)
                            textFieldValue = ""
                        }
                    },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val textColor = if (message.isUser) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor, fontSize = 14.sp)
        }
    }
}
