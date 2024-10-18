@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.android_practice_dpo.main.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.android_practice_dpo.R
import com.example.android_practice_dpo.main.LoginActivity
import com.example.android_practice_dpo.main.adapter.DownloadWorker
import com.example.android_practice_dpo.main.data.Photo
import com.example.android_practice_dpo.main.data.PhotoCollection
import com.example.android_practice_dpo.main.data.PhotoDescription
import com.example.android_practice_dpo.main.data.UnsplashUser
import com.example.android_practice_dpo.main.utils.toNonNull
import com.example.android_practice_dpo.main.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val ACCESS_TOKEN = "access_token"
private const val COLLECTION_ID = "collectionId"
private const val PHOTO_PATH = "photo_path"
private const val PHOTO_ID = "photo_id"
private const val PHOTO_QUERY = "photo_query"
private const val DOWNLOAD_PHOTO = "download_photo"
private const val IMAGE_URI = "image_uri"
private const val SEARCH_QUERY = "searchQuery"

@AndroidEntryPoint
class MainFragment : Fragment() {

    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var workManager: WorkManager
    private val viewModel: MainViewModel by viewModels()
    private lateinit var photoDescriptionStateFlow: State<PhotoDescription?>
    private lateinit var errorMessageFlow: State<String?>
    private lateinit var errorUserFlow: State<String?>
    private lateinit var userInfo: State<UnsplashUser?>
    private lateinit var photos: LazyPagingItems<Photo>


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        var accessToken = activity?.intent?.extras?.getString(ACCESS_TOKEN)
        Log.d("UNSPLASH_DEBUG", "Получаем TOKEN из Intent " + accessToken.toString())
        if (sharedPreferences.contains(ACCESS_TOKEN)) {
            accessToken = sharedPreferences.getString(ACCESS_TOKEN, null)
            viewModel.refreshToken(accessToken)
            Log.d("UNSPLASH_DEBUG", "Получаем TOKEN из shared " + accessToken.toString())
        }
        view.setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            photos = viewModel.photosPagingFlow.collectAsLazyPagingItems()
            val collections = viewModel.photoCollectionFlow.collectAsLazyPagingItems()
            userInfo = viewModel.userInfoFlow.collectAsState()
            errorUserFlow = viewModel.errorUserFlow.collectAsState()
            photoDescriptionStateFlow = viewModel.photoDescriptionFlow.collectAsState()
            errorMessageFlow = viewModel.errorMessageFlow.collectAsState()

            val navigationScreens = listOf(
                NavigationRoutes.Photos,
                NavigationRoutes.Collections,
                NavigationRoutes.Profile
            )

            val photoWindowState = remember {
                mutableStateOf(PhotoWindow.Item(photo = null, false))
            }

            val downloadState = workManager.getWorkInfosForUniqueWorkLiveData(DOWNLOAD_PHOTO)
                .observeAsState()
                .value

            var requestIdState by remember {
                mutableStateOf<String?>(null)
            }

            val info = remember(key1 = downloadState, key2 = requestIdState) {
                downloadState?.find { it.id.toString() == requestIdState }
            }

            var imageUri = remember {
                info?.outputData?.getString(IMAGE_URI)
            }

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(key1 = info) {
                imageUri = info?.outputData?.getString(IMAGE_URI)
                scope.launch {
                    if (imageUri != null) {
                        val action = snackbarHostState.showSnackbar(getString(R.string.photo_downloaded_snackbar),
                            duration = SnackbarDuration.Long,
                            actionLabel = getString(R.string.open_photo)
                        )
                        if (action == SnackbarResult.ActionPerformed) {
                            val intent =
                                Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.setDataAndType(imageUri!!.toUri(), "image/*")
                            startActivity(Intent.createChooser(intent, getString(R.string.open_photo_with)))
                        }
                    }
                }
            }

            val errorState by remember {
                errorMessageFlow
            }

