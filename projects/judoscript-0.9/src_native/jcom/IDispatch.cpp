#include <jni.h>
#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IDispatch.h"


/* **********************************************************************
 * Class:     JCom
 * Method:    _create
 * Signature: (Ljava/lang/String;)I
 IDispatch�C���^�[�t�F�[�X���쐬���A�t�B�[���h int pIUnknown �Ɋi�[����B
 ��������� 0 ��Ԃ��A���s����ƕ��̒l��Ԃ��B
 * **********************************************************************/
JNIEXPORT void JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_create
  (JNIEnv *env, jobject obj, jstring progid)
{
	// ���݂̃����o�ϐ� pIUnknown ���擾����B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch != NULL) {
		// ��O�𓊂���
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "Already COM allocated");
		return;
	}

	// �C���X�^���X���쐬
	char* sjisProgID = jstring2sjis(env, progid);
	HRESULT hr = createInstance(sjisProgID, &pIDispatch);
	free(sjisProgID);
	if(FAILED(hr)) {
		// ��O�𓊂���
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "createInstance() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return;
	}
	// �t�B�[���h�ϐ� pIUnknown �Ɋi�[
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// �v���p�e�B�������o���B
	char* sjisProp = jstring2sjis(env, prop);

	// �v���p�e�B���擾
	VARIANT vresult;
	HRESULT hr = getProperty(pIDispatch, sjisProp, &vresult);
	free(sjisProp);

	if(hr != 0) {
		// ��O���Z�b�g����
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
		// ��O���Z�b�g����
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// �v���p�e�B�������o���B
	char* sjisProp  = jstring2sjis(env, prop);
	jsize length = env->GetArrayLength(args);
	VARIANTARG* varg = NULL;
	if(length > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*length);
		for(int i=0; i<length; i++) {
			jobject obj = env->GetObjectArrayElement(args, i);
			int ret = jobject2VARIANT(env, obj, &varg[length-i-1]);	// �t���I
			if(ret != 0) {
				free(sjisProp);
				free(varg);
				// ��O���Z�b�g����
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
		// ��O���Z�b�g����
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
	// �v���p�e�B�̒l��߂�l�ɕϊ�
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// ��O���Z�b�g����
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return;
	}

	// �v���p�e�B�������o���B
	char* sjisProp  = jstring2sjis(env, prop);
	//printf("Property=%s\n",sjisProp);

	// �l���擾
	VARIANT var;
	int ret = jobject2VARIANT(env, val, &var);
	if(ret != 0) {
		free(sjisProp);
		// ��O�𓊂���
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "Invalid argument");
		return;
	}

	// �v���p�e�B��ݒ�
	HRESULT hr = putProperty(pIDispatch, sjisProp, &var);
	VARIANT_free(&var);
	free(sjisProp);
	if(hr != 0) {
		// ��O���Z�b�g����
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// �v���p�e�B�������o���B
	char* sjisMethod  = jstring2sjis(env, method);
	// ������ϊ�
	jsize length = (args != NULL) ? env->GetArrayLength(args) : 0;
	VARIANTARG* varg = NULL;
	if(length > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*length);
		for(int i=0; i<length; i++) {
			jobject obj = env->GetObjectArrayElement(args, i);
			int ret = jobject2VARIANT(env, obj, &varg[length-i-1]);	// �t���I
			if(ret != 0) {
				free(sjisMethod);
				free(varg);
				// ��O���Z�b�g����
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
		// ��O���Z�b�g����
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
	// ���\�b�h�̖߂�l��ϊ�
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// ��O���Z�b�g����
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return 0;
	}

	// ITypeInfo�C���^�[�t�F�[�X���擾
	unsigned int cTypeInfo = 0;
	HRESULT hr = pIDispatch->GetTypeInfoCount(&cTypeInfo);
	if(FAILED(hr)) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "IDispatch::GetTypeInfoCount() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// �ʏ��0��1�B�P�ȊO�Ȃ�ITypeInfo�́u�Ȃ��v�Ƃ���B
	if(cTypeInfo != 1) return 0;

	ITypeInfo* pITypeInfo = 0;
	hr = pIDispatch -> GetTypeInfo(0, GetUserDefaultLCID(), &pITypeInfo);
	if (FAILED(hr)) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "IDispatch::GetTypeInfo() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// �擾�����B�Ăяo�����́A�g�����������Release()���ĂԂ��ƁB
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
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// ������Java������COM���ɕϊ�����B
	// Java���̃N���X�Ŕ��f���Ă���̂ŁAJava���̃N���X��VARIANT�̌X�̌^�͂P�΂P�łȂ���΂Ȃ�Ȃ�
	jsize paramlength = (pDispParams != NULL) ? env->GetArrayLength(pDispParams) : 0;
	VARIANTARG* varg = NULL;
	if(paramlength > 0) {
		varg = (VARIANTARG*)malloc(sizeof(VARIANTARG)*paramlength);
		memset(varg, 0, sizeof(VARIANTARG)*paramlength);
		for(int i=0; i<paramlength; i++) {
			jobject obj = env->GetObjectArrayElement(pDispParams, i);
			int ret = jobject2VARIANT(env, obj, &varg[paramlength-i-1]);	// �t���I
			if(ret != 0) {
				free(varg);
				// ��O���Z�b�g����
				char message[256];
				sprintf(message, "Invalid argument(index=%d)", i);
				jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
				env->ThrowNew(clsJComException, message);
				return NULL;
			}
		}
	}

	// �Ăяo��
	VARIANT vresult;
	VariantInit(&vresult);
	int argerr = -1;
	HRESULT hr = InvokeHelper(pIDispatch, dispIdMember, wFlags,
							&vresult, varg, paramlength, &argerr);
	if(FAILED(hr)) {
		if(varg!=NULL) free(varg);
		// ��O���Z�b�g����B�������ԈႦ���ꍇ�́A���̈ʒu��m�点��B
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

	// �����̂����AVT_PTR�͌��ʂ������ɔ��f������B
	// �����ɂ͐V�����I�u�W�F�N�g�𐶐����Ċ��蓖�Ă�B
	// Java��Interger���ɂ́A�l��ݒ肷�郁�\�b�h�͂Ȃ��̂ŁA
	// �V���ȃI�u�W�F�N�g�𐶐����Ċ��蓖�Ă�B
	// (�I�u�W�F�N�g�̐����͒x���̂ŃX�s�[�h���C�ɂȂ�̂����E�E�E)
	if(varg!=NULL) {
		for(int i=0; i<paramlength; i++) {
			if(varg[paramlength-i-1].vt & VT_BYREF) {
				jobject jarg = env->GetObjectArrayElement(pDispParams, i);
				int ret = feedbackVARIANT2jobject(env, obj, &varg[paramlength-i-1], jarg);	// �t���I
				if(ret != 0) {
					free(varg);
					// ��O���Z�b�g����
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

	// ���������
	if(varg!=NULL) {
		for(int i=0; i<paramlength; i++) {
			VARIANT_free(&varg[i]);
		}
		free(varg);
	}
	// ���\�b�h�̖߂�l��ϊ�
	jobject jresult;
	int ret = VARIANT2jobject(env, obj, &vresult, &jresult);
	VARIANT_free(&vresult);
	if(ret != 0) {
		// ��O���Z�b�g����
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
	IDispatch::GetIDsOfNames�͖{���A�����̂h�c����x�Ɏ���Ă��邱�Ƃ�
	�ł��邪�A���̊֐��͂P�ɑ΂��Ă����擾�ł��Ȃ��B
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IDispatch__1getIDsOfNames
  (JNIEnv *env, jobject obj, jstring name)
{
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);
	if(pIDispatch == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IDispatch not created");
		return NULL;
	}

	// �v���p�e�B�������o���B
	BSTR bstrName  = jstring2BSTR(env, name);
	DISPID dispid = 0;
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &bstrName, 1, GetUserDefaultLCID(), &dispid);
	SysFreeString(bstrName);
	if(FAILED(hr)) {
		// ��O���Z�b�g����
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



#if 0	// �K�v�Ȃ��͂�
/* **********************************************************************
 * Class:     JCom
 * Method:    release
 * Signature: ()Z
 * **********************************************************************/
JNIEXPORT jboolean JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cJCom_release
  (JNIEnv *env, jobject obj)
{
	// IDispatch�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IDispatch* pIDispatch = (IDispatch*)env->GetIntField(obj, fieldID);

	// ���ł�NULL�Ȃ�������K�v�͂Ȃ��B
	if(pIDispatch==NULL) {
		return JNI_FALSE;	// ���łɉ���ς�
	}

	// ���
	pIDispatch->Release();

	// pIUnknown���O�ɂ���B
	env->SetIntField(obj, fieldID, (jint)0);

	return JNI_TRUE;	// ����
}
#endif

//======================================================================================
//	Java 1.1�ȑO�̃C���^�[�t�F�[�X ???

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
