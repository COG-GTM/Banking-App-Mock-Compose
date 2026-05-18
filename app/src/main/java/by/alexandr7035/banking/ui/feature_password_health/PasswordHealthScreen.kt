package by.alexandr7035.banking.ui.feature_password_health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.features.password_health.LoginItem
import by.alexandr7035.banking.domain.features.password_health.ReusedPasswordGroup
import by.alexandr7035.banking.ui.components.FullscreenProgressBar
import by.alexandr7035.banking.ui.components.ScreenPreview
import by.alexandr7035.banking.ui.components.SecondaryToolBar
import by.alexandr7035.banking.ui.core.resources.UiText
import by.alexandr7035.banking.ui.theme.primaryFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasswordHealthScreen(
    viewModel: PasswordHealthViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.emitIntent(PasswordHealthIntent.LoadData)
    }

    PasswordHealthScreenContent(state = state, onBack = onBack)
}

@Composable
fun PasswordHealthScreenContent(
    state: PasswordHealthState,
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SecondaryToolBar(
                onBack = onBack,
                title = UiText.StringResource(R.string.password_health)
            )
        }
    ) { pv ->
        if (state.isLoading) {
            FullscreenProgressBar()
        } else if (state.groups.isEmpty()) {
            EmptyReusedPasswords(modifier = Modifier.padding(pv))
        } else {
            ReusedPasswordsList(
                modifier = Modifier.padding(pv),
                groups = state.groups
            )
        }
    }
}

@Composable
private fun EmptyReusedPasswords(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("NoReusedPasswordsView"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = Color(0xFFE8F5E9),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_password_health_check),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.reused_passwords),
            fontFamily = primaryFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color(0xFF262626)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_reused_passwords),
            fontFamily = primaryFontFamily,
            fontSize = 14.sp,
            color = Color(0xFF8F8F8F),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReusedPasswordsList(
    modifier: Modifier = Modifier,
    groups: List<ReusedPasswordGroup>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("ReusedPasswordsList")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.reused_passwords),
            fontFamily = primaryFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF262626)
        )

        Spacer(modifier = Modifier.height(16.dp))

        groups.forEachIndexed { index, group ->
            ReusedPasswordGroupCard(group = group)
            if (index < groups.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReusedPasswordGroupCard(group: ReusedPasswordGroup) {
    Column {
        Text(
            text = pluralStringResource(
                R.plurals.x_accounts_use_this_password,
                group.items.size,
                group.items.size
            ),
            fontFamily = primaryFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = Color(0xFF8F8F8F),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.testTag("ReusedPasswordGroup")
        ) {
            group.items.forEachIndexed { index, item ->
                LoginItemRow(item = item)
                if (index < group.items.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(start = 56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginItemRow(item: LoginItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ReusedPasswordCipherItem"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = Color(0xFFF1EDFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontFamily = primaryFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF262626),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.username.isNotEmpty()) {
                Text(
                    text = item.username,
                    fontFamily = primaryFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun PasswordHealthScreen_Empty_Preview() {
    ScreenPreview {
        Scaffold(
            topBar = {
                SecondaryToolBar(
                    onBack = {},
                    title = UiText.StringResource(R.string.password_health)
                )
            }
        ) { pv ->
            EmptyReusedPasswords(modifier = Modifier.padding(pv))
        }
    }
}

@Preview
@Composable
private fun PasswordHealthScreen_WithGroups_Preview() {
    ScreenPreview {
        Scaffold(
            topBar = {
                SecondaryToolBar(
                    onBack = {},
                    title = UiText.StringResource(R.string.password_health)
                )
            }
        ) { pv ->
            ReusedPasswordsList(
                modifier = Modifier.padding(pv),
                groups = listOf(
                    ReusedPasswordGroup(
                        id = "abc",
                        items = listOf(
                            LoginItem("1", "Gmail", "user@gmail.com"),
                            LoginItem("2", "Yahoo Mail", "user@yahoo.com"),
                            LoginItem("3", "Outlook", "user@outlook.com"),
                        )
                    ),
                    ReusedPasswordGroup(
                        id = "def",
                        items = listOf(
                            LoginItem("4", "GitHub", "devuser"),
                            LoginItem("5", "GitLab", "devuser"),
                        )
                    )
                )
            )
        }
    }
}
