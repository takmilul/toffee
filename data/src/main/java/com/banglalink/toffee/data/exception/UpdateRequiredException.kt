package com.banglalink.toffee.data.exception

class UpdateRequiredException(val title:String,val updateMsg:String, val forceUpdate:Boolean):Exception(updateMsg)