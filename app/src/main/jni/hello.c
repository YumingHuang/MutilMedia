#include <stdlib.h>
#include <stdio.h>
#include <jni.h>

JNIEXPORT void JNICALL Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_printHello
        (JNIEnv *
        env, jobject thiz) {
    char *cstr = "hello from c!";
    printf("%s!\n", cstr);
    return;
}


JNIEXPORT jstring JNICALL Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_printString
        (JNIEnv *
        env,
         jobject thiz, jstring string) {
    const char *str = (*env)->GetStringUTFChars(env, string, 0);
    char *cstr = "hello from c!";
    return (*env)->NewStringUTF(env, str);
}