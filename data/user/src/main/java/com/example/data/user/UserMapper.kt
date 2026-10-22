package com.example.data.user

import com.example.core.database.UserEntity
import com.example.core.model.User

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    url = url
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    url = url
)
