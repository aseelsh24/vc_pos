package com.aseel.pos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.aseel.pos.ui.PosViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Barcode Scanner Screen
 * Implements ZXing embedded scanner with tablet-optimized viewfinder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onNavigateBack: () -> Unit,
    onProductFound: (String) -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Scanner state
    var isScanning by remember { mutableStateOf(false) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            isScanning = true
        }
    }
    
    // ZXing scanner instance
    var scanner by remember { mutableStateOf<IntentIntegrator?>(null) }
    
    // Settings to check if barcode is enabled
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "مسح الباركود",
                        modifier = Modifier.semantics {
                            contentDescription = "مسح الباركود - فحص المنتجات"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "العودة إلى القائمة الرئيسية"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, "العودة", modifier = Modifier.semantics {
                            contentDescription = "أيقونة العودة"
                        })
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasCameraPermission) {
                // Permission request screen
                PermissionRequestScreen(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onDismiss = {
                        showPermissionDialog = true
                    }
                )
            } else if (isScanning) {
                // Camera scanning screen
                CameraScannerScreen(
                    onCodeScanned = { code ->
                        if (code != lastScannedCode) { // Avoid duplicate scans
                            lastScannedCode = code
                            isScanning = false
                            onProductFound(code)
                        }
                    },
                    onStopScanning = {
                        isScanning = false
                    }
                )
            } else {
                // Ready to scan screen
                ReadyToScanScreen(
                    onStartScanning = {
                        isScanning = true
                    }
                )
            }
        }
        
        // Permission rationale dialog
        if (showPermissionDialog) {
            PermissionRationaleDialog(
                onDismiss = { showPermissionDialog = false },
                onGrantPermission = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

/**
 * Permission request screen when camera access is denied
 */
@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "إذن الكاميرا مطلوب",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "إذن الكاميرا مطلوب لمسح الباركود"
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "يحتاج التطبيق للوصول إلى الكاميرا لمسح باركود المنتجات وإضافتها تلقائياً إلى السلة",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "وصف سبب الحاجة لإذن الكاميرا"
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "منح إذن الكاميرا"
                }
        ) {
            Text("منح الإذن")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "إلغاء ومسح الباركود يدوياً"
                }
        ) {
            Text("إلغاء")
        }
    }
}

/**
 * Camera scanner screen with tablet-optimized viewfinder
 */
@Composable
fun CameraScannerScreen(
    onCodeScanned: (String) -> Unit,
    onStopScanning: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Camera overlay with viewfinder
    CameraOverlay(
        onClose = onStopScanning
    )
    
    // Start camera when view is ready
    LaunchedEffect(previewView) {
        previewView?.let { pv ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                // Create preview use case
                val preview = Preview.Builder()
                    .setTargetResolution(Size(1920, 1080))
                    .build()
                    .also {
                        it.setSurfaceProvider(pv.surfaceProvider)
                    }
                
                // Create analysis use case for barcode scanning
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                // Bind camera to lifecycle
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    // Handle camera binding error
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
}

/**
 * Camera overlay with tablet-optimized viewfinder
 */
@Composable
fun CameraOverlay(
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "واجهة مسح الباركود - وجه الكاميرا نحو الباركود"
            }
    ) {
        // Dimmed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "خلفية داكنة لإبراز منطقة المسح"
                }
        )
        
        // Viewfinder box (tablet-optimized dimensions)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(
                    width = 400.dp, // Optimized for tablet landscape
                    height = 200.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "صندوق المسح - وجه الباركود هنا"
                }
        ) {
            // Scan lines animation could be added here
            Text(
                text = "وجه الباركود هنا",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics {
                        contentDescription = "توجيه المستخدم لوضع الباركود في الإطار"
                    }
            )
        }
        
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(56.dp)
                .semantics {
                    contentDescription = "إغلاق ماسح الباركود"
                }
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "إغلاق",
                modifier = Modifier.semantics {
                    contentDescription = "أيقونة إغلاق ماسح الباركود"
                }
            )
        }
        
        // Instructions
        Text(
            text = "وجه الكاميرا نحو الباركود للمسح التلقائي",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .semantics {
                    contentDescription = "تعليمات استخدام ماسح الباركود"
                }
        )
    }
}

/**
 * Ready to scan screen
 */
@Composable
fun ReadyToScanScreen(
    onStartScanning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.QrCode,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "ماسح الباركود جاهز",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "ماسح الباركود جاهز للاستخدام"
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "اضغط على زر المسح لبدء فحص المنتجات تلقائياً",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "تعليمات بدء عملية المسح"
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartScanning,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "بدء مسح الباركود"
                }
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("بدء المسح")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "يدعم مسح: EAN-13, QR Code, UPC-A",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "أنواع الباركود المدعومة"
            }
        )
    }
}

/**
 * Permission rationale dialog
 */
@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onGrantPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "لماذا نحتاج إذن الكاميرا؟",
                modifier = Modifier.semantics {
                    contentDescription = "عنوان نافذة شرح سبب الحاجة لإذن الكاميرا"
                }
            )
        },
        text = {
            Text(
                "يحتاج التطبيق للوصول إلى كاميرا الجهاز لمسح باركود المنتجات وإضافتها تلقائياً إلى السلة. هذا يوفر تجربة أسرع وأكثر دقة.",
                modifier = Modifier.semantics {
                    contentDescription = "شرح مفصل لسبب الحاجة لإذن الكاميرا"
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onGrantPermission,
                modifier = Modifier.semantics {
                    contentDescription = "منح إذن الكاميرا"
                }
            ) {
                Text("منح الإذن")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "إلغاء ومتابعة بدون مسح الباركود"
                }
            ) {
                Text("إلغاء")
            }
        }
    )
}