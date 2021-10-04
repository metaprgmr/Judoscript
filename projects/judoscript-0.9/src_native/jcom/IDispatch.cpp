#include <jni.h>
#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch.h"


/* **********************************************************************
 * Class:     JCom
 * Method:    _create
 * Signature: (Ljava/lang/String;)I
 IDispatchインターフェースを作成し、フィールド int pIUnknown に格納する。
 成功すると 0 を返し、失敗すると負の値を返す。
 * **********************************************************************/
JNIEXPORT void JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_create
  (JNIEnv *env, jobject obj, jstring progid)
{
	// 現在のメンバ変数 pIUnknown を取得する。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch != NULL) {
		// 例外を投げる
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "Already COM allocated");
		return;
	}

	// インスタンスを作成
	char* sjisProgID = jstring2sjis(env, progid);
	HRESULT hr = createInstance(sjisProgID, &pIDispatch);
	free(sjisProgID);
	if(FAILED(hr)) {
		// 例外を投げる
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "createInstance() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return;
	}
	// フィールド変数 pIUnknown に格納
	env->SetIntField(obj, fieldID, (jint)pIDispatch);
	return;
}


/* **********************************************************************
 * Class:     JCom
 * Method:    _get
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_get__Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jstring prop)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// プロパティ名を取り出す。
	char* sjisProp = jstring2sjis(env, prop);

	// プロパティを取得
	VARIANT vresult;
	HRESULT hr = getProperty(pIDispatch, sjisProp, &vresult);
	free(sjisProp);

	if(hr != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "getProperty() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

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
 * Class:     JCom
 * Method:    get
 * Signature: (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_get__Ljava_lang_String_2_3Ljava_lang_Object_2
  (JNIEnv *env, jobject obj, jstring prop, jobjectArray args)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// プロパティ名を取り出す。
	char* sjisProp  = jstring2sjis(env, prop);
	jsize length = env->GetArrayLength(args);
	VARIANTARG* varg = NULL;
	if(length > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*length);
		for(int i=0; i<length; i++) {
			jobject obj = env->GetObjectArrayElement(args, i);
			int ret = jobject2VARIANT(env, obj, &varg[length-i-1]);	// 逆順！
			if(ret != 0) {
				free(sjisProp);
				free(varg);
				// 例外をセットする
				char message[256];
				sprintf(message, "Invalid argument(index=%d)", i);
				jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
				env->ThrowNew(clsJComException, message);
				return NULL;
			}
		}
	}

	VARIANT vresult;
	HRESULT hr = getPropertyArg(pIDispatch, sjisProp, varg, length, &vresult);
	free(sjisProp);
	if(hr != 0) {
		if(varg!=NULL) {
			for(int i=0; i<length; i++) {
				VARIANT_free(&varg[i]);
			}
			free(varg);
		}
		// 例外をセットする
		char message[256];
		sprintf(message, "getPropertyArg() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	if(varg!=NULL) {
		for(int i=0; i<length; i++) {
			VARIANT_free(&varg[i]);
		}
		free(varg);
	}
	// プロパティの値を戻り値に変換
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
 * Class:     JCom
 * Method:    put
 * Signature: (Ljava/lang/String;Ljava/lang/Object;)V
 * **********************************************************************/
