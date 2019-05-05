package ru.ifr0z.core.interfaces

import kotlinx.coroutines.CoroutineScope

interface ViewScope : CoroutineScope {

    fun onError(throwable: Throwable)

    fun onClose()
}