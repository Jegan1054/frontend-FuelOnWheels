package com.simats.fuelonwheels.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.simats.fuelonwheels.R
import com.simats.fuelonwheels.models.*
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.ApiRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.simats.fuelonwheels.network.RetrofitClient.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    token: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var shopDescription by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var shopRadius by remember { mutableStateOf(5) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    fun loadProfile() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getProfile(token)
                if (response.isSuccessful) {
                    profile = response.body()
                    profile?.user?.let { user ->
                        firstName = user.firstName ?: ""
                        lastName = user.lastName ?: ""
                        phone = user.phone ?: ""
                    }
                    profile?.shop?.let { shop ->
                        shopDescription = shop.description ?: ""
                        shopAddress = shop.address ?: ""
                        shopPhone = shop.phone ?: ""
                        shopRadius = shop.radius
                    }
                } else {
                    errorMessage = response.body()?.error ?: "Failed to load profile"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            scope.launch {
                isUploadingImage = true
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "temp_image.jpg")
                    FileOutputStream(file).use { output ->
                        inputStream?.copyTo(output)
                    }

                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData(
                        "image", file.name, requestBody
                    )

                    val response = repository.uploadImage(token, "profile", imagePart)
                    if (response.isSuccessful) {
                        loadProfile()
                        errorMessage = "Image uploaded successfully"
                    } else {
                        errorMessage = response.body()?.error ?: "Upload failed"
                    }
                } catch (e: Exception) {
                    errorMessage = "Upload error: ${e.message}"
                } finally {
                    isUploadingImage = false
                    selectedImageUri = null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            if (isEditing) "Cancel" else "Edit"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {

                    val imageUrl = profile?.user?.profileImage?.let {
                        URL + it
                    }
                    Log.d("ProfileScreen", "Profile Image URL: ${imageUrl}")
                    Image(
                        painter = rememberAsyncImagePainter(

                            model = imageUrl ?: R.drawable.ic_person
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp)
                        )
                    }

                    Icon(
                        Icons.Default.Edit,
                        "Edit Image",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Info
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = profile?.user?.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                // Shop Info (if mechanic or owner)
                if (profile?.shop != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Shop Information",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopDescription,
                        onValueChange = { shopDescription = it },
                        label = { Text("Description") },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        label = { Text("Address") },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text("Shop Phone") },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Service Radius: ${shopRadius}km")
                    Slider(
                        value = shopRadius.toFloat(),
                        onValueChange = { shopRadius = it.toInt() },
                        valueRange = 1f..50f,
                        enabled = isEditing
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isEditing) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    val updateRequest = UpdateProfileRequest(
                                        firstName = firstName,
                                        lastName = lastName,
                                        phone = phone,
                                        shop = if (profile?.shop != null) {
                                            ShopUpdateData(
                                                description = shopDescription,
                                                address = shopAddress,
                                                phone = shopPhone,
                                                radius = shopRadius
                                            )
                                        } else null
                                    )

                                    val response = repository.updateProfile(token, updateRequest)
                                    if (response.isSuccessful) {
                                        errorMessage = "Profile updated successfully"
                                        isEditing = false
                                        loadProfile()
                                    } else {
                                        errorMessage = response.body()?.error ?: "Update failed"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}