package by.alexandr7035.banking.ui.app_host

import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.app_lock.CheckAppLockUseCase
import by.alexandr7035.banking.domain.features.login.CheckIfLoggedInUseCase
import by.alexandr7035.banking.domain.features.onboarding.CheckIfPassedOnboardingUseCase
import by.alexandr7035.banking.testutils.MainDispatcherRule
import by.alexandr7035.banking.ui.app_host.navigation.model.ConditionalNavigation
import by.alexandr7035.banking.ui.core.resources.UiText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val checkIfLoggedIn: CheckIfLoggedInUseCase = mockk()
    private val checkIfPassedOnboarding: CheckIfPassedOnboardingUseCase = mockk()
    private val checkAppLock: CheckAppLockUseCase = mockk()

    private fun createViewModel(
        loggedIn: Boolean,
        onboardingPassed: Boolean,
        appLocked: Boolean,
    ): AppViewModel {
        coEvery { checkIfLoggedIn.execute() } returns loggedIn
        every { checkIfPassedOnboarding.execute() } returns onboardingPassed
        every { checkAppLock.execute() } returns appLocked
        return AppViewModel(checkIfLoggedIn, checkIfPassedOnboarding, checkAppLock)
    }

    private fun advance() = mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    @Test
    fun `state is Loading until checks complete`() = runTest {
        val vm = createViewModel(loggedIn = true, onboardingPassed = true, appLocked = true)

        assertEquals(AppState.Loading, vm.appState.value)
        advance()
        assertTrue(vm.appState.value is AppState.Ready)
    }

    @Test
    fun `logged in, onboarding passed, app locked requires unlock only`() = runTest {
        val vm = createViewModel(loggedIn = true, onboardingPassed = true, appLocked = true)
        advance()

        assertEquals(
            AppState.Ready(
                conditionalNavigation = ConditionalNavigation(
                    requireLogin = false,
                    requireOnboarding = false,
                    requireCreateAppLock = false
                ),
                requireUnlock = true
            ),
            vm.appState.value
        )
    }

    @Test
    fun `logged in without app lock requires creating app lock`() = runTest {
        val vm = createViewModel(loggedIn = true, onboardingPassed = true, appLocked = false)
        advance()

        assertEquals(
            AppState.Ready(
                conditionalNavigation = ConditionalNavigation(
                    requireLogin = false,
                    requireOnboarding = false,
                    requireCreateAppLock = true
                ),
                requireUnlock = false
            ),
            vm.appState.value
        )
    }

    @Test
    fun `not logged in and onboarding not passed requires login and onboarding`() = runTest {
        val vm = createViewModel(loggedIn = false, onboardingPassed = false, appLocked = false)
        advance()

        assertEquals(
            AppState.Ready(
                conditionalNavigation = ConditionalNavigation(
                    requireLogin = true,
                    requireOnboarding = true,
                    requireCreateAppLock = false
                ),
                requireUnlock = false
            ),
            vm.appState.value
        )
    }

    @Test
    fun `not logged in with stale app lock still requires unlock but not app lock creation`() = runTest {
        val vm = createViewModel(loggedIn = false, onboardingPassed = true, appLocked = true)
        advance()

        val ready = vm.appState.value as AppState.Ready
        assertTrue(ready.requireUnlock)
        assertTrue(ready.conditionalNavigation.requireLogin)
        assertEquals(false, ready.conditionalNavigation.requireCreateAppLock)
    }

    @Test
    fun `login check failure maps to InitFailure with UI error`() = runTest {
        coEvery { checkIfLoggedIn.execute() } throws AppError(ErrorType.USER_NOT_FOUND)
        val vm = AppViewModel(checkIfLoggedIn, checkIfPassedOnboarding, checkAppLock)
        advance()

        val failure = vm.appState.value as AppState.InitFailure
        assertEquals(R.string.user_not_found, (failure.error as UiText.StringResource).resId)
    }

    @Test
    fun `TryPostUnlock clears requireUnlock on Ready state`() = runTest {
        val vm = createViewModel(loggedIn = true, onboardingPassed = true, appLocked = true)
        advance()

        vm.emitIntent(AppIntent.TryPostUnlock)

        assertEquals(false, (vm.appState.value as AppState.Ready).requireUnlock)
    }

    @Test
    fun `AppLockLogout resets to login required without unlock`() = runTest {
        val vm = createViewModel(loggedIn = true, onboardingPassed = true, appLocked = true)
        advance()

        vm.emitIntent(AppIntent.AppLockLogout)

        assertEquals(
            AppState.Ready(
                conditionalNavigation = ConditionalNavigation(
                    requireLogin = true,
                    requireOnboarding = false,
                    requireCreateAppLock = false
                ),
                requireUnlock = false
            ),
            vm.appState.value
        )
    }
}
