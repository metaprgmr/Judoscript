#include <jni.h>
//#include "IEnumVARIANT.h"

// 新しいタイプのJNI(jdk1.2以降)
//#include "jp_0005cne_0005cso_0005fnet_0005cga2_0005cno_0005fji_0005cjcom_0005cIEnumVARIANT.h"
// 古いタイプのJNI(少なくともjdk1.2より前)
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IEnumVARIANT.h"

#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"



/* **********************************************************************
 * Class:     IEnumVARIANT
 * Method:    Next
 * Signature: ()Ljava/lang/Object;
	１つ次のオブジェクトを取り出します。
	次のオブジェクトがない場合はnullを返します。
 * **********************************************************************/
//JNIEXPORT jobject JNICALL Java_IEnumVARIANT_Next__(JNIEnv *env, jobject obj)
JNIEXPORT jobject JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__
  (JNIEnv *env, jobject obj)
{
	// IEnumVARIANTインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return NULL;
	}

	// Nextメソッドを呼ぶ
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Next(1, &vresult, NULL);
	if(hr != 0 && hr != S_FALSE) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IEnumVARIANT.Next() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// 取得したオブジェクトが最後のときには null を返す。
	if(hr == S_FALSE) {
		return NULL;
	}

	// 取得したオブジェクトをJavaのjava.lang.Objectに変換
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "cannot convert VT=0x%X",ret);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

	return jresult;
}


/* **********************************************************************
 * Class:     IEnumVARIANT
 * Method:    Next
 * Signature: (I)[Ljava/lang/Object;
	指定した数だけ次のオブジェクトを取り出します。
	残りが少ない場合は、指定した数以下になる場合があります。
	配列の要素数に注意して下さい。
	celtは１以上の数を指定して下さい。
	この実装は無理に行わなくても、引数なしのNext()を繰り返せばできる。
	しかし、このＡＰＩは関数呼び出しによるオーバーヘッドを減らすために
	用意しているので、それを実現させるため、このＡＰＩも用意している。
 * **********************************************************************/
//JNIEXPORT jobjectArray JNICALL Java_IEnumVARIANT_Next__I(JNIEnv *env, jobject obj, jint celt)
JNIEXPORT jobjectArray JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__I
  (JNIEnv *env, jobject obj, jint celt)
{
	int i;
	// IEnumVARIANTインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return NULL;
	}

	// Nextメソッドを呼ぶ
	VARIANT* vresult = new VARIANT[celt];
	for(i=0; i<celt; i++) {
		VariantInit(&vresult[i]);
	}

	unsigned long celtFetched = 0;
	HRESULT hr = pIEnumVARIANT->Next(celt, vresult, &celtFetched);
	if(hr != 0 && hr != S_FALSE) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IEnumVARIANT.Next() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// 確保したオブジェクトをすべて破棄
		delete[] vresult;
		return NULL;
	}

	// 空のObjectの配列を作成。
	jclass clsObject = env->FindClass("java/lang/Object");
	jobjectArray jresult = env->NewObjectArray(celtFetched, clsObject, NULL);
	if(jresult==NULL) {
		// 例外をセットする
		char message[256];
		sprintf(message, "Not enough memory ");
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// 確保したオブジェクトをすべて破棄
		delete[] vresult;
		return NULL;
	}

	// 取得したオブジェクトをJavaのjava.lang.Objectに変換
	for(i=0; i<celtFetched; i++) {
		jobject objElem;
		int ret = VARIANT2jobject(env, obj, &vresult[i], &objElem);
		VARIANT_free(&vresult[i]);
		if(ret != 0) {
			// 例外をセットする
			char message[256];
			sprintf(message, "cannot convert VT=0x%X",ret);
			jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
			env->ThrowNew(clsJComException, message);
			// 確保したオブジェクトをすべて破棄
			// 各要素は破棄する必要がない。さすがＧＣ。
			delete[] vresult;
			return NULL;
		}
		env->SetObjectArrayElement(jresult, i, objElem);
	}

	delete[] vresult;
	return jresult;
}

/* **********************************************************************
 * Class:     IEnumVARIANT
 * Method:    Reset
 * Signature: ()V
 * **********************************************************************/
//JNIEXPORT void JNICALL Java_IEnumVARIANT_Reset(JNIEnv *env, jobject obj)
JNIEXPORT void JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Reset
  (JNIEnv *env, jobject obj)
{
	// IEnumVARIANTインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return;
	}

	// Resetメソッドを呼ぶ
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Reset();
	if(hr != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IEnumVARIANT.Reset() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return;
	}
}

/* **********************************************************************
 * Class:     IEnumVARIANT
 * Method:    Skip
 * Signature: (I)V
 * **********************************************************************/
//JNIEXPORT void JNICALL Java_IEnumVARIANT_Skip(JNIEnv *env, jobject obj, jint celt)
JNIEXPORT void JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Skip
  (JNIEnv *env, jobject obj, jint celt)
{
	// IEnumVARIANTインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return;
	}

	// Skipメソッドを呼ぶ
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Skip((unsigned long)celt);
	if(hr != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IEnumVARIANT.Skip(%d) failed HRESULT=0x%XL",celt, hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return;
	}
}


//======================================================================================
//	Java 1.1以前のインターフェース

JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IEnumVARIANT__1next__
  (JNIEnv *env, jobject obj)
{
	return Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__(env, obj);
}

JNIEXPORT jobjectArray JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IEnumVARIANT__1next__I
  (JNIEnv *env, jobject obj, jint celt)
{
	return Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__I(env, obj, celt);
}

JNIEXPORT void JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IEnumVARIANT__1reset
  (JNIEnv *env, jobject obj)
{
	Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Reset(env, obj);
}

JNIEXPORT void JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IEnumVARIANT__1skip
  (JNIEnv *env, jobject obj, jint celt)
{
	Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Skip(env, obj, celt);
}

