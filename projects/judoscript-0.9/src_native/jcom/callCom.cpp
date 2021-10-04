/*
ＣＯＭを呼び出すライブラリ
多少、簡単なＩ／Ｆにした。
IDispatchインターフェースを使用している。
createInstance()した後は、IDispatch::Release()を行うこと。
末尾にサンプルプログラムあり	"VbProject.VbClass"が登録されていること。

尚、ＭＦＣにはこれとは別のアプローチにより、容易に扱えるクラスがある。
COleDispatchDriver::InvokeHelper()などがそうです。
特に支障なければそちらを使う方がよいだろう。

*/
#include "atlbase.h"
#include "atlconv.h"
#include "atlconv.cpp"	// 必要？
#include <stdio.h>

/**
*	指定されたＣＯＭのインスタンスを生成します。
*	文字列はShiftJISで指定すること。

	CoInitialize()での戻り値
	S_OK	The library was initialized successfully. 
	S_FALSE	The library is already initialized or 
		that it could not release the default allocator. 

	CLSIDFromProgID()での戻り値
	S_OK 
	The CLSID was retrieved successfully. 
	CO_E_CLASSSTRING 
	The registered CLSID for the ProgID is invalid. 
	REGDB_E_WRITEREGDB 
	An error occurred writing the CLSID to the registry. See "Remarks"below. 

	CoCreateInstance()での戻り値
	S_OK 
	An instance of the specified object class was successfully created. 
	REGDB_E_CLASSNOTREG 0x80040154L
	A specified class is not registered in the registration database.
	Also can indicate that the type of server you requested in the CLSCTX enumeration
	is not registered or the values for the server types in the registry are corrupt. 
	CLASS_E_NOAGGREGATION 0x80040110L
	This class cannot be created as part of an aggregate. 
*   @param progid		ProgID
*	@param ppIDispatch	IDispatchを返します。
*	@result				エラーコード 正常終了のときは(S_OK)を返します。
*/
HRESULT createInstance(const char* progid, IDispatch** ppIDispatch)
{
	USES_CONVERSION;
	HRESULT hr = CoInitialize(NULL);
	if(FAILED(hr)) return hr;

	CLSID clsid;
	hr = CLSIDFromProgID(T2OLE(progid), &clsid);
	if(FAILED(hr)) return hr;

	*ppIDispatch = NULL;	// 念のため
	hr = CoCreateInstance(clsid, NULL, CLSCTX_SERVER,
						IID_IDispatch, (void**)ppIDispatch);
	return hr;
}


/**
*	指定されたメソッドを呼び出します。
*	文字列はShiftJISで指定すること。
*   @param pIDispatch	I IDispatchのポインタ
*	@param mame			I メソッドの名前(Shift-JIS)
*	@param varg			U 引数。
*   @param args			I 引数の数
*	@param result		O ＣＯＭの関数の戻り値
*	@result				エラーコード 正常終了のときは(S_OK)を返します。
*/
HRESULT invokeMethod(IDispatch* pIDispatch, const char* name, VARIANTARG* varg, int args, VARIANT* result)
{
	USES_CONVERSION;

	DISPID dispid;
	OLECHAR* olename = A2OLE(name);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &olename, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	DISPPARAMS param = {varg, NULL, args, 0};

	if(result) VariantInit(result);
	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_METHOD,
						&param, result,
						NULL, NULL);
	return hr;
}

/*
int callMethod(IDispatch* pIDispatch, const char* mame, long* no, char** str, long* _len, long* _result)
{
	USES_CONVERSION;

	// メソッドの DISPID を取得
	DISPID dispid;
	OLECHAR* name = A2OLE(mame);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	// 引数を設定
	VARIANTARG varg[2];
	VariantInit(&varg[0]);
	varg[0].vt = VT_BSTR|VT_BYREF;
	BSTR bstr = SysAllocString(T2OLE(*str));
	varg[0].pbstrVal = &bstr;

	VariantInit(&varg[1]);
	varg[1].vt = VT_I4|VT_BYREF;
	varg[1].plVal = no;

	DISPPARAMS param;
	param.cArgs             = 2;
	param.rgvarg            = varg;
	param.cNamedArgs        = 0;
	param.rgdispidNamedArgs = NULL;

	VARIANT result;
	VariantInit(&result);
	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_METHOD,
						&param,
						&result,
						NULL, NULL);
	if(FAILED(hr)) return hr;
	char* p = OLE2T(bstr);
	*_len = strlen(p);
	free(*str);
	*str = (char*)malloc((*_len)+1);
	memcpy(*str, p, (*_len)+1);
	SysFreeString(bstr);
	*_result = result.lVal;
	// あぁ～、めんどくさい！！！
	return hr;
}
*/


