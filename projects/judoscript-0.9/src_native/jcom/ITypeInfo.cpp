#include <jni.h>
#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo.h"


static void type2str(char* sig, TYPEDESC* tdesc, ITypeInfo* pITypeInfo);
static jobject newElemDesc(JNIEnv *env, jobject obj, ELEMDESC* elem, ITypeInfo* pITypeInfo);

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getTypeAttr
 * Signature: ()Ljp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$TypeAttr;
	TextAttrを返す。とりあえず、必要最小限のものしか返していない。
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getTypeAttr
  (JNIEnv *env, jobject obj)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return NULL;
	}
	// TYPEATTR を取得
	TYPEATTR*  pAttr = NULL;
	HRESULT hr = pITypeInfo->GetTypeAttr(&pAttr);
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetTypeAttr() FAILED HRESULT=0x%X", hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

	// GUIDを作る。
	//	public GUID(int data1, short data2, short data3,
	//			byte data40, byte data41, byte data42, byte data43,
	//			byte data44, byte data45, byte data46, byte data47)
	jclass clsGUID = env->FindClass(CLASS_GUID);
	jmethodID mtdGUID = env->GetMethodID(clsGUID, "<init>", "(ISSBBBBBBBB)V");
	jobject iid = env->NewObject(clsGUID, mtdGUID,
			pAttr->guid.Data1,
			pAttr->guid.Data2,
			pAttr->guid.Data3,
			pAttr->guid.Data4[0],
			pAttr->guid.Data4[1],
			pAttr->guid.Data4[2],
			pAttr->guid.Data4[3],
			pAttr->guid.Data4[4],
			pAttr->guid.Data4[5],
			pAttr->guid.Data4[6],
			pAttr->guid.Data4[7]);

	// コンストラクタ TypeAttr(GUID IID, int typekind, int cFuncs, int cVars)を呼ぶ
	jclass clsTypeAttr = env->FindClass(CLASS_TYPEATTR);
	char sig[256];
	sprintf(sig, "(L%s;L%s;IIII)V", CLASS_ITYPEINFO, CLASS_GUID);
	jmethodID method = env->GetMethodID(clsTypeAttr, "<init>", sig);
	jobject result = env->NewObject(clsTypeAttr, method, 
				obj, 
				iid, pAttr->typekind, pAttr->cFuncs, pAttr->cVars, pAttr->cImplTypes);

	pITypeInfo->ReleaseTypeAttr(pAttr);
	return result;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getFuncDesc
 * signature: (I)Ljp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$FuncDesc;
	FUNCDESCを返す。これはもっとも複雑で厄介だ。
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getFuncDesc
		  (JNIEnv *env, jobject obj, jint index)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return NULL;
	}
	// FUNCDESCを取得。
	FUNCDESC *pFuncDesc = NULL;
	HRESULT hr = pITypeInfo->GetFuncDesc((unsigned int)index, &pFuncDesc);
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetFuncDesc(%d) FAILED HRESULT=0x%X", index, hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}


	// まず、引数の lprgelemdescParam を作成する。
	jobjectArray descParams = NULL;
	jclass clsElemDesc = env->FindClass(CLASS_ELEMDESC);
	if(pFuncDesc->cParams > 0) {
		// 空のITypeInfo.ElemDesc配列を作成。
		descParams = env->NewObjectArray(pFuncDesc->cParams, clsElemDesc, NULL);
		if(descParams==NULL) {
			// 例外をセットする
			char message[256];
			sprintf(message, "NewObjectArray(%d) FAILED", pFuncDesc->cParams);
			jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
			env->ThrowNew(clsJComException, message);
			// 確保したオブジェクトをすべて破棄
			pITypeInfo->ReleaseFuncDesc(pFuncDesc);
			return NULL;
		}
		// ElemDescを作る。配列にセット
		jobject descParam;
		for(int i=0; i<pFuncDesc->cParams; i++) {
			descParam = newElemDesc(env, obj, &pFuncDesc->lprgelemdescParam[i],pITypeInfo);
			env->SetObjectArrayElement(descParams, i, descParam);
		}
	}
	// 続いて、戻り値のElemDescを作成する
	jobject descFunc = newElemDesc(env, obj, &pFuncDesc->elemdescFunc, pITypeInfo);
	// コンストラクタ FuncDesc(int memid, int invkind, ElemDesc[] elemdescParam, ElemDesc elemdescFunc)を呼ぶ
	jclass clsFuncDesc = env->FindClass(CLASS_FUNCDESC);
	char sig[256];
	sprintf(sig, "(L%s;II[L%s;L%s;)V", CLASS_ITYPEINFO, CLASS_ELEMDESC, CLASS_ELEMDESC);
	jmethodID method = env->GetMethodID(clsFuncDesc, "<init>", sig);
	jobject result = env->NewObject(clsFuncDesc, method, 
				obj, (jint)pFuncDesc->memid, (jint)pFuncDesc->invkind,
				descParams, descFunc);

	pITypeInfo->ReleaseFuncDesc(pFuncDesc);
	return result;	// 正常終了
}


