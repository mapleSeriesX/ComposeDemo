package com.example.data.user

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 2. 追加在同一个文件里的接口绑定
@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
