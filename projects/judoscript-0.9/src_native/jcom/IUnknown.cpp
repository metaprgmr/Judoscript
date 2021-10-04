#include <jni.h>
//#include "JCom.h"

// �V�����^�C�v��JNI(jdk1.2�ȍ~)
//#include "jp_0005cne_0005cso_0005fnet_0005cga2_0005cno_0005fji_0005cjcom_0005cJCom.h"
// �Â��^�C�v��JNI(���Ȃ��Ƃ�jdk1.2���O)
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
	// IUnknown�C���^�[�t�F�[�X�̃|�C���^�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	// ��������IID���擾
	GUID IID;
	jobject2GUID(env, guid, &IID);

	// IUnknown::QueryInterface���ĂԁB
	void* pObject = NULL;
	HRESULT hr = pIUnknown->QueryInterface(IID, &pObject);
	if(hr==E_NOINTERFACE) {		// 0x80004002L
		return (jint)0;		// no interface
	}
	if(FAILED(hr)) {
		// ��O�𓊂���
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "IUnknown.queryInterface() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// IUnknown�C���^�[�t�F�[�X�̃|�C���^��Ԃ��B
	return (jint)(int)pObject;	// ����I��
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _release
 * Signature: ()Z
 * **********************************************************************/
JNIEXPORT jboolean JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1release
  (JNIEnv *env, jobject obj)
{
	// IUnknown�C���^�[�t�F�[�X�̃|�C���^�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	// ���ł�NULL�Ȃ�������K�v�͂Ȃ��B
	if(pIUnknown==NULL) {
		return JNI_FALSE;	// ���łɉ���ς�
	}

	// ���
	pIUnknown->Release();

	// pIUnknown���O�ɂ���B
//	env->SetIntField(obj, fieldID, (jint)0);

	return JNI_TRUE;	// ����
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_IUnknown
 * Method:    _addRef
 * Signature: ()I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown__1addRef
  (JNIEnv *env, jobject obj)
{
	// IUnknown�C���^�[�t�F�[�X�̃|�C���^�����o���B
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
	// ��������CLSID���擾
	GUID IID;
	jobject2GUID(env, iid, &IID);
	// CLSID����ProgID���擾
	BSTR bstrProgID = NULL;
	HRESULT hr = ProgIDFromCLSID(IID, &bstrProgID);
	if(FAILED(hr)) {
		printf("hr=%Xh \n", hr);
		return (jstring)NULL;
	}
	// BSTR����jstring�ɕϊ�
	jstring jProgID = BSTR2jstring(env, bstrProgID);
	SysFreeString(bstrProgID);
	return jProgID;
}

/*
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_IUnknown_queryInterface
  (JNIEnv *env, jobject obj, jobject guid)
{
	// IUnknown�C���^�[�t�F�[�X�̃|�C���^�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	IUnknown* pIUnknown = (IUnknown*)env->GetIntField(obj, fieldID);

	GUID IID;
	jobject2GUID(env, guid, &IID);

	void* pObject = NULL;
	HRESULT hr = pIUnknown->QueryInterface(IID, &pObject);
	if(FAILED(hr)) {
		// ��O�𓊂���
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "IUnknown.queryInterface() failed HRESULT=0x%XL",hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// IUnknown(JComManager)���ĂԁB���̍ۂɁA������JComManager jcm��n��
	jclass clsIUnknown = env->FindClass(CLASS_IUNKNOWN);
	jmethodID method = env->GetMethodID(clsIUnknown, "<init>", "(Ljp/ne/so_net/ga2/no_ji/jcom/JComManager;)V");
	jfieldID jcmID = env->GetFieldID(clsIUnknown, "jcm", "Ljp/ne/so_net/ga2/no_ji/jcom/JComManager;");
	jobject jcm = env->GetObjectField(obj,  jcmID);
	jobject result = env->NewObject(clsIUnknown, method, jcm);
	// �������ꂽ�I�u�W�F�N�g��_pIUnknown�t�B�[���h�� var->punkVal ���Z�b�g����B
	jfieldID field_pIUnknown = env->GetFieldID(clsIUnknown, "pIUnknown", "I");
	env->SetIntField(jcm, field_pIUnknown, (jint)(int)pObject);

	return result;	// ����I��
}
*/
