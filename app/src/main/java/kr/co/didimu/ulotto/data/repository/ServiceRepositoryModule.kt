package kr.co.didimu.ulotto.data.repository

import kr.co.didimu.ulotto.data.repository.remote.ServiceRemoteData
import dagger.Binds
import dagger.Module

@Module
abstract class ServiceRepositoryModule {
    @Binds
    abstract fun prvideRemoteDataSource(remoteService: ServiceRemoteData): ServiceData
}