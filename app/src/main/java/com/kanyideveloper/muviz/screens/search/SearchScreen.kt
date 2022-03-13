package com.kanyideveloper.muviz.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import com.kanyideveloper.muviz.R
import com.kanyideveloper.muviz.model.Search
import com.kanyideveloper.muviz.presentation.components.StandardToolbar
import com.kanyideveloper.muviz.screens.destinations.MovieDetailsScreenDestination
import com.kanyideveloper.muviz.screens.destinations.TvSeriesDetailsScreenDestination
import com.kanyideveloper.muviz.ui.theme.primaryDarkVariant
import com.kanyideveloper.muviz.ui.theme.primaryGray
import com.kanyideveloper.muviz.ui.theme.primaryPink
import com.kanyideveloper.muviz.util.Constants
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalFoundationApi::class)
@Destination(start = true)
@Composable
fun SearchScreen(
    navigator: DestinationsNavigator,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchResult = viewModel.searchSearch.value.collectAsLazyPagingItems()

    Column(
    ) {
        StandardToolbar(
            navigator = navigator,
            title = {
                Text(
                    text = "Search",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            },
            modifier = Modifier.fillMaxWidth(),
            showBackArrow = true
        )

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(67.dp)
                .padding(8.dp),
            onSearch = { searchParam ->
                viewModel.searchAll(searchParam)
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            LazyColumn(
                contentPadding = PaddingValues(8.dp)
            ) {
                items(searchResult) { search ->
                    SearchItem(
                        search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(4.dp),
                        onClick = {
                            when (search?.mediaType) {
                                "movie" -> {
                                    navigator.navigate(MovieDetailsScreenDestination(search.id!!))
                                }
                                "tv" -> {
                                    navigator.navigate(TvSeriesDetailsScreenDestination(search.id!!))
                                }
                                else -> {
                                    return@SearchItem
                                }
                            }
                        }
                    )
                }

                if (searchResult.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            searchResult.apply {
                loadState
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.wrapContentSize(Alignment.Center),
                            color = primaryPink,
                            strokeWidth = 2.dp,

                            )
                    }
                    is LoadState.Error -> {
                        val e = searchResult.loadState.refresh as LoadState.Error
                        Text(
                            text = when (e.error) {
                                is HttpException -> {
                                    "Oops, something went wrong!"
                                }
                                is IOException -> {
                                    "Couldn't reach server, check your internet connection!"
                                }
                                else -> {
                                    "Unknown error occurred"
                                }
                            },
                            textAlign = TextAlign.Center,
                            color = primaryPink
                        )
                    }
                }
            }

            if (viewModel.searchSearch.value == emptyFlow<Flow<PagingData<Search>>>()){
                Image(
                    modifier = Modifier
                        .size(100.dp),
                    painter = painterResource(id = R.drawable.ic_file_searching_amico),
                    contentDescription = null
                )
            }
        }
    }

}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    var text by remember {
        mutableStateOf("The 100")
    }
    TextField(
        value = text,
        onValueChange = {
            text = it
        },
        placeholder = {
            Text(
                text = "Search...",
                color = primaryGray
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, CircleShape)
            .background(Color.Transparent, CircleShape),
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
        ),
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.White,
            disabledTextColor = Color.Transparent,
            backgroundColor = primaryDarkVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(color = Color.White),
        maxLines = 1,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { onSearch(text) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = primaryGray,
                    contentDescription = null
                )
            }
        },
    )
}


@Composable
fun SearchItem(
    search: Search?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        elevation = 5.dp
    ) {
        Row() {
            Image(
                painter = rememberImagePainter(
                    data = "${Constants.IMAGE_BASE_UR}/${search?.posterPath}",
                    builder = {
                        placeholder(R.drawable.ic_placeholder)
                        crossfade(true)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth(0.3f),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )

            Column(
                modifier = modifier
                    .fillMaxWidth(0.7f)
                    .padding(8.dp)
            ) {

                Text(
                    text = (search?.name ?: search?.originalName ?: search?.originalTitle
                    ?: "No title provided"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )


                Spacer(modifier = Modifier.height(5.dp))

                (search?.firstAirDate ?: search?.releaseDate)?.let {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        text = it,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(3) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = search?.genreIds.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = search?.overview ?: "No description",
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
        }
    }
}