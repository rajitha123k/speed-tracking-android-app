package com.example.webexandroid.search

import com.example.webexandroid.search.ui.home.HomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel {
        HomeViewModel(get(), get(), get())
    }
    single { SearchRepository(get()) }
}