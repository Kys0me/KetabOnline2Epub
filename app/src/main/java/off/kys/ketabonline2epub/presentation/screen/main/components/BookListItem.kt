package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.flow.Flow
import off.kys.ketabonline2epub.R
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.util.extensions.toPlainText

@Composable
fun BookListItem(
    modifier: Modifier = Modifier,
    book: BookItem,
    isDownloaded: (BookId, BookType) -> Flow<Boolean>,
    enabled: Boolean = true,
    onDownloadClick: (BookType) -> Unit
) {
    val isEpubDownloaded by isDownloaded(book.id, BookType.EPUB).collectAsState(initial = false)
    val isPdfDownloaded by isDownloaded(book.id, BookType.PDF).collectAsState(initial = false)

    var downloadType: BookType by remember { mutableStateOf(BookType.EPUB) }


    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(CardDefaults.shape),
            leadingContent = {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl ?: R.drawable.book_cover)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.book_cover),
                        modifier = Modifier
                            .width(64.dp)
                            .aspectRatio(2 / 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (book.coverUrl == null)
                            ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                        else null
                    )
                    if (isEpubDownloaded && isPdfDownloaded || isEpubDownloaded && !book.hasPdf) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_check_circle_24),
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            headlineContent = {
                Text(
                    text = book.title.toPlainText(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // EPUB Chip
                        FilterChip(
                            modifier = Modifier.padding(top = 4.dp),
                            selected = downloadType is BookType.EPUB,
                            onClick = { downloadType = BookType.EPUB },
                            label = {
                                Text(stringResource(R.string.epub), style = MaterialTheme.typography.labelSmall)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(if (isEpubDownloaded) R.drawable.round_check_circle_24 else R.drawable.round_description_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        // PDF Chip (Conditional)
                        if (book.hasPdf) {
                            FilterChip(
                                modifier = Modifier.padding(top = 4.dp),
                                selected = downloadType is BookType.PDF,
                                onClick = { downloadType = BookType.PDF },
                                label = {
                                    Text(stringResource(R.string.pdf), style = MaterialTheme.typography.labelSmall)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(if (isPdfDownloaded) R.drawable.round_check_circle_24 else R.drawable.round_description_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            trailingContent = {
                FilledTonalIconButton(
                    onClick = { onDownloadClick(downloadType) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    enabled = enabled, // Button remains enabled even if isDownloaded is true
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_download_24),
                        contentDescription = stringResource(R.string.download),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}