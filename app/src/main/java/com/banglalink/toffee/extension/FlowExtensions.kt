package com.banglalink.toffee.extension

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

fun <T> LifecycleOwner.observe(
    flow: Flow<T>,
    execute: (T) -> Unit,
) {
    val ob = if(this is Fragment && this !is DialogFragment) this.viewLifecycleOwner else this
    ob.lifecycleScope.launchWhenStarted {
        flow.flowWithLifecycle(ob.lifecycle)
            .distinctUntilChanged()
            .collectLatest {
                execute(it)
            }
    }
}