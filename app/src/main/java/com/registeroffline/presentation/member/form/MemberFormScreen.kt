package com.registeroffline.presentation.member.form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.registeroffline.presentation.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MemberFormScreen(
    onBack: () -> Unit,
    viewModel: MemberFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.savedSuccess) { if (state.savedSuccess) onBack() }
    LaunchedEffect(state.uploadSuccess) { if (state.uploadSuccess) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearError() }
    }

    var ktpPrimaryUri by remember { mutableStateOf<Uri?>(null) }
    var ktpSecondaryUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Date picker visibility
    var showDatePicker by remember { mutableStateOf(false) }

    val primaryCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && ktpPrimaryUri != null) {
            val path = ktpPrimaryUri!!.path?.let {
                File(context.cacheDir, it.substringAfterLast("/"))
            }?.takeIf { it.exists() }?.absolutePath
                ?: getFileFromUri(context, ktpPrimaryUri!!)?.absolutePath
            if (path != null) viewModel.setKtpFile(path)
        }
    }
    val secondaryCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && ktpSecondaryUri != null) {
            val path = ktpSecondaryUri!!.path?.let {
                File(context.cacheDir, it.substringAfterLast("/"))
            }?.takeIf { it.exists() }?.absolutePath
                ?: getFileFromUri(context, ktpSecondaryUri!!)?.absolutePath
            if (path != null) viewModel.setKtpFileSecondary(path)
        }
    }

    fun launchCamera(isPrimary: Boolean) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
            return
        }
        val fileName = "ktp_${if (isPrimary) "primary" else "secondary"}_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        if (isPrimary) {
            ktpPrimaryUri = uri
            primaryCameraLauncher.launch(uri)
        } else {
            ktpSecondaryUri = uri
            secondaryCameraLauncher.launch(uri)
        }
    }

    // ── Date Picker Dialog ──
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToMillis(state.birthDate),
            yearRange = 1900..java.time.Year.now().value,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateBirthDate(formatMillisToDate(millis))
                        }
                        showDatePicker = false
                    },
                ) { Text("OK", color = Navy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                selectedDayContainerColor = Navy,
                selectedDayContentColor = Color.White,
                todayContentColor = Navy,
                todayDateBorderColor = Navy,
            ),
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = Navy,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Navy,
                    todayDateBorderColor = Navy,
                ),
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Data", fontWeight = FontWeight.Bold) },
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
                .background(Background),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ══════════ DATA UTAMA ══════════
                SectionHeader("Data Utama")

                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Info, null, tint = Navy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Nomor Handphone, NIK, Foto KTP, dan Foto Diri wajib diisi sebelum disimpan / di-upload",
                            fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp,
                        )
                    }
                }

                FormField("Nomor Handphone", state.phone, viewModel::updatePhone, required = true,
                    keyboardType = KeyboardType.Phone, placeholder = "Masukkan nomor handphone")
                FormField("NIK", state.nik, viewModel::updateNik, required = true,
                    keyboardType = KeyboardType.Number, placeholder = "16 digit no KTP")

                // ── KTP Photos ──
                Text("Foto KTP *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "Ambil 2 foto KTP untuk hasil yang lebih baik. Pastikan KTP terlihat jelas dan tidak blur.",
                    fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KtpPhotoBox(
                        filePath = state.ktpFilePath,
                        onClick = { launchCamera(isPrimary = true) },
                        modifier = Modifier.weight(1f),
                    )
                    KtpPhotoBox(
                        filePath = state.ktpFileSecondaryPath,
                        onClick = { launchCamera(isPrimary = false) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ══════════ INFORMASI LAINNYA ══════════
                SectionHeader("Informasi Lainnya")
                FormField("Nama Lengkap", state.name, viewModel::updateName, placeholder = "Masukkan nama sesuai KTP")
                FormField("Tempat Lahir", state.birthPlace, viewModel::updateBirthPlace, placeholder = "Masukkan tempat lahir sesuai KTP")

                // ── Tanggal Lahir pakai DatePicker ──
                DatePickerField(
                    label = "Tanggal Lahir",
                    value = state.birthDate,
                    onClick = { showDatePicker = true },
                )

                DropdownField("Jenis Kelamin", state.gender, viewModel::updateGender,
                    options = listOf("Laki-laki", "Perempuan"))

                DropdownField("Status", state.maritalStatus, viewModel::updateMaritalStatus,
                    options = listOf("Belum Menikah", "Menikah", "Cerai Hidup", "Cerai Mati"))

                DropdownField("Pekerjaan", state.occupation, viewModel::updateOccupation,
                    options = listOf("Pegawai Negeri", "Pegawai Swasta", "Wiraswasta", "Pelajar/Mahasiswa", "Lainnya"))

                Spacer(Modifier.height(8.dp))

                // ══════════ ALAMAT KTP ══════════
                SectionHeader("Informasi Alamat KTP")
                FormField("Alamat Lengkap", state.address, viewModel::updateAddress, placeholder = "Masukkan alamat sesuai KTP")
                FormField("Provinsi", state.provinsi, viewModel::updateProvinsi, placeholder = "Pilih Provinsi")
                FormField("Kota/Kabupaten", state.kotaKabupaten, viewModel::updateKotaKabupaten, placeholder = "Pilih Kota/Kabupaten")
                FormField("Kecamatan", state.kecamatan, viewModel::updateKecamatan, placeholder = "Pilih Kecamatan")
                FormField("Kelurahan", state.kelurahan, viewModel::updateKelurahan, placeholder = "Pilih Kelurahan")
                FormField("Kode Pos", state.kodePos, viewModel::updateKodePos, placeholder = "Masukkan Kode Pos",
                    keyboardType = KeyboardType.Number)

                Spacer(Modifier.height(8.dp))

                // ══════════ ALAMAT DOMISILI ══════════
                SectionHeader("Alamat Domisili")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.sameAsKtp,
                        onCheckedChange = viewModel::updateSameAsKtp,
                        colors = CheckboxDefaults.colors(checkedColor = Navy),
                    )
                    Text("Alamat domisili sama dengan alamat pada KTP", fontSize = 13.sp)
                }

                if (!state.sameAsKtp) {
                    FormField("Alamat Domisili", state.alamatDomisili, viewModel::updateAlamatDomisili, placeholder = "Masukkan alamat domisili")
                    FormField("Provinsi Domisili", state.provinsiDomisili, viewModel::updateProvinsiDomisili, placeholder = "Pilih Provinsi")
                    FormField("Kota/Kab Domisili", state.kotaKabupatenDomisili, viewModel::updateKotaKabupatenDomisili, placeholder = "Pilih Kota/Kabupaten")
                    FormField("Kecamatan Domisili", state.kecamatanDomisili, viewModel::updateKecamatanDomisili, placeholder = "Pilih Kecamatan")
                    FormField("Kelurahan Domisili", state.kelurahanDomisili, viewModel::updateKelurahanDomisili, placeholder = "Pilih Kelurahan")
                    FormField("Kode Pos Domisili", state.kodePosDomisili, viewModel::updateKodePosDomisili, placeholder = "Masukkan Kode Pos",
                        keyboardType = KeyboardType.Number)
                }

                Spacer(Modifier.height(16.dp))
            }

            // ── Bottom Buttons ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { viewModel.upload() },
                    enabled = !state.isUploading && !state.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        contentColor = Color.White,
                        disabledContainerColor = Navy.copy(alpha = 0.5f),
                        disabledContentColor = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Upload", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { viewModel.saveDraft() },
                    enabled = !state.isSaving && !state.isUploading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Navy,
                        disabledContentColor = Navy.copy(alpha = 0.5f),
                    ),
                    border = BorderStroke(1.5.dp, Navy),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Navy)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Simpan sebagai Draft", fontWeight = FontWeight.Bold, color = Navy)
                }
            }
        }
    }
}

