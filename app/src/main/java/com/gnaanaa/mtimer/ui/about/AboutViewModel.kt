package com.gnaanaa.mtimer.ui.about

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.gnaanaa.mtimer.data.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val productDetails = billingManager.productDetails
    val purchaseSuccess = billingManager.purchaseSuccess

    fun supportApp(activity: Activity, productId: String) {
        billingManager.launchPurchaseFlow(activity, productId)
    }
}
