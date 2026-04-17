package com.registeroffline.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.registeroffline.domain.model.Member
import com.registeroffline.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onNavigateToForm: (memberId: Long?) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showBulkDialog by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.uploadResult, state.error) {
        state.uploadResult?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Badge, null, tint = Navy, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Register Offline", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                    }
                },
                actions = {
                    // User chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DividerGray,
                        onClick = onNavigateToProfile,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                userName.ifBlank { "User" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 100.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Outlined.AccountCircle, null, Modifier.size(24.dp), tint = Navy)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
        ) {
            // ── Tab Row ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Navy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Navy,
                    )
                },
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(
                        "Draft",
                        modifier = Modifier.padding(vertical = 14.dp),
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(
                        "Sudah Di-Upload",
                        modifier = Modifier.padding(vertical = 14.dp),
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }

            // ── Content ──
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DraftTab(
                        members = state.draftMembers,
                        isUploading = state.isUploading,
                        uploadProgress = state.uploadProgress,
                        onEdit = { onNavigateToForm(it.id) },
                        onUpload = { viewModel.uploadSingleMember(it) },
                    )
                    1 -> SyncedTab(
                        members = state.syncedMembers,
                        isLoading = state.isFetchingSynced,
                    )
                }
            }

            // ── Bottom Buttons ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                // Tambah Data
                Button(
                    onClick = { onNavigateToForm(null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Data", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                // Upload Semua
                OutlinedButton(
                    onClick = { showBulkDialog = true },
                    enabled = state.draftCount > 0 && !state.isUploading && isOnline,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Navy,
                        disabledContentColor = Navy.copy(alpha = 0.4f),
                    ),
                    border = BorderStroke(1.5.dp, Navy),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Icon(
                        Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (state.isUploading) state.uploadProgress
                        else "Upload Semua (${state.draftCount})",
                        fontWeight = FontWeight.Bold,
                        color = Navy,
                    )
                }
            }
        }
    }

    // ── Bulk Sync Confirmation Dialog ──
    if (showBulkDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDialog = false },
            title = { Text("Upload Semua Data", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah kamu yakin ingin upload semua data?\nPastikan kamu sudah mengisi semua data yang diperlukan dengan benar, ya!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBulkDialog = false
                        viewModel.bulkSync()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) { Text("Ya, Upload Semua (${state.draftCount})") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBulkDialog = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun DraftTab(
    members: List<Member>,
    isUploading: Boolean,
    uploadProgress: String,
    onEdit: (Member) -> Unit,
    onUpload: (Member) -> Unit,
) {
    if (members.isEmpty()) {
        EmptyState(
            title = "Belum ada data",
            subtitle = "Klik \"Tambah Data\" untuk menambahkan data calon anggota",
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header info
        Text("List Draft KTA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Upload untuk mengirimkan data ini ke admin untuk di-verifikasi.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))

        // Info banner
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(10.dp),
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Info, null, tint = Navy, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Nomor Handphone, NIK, dan Foto KTP wajib diisi sebelum di-upload",
                    fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Upload progress
        AnimatedVisibility(visible = isUploading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = Navy,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(members, key = { _, m -> m.id }) { index, member ->
                MemberCard(
                    index = index + 1,
                    member = member,
                    onEdit = { onEdit(member) },
                    onUpload = { onUpload(member) },
                )
            }
        }
    }
}

@Composable
private fun SyncedTab(members: List<Member>, isLoading: Boolean) {
    if (members.isEmpty() && !isLoading) {
        EmptyState(
            title = "Belum ada data",
            subtitle = "Data yang sudah di-upload akan muncul di sini",
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Data yang sudah di-upload", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Data-data ini sudah dikirimkan ke admin verifikator.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Navy)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(members, key = { _, m -> m.id }) { index, member ->
                SyncedMemberCard(index = index + 1, member = member)
            }
        }
    }
}

@Composable
private fun MemberCard(index: Int, member: Member, onEdit: () -> Unit, onUpload: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Navy.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$index", fontWeight = FontWeight.Bold, color = Navy, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))

                // NIK + phone
                Column(modifier = Modifier.weight(1f)) {
                    // Masked NIK
                    val maskedNik = if (member.nik.length >= 16) {
                        member.nik.take(3) + "*".repeat(10) + member.nik.takeLast(3)
                    } else member.nik
                    Text(maskedNik, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (member.phone.isNotBlank()) {
                        Text(
                            "+62${member.phone.removePrefix("0")}",
                            fontSize = 12.sp, color = TextSecondary,
                        )
                    }
                }

                // Draft badge
                Surface(
                    color = DraftOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        "Draft",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = DraftOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray)
            Spacer(Modifier.height(8.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, null, Modifier.size(16.dp), tint = Navy)
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", color = Navy, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onUpload, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.CloudUpload, null, Modifier.size(16.dp), tint = Navy)
                    Spacer(Modifier.width(4.dp))
                    Text("Upload", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SyncedMemberCard(index: Int, member: Member) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Navy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$index", fontWeight = FontWeight.Bold, color = Navy, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val maskedNik = if (member.nik.length >= 16) {
                    member.nik.take(3) + "*".repeat(10) + member.nik.takeLast(3)
                } else member.nik
                Text(maskedNik, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (member.phone.isNotBlank()) {
                    Text("+62${member.phone.removePrefix("0")}", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Surface(
                color = SyncedGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    "Di-upload",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = SyncedGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.FolderOpen,
            null,
            modifier = Modifier.size(80.dp),
            tint = BorderGray,
        )
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 40.dp),
            lineHeight = 18.sp,
        )
    }
}
