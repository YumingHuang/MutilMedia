#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>


JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntInvokeVoid
        (JNIEnv *env, jobject jobj) {
    char *cstr = "hello from c!";
    __android_log_print(ANDROID_LOG_DEBUG, "system.out", "cTag：%s!\n", cstr);
    return;
}

JNIEXPORT jint JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntInvokeReturn
        (JNIEnv *env, jobject jobj, jint a, jint b) {
    return a + b;
}

JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntAccessField
        (JNIEnv *env, jobject jobj) {
//得到jclass
    jclass jcla = (*env)->GetObjectClass(env, jobj);
    //得到jfieldID，最后一个参数是签名，String对应的签名是Ljava/lang/String;(注意最后的分号)
    jfieldID jfID = (*env)->GetFieldID(env, jcla, "key", "Ljava/lang/String;");
    //得到key属性的值jstring
    jstring jstr = (*env)->GetObjectField(env, jobj, jfID);
    //jstring转化为C中的char*
    char *oriText = (*env)->GetStringUTFChars(env, jstr, NULL);
    //拼接得到新的字符串text="China John"
    char text[20] = "China";
    //strcat(text, oriText);
    //C中的char*转化为JNI中的jstring
    jstring jstrMod = (*env)->NewStringUTF(env, text);
    //修改key
    (*env)->SetObjectField(env, jobj, jfID, jstrMod);
    //只要使用了GetStringUTFChars，就需要释放
    (*env)->ReleaseStringUTFChars(env, jstr, oriText);
}

JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntAccessStaticField
        (JNIEnv *env, jobject jobj) {
    //得到jclass
    jclass jcla = (*env)->GetObjectClass(env, jobj);
    //得到jfieldID
    jfieldID jfid = (*env)->GetStaticFieldID(env, jcla, "count", "I");
    //得到静态属性的值count
    jint count = (*env)->GetStaticIntField(env, jcla, jfid);
    //自增
    count++;
    //修改count的值
    (*env)->SetStaticIntField(env, jcla, jfid, count);
}

JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntAccessMethod
        (JNIEnv *env, jobject jobj) {
    //得到jclass
    jclass jcla = (*env)->GetObjectClass(env, jobj);
    //得到jmethodID
    jmethodID jmid = (*env)->GetMethodID(env, jcla, "getRandomInt", "(I)I");
    //调用java方法获取返回值，第四个参数100表示传入到java方法中的值
    jint jRandom = (*env)->CallIntMethod(env, jobj, jmid, 100);
    //可以在Android Studio中Logcat显示，需要定义头文件#include <android/log.h>
    __android_log_print(ANDROID_LOG_DEBUG, "system.out", "cTag：%ld", jRandom);
}

JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntAccessStaticMethod
        (JNIEnv *env, jobject jobj) {
    jclass jcla = (*env)->GetObjectClass(env, jobj);
    jmethodID  jmid = (*env)->GetStaticMethodID(env, jcla, "getUUID", "()Ljava/lang/String;");
    jstring uuid = (*env)->CallStaticObjectMethod(env, jcla, jmid);
    char* uuid_str = (*env)->GetStringUTFChars(env, uuid, NULL);
    __android_log_print(ANDROID_LOG_DEBUG, "system.out", "uuid_str：%ld", uuid_str);
}

JNIEXPORT void JNICALL
Java_com_example_multimedia_ui_activity_image_SimpleJniActivity_ntAccessConstructMethod
        (JNIEnv *env, jobject jobj) {
    //得到类Date对应的jclass
    jclass jcla = (*env)->FindClass(env, "java/util/Date");
    //构造方法对应的都是<init>
    //在任意位置打开命令行，输入javap -s -p java.util.Date可以看到空参构造的签名是()V
    jmethodID jmid = (*env)->GetMethodID(env, jcla, "<init>", "()V");
    //实例化Date对象
    jobject jDate = (*env)->NewObject(env, jcla, jmid);
    //下面需要调用的是getTime，对应这里第三个参数传入getTime，第四个参数为其签名
    jmethodID jTimeMid = (*env)->GetMethodID(env, jcla, "getTime", "()J");
    //调用getTime方法得到返回值jlong
    jlong jtime = (*env)->CallLongMethod(env, jDate, jTimeMid);
    __android_log_print(ANDROID_LOG_DEBUG, "system.out", "jtime：%ld", jtime);
}