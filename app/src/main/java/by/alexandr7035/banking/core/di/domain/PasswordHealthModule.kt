package by.alexandr7035.banking.core.di.domain

import by.alexandr7035.banking.domain.features.password_health.GetReusedPasswordGroupsUseCase
import org.koin.dsl.module

val passwordHealthModule = module {
    factory {
        GetReusedPasswordGroupsUseCase(repository = get())
    }
}
