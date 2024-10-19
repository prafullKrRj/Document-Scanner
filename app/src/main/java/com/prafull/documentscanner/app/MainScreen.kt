package com.prafull.documentscanner.app

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(viewModel: MainViewModel) {
    val activity = LocalContext.current as Activity
    val createdDocuments by viewModel.createdDocuments.collectAsState()
    val savePdf =
        rememberLauncherForActivityResult(contract = CreateDocument("application/pdf")) { uri ->
            if (uri != null) {
                val contentResolver = activity.contentResolver
                viewModel.currUri?.let { scannedPdfUri ->
                    try {
                        contentResolver.openInputStream(scannedPdfUri)?.use { inputStream ->
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        viewModel.addDocument(uri)
                        showToast(activity, "PDF saved")
                    } catch (e: Exception) {
                        showToast(activity, "Failed to save PDF")
                    }
                }
            }
        }
    Scaffold(Modifier.fillMaxSize(), floatingActionButton = {
        NewPdfButton(activity = activity, viewModel = viewModel) {
            val fileName = "Document_${LocalDateTime.now()}.pdf"
            savePdf.launch(fileName)
        }
    }, topBar = {
        TopAppBar(title = {
            Text(text = "Document Scanner")
        })
    }) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues), contentPadding = PaddingValues(12.dp)) {
            items(createdDocuments) { document ->
                Text(text = document.name)
            }
        }
    }
}

fun showToast(activity: Activity, message: String) {
    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun NewPdfButton(activity: Activity, viewModel: MainViewModel, launchSavePdf: () -> Unit) {
    val options = GmsDocumentScannerOptions.Builder().setScannerMode(SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true).setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .build()
    val scanner = GmsDocumentScanning.getClient(options)
    val scannerResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) { activityResult: ActivityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
                result?.pdf?.uri?.let { uri ->
                    viewModel.currUri = uri
                    launchSavePdf()
                }
                Toast.makeText(activity, "Document scanned", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Failed to scan document", Toast.LENGTH_SHORT).show()
            }
        }
    FloatingActionButton(onClick = {
        scanner.getStartScanIntent(activity).addOnSuccessListener { intentSender ->
            scannerResult.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to start scanner", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add pdf")
    }
}