#include <jni.h>
#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib.h"


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _getTypeInfoCount
 * Signature: ()I
	 ITypeInfo�̐����擾����
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1getTypeInfoCount
  (JNIEnv *env, jobject obj)
{
	// ITypeLib�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeLib* pITypeLib = (ITypeLib*)env->GetIntField(obj, fieldID);
	if(pITypeLib == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeLib not created");
		return NULL;
	}

	// ITypeInfo�̐����擾
	int typeinfocount = pITypeLib->GetTypeInfoCount();
	return (jint)typeinfocount;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _getTypeInfo
 * Signature: (I)I
 	�w�肳�ꂽindex��ITypeInfo��Ԃ�
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1getTypeInfo
  (JNIEnv *env, jobject obj, jint index)
{
	// ITypeLib�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeLib* pITypeLib = (ITypeLib*)env->GetIntField(obj, fieldID);
	if(pITypeLib == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeLib not created");
		return NULL;
	}
	// �w�肳�ꂽindex��ITypeInfo���擾
	ITypeInfo* pITypeInfo = NULL;
	HRESULT hr = pITypeLib->GetTypeInfo(index, &pITypeInfo);
	if(FAILED(hr)) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeLib::GetTypeInfo(%d) FAILED HRESULT=0x%X", index, hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// int�ɂ��ĕԂ��B�Ăяo�����͎g�p���Release()���邱�ƁB
	return (jint)(int)pITypeInfo;
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _getDocumentation
 * Signature: (I)[Ljava/lang/String;
 	�w�肳�ꂽ�ԍ��̃h�L�������g���擾����B
 	index=-1�̂Ƃ��͎������g�̃h�L�������g��Ԃ��B
 * **********************************************************************/
JNIEXPORT jobjectArray JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1getDocumentation
  (JNIEnv *env, jobject obj, jint index)
{
	// ITypeLib�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeLib* pITypeLib = (ITypeLib*)env->GetIntField(obj, fieldID);
	if(pITypeLib == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeLib not created");
		return NULL;
	}

	BSTR bstrName;
	BSTR bstrDocString;
	unsigned long dwHelpContext = 0;
	BSTR bstrHelpFile;
	HRESULT hr = pITypeLib->GetDocumentation( index,
					&bstrName, &bstrDocString, &dwHelpContext, &bstrHelpFile ); 
	if(FAILED(hr)) {
		// �G���[�ɂ��Ȃ��B
		// �����ɂ̓h�L�������g���Ȃ������Ƃ��ƁA�v���I�Ȃ��̂ƕ�����ׂ��B
		return NULL;
	}
	// ������̔z��ɕϊ�����B
	char helpcontext[256];
	sprintf(helpcontext, "%d", dwHelpContext);
	
	// ���4��String�z����쐬�B
	jclass clsString = env->FindClass("java/lang/String");
	jobjectArray jresult = env->NewObjectArray(4, clsString, NULL);
	if(jresult==NULL) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "NewObjectArray(%d) FAILED ",4);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// �m�ۂ����I�u�W�F�N�g�����ׂĔj��
		if(bstrName!=NULL) SysFreeString(bstrName);
		if(bstrDocString!=NULL) SysFreeString(bstrDocString);
		if(bstrHelpFile!=NULL) SysFreeString(bstrHelpFile);
		return NULL;
	}

	// �擾�����I�u�W�F�N�g��Java��java.lang.String�ɕϊ�
	// ���ɂ́ANULL�̂��̂�����̂Œ���
	if(bstrName!=NULL) {
		jstring	jstr = BSTR2jstring(env, bstrName);
		env->SetObjectArrayElement(jresult, 0, jstr);
		SysFreeString(bstrName);
	}
	if(bstrDocString != NULL) {
		jstring jstr = BSTR2jstring(env, bstrDocString);
		env->SetObjectArrayElement(jresult, 1, jstr);
		SysFreeString(bstrDocString);
	}
	jstring jstr = BSTR2jstring(env, A2BSTR(helpcontext));
	env->SetObjectArrayElement(jresult, 2, jstr);
	if(bstrHelpFile != NULL) {
		jstring jstr = BSTR2jstring(env, bstrHelpFile);
		env->SetObjectArrayElement(jresult, 3, jstr);
		SysFreeString(bstrHelpFile);
	}

	return jresult;
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _getTLibAttr
 * Signature: ()Ljp/ne/so_net/ga2/no_ji/jcom/ITypeLib$TLibAttr;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1getTLibAttr
  (JNIEnv *env, jobject obj)
{
	// ITypeLib�C���^�[�t�F�[�X�����o���B
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeLib* pITypeLib = (ITypeLib*)env->GetIntField(obj, fieldID);
	if(pITypeLib == NULL) {
		// ��O���Z�b�g����
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeLib not created");
		return NULL;
	}

	// TYPEATTR ���擾
	TLIBATTR*  pTLibAttr = NULL;
	HRESULT hr = pITypeLib->GetLibAttr(&pTLibAttr);
	if(FAILED(hr)) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "ITypeLib::GetLibAttr() FAILED hr=0x%X.",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	// LIBID,verMajor,verMinor���擾
	GUID LibID;
	memcpy(&LibID, &pTLibAttr->guid, sizeof(GUID));
	int verMajor = pTLibAttr->wMajorVerNum;
	int verMinor = pTLibAttr->wMinorVerNum;
	pITypeLib->ReleaseTLibAttr(pTLibAttr);
	// GUID�C���X�^���X�𐶐�
	jclass clsGUID = env->FindClass(CLASS_GUID);
	jmethodID method = env->GetMethodID(clsGUID, "<init>", "(ISSBBBBBBBB)V");
	jobject jGuid = env->NewObject(clsGUID, method, 
				(jint)LibID.Data1,(jshort)LibID.Data2,(jshort)LibID.Data3,
				(jbyte)LibID.Data4[0],(jbyte)LibID.Data4[1],(jbyte)LibID.Data4[2],(jbyte)LibID.Data4[3],
				(jbyte)LibID.Data4[4],(jbyte)LibID.Data4[5],(jbyte)LibID.Data4[6],(jbyte)LibID.Data4[7]);
	// TLibAttr�C���X�^���X�𐶐� (Ljp/ne/so_net/ga2/no_ji/jcom/ITypeLib;Ljp/ne/so_net/ga2/no_ji/jcom/GUID;II)V
	jclass clsTLibAttr = env->FindClass(CLASS_TLIBATTR);
	char sig[256];
	sprintf(sig, "(L%s;L%s;II)V", CLASS_ITYPELIB, CLASS_GUID);
	jmethodID mthTLibAttr = env->GetMethodID(clsTLibAttr, "<init>", sig);
	jobject jTLibAttr = env->NewObject(clsTLibAttr, mthTLibAttr,
				obj, jGuid, (jint)verMajor, (jint)verMinor);
	return jTLibAttr;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _loadTypeLibEx
 * Signature: (Ljava/lang/String;)I
 	LoadTypeLibEx()���ĂсAITypeLib*��Ԃ��B
 	�������AREGKIND_NONE�ŁA���W�X�g���ɓo�^�͂��Ȃ��B
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1loadTypeLibEx
  (JNIEnv *env, jclass cls, jstring szFile)
{
	BSTR bstrFile = jstring2BSTR(env, szFile);
	ITypeLib* pITypeLib = NULL;
	HRESULT hr = LoadTypeLibEx(bstrFile, REGKIND_NONE, &pITypeLib);
	SysFreeString(bstrFile);
	if( FAILED(hr) ) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "LoadTypeLibEx() FAILED HRESULT=0x%Xh",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	return (jint)(int)pITypeLib;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeLib
 * Method:    _loadRegTypeLib
 * Signature: (Ljp/ne/so_net/ga2/no_ji/jcom/GUID;II)I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeLib__1loadRegTypeLib
  (JNIEnv *env, jclass cls, jobject guid, jint verMajor, jint verMinor)
{
	// guid����LIBID�ɕϊ�
	GUID LIBID;
	jobject2GUID(env, guid, &LIBID);

	// �Ăяo��
	ITypeLib* pITypeLib = NULL;
	HRESULT hr = LoadRegTypeLib(LIBID, (unsigned short)verMajor, (unsigned short)verMinor,
				 GetUserDefaultLCID(), &pITypeLib); 
	if(FAILED(hr)) {
		// ��O���Z�b�g����
		char message[256];
		sprintf(message, "LoadRegTypeLib() FAILED hr=0x%X",hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

	// �^�C�v���C�u������Ԃ�
	return (jint)(int)pITypeLib;
}

