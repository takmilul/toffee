#include <jni.h>

const char* serverUrl = "https://mapi.toffeelive.com/";          // production server
//const char* serverUrl = "https://j1-staging.toffeelive.com/";    // staging server

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_banglalink_toffee_di_NetworkModule_getUrl(JNIEnv *env, jobject obj) {
        return env ->NewStringUTF(serverUrl);
    }
}