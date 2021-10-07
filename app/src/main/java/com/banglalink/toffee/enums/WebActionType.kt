package com.banglalink.toffee.enums

enum class WebActionType(val value: Int) {
    HOME_SCREEN(1),
    LOGIN_DIALOG(2),
    MESSAGE_DIALOG(3),
    PLAY_CONTENT(4),
    DEEP_LINK(5),
    CLOSE_APP(6),
    FORCE_LOGOUT(7)
}