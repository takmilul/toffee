package com.banglalink.toffee.exception

import java.lang.Exception

class UpdateRequiredException(val title:String,val updateMsg:String, val forceUpdate:Boolean):Exception(updateMsg)