// プロパティから値を取得
//	DISP_E_BADVARTYPE		0x80020008L
HRESULT getProperty(IDispatch* pIDispatch, const char* name, VARIANT* pVarResult)
{
	USES_CONVERSION;

	DISPID dispid;
	OLECHAR* ole_name = T2OLE(name);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &ole_name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	DISPPARAMS dispparams = {NULL, NULL, 0, 0};
	VariantInit(pVarResult);	// 必要
	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_PROPERTYGET,
						&dispparams,
						pVarResult,
						NULL, NULL);
	return hr;
}


// プロパティに値をセット
HRESULT putProperty(IDispatch* pIDispatch, const char* name, VARIANT* pVar)
{
	USES_CONVERSION;

	DISPID dispid;
	OLECHAR* ole_name = T2OLE(name);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &ole_name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	DISPID dispidPut = DISPID_PROPERTYPUT;
	DISPPARAMS dispparams = { pVar, &dispidPut, 1,1 };
	if (pVar->vt == VT_UNKNOWN || pVar->vt == VT_DISPATCH || 
		(pVar->vt & VT_ARRAY) || (pVar->vt & VT_BYREF))
	{
		hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_PROPERTYPUTREF,
						&dispparams,
						NULL, NULL, NULL);
		if (SUCCEEDED(hr))
			return hr;
	}

	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_PROPERTYPUT,
						&dispparams,
						NULL, NULL, NULL);
	return hr;
}

// 引数付きでプロパティから値を取得
HRESULT getPropertyArg(IDispatch* pIDispatch, const char* name, VARIANTARG* varg, int args, VARIANT* pVarResult)
{
	USES_CONVERSION;
	//printf("getProperty2(%p,'%s',varg,%d,result)\n",pIDispatch, name, args);
	DISPID dispid;
	OLECHAR* ole_name = T2OLE(name);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &ole_name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	DISPPARAMS param = {varg, NULL, args, 0};
	if(pVarResult!=NULL) VariantInit(pVarResult);
	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_PROPERTYGET,
						&param,
						pVarResult,
						NULL, NULL);
	return hr;
}



#ifdef TEST
#include <stdio.h>

