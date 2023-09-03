package io.github.caimucheng.leaf.ide.ui

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.github.caimucheng.leaf.common.component.LeafApp
import io.github.caimucheng.leaf.ide.R
import io.github.caimucheng.leaf.ide.viewmodel.CreateProjectUIIntent
import io.github.caimucheng.leaf.ide.viewmodel.CreateProjectUIState
import io.github.caimucheng.leaf.ide.viewmodel.CreateProjectViewModel
import io.github.caimucheng.leaf.plugin.model.Plugin

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectPage(
    pageNavController: NavHostController,
    viewModel: CreateProjectViewModel = viewModel()
) {
    LeafApp(
        title = stringResource(id = R.string.create_project),
        navigationIcon = {
            IconButton(onClick = {
                pageNavController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        content = { paddings ->
            var isLoading by rememberSaveable {
                mutableStateOf(true)
            }
            var plugins: List<Plugin> by remember {
                mutableStateOf(emptyList())
            }
            Crossfade(
                targetState = isLoading,
                label = "CrossfadeLoading",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
            ) {
                if (it) {
                    Loading()
                } else {
                    NewProjectList(plugins)
                }
            }

            val state by viewModel.state.collectAsState()
            when (state) {
                CreateProjectUIState.Default -> {}
                CreateProjectUIState.Loading -> {
                    isLoading = true
                }

                is CreateProjectUIState.UnLoading -> {
                    plugins = (state as CreateProjectUIState.UnLoading).plugins
                    isLoading = false
                }
            }

            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(key1 = lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event === ON_RESUME) {
                        viewModel.intent.trySend(CreateProjectUIIntent.Refresh)
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
        }
    )
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp),
            strokeWidth = 5.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewProjectList(plugins: List<Plugin>) {
    if (plugins.isEmpty()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (column) = createRefs()
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .constrainAs(column) {
                        centerTo(parent)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_plugin_project),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.download_from_leaf_flow),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp)
        ) {
            items(plugins.size) {
                val plugin = plugins[it]
                val pluginProject = plugin.project!!
                val resources = pluginProject.getResources()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(25.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    onClick = {},
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.padding(top = 10.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                painter = BitmapPainter(
                                    ImageBitmap.imageResource(
                                        resources,
                                        pluginProject.getDisplayedPictureResId()
                                    )
                                ),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                        Text(
                            text = resources.getString(pluginProject.getDisplayedTitleId()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}