package com.banglalink.toffee.extension

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> LifecycleOwner.observe(
    flow: Flow<T>,
    execute: suspend (T) -> Unit,
): Job {
    val lifecycleOwner = if(this is Fragment && this !is DialogFragment) this.viewLifecycleOwner else this
    
    return lifecycleOwner.lifecycleScope.launch {
        flow.flowWithLifecycle(lifecycleOwner.lifecycle)
            .collectLatest {
                execute(it)
            }
    }
}