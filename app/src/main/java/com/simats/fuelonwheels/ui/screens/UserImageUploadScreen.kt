package com.simats.fuelonwheels.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.repository.ApiRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserImageUploadScreen(
    token: String,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Profile") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Upload Profile Photo",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "This helps service providers identify you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(30.dp))

            // 🔵 IMAGE PREVIEW CARD
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.size(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 📸 SELECT IMAGE BUTTON
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Image")
            }

            Spacer(Modifier.height(24.dp))

            // ⬆️ UPLOAD BUTTON
            Button(
                onClick = {
                    scope.launch {
                        uploading = true
                        imageUri?.let { uri ->
                            val file = uriToFile(uri, context)
                            val body = MultipartBody.Part.createFormData(
                                "image",
                                file.name,
                                file.asRequestBody("image/*".toMediaType())
                            )

                            val response =
                                ApiRepository(RetrofitClient.apiService)
                                    .uploadImage(token, "profile", body)

                            if (response.isSuccessful) {
                                onUploadSuccess()
                            }
                        }
                        uploading = false
                    }
                },
                enabled = imageUri != null && !uploading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload & Continue")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ⏭️ SKIP OPTION
            TextButton(
                onClick = { onUploadSuccess() }
            ) {
                Text("Skip for now")
            }
        }
    }
}

/* ------------------ HELPER ------------------ */

fun uriToFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "upload.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.copyTo(outputStream)
    inputStream.close()
    outputStream.close()
    return file
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun userImageUploadScreen() {
    UserImageUploadScreen(token = "your_token_here", onUploadSuccess = {})
}
