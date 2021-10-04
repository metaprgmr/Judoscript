// JNI��jstring���������߂̓��{�l�̓��{�l�ɂ����{�l�̂��߂̃��C�u�����B�Ȃ�ĂˁB

#include <jni.h>
#include "JCom.h"
#include "atlbase.h"
//#include "atlconv.h"

//------------------------------------------------------------------------------
//  jstring���� Shift-JIS�̕�����ɕϊ�����B
// �g�p��͖߂�l�� free()���邱��
// �ǂ����jstring��null-terminate�ł͂Ȃ��炵���BW2OLE��W2A���g���Ȃ��̂��B
// �ŁAW2OLE�Ȃǂ̃}�N���̒��g�����Ă݂�ƁAWideCharToMultiByte()�����g���Ă���B
// ����́Anull-terminate�ȊO�ł��g����悤�ɂł��Ă���B
// jstr��null�̂Ƃ��ɂ́ANULL��Ԃ��B���̏ꍇ�́A�߂�l��free()����K�v�͂Ȃ��B
//------------------------------------------------------------------------------
char* jstring2sjis(JNIEnv *env, jstring jstr)
{
    if(jstr==NULL) return NULL;
    const jchar* jc  = env->GetStringChars(jstr, NULL);
    int len = env->GetStringLength(jstr);
    char* sjis = (char*)malloc(len*2+1);
    int bytes = WideCharToMultiByte(CP_ACP, 0, jc, len, sjis, len*2+1, NULL, NULL);
    sjis[bytes] = '\0';
    env->ReleaseStringChars(jstr, jc);
    return sjis;
}

// �g�p��͖߂�l�� SysFreeString()���邱��
BSTR	jstring2BSTR(JNIEnv *env, jstring jstr)
{
	const jchar* jc  = env->GetStringChars(jstr, NULL);
	int len = env->GetStringLength(jstr);
	BSTR bstr = SysAllocStringLen(jc, len);
	env->ReleaseStringChars(jstr, jc);
	return bstr;
}


// �g�p��͖߂�l���E�E�E�A�������Ȃ��Ă������̂��ȁH
// �쐬���ɂu�l�̊Ǘ����ɓ���̂ŁAfree��release���K�v�Ȃ��H
jstring	BSTR2jstring(JNIEnv *env, BSTR bstr)
{
	int len = SysStringLen(bstr);
	return env->NewString(bstr, len);
}

#if 0

//------------------------------------------------------------------------------
//	jstring���� Shift-JIS�̕�����ɕϊ�����B
// �g�p��͖߂�l�� free()���邱��
// �ǂ����jstring��null-terminate�ł͂Ȃ��炵���BW2OLE��W2A���g���Ȃ��̂��B
// �ŁAW2OLE�Ȃǂ̃}�N���̒��g�����Ă݂�ƁAWideCharToMultiByte()�����g���Ă���B
// ����́Anull-terminate�ȊO�ł��g����悤�ɂł��Ă���B
//------------------------------------------------------------------------------
jstring sjis2jstring(JNIEnv *env, char* sjis)
{
int MultiByteToWideChar( 
UINT CodePage, // code page 
DWORD dwFlags, // character-type options 
LPCSTR lpMultiByteStr, // address of string to map 
int cchMultiByte, 		// number of characters in string 
LPWSTR lpWideCharStr, // address of wide-character buffer 
int cchWideChar // size of buffer 
); 

typedef OLECHAR *BSTR;

OLECHAR	�Q�o�C�g���j�R�[�h�̂O�I�[
#endif

//------------------------------------------------------------------------------
// �f�o�b�O�p
void print_BSTR(OLECHAR* psz)
{
    char bufA[256];
    if(psz==NULL) { printf("(null)"); return; }
    WideCharToMultiByte(CP_ACP, NULL, psz, -1, bufA, 256, NULL, NULL);
	printf("%s", bufA);
}

