package com.banglalink.toffee.ui.premium.payment

enum class PaymentMethod(val value: Int) {
    BKASH(4),
    BL_PACK(10),
    VOUCHER(12),
    TRIAL(0),
    SSL(13),
    NAGAD(9),
}
enum class PaymentMethodString(val value: String) {
    BKASH ("BKASH"),
    BL ("BL"),
    VOUCHER ("VOUCHER"),
    FREE ("FREE"),
    SSL ("SSL"),
    NAGAD("NAGAD"),
}