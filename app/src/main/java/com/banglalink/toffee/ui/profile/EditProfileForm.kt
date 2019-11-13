package com.banglalink.toffee.ui.profile

import androidx.databinding.BaseObservable
import java.io.Serializable

class EditProfileForm:BaseObservable(),Serializable {
    var fullName:String=""
    var email:String=""
    var phoneNo:String=""
    var address:String=""
    var photoUrl:String=""

}