JNIEXPORT void JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_put
  (JNIEnv *env, jobject obj, jstring prop, jobject val)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return;
	}

	// プロパティ名を取り出す。
	char* sjisProp  = jstring2sjis(env, prop);
	//printf("Property=%s\n",sjisProp);

	// 値を取得
	VARIANT var;
	int ret = jobject2VARIANT(env, val, &var);
	if(ret != 0) {
		free(sjisProp);
		// 例外を投げる
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "Invalid argument");
		return;
	}

	// プロパティを設定
	HRESULT hr = putProperty(pIDispatch, sjisProp, &var);
	VARIANT_free(&var);
	free(sjisProp);
	if(hr != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "putProperty() failed HRESULT=0x%XL", hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return;
	}
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch
 * Method:    _method
 * Signature: (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1method
  (JNIEnv *env, jobject obj, jstring method, jobjectArray args)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// プロパティ名を取り出す。
	char* sjisMethod  = jstring2sjis(env, method);
	// 引数を変換
	jsize length = (args != NULL) ? env->GetArrayLength(args) : 0;
	VARIANTARG* varg = NULL;
	if(length > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*length);
		for(int i=0; i<length; i++) {
			jobject obj = env->GetObjectArrayElement(args, i);
			int ret = jobject2VARIANT(env, obj, &varg[length-i-1]);	// 逆順！
			if(ret != 0) {
				free(sjisMethod);
				free(varg);
				// 例外をセットする
				char message[256];
				sprintf(message, "Invalid argument(index=%d)", i);
				jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
				env->ThrowNew(clsJComException, message);
				return NULL;
			}
		}
	}

	VARIANT vresult;
	HRESULT hr = invokeMethod(pIDispatch, sjisMethod, varg, length, &vresult);
	free(sjisMethod);
	if(hr != 0) {
		if(varg!=NULL) free(varg);
		// 例外をセットする
		char message[256];
		sprintf(message, "invokeMethod() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	if(varg!=NULL) {
		for(int i=0; i<length; i++) {
			VARIANT_free(&varg[i]);
		}
		free(varg);
	}
	// メソッドの戻り値を変換
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "cannot convert VT=0x%X",ret);
		jclass clsJComException = env->FindClass("JComException");
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	return jresult;
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch
 * Method:    _getTypeInfo
 * Signature: ()I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1getTypeInfo
  (JNIEnv *env, jobject obj)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return 0;
	}

	// ITypeInfoインターフェースを取得
	unsigned int cTypeInfo = 0;
	HRESULT hr = pIDispatch->GetTypeInfoCount(&cTypeInfo);
	if(FAILED(hr)) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IDispatch::GetTypeInfoCount() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// 通常は0か1。１以外ならITypeInfoは「なし」とする。
	if(cTypeInfo != 1) return 0;

	ITypeInfo* pITypeInfo = 0;
	hr = pIDispatch -> GetTypeInfo(0, GetUserDefaultLCID(), &pITypeInfo);
	if (FAILED(hr)) {
		// 例外をセットする
		char message[256];
		sprintf(message, "IDispatch::GetTypeInfo() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// 取得成功。呼び出し側は、使いおわったらRelease()を呼ぶこと。
	return (jint)(int)pITypeInfo;
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch
 * Method:    _invoke
 * Signature: (II[Ljava/lang/Object;)Ljava/lang/Object;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1invoke
  (JNIEnv *env, jobject obj, jint dispIdMember, jint wFlags, jobjectArray pDispParams)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// 引数をJava側からCOM側に変換する。
	// Java側のクラスで判断しているので、Java側のクラスとVARIANTの個々の型は１対１でなければならない
	jsize paramlength = (pDispParams != NULL) ? env->GetArrayLength(pDispParams) : 0;
	VARIANTARG* varg = NULL;
	if(paramlength > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*paramlength);
		memset(varg, 0, sizeof(VARIANTARG)*paramlength);
		for(int i=0; i<paramlength; i++) {
			jobject obj = env->GetObjectArrayElement(pDispParams, i);
			int ret = jobject2VARIANT(env, obj, &varg[paramlength-i-1]);	// 逆順！
			if(ret != 0) {
				free(varg);
				// 例外をセットする
				char message[256];
				sprintf(message, "Invalid argument(index=%d)", i);
				jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
				env->ThrowNew(clsJComException, message);
				return NULL;
			}
		}
	}

	// 呼び出す
	VARIANT vresult;
	VariantInit(&vresult);
	int argerr = -1;
	HRESULT hr = InvokeHelper(pIDispatch, dispIdMember, wFlags,
							&vresult, varg, paramlength, &argerr);
	if(FAILED(hr)) {
		if(varg!=NULL) free(varg);
		// 例外をセットする。引数を間違えた場合は、その位置を知らせる。
		char message[256];
		switch(hr) {
		case DISP_E_TYPEMISMATCH:	// 80020005h
			sprintf(message, "IDispatch::Invoke(0x%X,%d) DISP_E_TYPEMISMATCH %d",dispIdMember, wFlags, argerr);
			break;
		case DISP_E_PARAMNOTFOUND:	// 80020004h
			sprintf(message, "IDispatch::Invoke(0x%X,%d) DISP_E_PARAMNOTFOUND %d",dispIdMember, wFlags, argerr);
			break;
		default:
			sprintf(message, "IDispatch::Invoke(0x%X,%d) failed HRESULT=0x%XL",dispIdMember, wFlags, hr);
			break;
		}
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

	// 引数のうち、VT_PTRは結果を引数に反映させる。
	// 引数には新しいオブジェクトを生成して割り当てる。
	// JavaのInterger等には、値を設定するメソッドはないので、
	// 新たなオブジェクトを生成して割り当てる。
	// (オブジェクトの生成は遅いのでスピードが気になるのだが・・・)
	if(varg!=NULL) {
		for(int i=0; i<paramlength; i++) {
			if(varg[paramlength-i-1].vt & VT_BYREF) {
				jobject jarg = env->GetObjectArrayElement(pDispParams, i);
				int ret = feedbackVARIANT2jobject(env, obj, &varg[paramlength-i-1], jarg);	// 逆順！
				if(ret != 0) {
					free(varg);
					// 例外をセットする
					char message[256];
					sprintf(message, "Invalid argument(index=%d)", i);
					jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
					env->ThrowNew(clsJComException, message);
					return NULL;
				}
//				env->SetObjectArrayElement(pDispParams, i, jarg);
			}
		}
	}

	// 引数を解放
	if(varg!=NULL) {
		for(int i=0; i<paramlength; i++) {
			VARIANT_free(&varg[i]);
		}
		free(varg);
	}
	// メソッドの戻り値を変換
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// 例外をセットする
		char message[256];
		sprintf(message, "cannot convert VT=0x%X",ret);
		jclass clsJComException = env->FindClass("JComException");
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	return jresult;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch
 * Method:    _getIDsOfNames
 * Signature: (Ljava/lang/String;)I
	IDispatch::GetIDsOfNamesは本来、複数のＩＤを一度に取ってくることが
	できるが、この関数は１つに対してしか取得できない。
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1getIDsOfNames
  (JNIEnv *env, jobject obj, jstring name)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// プロパティ名を取り出す。
	BSTR bstrName  = jstring2BSTR(env, name);
	DISPID dispid = 0;
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &bstrName, 1, GetUserDefaultLCID(), &dispid);
	SysFreeString(bstrName);
	if(FAILED(hr)) {
		// 例外をセットする
		char message[256];
		char* p = jstring2sjis(env, name);
		sprintf(message, "IDispatch::GetIDsOfNames(%s) failed HRESULT=0x%X",p, hr);
		free(p);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	return (jint)dispid;
}



#if 0	// 必要ないはず
/* **********************************************************************
 * Class:     JCom
 * Method:    release
 * Signature: ()Z
 * **********************************************************************/
JNIEXPORT jboolean JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_release
  (JNIEnv *env, jobject obj)
{
	// IDispatchインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);

	// すでにNULLなら解放する必要はない。
	if(pIDispatch==NULL) {
		return JNI_FALSE;	// すでに解放済み
	}

	// 解放
	pIDispatch->Release();

	// pIUnknownを０にする。
	env->SetIntField(obj, fieldID, (jint)0);

	return JNI_TRUE;	// 成功
}
#endif

//======================================================================================
//	Java 1.1以前のインターフェース ???

JNIEXPORT void JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1create
  (JNIEnv *env, jobject obj, jstring progid)
{
	Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_create(env, obj, progid);
}

JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1get__Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jstring prop)
{
	return Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_get__Ljava_lang_String_2(env, obj, prop);
}

JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1get__Ljava_lang_String_2_3Ljava_lang_Object_2
  (JNIEnv *env, jobject obj, jstring prop, jobjectArray args)
{
	return Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_get__Ljava_lang_String_2_3Ljava_lang_Object_2(env, obj, prop, args);
}

JNIEXPORT void JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1put
  (JNIEnv *env, jobject obj, jstring prop, jobject val)
{
	Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_put(env, obj, prop, val);
}
