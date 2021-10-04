#include <jni.h>
//#include "IEnumVARIANT.h"

// �V�����^�C�v��JNI(jdk1.2�ȍ~)
//#include "jp_0005cne_0005cso_0005fnet_0005cga2_0005cno_0005fji_0005cjcom_0005cIEnumVARIANT.h"
// �Â��^�C�v��JNI(���Ȃ��Ƃ�jdk1.2���O)
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IEnumVARIANT.h"

#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"



/* **********************************************************************
 * Class:     IEnumVARIANT
 * Method:    Next
 * Signature: ()Ljava/lang/Object;
	�P���̃I�u�W�F�N�g�����o���܂��B
	���̃I�u�W�F�N�g���Ȃ��ꍇ��null��Ԃ��܂��B
 * **********************************************************************/
//JNIEXPORT jobject JNICALL Java_IEnumVARIANT_Next__(JNIEnv *env, jobject obj)
JNIEXPORT jobject JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__
  (JNIEnv *env, jobject obj)
{
	// IEnumVARIANT�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return NULL;
	}

	// Next���\�b�h���Ă�
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Next(1, &vresult, NULL);
	if(hr != 0 && hr != S_FALSE) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "IEnumVARIANT.Next() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// �擾�����I�u�W�F�N�g���Ō�̂Ƃ��ɂ� null ��Ԃ��B
	if(hr == S_FALSE) {
		return NULL;
	}

	// �擾�����I�u�W�F�N�g��Java��java.lang.Object�ɕϊ�
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
 * Class:     IEnumVARIANT
 * Method:    Next
 * Signature: (I)[Ljava/lang/Object;
	�w�肵�����������̃I�u�W�F�N�g�����o���܂��B
	�c�肪���Ȃ��ꍇ�́A�w�肵�����ȉ��ɂȂ�ꍇ������܂��B
	�z��̗v�f���ɒ��ӂ��ĉ������B
	celt�͂P�ȏ�̐����w�肵�ĉ������B
	���̎����͖����ɍs��Ȃ��Ă��A�����Ȃ���Next()���J��Ԃ��΂ł���B
	�������A���̂`�o�h�͊֐��Ăяo���ɂ��I�[�o�[�w�b�h�����炷���߂�
	�p�ӂ��Ă���̂ŁA��������������邽�߁A���̂`�o�h���p�ӂ��Ă���B
 * **********************************************************************/
//JNIEXPORT jobjectArray JNICALL Java_IEnumVARIANT_Next__I(JNIEnv *env, jobject obj, jint celt)
JNIEXPORT jobjectArray JNICALL Java_jp_0005cne_0005cso_1net_0005cga2_0005cno_1ji_0005cjcom_0005cIEnumVARIANT_Next__I
  (JNIEnv *env, jobject obj, jint celt)
{
	int i;
	// IEnumVARIANT�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return NULL;
	}

	// Next���\�b�h���Ă�
	VARIANT* vresult = new VARIANT[celt];
	for(i=0; i<celt; i++) {
		VariantInit(&vresult[i]);
	}

	unsigned long celtFetched = 0;
	HRESULT hr = pIEnumVARIANT->Next(celt, vresult, &celtFetched);
	if(hr != 0 && hr != S_FALSE) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "IEnumVARIANT.Next() failed HRESULT=0x%XL",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// �m�ۂ����I�u�W�F�N�g�����ׂĔj��
		delete[] vresult;
		return NULL;
	}

	// ���Object�̔z����쐬�B
	jclass clsObject = env->FindClass("java/lang/Object");
	jobjectArray jresult = env->NewObjectArray(celtFetched, clsObject, NULL);
	if(jresult==NULL) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "Not enough memory ");
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// �m�ۂ����I�u�W�F�N�g�����ׂĔj��
		delete[] vresult;
		return NULL;
	}

	// �擾�����I�u�W�F�N�g��Java��java.lang.Object�ɕϊ�
	for(i=0; i<celtFetched; i++) {
		jobject objElem;
		int ret = VARIANT2jobject(env, obj, &vresult[i], &objElem);
		VARIANT_free(&vresult[i]);
		if(ret != 0) {
			// ��O���Z�b�g����
			char message[256];
			sprintf(message, "cannot convert VT=0x%X",ret);
			jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
			env->ThrowNew(clsJComException, message);
			// �m�ۂ����I�u�W�F�N�g�����ׂĔj��
			// �e�v�f�͔j������K�v���Ȃ��B�������f�b�B
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
	// IEnumVARIANT�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return;
	}

	// Reset���\�b�h���Ă�
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Reset();
	if(hr != 0) {
		// ��O���Z�b�g����
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
	// IEnumVARIANT�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IEnumVARIANT* pIEnumVARIANT = (IEnumVARIANT*)env->GetIntField(obj, fieldID);
	if(pIEnumVARIANT == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "IEnumVARIANT not created");
		return;
	}

	// Skip���\�b�h���Ă�
	VARIANT vresult;
	VariantInit(&vresult);
	HRESULT hr = pIEnumVARIANT->Skip((unsigned long)celt);
	if(hr != 0) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "IEnumVARIANT.Skip(%d) failed HRESULT=0x%XL",celt, hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return;
	}
}


//======================================================================================
//	Java 1.1�ȑO�̃C���^�[�t�F�[�X

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

