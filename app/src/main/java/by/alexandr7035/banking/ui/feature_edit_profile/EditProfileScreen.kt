package by.alexandr7035.banking.ui.feature_edit_profile

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.ui.app_host.host_utils.LocalScopedSnackbarState
import by.alexandr7035.banking.ui.components.FullscreenProgressBar
import by.alexandr7035.banking.ui.components.PrimaryButton
import by.alexandr7035.banking.ui.components.ScreenPreview
import by.alexandr7035.banking.ui.components.SecondaryToolBar
import by.alexandr7035.banking.ui.components.error.ErrorFullScreen
import by.alexandr7035.banking.ui.components.forms.DecoratedFormField
import by.alexandr7035.banking.ui.components.snackbar.SnackBarMode
import by.alexandr7035.banking.ui.core.error.asUiTextError
import by.alexandr7035.banking.ui.core.resources.UiText
import by.alexandr7035.banking.ui.feature_cards.screen_add_card.UiField
import de.palm.composestateevents.EventEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackBarState = LocalScopedSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.emitIntent(EditProfileIntent.EnterScreen)
    }

    EditProfileScreen_Ui(
        state = state,
        onIntent = { viewModel.emitIntent(it) },
        onBack = onBack,
    )

    EventEffect(
        event = state.saveEvent,
        onConsumed = viewModel::consumeSaveEvent,
    ) { result ->
        when (result) {
            is OperationResult.Success -> {
                snackBarState.show(
                    message = context.getString(R.string.profile_updated_successfully),
                    snackBarMode = SnackBarMode.Positive
                )
                onBack()
            }

            is OperationResult.Failure -> {
                snackBarState.show(
                    message = result.error.errorType.asUiTextError().asString(context),
                    snackBarMode = SnackBarMode.Negative
                )
            }
        }
    }
}

@Composable
internal fun EditProfileScreen_Ui(
    state: EditProfileState,
    onIntent: (EditProfileIntent) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        SecondaryToolBar(
            onBack = onBack,
            title = UiText.StringResource(R.string.edit_profile),
        )

        when {
            state.error != null -> {
                ErrorFullScreen(
                    error = state.error,
                    onRetry = { onIntent(EditProfileIntent.EnterScreen) }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    DecoratedFormField(
                        modifier = Modifier.fillMaxWidth(),
                        fieldTitle = UiText.StringResource(R.string.first_name),
                        uiField = state.firstName,
                        onValueChange = { onIntent(EditProfileIntent.FirstNameChanged(it)) },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DecoratedFormField(
                        modifier = Modifier.fillMaxWidth(),
                        fieldTitle = UiText.StringResource(R.string.last_name),
                        uiField = state.lastName,
                        onValueChange = { onIntent(EditProfileIntent.LastNameChanged(it)) },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DecoratedFormField(
                        modifier = Modifier.fillMaxWidth(),
                        fieldTitle = UiText.StringResource(R.string.nickname),
                        uiField = state.nickName,
                        onValueChange = { onIntent(EditProfileIntent.NickNameChanged(it)) },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DecoratedFormField(
                        modifier = Modifier.fillMaxWidth(),
                        fieldTitle = UiText.StringResource(R.string.email_address),
                        uiField = state.email,
                        onValueChange = { onIntent(EditProfileIntent.EmailChanged(it)) },
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        onClick = { onIntent(EditProfileIntent.SaveProfile) },
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.save_changes),
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (state.isLoading || state.isSaving) {
        FullscreenProgressBar()
    }
}

@Preview
@Composable
fun EditProfileScreen_Preview() {
    ScreenPreview {
        EditProfileScreen_Ui(
            state = EditProfileState(
                isLoading = false,
                firstName = UiField(value = "Alexander"),
                lastName = UiField(value = "Michael"),
                nickName = UiField(value = "@alexandermichael"),
                email = UiField(value = "test@example.com"),
            )
        )
    }
}
