package com.banglalink.toffee.apiservice

class GetNewPaymentMethodInfo {
    suspend fun getAccountTypes(): List<String>{
        return listOf("Bank", "Mobile Banking")
    }

    suspend fun getBankNames(): List<String> {
        return listOf("Brac Bank Limited", "Eastern Bank Limited", "City Bank Limited", "Prime Bank Limited", "bKash", "Rocket")
    }

    suspend fun getDistrictNames(): List<String> {
        return listOf("Dhaka", "Chittagong", "Khulna", "Barishal", "Rajshahi", "Sylhet")
    }

    suspend fun getBranchNames(): List<String> {
        return listOf("Motijheel Branch", "Kawran Bazar Branch", "Farmgate Branch", "Banani Branch", "Dhanmondi Branch", "Gulshan Branch")
    }
}