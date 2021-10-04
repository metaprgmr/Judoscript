#include <jni.h>
#include "atlbase.h"
//#include "atlconv.h"

#define CLASS_IUNKNOWN         "jp/ne/so_net/ga2/no_ji/jcom/IUnknown"
#define CLASS_IDISPATCH        "jp/ne/so_net/ga2/no_ji/jcom/IDispatch"
#define CLASS_ITYPEINFO        "jp/ne/so_net/ga2/no_ji/jcom/ITypeInfo"
#define CLASS_TYPEATTR         "jp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$TypeAttr"
#define CLASS_FUNCDESC         "jp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$FuncDesc"
#define CLASS_ELEMDESC         "jp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$ElemDesc"
#define CLASS_VARDESC          "jp/ne/so_net/ga2/no_ji/jcom/ITypeInfo$VarDesc"
#define CLASS_ITYPELIB         "jp/ne/so_net/ga2/no_ji/jcom/ITypeLib"
#define CLASS_TLIBATTR         "jp/ne/so_net/ga2/no_ji/jcom/ITypeLib$TLibAttr"
#define CLASS_GUID             "jp/ne/so_net/ga2/no_ji/jcom/GUID"
#define CLASS_JCOM_EXCEPTION   "jp/ne/so_net/ga2/no_ji/jcom/JComException"
#define CLASS_VARIANT_CURRENCY "jp/ne/so_net/ga2/no_ji/jcom/VariantCurrency"
#define CLASS_RELEASEMANAGER   "jp/ne/so_net/ga2/no_ji/jcom/ReleaseManager"

#define CLASS_VARIANT_CURRENCY_DOT  "jp.ne.so_net.ga2.no_ji.jcom.VariantCurrency"
#define CLASS_IUNKNOWN_DOT          "jp.ne.so_net.ga2.no_ji.jcom.IUnknown"
#define CLASS_IDISPATCH_DOT         "jp.ne.so_net.ga2.no_ji.jcom.IDispatch"

// jstring.cpp
char*	jstring2sjis(JNIEnv *env, jstring jstr);
BSTR	jstring2BSTR(JNIEnv *env, jstring jstr);
jstring	BSTR2jstring(JNIEnv *env, BSTR bstr);
void print_BSTR(OLECHAR* psz);

// guid
void jobject2GUID(JNIEnv* env, jobject guid, GUID* IID);
jobject GUID2jobject(JNIEnv* env, GUID* guid);

// VARIANT.cpp
int	jobject2VARIANT(JNIEnv* env, jobject obj, VARIANT* var);
int	VARIANT2jobject(JNIEnv* env, jobject obj, VARIANT* var, jobject* result);
void	VARIANT_free(VARIANT* var);
void print_VARTYPE(VARTYPE vt);
int	feedbackVARIANT2jobject(JNIEnv* env, jobject obj, VARIANT* var, jobject result);

// callCom.cpp
HRESULT createInstance(const char* progid, IDispatch** ppIDispatch);
HRESULT invokeMethod(IDispatch* pIDispatch, const char* name, VARIANTARG* varg, int args, VARIANT* result);
HRESULT getProperty(IDispatch* pIDispatch, const char* name, VARIANT* pVarResult);
HRESULT putProperty(IDispatch* pIDispatch, const char* name, VARIANT* pVar);
HRESULT getPropertyArg(IDispatch* pIDispatch, const char* name, VARIANTARG* varg, int args, VARIANT* pVarResult);

// InvokeHelper.cpp
//HRESULT InvokeHelper(IDispatch* pDispatch, DISPID dwDispID, WORD wFlags,
//	VARIANT* pvRet, VARIANT* argList, int argcount);
HRESULT InvokeHelper(IDispatch* pDispatch, DISPID dwDispID, WORD wFlags,
	VARIANT* pvRet, VARIANT* argList, int argcount,
	int* argerr);
