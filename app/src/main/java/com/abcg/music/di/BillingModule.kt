package com.abcg.music.di

import com.abcg.music.billing.BillingManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val billingModule = module {
    single { 
        BillingManager(
            context = androidContext(),
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ) 
    }
}