            LaunchedEffect(key1 = errorState) {
                if (errorState != null) Toast.makeText(
                    context,
                    errorMessageFlow.value,
                    Toast.LENGTH_LONG
                ).show()
            }

            LaunchedEffect(key1 = imageUri) {
                if (downloadState != null) snackbarHostState.showSnackbar(
                    "$imageUri", duration = SnackbarDuration.Long
                )
            }


            MaterialTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        BottomNavigation(
                            backgroundColor = Color.LightGray
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            navigationScreens.forEach { screen ->
                                BottomNavigationItem(
                                    icon = {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = screen.icon),
                                            contentDescription = screen.title,
                                            tint = Color.Black
                                        )
                                    },
                                    label = { Text(screen.title, color = Color.Black) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        if (screen.route == NavigationRoutes.Collections.route) viewModel.clearCollectionsPhotoPagedSource()
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = NavigationRoutes.Photos.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(
                            route = NavigationRoutes.Photos.route
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                PhotosScreen(
                                    photos = photos,
                                    onPhotoClick = { photo ->
                                        photoWindowState.value = PhotoWindow.Item(photo, true)
                                    }
                                )
                                PhotoPopUpWindow(photoWindowState = photoWindowState.value,
                                    onDismissClick = {
                                        photoWindowState.value = PhotoWindow.Item(null, false)
                                        viewModel.clearState()
                                    },
                                    onDownloadClick = { requestId -> requestIdState = requestId }
                                )
                                FloatingActionButton(onClick = {
                                    navController.navigate(NavigationRoutes.SearchQuery.route) {
                                        popUpTo(NavigationRoutes.Photos.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }, modifier = Modifier.padding(20.dp)) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.icon_search),
                                        contentDescription = getString(R.string.search_photos),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                        composable(NavigationRoutes.SearchQuery.route) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var searchQuery by remember {
                                    mutableStateOf("")
                                }
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(0.9f),
                                    maxLines = 1,
                                    shape = RoundedCornerShape(9.dp)
                                )
                                IconButton(onClick = {
                                    navController.navigate(NavigationRoutes.SearchQuery.buildSearchQuery(searchQuery)) {
                                        popUpTo(NavigationRoutes.SearchQuery.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.icon_search),
                                        contentDescription = getString(R.string.search_photos),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                        composable(route = NavigationRoutes.SearchQuery.title,
                            arguments = listOf(
                                navArgument(SEARCH_QUERY) {
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            val searchQuery = it.arguments?.getString(SEARCH_QUERY) ?: ""
                            viewModel.searchPhotosPagedSource(searchQuery)
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                SearchScreen()
                            }
                        }
                        composable(NavigationRoutes.Collections.route) {
                            CollectionsScreen(
                                collections = collections,
                                onCollectionClick = {
                                    navController.navigate("collections/photo/${it.id}") {
                                        popUpTo(NavigationRoutes.Collections.route) {
                                            saveState = false
                                            inclusive = true
                                        }
                                        restoreState = false
                                    }
                                })
                        }
                        composable(route = NavigationRoutes.Profile.route) {
                            viewModel.getUserInfo()
                            ProfileScreen()
                        }
                        composable(
                            route = "single_photo",
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = "https://unsplash.com/photos/{id}"
                                    action = Intent.ACTION_VIEW
                                }
                            ),
                            arguments = listOf(
                                navArgument("id") {
                                    type = NavType.StringType
                                    defaultValue = null
                                    nullable = true
                                }
                            )
                        ) {
                            val intentPhotoId: String? = it.arguments?.getString("id")
                            LaunchedEffect(key1 = intentPhotoId) {
                                intentPhotoId?.let { it1 -> viewModel.getPhotoDescription(it1) }
                            }
                            IntentPhotoScreen(
                                onLikeClick = {},
                                onDownloadClick = {})
                        }
                        composable(route = "collections/photo/{collectionId}",
                            arguments = listOf(
                                navArgument(COLLECTION_ID) {
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            val collectionId = it.arguments?.getString(COLLECTION_ID) ?: ""
                            LaunchedEffect(key1 = collectionId) {
                                viewModel.getPagedCollectionsPhoto(collectionId)
                            }
                            val collectionsPhotoPagedSource =
                                viewModel.collectionsPhotoPagedSource?.collectAsLazyPagingItems()

                            collectionsPhotoPagedSource?.let { photos ->
                                PhotosScreen(
                                    photos = photos,
                                    onPhotoClick = { photo ->
                                        photoWindowState.value = PhotoWindow.Item(photo, true)
                                    }
                                )
                            }
                            PhotoPopUpWindow(photoWindowState = photoWindowState.value,
                                onDismissClick = {
                                    photoWindowState.value = PhotoWindow.Item(null, false)
                                    viewModel.clearState()
                                },
                                onDownloadClick = { requestId -> requestIdState = requestId }
                            )
                        }
                    }
                }
            }
        }
        return view
    }

    @Composable
    fun SearchScreen() {
        val searchResults = viewModel.searchPhotoPagedSource?.collectAsLazyPagingItems()
        searchResults?.let {
            LaunchedEffect(key1 = searchResults.loadState) {
                if (searchResults.loadState.refresh is LoadState.Error) {
                    Toast.makeText(
                        context,
                        "Error: " + (searchResults.loadState.refresh as LoadState.Error).error.message,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(
                        "UNSPLASH_DEBUG",
                        (searchResults.loadState.refresh as LoadState.Error).error.localizedMessage as String
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (searchResults.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 9.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                        columns = GridCells.Adaptive(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        items(
                            count = searchResults.itemCount,
                            key = searchResults.itemKey(),
                            contentType = searchResults.itemContentType(
                            )
                        ) { index ->
                            val item = searchResults[index]
                            if (item != null) {
                                PhotoItem(photo = item, onPhotoClick = {})
                            }
                        }
                        item {
                            if (searchResults.loadState.append is LoadState.Loading) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PhotosScreen(
        photos: LazyPagingItems<Photo>,
        onPhotoClick: (Photo) -> Unit,
    ) {
        val context = LocalContext.current
        LaunchedEffect(key1 = photos.loadState) {
            if (photos.loadState.refresh is LoadState.Error) {
                Toast.makeText(
                    context,
                    "Error: " + (photos.loadState.refresh as LoadState.Error).error.message,
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    "UNSPLASH_DEBUG",
                    (photos.loadState.refresh as LoadState.Error).error.localizedMessage as String
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (photos.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 9.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    columns = GridCells.Adaptive(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(
                        count = photos.itemCount,
                        key = photos.itemKey(),
                        contentType = photos.itemContentType(
                        )
                    ) { index ->
                        val item = photos[index]
                        if (item != null) {
                            PhotoItem(
                                photo = item,
                                onPhotoClick = { onPhotoClick(item) }
                            )
                        }
                    }
                    item {
                        if (photos.loadState.append is LoadState.Loading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun PhotoItem(
        photo: Photo,
        onPhotoClick: (Photo) -> Unit,
    ) {
        val scope = rememberCoroutineScope()

        var colorState by remember {
            mutableStateOf(if (photo.likedByUser) Color.Red else Color.Black)
        }

        var likeCountState by remember {
            mutableStateOf(photo.likes)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onPhotoClick(photo)
                },
            shape = RoundedCornerShape(17.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                GlideImage(
                    model = photo.urls.small,
                    contentDescription = photo.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .background(color = Color.Transparent)
                        .fillMaxWidth()
                        .padding(7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier
                            .padding(7.dp)
                            .alpha(0.7f),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GlideImage(
                                model = photo.user.profileImage.medium,
                                contentDescription = photo.description,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(shape = CircleShape)
                            )
                            Text(
                                text = photo.user.name,
                                color = Color.Black,
                                modifier = Modifier.padding(7.dp),
                                maxLines = 2,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier
                            .padding(7.dp)
                            .alpha(0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                scope.launch {
                                    if (viewModel.likeUnlikePhoto(photo, requireContext())) {
                                        Log.d("UNSPLASH_DEBUG", "Composable success")
                                        colorState =
                                            if (photo.likedByUser) {
                                                Color.Black
                                            } else {
                                                Color.Red
                                            }
                                        likeCountState =
                                            if (photo.likedByUser) photo.likes-- else photo.likes++
                                    } else {
                                        Log.d("UNSPLASH_DEBUG", "Composable failed")
                                    }
                                }
                            }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        id = R.drawable.icon_favorite
                                    ),
                                    contentDescription = getString(R.string.like_photo),
                                    modifier = Modifier.size(24.dp),
                                    tint = colorState
                                )
                            }
                            Text(
                                text = photo.likes.toString(), maxLines = 1, softWrap = false,
                                color = Color.Black,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun PhotoPopUpWindow(
        photoWindowState: PhotoWindow.Item,
        onDismissClick: (Boolean) -> Unit,
        onDownloadClick: (String) -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        if (photoWindowState.showDialog && photoWindowState.photo != null) {
            val photoItem = photoWindowState.photo

            viewModel.getPhotoDescription(photoItem.id)
            LaunchedEffect(key1 = errorMessageFlow) {
                if (errorMessageFlow.value != null) {
                    Toast.makeText(
                        context,
                        "Error: ${errorMessageFlow.value}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(
                        "UNSPLASH_DEBUG", errorMessageFlow.value.toString()
                    )
                }
            }
            Dialog(
                onDismissRequest = { onDismissClick(false) },
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.99f)
                        .fillMaxHeight(0.99f),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Card(
                            shape = RoundedCornerShape(17.dp),
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { viewModel.getPhotoDescription(photoItem.id) },
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                GlideImage(
                                    model = photoWindowState.photo.urls.regular,
                                    contentDescription = photoWindowState.photo.description,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.5f)
                                )
                                if (photoDescriptionStateFlow.value == null) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    val response = photoDescriptionStateFlow.value
                                    val photo = response!!.toNonNull()
                                    val colorState by remember {
                                        mutableStateOf(if (photoItem.likedByUser) Color.Red else Color.Black)
                                    }
                                    val tags: String = if (photo.tags.isEmpty()) {
                                        getString(R.string.no_tag_data)
                                    } else {
                                        val listSize =
                                            if (photo.tags.size < 3) photo.tags.size else 3
                                        val tagList = photo.tags.take(listSize)
                                        val tagsStringBuilder = StringBuilder()
                                        tagList.forEach { tag ->
                                            tagsStringBuilder.append("#${tag.title} ")
                                        }
                                        tagsStringBuilder.toString()
                                    }
                                    val location =
                                        if (photo.location.position!!.latitude == 0.0 && photo.location.position.longitude == 0.0) {
                                            getString(R.string.no_location_data)
                                        } else {
                                            "${photo.location.position.latitude} / ${photo.location.position.longitude}"
                                        }
                                    val enableLocation = location != getString(R.string.no_location_data)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .padding(9.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = getString(R.string.author, photo.user.name),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = getString(R.string.make, photo.exif.make),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = getString(R.string.model, photo.exif.model),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = getString(R.string.exposure_time, photo.exif.exposureTime),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = getString(R.string.location, location),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = getString(R.string.tags, tags),
                                                fontSize = 9.sp
                                            )
                                            Row(modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(7.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    IconButton(onClick = {
                                                        val requestId = downloadPhoto(
                                                            photoId = photo.id,
                                                            urlInput = photo.urls.raw
                                                        )
                                                        scope.launch {
                                                            viewModel.downloadPhotoCounter(photo.id)
                                                        }
                                                        onDownloadClick(requestId)
                                                    }) {
                                                        Icon(
                                                            imageVector = ImageVector.vectorResource(
                                                                id = R.drawable.icon_download
                                                            ),
                                                            contentDescription = getString(R.string.download_this_photo),
                                                            modifier = Modifier.size(24.dp),
                                                            tint = Color.Black
                                                        )
                                                    }
                                                    Text(
                                                        text = photo.downloads.toString(),
                                                        maxLines = 1,
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(7.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(
                                                        imageVector = ImageVector.vectorResource(
                                                            id = R.drawable.icon_favorite
                                                        ),
                                                        contentDescription = getString(R.string.like_photo),
                                                        modifier = Modifier.size(24.dp),
                                                        tint = colorState
                                                    )
                                                    Text(
                                                        text = photoItem.likes.toString(),
                                                        maxLines = 1,
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(7.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    IconButton(onClick = {
                                                        sharePhotoUrl(photo.id)
                                                    }) {
                                                        Icon(
                                                            imageVector = ImageVector.vectorResource(
                                                                id = R.drawable.icon_share
                                                            ),
                                                            contentDescription = getString(R.string.share_this_photo),
                                                            modifier = Modifier.size(24.dp),
                                                            tint = Color.Black
                                                        )
                                                    }
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(7.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            openPhotoLocation(
                                                                photo.location.position.latitude!!,
                                                                photo.location.position.longitude!!
                                                            )
                                                        },
                                                        enabled = enableLocation
                                                    ) {
                                                        Icon(
                                                            imageVector = ImageVector.vectorResource(
                                                                id = R.drawable.icon_location
                                                            ),
                                                            contentDescription = getString(R.string.view_geolocation),
                                                            modifier = Modifier.size(24.dp),
                                                            tint = Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                onDismissClick(false)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = getString(R.string.close_photo_description),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(24.dp)
                                        .background(Color.White),
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CollectionsScreen(
        collections: LazyPagingItems<PhotoCollection>,
        onCollectionClick: (PhotoCollection) -> Unit
    ) {
        val context = LocalContext.current
        LaunchedEffect(key1 = collections.loadState) {
            if (collections.loadState.refresh is LoadState.Error) {
                Toast.makeText(
                    context,
                    "Error: " + (collections.loadState.refresh as LoadState.Error).error.message,
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    "UNSPLASH_DEBUG",
                    (collections.loadState.refresh as LoadState.Error).error.localizedMessage as String
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (collections.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 9.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    columns = GridCells.Adaptive(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(
                        count = collections.itemCount,
                        key = collections.itemKey(),
                        contentType = collections.itemContentType(
                        )
                    ) { index ->
                        val item = collections[index]
                        if (item != null) {
                            CollectionItem(collection = item,
                                onCollectionClick = {
                                    onCollectionClick(item)
                                })
                        }
                    }
                    item {
                        if (collections.loadState.append is LoadState.Loading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun CollectionItem(
        collection: PhotoCollection,
        onCollectionClick: (PhotoCollection) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onCollectionClick(collection)
                },
            shape = RoundedCornerShape(17.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                GlideImage(
                    model = collection.coverPhoto.urls.regular,
                    contentDescription = getString(R.string.collection_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .background(color = Color.Transparent)
                        .fillMaxWidth()
                        .padding(7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier
                            .padding(7.dp)
                            .alpha(0.7f),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GlideImage(
                                model = collection.user.profileImage.medium,
                                contentDescription = getString(R.string.profile_photo),
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(shape = CircleShape)
                            )
                            Text(
                                text = collection.user.name,
                                color = Color.Black,
                                modifier = Modifier.padding(7.dp),
                                maxLines = 2,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier
                            .padding(7.dp)
                            .alpha(0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = R.drawable.icon_favorite
                                ),
                                contentDescription = getString(R.string.like_photo),
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                            Text(
                                text = collection.coverPhoto.likes.toString(),
                                maxLines = 1,
                                softWrap = false,
                                color = Color.Black,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun ProfileScreen() {
        var openLogoutDialog by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(key1 = errorUserFlow) {
            if (errorUserFlow.value != null) {
                Toast.makeText(
                    context,
                    "Error: ${errorUserFlow.value}",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    "UNSPLASH_DEBUG", errorUserFlow.value.toString()
                )
            }
        }
        val user: UnsplashUser?
        if (userInfo.value == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else {
            user = userInfo.value!!.toNonNull()
            LaunchedEffect(key1 = user.username) {
                viewModel.getLikedPhotoPagedSource(username = user.username)

            }
            val likedPhotosPagingSource =
                viewModel.likedPhotoPagedSource?.collectAsLazyPagingItems()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Text(
                    text = getString(R.string.user_profile_info),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(text = getString(R.string.username, user.username))
                Text(text = getString(R.string.first_name, user.firstName))
                Text(text = getString(R.string.last_name, user.lastName))
                Text(text = getString(R.string.user_location, user.location))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { openLogoutDialog = true }) {
                        Text(text = getString(R.string.logout))
                    }
                }
                LogoutDialog(openLogoutDialog, closeDialog = { close -> openLogoutDialog = close })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.icon_download
                            ),
                            contentDescription = getString(R.string.downloaded_photos),
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                        Text(
                            text = user.downloads.toString(),
                            maxLines = 1,
                            color = Color.Black,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.icon_favorite
                            ),
                            contentDescription = getString(R.string.liked_photos),
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                        Text(
                            text = user.totalLikes.toString(),
                            maxLines = 1,
                            color = Color.Black,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                    }
                }
                likedPhotosPagingSource?.let {
                    LaunchedEffect(key1 = likedPhotosPagingSource.loadState) {
                        if (likedPhotosPagingSource.loadState.refresh is LoadState.Error) {
                            Toast.makeText(
                                context,
                                "Error: " + (likedPhotosPagingSource.loadState.refresh as LoadState.Error).error.message,
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(
                                "UNSPLASH_DEBUG",
                                (likedPhotosPagingSource.loadState.refresh as LoadState.Error).error.localizedMessage as String
                            )
                        }
                    }
                    if (likedPhotosPagingSource.loadState.refresh is LoadState.Loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 9.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                            columns = GridCells.Adaptive(100.dp),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            items(
                                count = likedPhotosPagingSource.itemCount,
                                key = likedPhotosPagingSource.itemKey(),
                                contentType = likedPhotosPagingSource.itemContentType(
                                )
                            ) { index ->
                                val item = likedPhotosPagingSource[index]
                                if (item != null) {
                                    GlideImage(
                                        model = item.urls.regular,
                                        contentDescription = getString(R.string.liked_photos_single),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                    )
                                }
                            }
                            item {
                                if (likedPhotosPagingSource.loadState.append is LoadState.Loading) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LogoutDialog(openLogoutDialog: Boolean, closeDialog: (Boolean) -> Unit) {
        if (openLogoutDialog) {
            Dialog(
                onDismissRequest = { closeDialog(false) }
            ) {
                Card(shape = RoundedCornerShape(20)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(17.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(19.dp)
                    ) {
                        Text(text = getString(R.string.logout_text), color = Color.Black)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { logout(requireContext()) }) {
                            Text(text = getString(R.string.yes))
                        }
                        Button(onClick = { closeDialog(false) }) {
                            Text(text = getString(R.string.no))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun IntentPhotoScreen(
        onLikeClick: (String) -> Unit,
        onDownloadClick: (String) -> Unit,
    ) {
        LaunchedEffect(key1 = errorMessageFlow) {
            if (errorMessageFlow.value != null) {
                Toast.makeText(
                    context,
                    "Error: ${errorMessageFlow.value}",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    "UNSPLASH_DEBUG", errorMessageFlow.value.toString()
                )
            }
        }
        Card(
            shape = RoundedCornerShape(17.dp),
            modifier = Modifier
                .padding(4.dp),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (photoDescriptionStateFlow.value == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val response = photoDescriptionStateFlow.value
                    val photo = response!!.toNonNull()
                    val likeIconColor: Color =
                        if (photo.likedByUser) Color.Red else Color.Black
                    val tags: String = if (photo.tags.isEmpty()) {
                        getString(R.string.no_tag_data)
                    } else {
                        val listSize =
                            if (photo.tags.size < 3) photo.tags.size else 3
                        val tagList = photo.tags.take(listSize)
                        val tagsStringBuilder = StringBuilder()
                        tagList.forEach { tag ->
                            tagsStringBuilder.append("#${tag.title} ")
                        }
                        tagsStringBuilder.toString()
                    }
                    val location =
                        if (photo.location.position!!.latitude == 0.0 && photo.location.position.longitude == 0.0) {
                            getString(R.string.no_location_data)
                        } else {
                            "${photo.location.position.latitude} / ${photo.location.position.longitude}"
                        }
                    val enableLocation = location != getString(R.string.no_location_data)
                    GlideImage(
                        model = photo.urls.regular,
                        contentDescription = getString(R.string.photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(9.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = getString(R.string.author, photo.user.name),
                                fontSize = 11.sp
                            )
                            Text(
                                text = getString(R.string.make, photo.exif.make),
                                fontSize = 11.sp
                            )
                            Text(
                                text = getString(R.string.model, photo.exif.model),
                                fontSize = 11.sp
                            )
                            Text(
                                text = getString(R.string.exposure_time, photo.exif.exposureTime),
                                fontSize = 11.sp
                            )
                            Text(
                                text = getString(R.string.location, location),
                                fontSize = 11.sp
                            )
                            Text(
                                text = getString(R.string.tags, tags),
                                fontSize = 11.sp
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(7.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = {
                                        onDownloadClick(photo.id)
                                    }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                id = R.drawable.icon_download
                                            ),
                                            contentDescription = getString(R.string.download_this_photo),
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Black
                                        )
                                    }
                                    Text(
                                        text = photo.downloads.toString(),
                                        maxLines = 1,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(7.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = {
                                        onLikeClick(photo.id)
                                    }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                id = R.drawable.icon_favorite
                                            ),
                                            contentDescription = getString(R.string.like_photo),
                                            modifier = Modifier.size(24.dp),
                                            tint = likeIconColor
                                        )
                                    }
                                    Text(
                                        text = photo.likes.toString(),
                                        maxLines = 1,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(7.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = {
                                        sharePhotoUrl(photo.id)
                                    }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                id = R.drawable.icon_share
                                            ),
                                            contentDescription = getString(R.string.share_this_photo),
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(7.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = {
                                            openPhotoLocation(
                                                photo.location.position.latitude!!,
                                                photo.location.position.longitude!!
                                            )
                                        },
                                        enabled = enableLocation
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                id = R.drawable.icon_location
                                            ),
                                            contentDescription = getString(R.string.view_geolocation),
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logout(context: Context) {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openPhotoLocation(latitude: Double, longitude: Double) {
        val intentUri = Uri.parse("geo:0,0?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    private fun sharePhotoUrl(photoId: String) {
        val photoUrl = "https://unsplash.com/photos/$photoId"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, photoUrl)
        startActivity(Intent.createChooser(intent, getString(R.string.share_photo_with)))
    }

    private fun downloadPhoto(urlInput: String, photoId: String): String {
        val uri = Uri.parse(urlInput)
        val query = uri.query
        val path = uri.path?.drop(1)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setInputData(workDataOf(PHOTO_PATH to path, PHOTO_QUERY to query, PHOTO_ID to photoId))
            .build()


//        val workManager = WorkManager.getInstance(requireContext())
        workManager.beginUniqueWork(
            DOWNLOAD_PHOTO,
            ExistingWorkPolicy.KEEP,
            request
        ).enqueue()
        val requestId = request.id.toString()
        Log.d("UNSPLASH_DEBUG", "Request ID $requestId")
        return requestId
    }
}

sealed class PhotoWindow {
    class Item(val photo: Photo?, val showDialog: Boolean = false) :
        PhotoWindow()
}

