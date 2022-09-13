/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.VibrationEffect.createOneShot
import android.os.VibrationEffect.createWaveform
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.prescription.ui.model.ScanScreenData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.BottomSheetAction
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScanScreen(
    mainNavController: NavController,
    scanViewModel: ScanPrescriptionViewModel
) {
    val context = LocalContext.current

    var camPermissionGranted by rememberSaveable { mutableStateOf(false) }

    val camPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            camPermissionGranted = it
        }

    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            camPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            camPermissionGranted = true
        }
    }

    var flashEnabled by remember { mutableStateOf(false) }

    val state by scanViewModel.screenState().collectAsState(ScanScreenData.defaultScreenState)
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    var cancelRequested by remember { mutableStateOf(false) }
    BackHandler(!cancelRequested && state.hasCodesToSave()) {
        cancelRequested = true
    }
    BackHandler(sheetState.isVisible) {
        coroutineScope.launch { sheetState.hide() }
    }

    if (cancelRequested && state.hasCodesToSave()) {
        SaveDialog(
            onDismissRequest = { cancelRequested = false },
            onCancel = { mainNavController.popBackStack() }
        )
    }

    val tracker = LocalAnalytics.current
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            SheetContent(
                onClickSave = {
                    scanViewModel.saveToDatabase()
                    tracker.trackSaveScannedPrescriptions()
                    mainNavController.popBackStack()
                }
            )
        }
    ) {
        Box {
            if (camPermissionGranted) {
                CameraView(
                    scanViewModel,
                    Modifier.fillMaxSize(),
                    flashEnabled = flashEnabled,
                    onFlashToggled = {
                        flashEnabled = it
                    }
                )
            } else {
                AccessDenied()
            }
            if (camPermissionGranted) {
                ScanOverlay(
                    enabled = !sheetState.isVisible && !cancelRequested,
                    flashEnabled = flashEnabled,
                    onFlashClick = { flashEnabled = it },
                    modifier = Modifier.fillMaxSize()
                )

                if (state.snackBar.shouldShow()) {
                    ActionBarButton(
                        state.snackBar,
                        onClick = { coroutineScope.launch { sheetState.show() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(PaddingDefaults.Medium)
                            .padding(bottom = PaddingDefaults.Medium)
                    )
                }
            }
        }
    }

    HapticAndAudibleFeedback()
}

@Composable
private fun SheetContent(
    onClickSave: () -> Unit
) {
    SpacerMedium()
    Text(
        stringResource(R.string.cam_next_sheet_how_to_proceed),
        style = AppTheme.typography.body1l,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium)
    )
    SpacerSmall()
    BottomSheetAction(
        enabled = false,
        icon = { Icon(Icons.Rounded.ShoppingBag, null) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.cam_next_sheet_order_now_title))
                SpacerSmall()
                Surface(
                    color = AppTheme.colors.primary100,
                    contentColor = AppTheme.colors.primary600,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cam_next_sheet_available_soon), Modifier.padding(horizontal = PaddingDefaults.Small, vertical = 2.dp))
                }
            }
        },
        info = { Text(stringResource(R.string.cam_next_sheet_order_now_info)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        // TODO - currently disabled; teaser for prescription orders without eGK
    }
    BottomSheetAction(
        icon = Icons.Rounded.SaveAlt,
        title = stringResource(R.string.cam_next_sheet_order_later_title),
        info = stringResource(R.string.cam_next_sheet_order_later_info),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClickSave
    )
    Spacer(Modifier.navigationBarsPadding())
}

@Composable
private fun AccessDenied() {
    Surface(
        color = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .testTag("camera/disallowed")
        ) {
            TopBar(
                flashEnabled = false,
                onFlashClick = {}
            )
            Spacer(Modifier.weight(0.4f))
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.ErrorOutline,
                    null,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    stringResource(R.string.cam_access_denied_headline),
                    style = AppTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.cam_access_denied_description),
                    style = AppTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.weight(0.6f))
        }
    }
}

@Composable
private fun HapticAndAudibleFeedback(scanVM: ScanPrescriptionViewModel = viewModel()) {
    val context = LocalContext.current

    val toneGenerator = remember {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            ToneGenerator(AudioManager.STREAM_ACCESSIBILITY, 100)
        } else {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        }
    }

    LaunchedEffect(scanVM.vibration) {
        scanVM.vibration.collect {
            val vibrator = if (VERSION.SDK_INT >= VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("deprecation")
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrate(vibrator, it)
            beep(toneGenerator, it)
        }
    }
    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }
}

