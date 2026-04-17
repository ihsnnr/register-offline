package com.registeroffline.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.registeroffline.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLogout() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DividerGray),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Person,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = TextSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                state.fullName.ifBlank { "User" },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Navy,
            )
            Spacer(Modifier.height(4.dp))
            Text(state.email, fontSize = 14.sp, color = TextSecondary)

            Spacer(Modifier.height(32.dp))

            // Menu items
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileMenuItem(
                    icon = Icons.Outlined.Lock,
                    title = "Ganti Password",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider(color = DividerGray)
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Bantuan",
                    onClick = { /* TODO */ },
                )
                HorizontalDivider(color = DividerGray)
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    title = "Keluar",
                    titleColor = ErrorRed,
                    onClick = { showLogoutDialog = true },
                )
            }

            Spacer(Modifier.weight(1f))

            Text("v1.0.1", fontSize = 12.sp, color = TextHint)
            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Logout Dialog ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Apakah kamu yakin ingin keluar?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Data yang ada di draft-mu mungkin akan hilang. Kami sarankan untuk upload terlebih dahulu.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) { Text("Ya, keluar", fontWeight = FontWeight.Bold) }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) { Text("Batal", fontWeight = FontWeight.Bold, color = TextPrimary) }
                }
            },
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = titleColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium, color = titleColor, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, null, tint = TextHint)
        }
    }
}
