// GUID関係のライブラリ

#include <jni.h>
#include "atlbase.h"
#include "JCom.h"

//------------------------------------------------------------------------------
//	javaのjcom.GUIDクラスからC++のGUID構造体を作成する。
//	JNIの配列は、全体ではオブジェクトとして扱う。要素がプリミティブ型の場合、
//	その配列のイメージで、複数の要素を一度に転送できる。もちろん、１つづつも
//	できる。
//------------------------------------------------------------------------------
void jobject2GUID(JNIEnv* env, jobject guid, GUID* IID)
{
	jclass clsGUID = env->GetObjectClass(guid);
	// int data1 を取り出す
	jfieldID fieldData1 = env->GetFieldID(clsGUID, "data1", "I");
	IID->Data1 = (unsigned long)env->GetIntField(guid, fieldData1);
	// short data2 を取り出す
	jfieldID fieldData2 = env->GetFieldID(clsGUID, "data2", "S");
	IID->Data2 = (unsigned short)env->GetShortField(guid, fieldData2);
	// short data3 を取り出す
	jfieldID fieldData3 = env->GetFieldID(clsGUID, "data3", "S");
	IID->Data3 = (unsigned short)env->GetShortField(guid, fieldData3);
	// byte[] data4 を取り出す
	jfieldID fieldData4 = env->GetFieldID(clsGUID, "data4", "[B");
	jbyteArray data4 = (jbyteArray)env->GetObjectField(guid, fieldData4);
	jbyte buf[8];
	env->GetByteArrayRegion(data4, 0, 8, buf);
	for(int i=0; i<8; i++) {
		IID->Data4[i] = (BYTE)buf[i];
	}
}

//------------------------------------------------------------------------------
//	C++側のGUID構造体からJava側のGUIDオブジェクトを生成する
//------------------------------------------------------------------------------
jobject GUID2jobject(JNIEnv* env, GUID* guid)
{
	// GUIDインスタンスを生成
	jclass clsGUID = env->FindClass(CLASS_GUID);
	jmethodID method = env->GetMethodID(clsGUID, "<init>", "(ISSBBBBBBBB)V");
	jobject jGuid = env->NewObject(clsGUID, method, 
				(jint)guid->Data1,(jshort)guid->Data2,(jshort)guid->Data3,
				(jbyte)guid->Data4[0],(jbyte)guid->Data4[1],(jbyte)guid->Data4[2],(jbyte)guid->Data4[3],
				(jbyte)guid->Data4[4],(jbyte)guid->Data4[5],(jbyte)guid->Data4[6],(jbyte)guid->Data4[7]);
	return jGuid;
}