@Composable
private fun SaveDialog(
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit
) =
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                stringResource(R.string.cam_cancel_msg)
            )
        },
        buttons = {
            TextButton(
                onClick = { onDismissRequest() },
                modifier = Modifier.testTag("camera/saveDialog/dismissDialogButton")
            ) {
                Text(stringResource(R.string.cam_cancel_resume).uppercase(Locale.getDefault()))
            }
            TextButton(onClick = { onCancel() }, modifier = Modifier.testTag("camera/saveDialog/saveButton")) {
                Text(stringResource(R.string.cam_cancel_ok).uppercase(Locale.getDefault()))
            }
        }
    )

private fun beep(toneGenerator: ToneGenerator, pattern: ScanScreenData.VibrationPattern) {
    @Suppress("NON_EXHAUSTIVE_WHEN")
    when (pattern) {
        ScanScreenData.VibrationPattern.Saved -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_PROMPT,
            1000
        )
        ScanScreenData.VibrationPattern.Error -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_NACK,
            1000
        )
        else -> {}
    }
}

private fun vibrate(vibrator: Vibrator, pattern: ScanScreenData.VibrationPattern) {
    if (pattern == ScanScreenData.VibrationPattern.None) {
        return
    }

    val canVibrate: Boolean = vibrator.hasVibrator()
    if (canVibrate) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            vibrator.vibrate(
                when (pattern) {
                    ScanScreenData.VibrationPattern.Focused ->
                        createOneShot(100L, 100)
                    ScanScreenData.VibrationPattern.Saved ->
                        createOneShot(300L, 100)
                    ScanScreenData.VibrationPattern.Error ->
                        createWaveform(
                            longArrayOf(100, 100, 300),
                            intArrayOf(
                                100,
                                0,
                                100
                            ),
                            -1
                        )
                    ScanScreenData.VibrationPattern.None -> error("Should not be reached")
                }
            )
        } else {
            @Suppress("DEPRECATION")
            when (pattern) {
                ScanScreenData.VibrationPattern.Focused ->
                    vibrator.vibrate(longArrayOf(0L, 100L), -1)
                ScanScreenData.VibrationPattern.Saved ->
                    vibrator.vibrate(longArrayOf(0L, 300L), -1)
                ScanScreenData.VibrationPattern.Error ->
                    vibrator.vibrate(longArrayOf(0L, 100L, 100L, 300L), -1)
                ScanScreenData.VibrationPattern.None -> error("Should not be reached")
            }
        }
    }
}

@Composable
private fun ActionBarButton(
    data: ScanScreenData.ActionBar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) =
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = AppTheme.colors.primary700,
            backgroundColor = AppTheme.colors.neutral100
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("camera/nextButton")
    ) {
        Text(
            annotatedPluralsResource(
                R.plurals.cam_next_with,
                data.totalNrOfPrescriptions,
                AnnotatedString(data.totalNrOfPrescriptions.toString())
            )
        )
    }

@Composable
private fun InfoCard(
    info: ScanScreenData.Info,
    modifier: Modifier
) =
    Card(
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
        modifier = modifier.padding(horizontal = PaddingDefaults.Large)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
        ) {
            val view = LocalView.current

            val scanning = stringResource(R.string.cam_info_scanning)
            val invalid = stringResource(R.string.cam_info_invalid)
            val duplicated = stringResource(R.string.cam_info_duplicated)
            val detected = if (info is ScanScreenData.Info.Scanned) {
                annotatedPluralsResource(
                    R.plurals.cam_info_detected,
                    info.nr,
                    annotatedStringBold(info.nr.toString())
                )
            } else {
                buildAnnotatedString { }
            }

            when (info) {
                ScanScreenData.Info.Focus -> Text(
                    scanning,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.subtitle1
                )
                ScanScreenData.Info.ErrorNotValid -> InfoError(invalid)
                ScanScreenData.Info.ErrorDuplicated -> InfoError(duplicated)
                is ScanScreenData.Info.Scanned -> Text(
                    detected,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.subtitle1
                )
            }

            val acc = when (info) {
                ScanScreenData.Info.Focus -> scanning
                ScanScreenData.Info.ErrorNotValid -> invalid
                ScanScreenData.Info.ErrorDuplicated -> duplicated
                is ScanScreenData.Info.Scanned -> detected.text
            }

            DisposableEffect(view, acc) {
                view.announceForAccessibility(acc)

                onDispose {}
            }
        }
    }