// ELEMDESC構造体からITypeInfo.ElemDescクラスを生成
jobject newElemDesc(JNIEnv *env, jobject obj, ELEMDESC* elem, ITypeInfo* pITypeInfo)
{
	// 型を文字列に直す。
	char sig[1024];	// これだけあれば足りるだろう
	type2str(sig, &elem->tdesc, pITypeInfo);
	jstring strsig = env->NewStringUTF(sig);

	// コンストラクタ ElemDesc(String typedesc, int idl)を呼ぶ。
	// ローカルクラスは第1引数にグローバルクラスが必要？！
	jclass clsElemDesc = env->FindClass(CLASS_ELEMDESC);
	char sigElemDesc[256];
	sprintf(sigElemDesc,"(L%s;Ljava/lang/String;I)V", CLASS_ITYPEINFO);
	jmethodID method = env->GetMethodID(clsElemDesc, "<init>", sigElemDesc);
	jobject result = env->NewObject(clsElemDesc, method, obj, strsig, (jint)elem->idldesc.wIDLFlags);
	return result;	// 正常終了
}

#if 1
//	TYPEDESCを忠実に変換。解釈はＪａｖａ側に任せる。
void type2str(char* sig, TYPEDESC* tdesc, ITypeInfo* pITypeInfo)
{
	USES_CONVERSION;

	switch(tdesc->vt) {
	case VT_EMPTY:				strcpy(sig, "VT_EMPTY");	return;	break;		// これでいいのか？
	case VT_NULL:				strcpy(sig, "VT_NULL");		return;	break;		// これでいいのか？
	case VT_I2:					strcpy(sig, "VT_I2");		return;	break;
	case VT_I4:					strcpy(sig, "VT_I4");		return;	break;
	case VT_R4:					strcpy(sig, "VT_R4");		return;	break;
	case VT_R8:					strcpy(sig, "VT_R8");		return;	break;
	case VT_CY:					strcpy(sig, "VT_CY");		return;	break;
	case VT_DATE:				strcpy(sig, "VT_DATE");		return;	break;
	case VT_BSTR:				strcpy(sig, "VT_BSTR");		return;	break;
	case VT_DISPATCH:			strcpy(sig, "VT_DISPATCH");	return;	break;
	case VT_ERROR:				strcpy(sig, "VT_ERROR");	return;	break;	// これでいいのか？
	case VT_BOOL:				strcpy(sig, "VT_BOOL");		return;	break;
	case VT_VARIANT:			strcpy(sig, "VT_VARIANT");	return;	break;
	case VT_UNKNOWN:			strcpy(sig, "VT_UNKNOWN");	return;	break;
	case VT_DECIMAL:			strcpy(sig, "VT_DECIMAL");	return;	break;	// ????
	case VT_I1:					strcpy(sig, "VT_I1");		return;	break;
	case VT_UI1:				strcpy(sig, "VT_UI1");		return;	break;
	case VT_UI2:				strcpy(sig, "VT_UI2");		return;	break;
	case VT_UI4:				strcpy(sig, "VT_UI4");		return;	break;
	case VT_I8:					strcpy(sig, "VT_I8");		return;	break;
	case VT_UI8:				strcpy(sig, "VT_UI8");		return;	break;
	case VT_INT:				strcpy(sig, "VT_INT");		return;	break;
	case VT_UINT:				strcpy(sig, "VT_UINT");		return;	break;
	case VT_VOID:				strcpy(sig, "VT_VOID");		return;	break;
	case VT_HRESULT:			strcpy(sig, "VT_HRESULT");	return;	break;
	case VT_PTR:
		strcpy(sig,"VT_PTR+");
		type2str(sig+strlen(sig), &tdesc->lptdesc[0], pITypeInfo);
		return;
	case VT_SAFEARRAY:
		strcpy(sig,"VT_SAFEARRAY+");
		type2str(sig+strlen(sig), &tdesc->lptdesc[0], pITypeInfo);
		return;

	case VT_CARRAY:				strcpy(sig, "VT_CARRAY");	return;	break;	// ????
	case VT_USERDEFINED:
		{
		ITypeInfo* pUserTypeInfo = NULL;
		HRESULT hr = pITypeInfo->GetRefTypeInfo( tdesc->hreftype, &pUserTypeInfo ); 
		BSTR bstrName;
		hr = pUserTypeInfo->GetDocumentation( MEMBERID_NIL, &bstrName, NULL, NULL, NULL ); 
		if(SUCCEEDED(hr)) {
			sprintf(sig,"VT_USERDEFINED(%x):%s", tdesc->hreftype, OLE2A(bstrName));	// パッケージ名はどうする？
			SysFreeString(bstrName);
		}else {
			sprintf(sig,"VT_USERDEFINED(%x):???", tdesc->hreftype);
		}
		pUserTypeInfo->Release();
		return;
		}
	case VT_LPSTR:				strcpy(sig, "VT_LPSTR");			return;	break;	// これでいいのか？
	case VT_LPWSTR:				strcpy(sig, "VT_LPWSTR");			return;	break;	// これでいいのか？
	case VT_FILETIME:			strcpy(sig, "VT_FILETIME");			return;	break;	// ????
	case VT_BLOB:				strcpy(sig, "VT_BLOB");				return;	break;	// ????
	case VT_STREAM:				strcpy(sig, "VT_STREAM");			return;	break;	// ????
	case VT_STORAGE:			strcpy(sig, "VT_STORAGE");			return;	break;	// ????
	case VT_STREAMED_OBJECT:	strcpy(sig, "VT_STREAMED_OBJECT");	return;	break;	// ????
	case VT_STORED_OBJECT:		strcpy(sig, "VT_STORED_OBJECT");	return;	break;	// ????
	case VT_BLOB_OBJECT:		strcpy(sig, "VT_BLOB_OBJECT");		return;	break;	// ????
	case VT_CF:					strcpy(sig, "VT_CF");				return;	break;	// ????
	case VT_CLSID:				strcpy(sig, "VT_CLSID");			return;	break;	// ????
	default:					strcpy(sig, "???");					return;	break;	// ????
    }
	return;
}
#elif 0
//	TYPEDESCをJavaの型名に変換
void type2str(char* sig, TYPEDESC* tdesc, ITypeInfo* pITypeInfo)
{
	USES_CONVERSION;

	switch(tdesc->vt) {
	case VT_EMPTY:				strcpy(sig, "void");	return;	break;		// これでいいのか？
	case VT_NULL:				strcpy(sig, "void");	return;	break;		// これでいいのか？
	case VT_I2:					strcpy(sig, "short");	return;	break;
	case VT_I4:					strcpy(sig, "int");	return;	break;
	case VT_R4:					strcpy(sig, "float");	return;	break;
	case VT_R8:					strcpy(sig, "double");	return;	break;
	case VT_CY:					sprintf(sig, "%s",CLASS_VARIANT_CURRENCY_DOT);	return;	break;
	case VT_DATE:				strcpy(sig, "java.util.Date");		return;	break;
	case VT_BSTR:				strcpy(sig, "String");		return;	break;
	case VT_DISPATCH:			sprintf(sig, "%s",CLASS_IDISPATCH_DOT);	return;	break;
	case VT_ERROR:				strcpy(sig, "String");		return;	break;	// これでいいのか？
	case VT_BOOL:				strcpy(sig, "boolean");	return;	break;
	case VT_VARIANT:			strcpy(sig, "Object");		return;	break;
	case VT_UNKNOWN:			sprintf(sig, "%s",CLASS_IUNKNOWN_DOT);	return;	break;
	case VT_DECIMAL:			strcpy(sig, "@@@@VT_DECIMAL");			return;	break;	// ????
	case VT_I1:					strcpy(sig, "byte");	return;	break;
	case VT_UI1:				strcpy(sig, "byte");	return;	break;
	case VT_UI2:				strcpy(sig, "short");	return;	break;
	case VT_UI4:				strcpy(sig, "int");	return;	break;
	case VT_I8:					strcpy(sig, "long");	return;	break;
	case VT_UI8:				strcpy(sig, "long");	return;	break;
	case VT_INT:				strcpy(sig, "int");	return;	break;
	case VT_UINT:				strcpy(sig, "int");	return;	break;
	case VT_VOID:				strcpy(sig, "void");	return;	break;
	case VT_HRESULT:			strcpy(sig, "int");	return;	break;
	case VT_PTR:
	case VT_SAFEARRAY:
		type2str(sig, &tdesc->lptdesc[0], pITypeInfo);
		strcat(sig,"[]");
		return;

	case VT_CARRAY:				strcpy(sig, "@@@@VT_CARRAY");			return;	break;	// ????
	case VT_USERDEFINED:
		{
		ITypeInfo* pUserTypeInfo = NULL;
		HRESULT hr = pITypeInfo->GetRefTypeInfo( tdesc->hreftype, &pUserTypeInfo ); 
		BSTR bstrName;
		hr = pUserTypeInfo->GetDocumentation( MEMBERID_NIL, &bstrName, NULL, NULL, NULL ); 
		if(SUCCEEDED(hr)) {
			sprintf(sig,"%s", OLE2A(bstrName));	// パッケージ名はどうする？
			SysFreeString(bstrName);
		}else {
			sprintf(sig,"VT_USERDEFINED???");
		}
		pUserTypeInfo->Release();
		return;
		}
	case VT_LPSTR:				strcpy(sig, "String");		return;	break;	// これでいいのか？
	case VT_LPWSTR:				strcpy(sig, "String");		return;	break;	// これでいいのか？
	case VT_FILETIME:			strcpy(sig, "@@@@VT_FILETIME");		return;	break;	// ????
	case VT_BLOB:				strcpy(sig, "@@@@VT_BLOB");			return;	break;	// ????
	case VT_STREAM:				strcpy(sig, "@@@@VT_STREAM");			return;	break;	// ????
	case VT_STORAGE:			strcpy(sig, "@@@@VT_STORAGE");			return;	break;	// ????
	case VT_STREAMED_OBJECT:	strcpy(sig, "@@@@VT_STREAMED_OBJECT");	return;	break;	// ????
	case VT_STORED_OBJECT:		strcpy(sig, "@@@@VT_STORED_OBJECT");	return;	break;	// ????
	case VT_BLOB_OBJECT:		strcpy(sig, "@@@@VT_BLOB_OBJECT");		return;	break;	// ????
	case VT_CF:					strcpy(sig, "@@@@VT_CF");				return;	break;	// ????
	case VT_CLSID:				strcpy(sig, "@@@@VT_CLSID");			return;	break;	// ????
	default:					strcpy(sig, "@@@@???");				return;	break;	// ????
    }
	return;
}
#else
//	TYPEDESCをJavaの型シグニチャーに変換
void type2str(char* sig, TYPEDESC* tdesc, ITypeInfo* pITypeInfo)
{
	USES_CONVERSION;

	switch(tdesc->vt) {
	case VT_EMPTY:				strcpy(sig, "V");	return;	break;		// これでいいのか？
	case VT_NULL:				strcpy(sig, "V");	return;	break;		// これでいいのか？
	case VT_I2:					strcpy(sig, "S");	return;	break;
	case VT_I4:					strcpy(sig, "I");	return;	break;
	case VT_R4:					strcpy(sig, "F");	return;	break;
	case VT_R8:					strcpy(sig, "D");	return;	break;
	case VT_CY:					sprintf(sig, "L%s;",CLASS_VARIANT_CURRENCY);	return;	break;
	case VT_DATE:				strcpy(sig, "Ljava/util/Date;");		return;	break;
	case VT_BSTR:				strcpy(sig, "Ljava/lang/String;");		return;	break;
	case VT_DISPATCH:			sprintf(sig, "L%s;",CLASS_IDISPATCH);	return;	break;
	case VT_ERROR:				strcpy(sig, "Ljava/lang/String;");		return;	break;	// これでいいのか？
	case VT_BOOL:				strcpy(sig, "Z");	return;	break;
	case VT_VARIANT:			strcpy(sig, "Ljava/lang/Object;");		return;	break;
	case VT_UNKNOWN:			sprintf(sig, "L%s;",CLASS_IUNKNOWN);	return;	break;
	case VT_DECIMAL:			strcpy(sig, "@@@@VT_DECIMAL");			return;	break;	// ????
	case VT_I1:					strcpy(sig, "B");	return;	break;
	case VT_UI1:				strcpy(sig, "B");	return;	break;
	case VT_UI2:				strcpy(sig, "S");	return;	break;
	case VT_UI4:				strcpy(sig, "I");	return;	break;
	case VT_I8:					strcpy(sig, "J");	return;	break;
	case VT_UI8:				strcpy(sig, "J");	return;	break;
	case VT_INT:				strcpy(sig, "I");	return;	break;
	case VT_UINT:				strcpy(sig, "I");	return;	break;
	case VT_VOID:				strcpy(sig, "V");	return;	break;
	case VT_HRESULT:			strcpy(sig, "I");	return;	break;
	case VT_PTR:
	case VT_SAFEARRAY:
		sig[0] = '[';
		type2str(sig+1, &tdesc->lptdesc[0], pITypeInfo);
		return;

	case VT_CARRAY:				strcpy(sig, "@@@@VT_CARRAY");			return;	break;	// ????
	case VT_USERDEFINED:
		{
		ITypeInfo* pUserTypeInfo = NULL;
		HRESULT hr = pITypeInfo->GetRefTypeInfo( tdesc->hreftype, &pUserTypeInfo ); 
		BSTR bstrName;
		hr = pUserTypeInfo->GetDocumentation( MEMBERID_NIL, &bstrName, NULL, NULL, NULL ); 
		if(SUCCEEDED(hr)) {
			sprintf(sig,"L%s;", OLE2A(bstrName));	// パッケージ名はどうする？
			SysFreeString(bstrName);
		}else {
			sprintf(sig,"L???;");
		}
		pUserTypeInfo->Release();
		return;
		}
	case VT_LPSTR:				strcpy(sig, "Ljava/lang/String;");		return;	break;	// これでいいのか？
	case VT_LPWSTR:				strcpy(sig, "Ljava/lang/String;");		return;	break;	// これでいいのか？
	case VT_FILETIME:			strcpy(sig, "@@@@VT_FILETIME");		return;	break;	// ????
	case VT_BLOB:				strcpy(sig, "@@@@VT_BLOB");			return;	break;	// ????
	case VT_STREAM:				strcpy(sig, "@@@@VT_STREAM");			return;	break;	// ????
	case VT_STORAGE:			strcpy(sig, "@@@@VT_STORAGE");			return;	break;	// ????
	case VT_STREAMED_OBJECT:	strcpy(sig, "@@@@VT_STREAMED_OBJECT");	return;	break;	// ????
	case VT_STORED_OBJECT:		strcpy(sig, "@@@@VT_STORED_OBJECT");	return;	break;	// ????
	case VT_BLOB_OBJECT:		strcpy(sig, "@@@@VT_BLOB_OBJECT");		return;	break;	// ????
	case VT_CF:					strcpy(sig, "@@@@VT_CF");				return;	break;	// ????
	case VT_CLSID:				strcpy(sig, "@@@@VT_CLSID");			return;	break;	// ????
	default:					strcpy(sig, "@@@@???");				return;	break;	// ????
    }
	return;
}

