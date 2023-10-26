#include <jni.h>

//std::string serverUrl = "https://mapi.toffeelive.com/";          // production server
//std::string serverUrl = "https://j1-staging.toffeelive.com/";    // staging server

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_banglalink_toffee_di_NetworkModule_getBaseUrl(JNIEnv *env, jobject thiz) {
//        return env ->NewStringUTF("https://mapi.toffeelive.com/");
        return env ->NewStringUTF("https://j1-staging.toffeelive.com/");
    }
}