// ── Reusable Components ──

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
    Spacer(Modifier.height(4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    required: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        Row {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (required) Text(" *", color = ErrorRed, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextHint, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderGray,
                focusedBorderColor = Navy,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
    }
}

/**
 * Tanggal read-only yg buka DatePicker saat diklik.
 * Value disimpan format DD/MM/YYYY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = { Text("Pilih tanggal lahir", color = TextHint, fontSize = 14.sp) },
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pilih tanggal", tint = Navy)
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = BorderGray,
                    disabledPlaceholderColor = TextHint,
                    disabledTrailingIconColor = Navy,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            // Overlay transparan utk capture click krn TextField disabled ga trigger click sendiri
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    ),
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Pilih $label", color = TextHint, fontSize = 14.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = Navy),
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onValueChange(option); expanded = false },
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun KtpPhotoBox(filePath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .background(DividerGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (filePath != null) {
            AsyncImage(
                model = File(filePath),
                contentDescription = "KTP Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
            )
        } else {
            Icon(Icons.Outlined.CameraAlt, null, tint = TextHint, modifier = Modifier.size(32.dp))
        }
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "ktp_temp_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out -> inputStream.copyTo(out) }
        file
    } catch (_: Exception) { null }
}

// ── Date helpers ──

private fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}

private fun parseDateToMillis(date: String): Long? {
    if (date.isBlank()) return null
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(date)?.time
    } catch (_: Exception) { null }
}