#endif		// いろんなアプローチを採ったが、Nativeは素直に、皺寄せはJavaに、がラク。


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getDocumentation
 * signature: (I)[Ljava/lang/String;
 	指定されたメンバＩＤのドキュメントを返す。
 	memidが-1のときはこのオブジェクトに対するドキュメントを返す。
 * **********************************************************************/
JNIEXPORT jobjectArray JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getDocumentation
  (JNIEnv *env, jobject obj, jint memid)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return NULL;
	}

	BSTR bstrName;
	BSTR bstrDocString;
	unsigned long dwHelpContext = 0;
	BSTR bstrHelpFile;
	HRESULT hr = pITypeInfo->GetDocumentation( (MEMBERID)memid,
					&bstrName, &bstrDocString, &dwHelpContext, &bstrHelpFile ); 
	if(FAILED(hr)) {
		/*
			Excel.Applicationの場合、大半のメンバは 8002802B TYPE_E_ELEMENTNOTFOUND を返す。
			つまり、ドキュメントがないということ。
			だから、エラー処理はせずに軽～くながす。
		*/
		return NULL;
	}
	// 文字列の配列に変換する。
	char helpcontext[256];
	sprintf(helpcontext, "%d", dwHelpContext);
	
	// 空の4つのString配列を作成。
	jclass clsString = env->FindClass("java/lang/String");
	jobjectArray jresult = env->NewObjectArray(4, clsString, NULL);
	if(jresult==NULL) {
		// 例外をセットする
		char message[256];
		sprintf(message, "Not enough memory ");
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// 確保したオブジェクトをすべて破棄
		if(bstrName!=NULL) SysFreeString(bstrName);
		if(bstrDocString!=NULL) SysFreeString(bstrDocString);
		if(bstrHelpFile!=NULL) SysFreeString(bstrHelpFile);
		return NULL;
	}

	// 取得したオブジェクトをJavaのjava.lang.Stringに変換
	// 中には、NULLのものもあるので注意
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
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getNames
 * Signature: (I)[Ljava/lang/String;
 * **********************************************************************/
JNIEXPORT jobjectArray JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getNames
  (JNIEnv *env, jobject obj, jint memid)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return NULL;
	}

	// 名前を表示。どうやら、最初の名前が関数名のようだ。
	unsigned int cNames;
	const unsigned int MAX_NAMES = 100;
	BSTR rgNames[MAX_NAMES];
	HRESULT hr = pITypeInfo->GetNames(memid, rgNames, MAX_NAMES, &cNames);
	if(FAILED(hr)) {
		// 例外をセットする
		char message[256];
		sprintf(message, "ITypeInfo::GetNames(%d) FAILED. HRESULT=0x%X",memid, hr);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}
	if(cNames==0) return NULL;

	// 空のString配列を作成。
	jclass clsString = env->FindClass("java/lang/String");
	jobjectArray jresult = env->NewObjectArray(cNames, clsString, NULL);
	if(jresult==NULL) {
		// 例外をセットする
		char message[256];
		sprintf(message, "NewObjectArray(%d) FAILED ", cNames);
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		// 確保したオブジェクトをすべて破棄
		for(int i=0; i<cNames; i++) {
			SysFreeString(rgNames[i]);
		}
		return NULL;
	}

	// 取得したオブジェクトをJavaのjava.lang.Stringに変換
	// 中には、NULLのものもあるので注意
	for(int i=0; i<cNames; i++) {
		jstring	jstr = BSTR2jstring(env, rgNames[i]);
		env->SetObjectArrayElement(jresult, i, jstr);
		SysFreeString(rgNames[i]);
	}
	return jresult;
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getTypeLib
 * Signature: ()I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getTypeLib
  (JNIEnv *env, jobject obj)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return 0;
	}

	// ITypeLibインターフェースを取得
	ITypeLib* pITypeLib = NULL;
	unsigned int index = 0;
	HRESULT hr = pITypeInfo ->GetContainingTypeLib(&pITypeLib, &index);
	if(FAILED(hr)) {
		// 例外をセットする
		char message[256];
		sprintf(message, "ITypeInfo::GetContainingTypeLib() FAILED");
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	
	return (jint)(int)pITypeLib;
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getVarDesc
 * Signature: (I)Ljp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$VarDesc;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getVarDesc
  (JNIEnv *env, jobject obj, jint index)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return NULL;
	}

	// VARDESCを取得。
	VARDESC *pVarDesc = NULL;
	HRESULT hr = pITypeInfo->GetVarDesc((unsigned int)index, &pVarDesc);
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetVarDesc(%d) FAILED HRESULT=0x%X", index, hr);
		env->ThrowNew(clsJComException, message);
		return NULL;
	}

	// VarDescに必要な情報を集める。
	jobject descVar = newElemDesc(env, obj, &pVarDesc->elemdescVar, pITypeInfo);
	jobject jValue = NULL;
	if(pVarDesc->varkind == VAR_CONST) {
		int ret = VARIANT2jobject(env, obj, pVarDesc->lpvarValue, &jValue);
		if(ret != 0) {
			// 例外をセットする
			char message[256];
			sprintf(message, "cannot convert VT=0x%X",ret);
			jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
			env->ThrowNew(clsJComException, message);
			pITypeInfo->ReleaseVarDesc(pVarDesc);
			return NULL;
		}
	}

	// コンストラクタ (int memid, int varkind, ElemDesc elemdescVar, Object varValue)を呼ぶ
	jclass clsVarDesc = env->FindClass(CLASS_VARDESC);
	char sig[256];
	sprintf(sig, "(L%s;IIL%s;L%s;)V", CLASS_ITYPEINFO, CLASS_ELEMDESC, "java/lang/Object");
	jmethodID method = env->GetMethodID(clsVarDesc, "<init>", sig);
	jobject result = env->NewObject(clsVarDesc, method, 
				obj, (jint)pVarDesc->memid, (jint)pVarDesc->varkind,
				descVar, jValue);

	// VARDESCを解放
	pITypeInfo->ReleaseVarDesc(pVarDesc);
	return result;	// 正常終了
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getImplType
 * Signature: (I)I
 	実装しているTypeInfoを返す
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getImplType
  (JNIEnv *env, jobject obj, jint index)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return 0;
	}

	// 参照している型を取得する
	HREFTYPE hRefType = NULL;
	HRESULT hr = pITypeInfo->GetRefTypeOfImplType((unsigned int)index, &hRefType);
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetRefTypeOfImplType(%d) FAILED HRESULT=0x%X", index, hr);
		env->ThrowNew(clsJComException, message);
		return 0;
	}

	// 参照している型から、ITypeInfoのポインタを取得する
	ITypeInfo* pUserTypeInfo = NULL;
	hr = pITypeInfo->GetRefTypeInfo( hRefType, &pUserTypeInfo ); 
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetRefTypeInfo(%Xh) FAILED HRESULT=0x%X", hRefType, hr);
		env->ThrowNew(clsJComException, message);
		return 0;
	}

	return (jint)(int)pUserTypeInfo;	// 正常終了
}

