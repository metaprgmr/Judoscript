#include <jni.h>
//#include "JCom.h"

// 新しいタイプのJNI(jdk1.2以降)
//#include "jp_0005cne_0005cso_0005fnet_0005cga2_0005cno_0005fji_0005cjcom_0005cJCom.h"
// 古いタイプのJNI(少なくともjdk1.2より前)
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown.h"

#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _queryInterface
 * Signature: (Ljp/ne/so_net/ga2/no_ji/jcom/GUID;)I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1queryInterface
  (JNIEnv *env, jobject obj, jobject guid)
{
	// IUnknownインターフェースのポインタを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	// 引数からIIDを取得
	GUID IID;
	jobject2GUID(env, guid, &IID);

	// IUnknown::QueryInterfaceを呼ぶ。
	void* pObject = NULL;
	HRESULT hr = pIUnknown->QueryInterface(IID, &pObject);
	if(hr==E_NOINTERFACE) {		// 0x80004002L
		return (jint)0;		// no interface
	}
	if(FAILED(hr)) {
		// 例外を投げる
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "IUnknown.queryInterface() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// IUnknownインターフェースのポインタを返す。
	return (jint)(int)pObject;	// 正常終了
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _release
 * Signature: ()Z
 * **********************************************************************/
JNIEXPORT jboolean JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1release
  (JNIEnv *env, jobject obj)
{
	// IUnknownインターフェースのポインタを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	// すでにNULLなら解放する必要はない。
	if(pIUnknown==NULL) {
		return JNI_FALSE;	// すでに解放済み
	}

	// 解放
	pIUnknown->Release();

	// pIUnknownを０にする。
//	env->SetIntField(obj, fieldID, (jint)0);

	return JNI_TRUE;	// 成功
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _addRef
 * Signature: ()I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1addRef
  (JNIEnv *env, jobject obj)
{
	// IUnknownインターフェースのポインタを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	ULONG count = pIUnknown->AddRef();
	return (jint)count;
}




/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _getProgIDFromCLSID
 * Signature: (Ljp/ne/so_net/ga2/no_ji/jcom/GUID;)Ljava/lang/String;
 * **********************************************************************/
JNIEXPORT jstring JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1getProgIDFromCLSID
  (JNIEnv *env, jclass cls, jobject iid)
{
	// 引数からCLSIDを取得
	GUID IID;
	jobject2GUID(env, iid, &IID);
	// CLSIDからProgIDを取得
	BSTR bstrProgID = NULL;
	HRESULT hr = ProgIDFromCLSID(IID, &bstrProgID);
	if(FAILED(hr)) {
		printf("hr=%Xh \n", hr);
		return (jstring)NULL;
	}
	// BSTRからjstringに変換
	jstring jProgID = BSTR2jstring(env, bstrProgID);
	SysFreeString(bstrProgID);
	return jProgID;
}

/*
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown_queryInterface
  (JNIEnv *env, jobject obj, jobject guid)
{
	// IUnknownインターフェースのポインタを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	GUID IID;
	jobject2GUID(env, guid, &IID);

	void* pObject = NULL;
	HRESULT hr = pIUnknown->QueryInterface(IID, &pObject);
	if(FAILED(hr)) {
		// 例外を投げる
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "IUnknown.queryInterface() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// IUnknown(JComManager)を呼ぶ。その際に、引数のJComManager jcmを渡す
	jclass clsIUnknown = env->FindClass(CLASS_IUNKNOWN);
	jmethodID method = env->GetMethodID(clsIUnknown, "<init>", "(Ljp/ne/so_net/ga2/no_ji/jcom/JComManager;)V");
	jfieldID jcmID = env->GetFieldID(clsIUnknown, "jcm", "Ljp/ne/so_net/ga2/no_ji/jcom/JComManager;");
	jobject jcm = env->GetObjectField(obj,  jcmID);
	jobject result = env->NewObject(clsIUnknown, method, jcm);
	// 生成されたオブジェクトの_pIUnknownフィールドに var->punkVal をセットする。
	jfieldID field_pIUnknown = env->GetFieldID(clsIUnknown, "pIUnknown", "I");
	env->SetIntField(jcm, field_pIUnknown, (jint)(int)pObject);

	return result;	// 正常終了
}
*/