// サンプルプログラム
// cl /DTEST callCom.cpp で作成します。
// あらかじめ、ＶＢ等で下記のＣＯＭを作成し、そのＣＯＭを呼びます。
int main()
{
	USES_CONVERSION;
	VARIANT var, result;
	BSTR	bstr;

	// インスタンスを生成
	IDispatch* pIDispatch=NULL;
	HRESULT hr = createInstance("VbProject.VbClass", &pIDispatch);
	printf("hr=%xh\n", hr);

	// プロパティに値をセット
	printf("'** num = 55\n");
	VariantInit(&var);	var.vt = VT_I4;		var.lVal = 55;
	hr = putProperty(pIDispatch, "num", &var);
	printf("hr=%xh\n", hr);

	// プロパティから値を取得
	printf("'** Print num\n");
	hr = getProperty(pIDispatch, "num", &result);
	printf("hr=%xh num=%d\n", hr, result.lVal);

	// メソッドを呼ぶ
	printf("'** Call funcType0()\n");
	hr = invokeMethod(pIDispatch, "funcType0", NULL, 0, NULL);
	printf("hr = %xh \n", hr);

	// プロパティの値を取得
	printf("'** Print num\n");
	hr = getProperty(pIDispatch, "num", &result);
	printf("hr=%xh num=%d\n", hr, result.lVal);

	// プロパティに値をセット
	printf("'** str = \"モジモジもじれつ\"\n");
	bstr = SysAllocString(T2OLE("モジモジもじれつ"));
	VariantInit(&var);	var.vt = VT_BSTR;	var.bstrVal = bstr;
	hr = putProperty(pIDispatch, "str", &var);
	printf("hr=%xh\n", hr);
	SysFreeString(bstr);

	// メソッドを呼ぶ
	printf("'** str = \"今年\" : Print funcType1(1999, str), str\n");
	bstr = SysAllocString(T2OLE("今年"));
	VARIANTARG varg[2];
	VariantInit(&varg[0]);	varg[0].vt = VT_BSTR|VT_BYREF;	varg[0].pbstrVal = &bstr;
	VariantInit(&varg[1]);	varg[1].vt = VT_I4;				varg[1].lVal = 1999;
	hr = invokeMethod(pIDispatch, "funcType1", varg, 2, &result);
	printf("hr = %xh return=%d str=%s\n", hr, result.lVal, OLE2A(*varg[0].pbstrVal));
	SysFreeString(*varg[0].pbstrVal);

	// リリース
	pIDispatch->Release();		// 使い終わったらリリースを
	return 0;
}
/*
ＶＢ５で VbProject プロジェクト  VbClass クラスモジュール として作成。
このＩ／Ｆは非常に単純だが、大半の要望を満たすほどの広く汎用的な橋である。
ここのサブルーチンの中は何でもよい。
--------------------------------------------------------------------
Public str As String
Public num As Long

'*** タイプ０：引数なし、戻り値なし
Public Sub funcType0()
    num = 0
    str = ""
End Sub

'*** タイプ１：引数 Long(ByVal)とString(ByRef) 戻り値Long
Public Function funcType1(ByVal n As Long, ByRef s As String) As Long
    num = n
    s = str + s
    funcType1 = Len(s)
End Function
--------------------------------------------------------------------
ＯＬＥ－ＣＯＭオブジェクトビューアでは以下のように見える。
dispinterface _VbClass {
    properties:
    methods:
        [id(0x40030000), propget]
        BSTR str();
        [id(0x40030000), propput]
        void str([in] BSTR rhs);
        [id(0x40030001), propget]
        long num();
        [id(0x40030001), propput]
        void num([in] long rhs);
        [id(0x60030000)]
        void funcType0();
        [id(0x60030001)]
        long funcType1(
                        [in] long n, 
                        [in, out] BSTR* s);
};
*/
/*
Invoke()で返される値
S_OK 0
Success. 
DISP_E_BADPARAMCOUNT 0x8002000EL
The number of elements provided to DISPPARAMS is different from the number
 of arguments accepted by the method or property. 
DISP_E_BADVARTYPE 0x80020008L
One of the arguments in rgvarg is not a valid variant type. 

DISP_E_EXCEPTION 0x80020009L
The application needs to raise an exception. In this case, the structure passed in pExcepInfo should be filled in. 

DISP_E_MEMBERNOTFOUND 0x80020003L
The requested member does not exist, or the call to Invoke tried to set the value of a read-only property. 

DISP_E_NONAMEDARGS 0x80020007L
This implementation of IDispatch does not support named arguments. 

DISP_E_OVERFLOW 
One of the arguments in rgvarg could not be coerced to the specified type. 

DISP_E_PARAMNOTFOUND 0x80020004L
One of the parameter DISPIDs does not correspond to a parameter on the method. In this case, puArgErr should be set to the first argument that contains the error. 

DISP_E_TYPEMISMATCH 0x80020005L
One or more of the arguments could not be coerced. The index within rgvarg of the first parameter with the incorrect type is returned in the puArgErr parameter. 

DISP_E_UNKNOWNINTERFACE 0x80020001L
The interface identifier passed in riid is not IID_NULL. 

DISP_E_UNKNOWNLCID 0x8002000CL
The member being invoked interprets string arguments according to the LCID, and the LCID is not recognized. If the LCID is not needed to interpret arguments, this error should not be returned. 

DISP_E_PARAMNOTOPTIONAL 0x8002000FL
A required parameter was omitted. 
*/
#endif
