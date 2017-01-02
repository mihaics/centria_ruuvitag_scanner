#include <jni.h>
#include <string>
#include "RealClass.h"


RealClass* cppCalls;

extern "C" void Java_rfid_tki_centria_fi_myapplication_MainActivity_init(JNIEnv* env, jobject /* this */)
{
    cppCalls = new RealClass();
}

extern "C" void Java_rfid_tki_centria_fi_myapplication_MainActivity_close(JNIEnv* env, jobject /* this */)
{
    delete cppCalls;
    cppCalls = NULL;
}

extern "C" void Java_rfid_tki_centria_fi_myapplication_MainActivity_calculate(JNIEnv* env, jobject, jbyteArray data, int len)
{


    jsize length = env->GetArrayLength(data);

    unsigned char* localByteData = new unsigned char[length];
    env->GetByteArrayRegion(data,0,length, reinterpret_cast<jbyte*>(localByteData));

    cppCalls->calculate(localByteData,len);

    delete[] localByteData;



}

extern "C" jint Java_rfid_tki_centria_fi_myapplication_MainActivity_getMin(JNIEnv* env, jobject /* this */)
{
    return cppCalls->getMin();
}


extern "C" jint Java_rfid_tki_centria_fi_myapplication_MainActivity_getMax(JNIEnv* env, jobject /* this */)
{
    return cppCalls->getMax();
}