@Composable
private fun InfoError(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
        Spacer4()
        Text(
            text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.subtitle1
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraView(
    scanVM: ScanPrescriptionViewModel = viewModel(),
    modifier: Modifier,
    flashEnabled: Boolean,
    onFlashToggled: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camPreviewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(camera, flashEnabled) {
        withContext(Dispatchers.Main) {
            camera?.apply {
                if (cameraInfo.torchState.value == TorchState.ON != flashEnabled) {
                    cameraControl.enableTorch(flashEnabled)
                }
            }
        }
    }

    SideEffect {
        camera?.apply {
            if (cameraInfo.torchState.value == TorchState.ON != flashEnabled) {
                onFlashToggled(flashEnabled)
            }
        }
    }

    DisposableEffect(camPreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1024, 768))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(Executors.newSingleThreadExecutor(), scanVM.scanner)
                    }

                preview.setSurfaceProvider(camPreviewView.surfaceProvider)

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            cameraProviderFuture.cancel(true)
        }
    }

    AnimatedVisibility(
        modifier = Modifier.background(Color.Black),
        visibleState = remember { MutableTransitionState(false) }.apply { targetState = true },
        exit = fadeOut(),
        enter = fadeIn(animationSpec = tween(800, 200))
    ) {
        AndroidView(
            {
                camPreviewView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                camPreviewView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    scanVM.processor.onLayoutChange(
                        Size(
                            camPreviewView.width,
                            camPreviewView.height
                        )
                    )
                }

                camPreviewView
            },
            modifier = modifier
        )
    }
}

@Composable
private fun TopBar(
    flashEnabled: Boolean,
    onFlashClick: (Boolean) -> Unit
) {
    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher

    Surface(
        color = Color.Unspecified,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val accCancel = stringResource(R.string.cam_acc_cancel)
            val accTorch = stringResource(R.string.cam_acc_torch)

            IconButton(
                onClick = { backPressDispatcher.onBackPressed() },
                modifier = Modifier
                    .testTag("camera/closeButton")
                    .semantics { contentDescription = accCancel }
            ) {
                Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            IconToggleButton(
                checked = flashEnabled,
                onCheckedChange = onFlashClick,
                modifier = Modifier
                    .testTag("camera/flashToggle")
                    .semantics { contentDescription = accTorch }
            ) {
                val ic = if (flashEnabled) {
                    Icons.Rounded.FlashOn
                } else {
                    Icons.Rounded.FlashOff
                }
                Icon(ic, null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ScanOverlay(
    enabled: Boolean,
    flashEnabled: Boolean,
    onFlashClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scanVM: ScanPrescriptionViewModel = viewModel()
) {
    var points by remember { mutableStateOf(FloatArray(8)) }

    var state by remember { mutableStateOf(ScanScreenData.defaultOverlayState) }

    LaunchedEffect(enabled) {
        if (enabled) {
            scanVM.scanOverlayState().collect {
                state = it
                it.area?.let {
                    points = it
                }
            }
        } else {
            state = ScanScreenData.defaultOverlayState
        }
    }

    val fillColor =
        when (state.state) {
            ScanScreenData.ScanState.Hold -> AppTheme.colors.scanOverlayHoldFill
            ScanScreenData.ScanState.Save -> AppTheme.colors.scanOverlaySavedFill
            ScanScreenData.ScanState.Error -> AppTheme.colors.scanOverlayErrorFill
            ScanScreenData.ScanState.Final -> AppTheme.colors.scanOverlayHoldFill
        }

    val outlineColor =
        when (state.state) {
            ScanScreenData.ScanState.Hold -> AppTheme.colors.scanOverlayHoldOutline
            ScanScreenData.ScanState.Save -> AppTheme.colors.scanOverlaySavedOutline
            ScanScreenData.ScanState.Error -> AppTheme.colors.scanOverlayErrorOutline
            ScanScreenData.ScanState.Final -> AppTheme.colors.scanOverlayHoldOutline
        }

    val showAimAid = state.area == null && enabled

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (state.area != null) {
                val p = Path().apply {
                    moveTo(points[0], points[1])
                    lineTo(points[2], points[3])
                    lineTo(points[4], points[5])
                    lineTo(points[6], points[7])
                    close()
                }

                // fill
                drawPath(p, SolidColor(fillColor))

                // outline
                drawPath(
                    p,
                    SolidColor(outlineColor),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(4.dp.toPx())
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopBar(
                flashEnabled = flashEnabled,
                onFlashClick = onFlashClick
            )
            Spacer(modifier = Modifier.size(24.dp))
            InfoCard(
                state.info,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            if (showAimAid) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val infTransition = rememberInfiniteTransition()
                    val scale by infTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Icon(
                        Icons.Rounded.CropFree,
                        null,
                        tint = AppTheme.colors.yellow500,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale)
                            .align(
                                Alignment { size, space, _ ->
                                    IntOffset(
                                        space.width / 2 - size.width / 2,
                                        space.height / 3 - size.height / 2
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}
