// GUID�֌W�̃��C�u����

#include <jni.h>
#include "atlbase.h"
#include "JCom.h"

//------------------------------------------------------------------------------
//	java��jcom.GUID�N���X����C++��GUID�\���̂��쐬����B
//	JNI�̔z��́A�S�̂ł̓I�u�W�F�N�g�Ƃ��Ĉ����B�v�f���v���~�e�B�u�^�̏ꍇ�A
//	���̔z��̃C���[�W�ŁA�����̗v�f����x�ɓ]���ł���B�������A�P�Â�
//	�ł���B
//------------------------------------------------------------------------------
void jobject2GUID(JNIEnv* env, jobject guid, GUID* IID)
{
	jclass clsGUID = env->GetObjectClass(guid);
	// int data1 �����o��
	jfieldID fieldData1 = env->GetFieldID(clsGUID, "data1", "I");
	IID->Data1 = (unsigned long)env->GetIntField(guid, fieldData1);
	// short data2 �����o��
	jfieldID fieldData2 = env->GetFieldID(clsGUID, "data2", "S");
	IID->Data2 = (unsigned short)env->GetShortField(guid, fieldData2);
	// short data3 �����o��
	jfieldID fieldData3 = env->GetFieldID(clsGUID, "data3", "S");
	IID->Data3 = (unsigned short)env->GetShortField(guid, fieldData3);
	// byte[] data4 �����o��
	jfieldID fieldData4 = env->GetFieldID(clsGUID, "data4", "[B");
	jbyteArray data4 = (jbyteArray)env->GetObjectField(guid, fieldData4);
	jbyte buf[8];
	env->GetByteArrayRegion(data4, 0, 8, buf);
	for(int i=0; i<8; i++) {
		IID->Data4[i] = (BYTE)buf[i];
	}
}

//------------------------------------------------------------------------------
//	C++����GUID�\���̂���Java����GUID�I�u�W�F�N�g�𐶐�����
//------------------------------------------------------------------------------
jobject GUID2jobject(JNIEnv* env, GUID* guid)
{
	// GUID�C���X�^���X�𐶐�
	jclass clsGUID = env->FindClass(CLASS_GUID);
	jmethodID method = env->GetMethodID(clsGUID, "<init>", "(ISSBBBBBBBB)V");
	jobject jGuid = env->NewObject(clsGUID, method, 
				(jint)guid->Data1,(jshort)guid->Data2,(jshort)guid->Data3,
				(jbyte)guid->Data4[0],(jbyte)guid->Data4[1],(jbyte)guid->Data4[2],(jbyte)guid->Data4[3],
				(jbyte)guid->Data4[4],(jbyte)guid->Data4[5],(jbyte)guid->Data4[6],(jbyte)guid->Data4[7]);
	return jGuid;
}


