// JNIでjstringを扱うための日本人の日本人による日本人のためのライブラリ。なんてね。

#include <jni.h>
#include "JCom.h"
#include "atlbase.h"
//#include "atlconv.h"

//------------------------------------------------------------------------------
//  jstringから Shift-JISの文字列に変換する。
// 使用後は戻り値を free()すること
// どうやらjstringはnull-terminateではないらしい。W2OLEやW2Aが使えないのだ。
// で、W2OLEなどのマクロの中身を見てみると、WideCharToMultiByte()等を使っている。
// これは、null-terminate以外でも使えるようにできている。
// jstrがnullのときには、NULLを返す。その場合は、戻り値はfree()する必要はない。
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

// 使用後は戻り値を SysFreeString()すること
BSTR	jstring2BSTR(JNIEnv *env, jstring jstr)
{
	const jchar* jc  = env->GetStringChars(jstr, NULL);
	int len = env->GetStringLength(jstr);
	BSTR bstr = SysAllocStringLen(jc, len);
	env->ReleaseStringChars(jstr, jc);
	return bstr;
}


// 使用後は戻り値を・・・、何もしなくてもいいのかな？
// 作成時にＶＭの管理下に入るので、freeもreleaseも必要ない？
jstring	BSTR2jstring(JNIEnv *env, BSTR bstr)
{
	int len = SysStringLen(bstr);
	return env->NewString(bstr, len);
}

#if 0

//------------------------------------------------------------------------------
//	jstringから Shift-JISの文字列に変換する。
// 使用後は戻り値を free()すること
// どうやらjstringはnull-terminateではないらしい。W2OLEやW2Aが使えないのだ。
// で、W2OLEなどのマクロの中身を見てみると、WideCharToMultiByte()等を使っている。
// これは、null-terminate以外でも使えるようにできている。
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

OLECHAR	２バイトユニコードの０終端
#endif

//------------------------------------------------------------------------------
// デバッグ用
void print_BSTR(OLECHAR* psz)
{
    char bufA[256];
    if(psz==NULL) { printf("(null)"); return; }
    WideCharToMultiByte(CP_ACP, NULL, psz, -1, bufA, 256, NULL, NULL);
	printf("%s", bufA);
}

