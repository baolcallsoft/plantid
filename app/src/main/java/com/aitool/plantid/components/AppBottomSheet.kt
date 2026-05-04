package com.aitool.plantid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    containerColor: Color = Color.White,
    isDraggable: Boolean = true,
    showDragHandle: Boolean = false,
    windowInsets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            if (!isDraggable) {
                newValue == SheetValue.Expanded
            } else {
                true
            }
        }
    )
    val scope = rememberCoroutineScope()
    var isActuallyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(showSheet) {
        if (showSheet) {
            isActuallyVisible = true
            sheetState.show()
        } else {
            if (sheetState.isVisible) {
                scope.launch {
                    sheetState.hide()
                    isActuallyVisible = false
                }.join()
            } else {
                isActuallyVisible = false
            }
        }
    }

    if (isActuallyVisible) {
        val view = LocalView.current
        LaunchedEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.let { win ->
                WindowCompat.setDecorFitsSystemWindows(win, false)
                win.navigationBarColor = android.graphics.Color.WHITE

                val controller = WindowInsetsControllerCompat(win, win.decorView)
                controller.isAppearanceLightNavigationBars = true
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                if (isDraggable) {
                    scope.launch {
                        sheetState.hide()
                        isActuallyVisible = false
                        onDismiss()
                    }
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = containerColor,
            contentWindowInsets = { WindowInsets(top = 0.dp, bottom = 0.dp) },
            dragHandle = null

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isDraggable) {
                            Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures { change, _ ->
                                    change.consume()
                                }
                            }
                        } else Modifier
                    )
            ) {
                if (showDragHandle && isDraggable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 10.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 32.dp, height = 4.dp)
                                .background(
                                    color = Color.Gray.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }

                content()
            }
        }
    }
}