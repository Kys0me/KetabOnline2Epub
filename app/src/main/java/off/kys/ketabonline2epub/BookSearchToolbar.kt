package off.kys.ketabonline2epub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchToolbar(
    mainUiState: MainUiState,
    onEvent: (MainUiEvent) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        tonalElevation = 3.dp, // Gives it that subtle Material 3 "elevated" color
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .clip(CircleShape), // Rounded pill shape
                value = mainUiState.bookName,
                onValueChange = { onEvent(MainUiEvent.OnBookNameChange(it)) },
                placeholder = { Text(stringResource(R.string.search_books)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.round_search_24),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (mainUiState.bookName.isNotEmpty()) {
                        IconButton(onClick = { onEvent(MainUiEvent.OnBookNameChange("")) }) {
                            Icon(
                                painterResource(R.drawable.round_close_24),
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                shape = CircleShape
            )

            Box(
                modifier = Modifier
                    .statusBarsPadding()
            ) {
                Button(
                    onClick = { onEvent(MainUiEvent.OnSearchClicked) },
                    enabled = !mainUiState.isLoading,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (mainUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.search))
                    }
                }
            }
        }
    }
}