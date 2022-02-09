package com.banglalink.toffee.di.databinding

import dagger.hilt.DefineComponent

@DefineComponent.Builder
interface CustomBindingComponentBuilder {
    fun build(): CustomBindingComponent
}