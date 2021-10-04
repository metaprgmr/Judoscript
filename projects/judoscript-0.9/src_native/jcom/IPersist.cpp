#include <jni.h>
#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IPersist.h"

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IPersist
 * Method:    _getClassID
 * Signature: ()Ljp/ne/so_net/ga2/no_ji/jcom/GUID;
	IPersistインターフェースを持っているかどうか調べ、持っているならば、
	IPersist::getClassIDを行う。持っていなければ、null を返す。
	if it have IPersist interface, then return IPersist::getClassID().
	else return (null).
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IPersist__1getClassID
  (JNIEnv *env, jobject obj)
{
	// IPersistインターフェースを取り出す。// get IPersist pointer from env
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IPersist* pIPersist = (IPersist*)env->GetIntField(obj, fieldID);
	if(pIPersist == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IPersist not created");
		return NULL;
	}
	CLSID clsid;
	HRESULT hr = pIPersist->GetClassID(&clsid);
	if(FAILED(hr)) {
		// 例外をセットする. // set exception
		char message[256];
		sprintf(message, "IPersist::getClassID() failed HRESULT=0x%X", hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	return GUID2jobject(env, &clsid);
}


