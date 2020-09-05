package com.banglalink.toffee.ui.common

interface SingleListRepository<T: Any> {
    suspend fun execute(): List<T>
}