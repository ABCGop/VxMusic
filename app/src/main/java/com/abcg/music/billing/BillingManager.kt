package com.abcg.music.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val _isPro = MutableStateFlow(true)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    // Dummy formatted price
    private val _formattedPrice = MutableStateFlow<String?>(null)
    val formattedPrice: StateFlow<String?> = _formattedPrice.asStateFlow()

    fun launchBillingFlow(activity: Activity) {
        // No-op
    }

    fun checkProStatus() {
        _isPro.value = true
    }
    
    companion object {
        const val PRODUCT_ID = "vxmusic_pro_3m"
    }
}