/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_ITypeInfo
 * Method:    _getRefTypeInfo
 * Signature: (I)I
 * **********************************************************************/
JNIEXPORT jint JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_ITypeInfo__1getRefTypeInfo
  (JNIEnv *env, jobject obj, jint hreftype)
{
	// ITypeInfoインターフェースを取り出す。
	jclass cls = env->GetObjectClass(obj);
	jfieldID fieldID = env->GetFieldID(cls, "pIUnknown", "I"); 
	ITypeInfo* pITypeInfo = (ITypeInfo*)env->GetIntField(obj, fieldID);
	if(pITypeInfo == NULL) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		env->ThrowNew(clsJComException, "ITypeInfo not created");
		return 0;
	}

	// 参照している型を取得する
	ITypeInfo* pUserTypeInfo = NULL;
	HRESULT hr = pITypeInfo->GetRefTypeInfo(hreftype, &pUserTypeInfo);
	if(FAILED(hr)) {
		// 例外をセットする
		jclass clsJComException = env->FindClass(CLASS_JCOM_EXCEPTION);
		char message[256];
		sprintf(message, "ITypeInfo::GetRefTypeInfo(%d) FAILED HRESULT=0x%X", hreftype, hr);
		env->ThrowNew(clsJComException, message);
		return 0;
	}
	// ITypeInfoを返す。呼び出し側はRelease()を忘れないように
	return (jint)(int)pUserTypeInfo;
}
