/*
�b�n�l���Ăяo�����C�u����
�����A�ȒP�Ȃh�^�e�ɂ����B
IDispatch�C���^�[�t�F�[�X���g�p���Ă���B
createInstance()������́AIDispatch::Release()���s�����ƁB
�����ɃT���v���v���O��������	"VbProject.VbClass"���o�^����Ă��邱�ƁB

���A�l�e�b�ɂ͂���Ƃ͕ʂ̃A�v���[�`�ɂ��A�e�ՂɈ�����N���X������B
COleDispatchDriver::InvokeHelper()�Ȃǂ������ł��B
���Ɏx��Ȃ���΂�������g�������悢���낤�B

*/
#include "atlbase.h"
#include "atlconv.h"
#include "atlconv.cpp"	// �K�v�H
#include <stdio.h>

/**
*	�w�肳�ꂽ�b�n�l�̃C���X�^���X�𐶐����܂��B
*	�������ShiftJIS�Ŏw�肷�邱�ƁB

	CoInitialize()�ł̖߂�l
	S_OK	The library was initialized successfully. 
	S_FALSE	The library is already initialized or 
		that it could not release the default allocator. 

	CLSIDFromProgID()�ł̖߂�l
	S_OK 
	The CLSID was retrieved successfully. 
	CO_E_CLASSSTRING 
	The registered CLSID for the ProgID is invalid. 
	REGDB_E_WRITEREGDB 
	An error occurred writing the CLSID to the registry. See "Remarks"below. 

	CoCreateInstance()�ł̖߂�l
	S_OK 
	An instance of the specified object class was successfully created. 
	REGDB_E_CLASSNOTREG 0x80040154L
	A specified class is not registered in the registration database.
	Also can indicate that the type of server you requested in the CLSCTX enumeration
	is not registered or the values for the server types in the registry are corrupt. 
	CLASS_E_NOAGGREGATION 0x80040110L
	This class cannot be created as part of an aggregate. 
*   @param progid		ProgID
*	@param ppIDispatch	IDispatch��Ԃ��܂��B
*	@result				�G���[�R�[�h ����I���̂Ƃ���(S_OK)��Ԃ��܂��B
*/
HRESULT createInstance(const char* progid, IDispatch** ppIDispatch)
{
	USES_CONVERSION;
	HRESULT hr = CoInitialize(NULL);
	if(FAILED(hr)) return hr;

	CLSID clsid;
	hr = CLSIDFromProgID(T2OLE(progid), &clsid);
	if(FAILED(hr)) return hr;

	*ppIDispatch = NULL;	// �O�̂���
	hr = CoCreateInstance(clsid, NULL, CLSCTX_SERVER,
						IID_IDispatch, (void**)ppIDispatch);
	return hr;
}


/**
*	�w�肳�ꂽ���\�b�h���Ăяo���܂��B
*	�������ShiftJIS�Ŏw�肷�邱�ƁB
*   @param pIDispatch	I IDispatch�̃|�C���^
*	@param mame			I ���\�b�h�̖��O(Shift-JIS)
*	@param varg			U �����B
*   @param args			I �����̐�
*	@param result		O �b�n�l�̊֐��̖߂�l
*	@result				�G���[�R�[�h ����I���̂Ƃ���(S_OK)��Ԃ��܂��B
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

	// ���\�b�h�� DISPID ���擾
	DISPID dispid;
	OLECHAR* name = A2OLE(mame);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	// ������ݒ�
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
	// �����`�A�߂�ǂ������I�I�I
	return hr;
}
*/


// �v���p�e�B����l���擾
//	DISP_E_BADVARTYPE		0x80020008L
HRESULT getProperty(IDispatch* pIDispatch, const char* name, VARIANT* pVarResult)
{
	USES_CONVERSION;

	DISPID dispid;
	OLECHAR* ole_name = T2OLE(name);
	HRESULT hr = pIDispatch->GetIDsOfNames(IID_NULL, &ole_name, 1, GetUserDefaultLCID(), &dispid);
	if(FAILED(hr)) return hr;

	DISPPARAMS dispparams = {NULL, NULL, 0, 0};
	VariantInit(pVarResult);	// �K�v
	hr = pIDispatch->Invoke(dispid, IID_NULL, GetUserDefaultLCID(), DISPATCH_PROPERTYGET,
						&dispparams,
						pVarResult,
						NULL, NULL);
	return hr;
}


// �v���p�e�B�ɒl���Z�b�g
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

// �����t���Ńv���p�e�B����l���擾
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

// �T���v���v���O����
// cl /DTEST callCom.cpp �ō쐬���܂��B
// ���炩���߁A�u�a���ŉ��L�̂b�n�l���쐬���A���̂b�n�l���Ăт܂��B
int main()
{
	USES_CONVERSION;
	VARIANT var, result;
	BSTR	bstr;

	// �C���X�^���X�𐶐�
	IDispatch* pIDispatch=NULL;
	HRESULT hr = createInstance("VbProject.VbClass", &pIDispatch);
	printf("hr=%xh\n", hr);

	// �v���p�e�B�ɒl���Z�b�g
	printf("'** num = 55\n");
	VariantInit(&var);	var.vt = VT_I4;		var.lVal = 55;
	hr = putProperty(pIDispatch, "num", &var);
	printf("hr=%xh\n", hr);

	// �v���p�e�B����l���擾
	printf("'** Print num\n");
	hr = getProperty(pIDispatch, "num", &result);
	printf("hr=%xh num=%d\n", hr, result.lVal);

	// ���\�b�h���Ă�
	printf("'** Call funcType0()\n");
	hr = invokeMethod(pIDispatch, "funcType0", NULL, 0, NULL);
	printf("hr = %xh \n", hr);

	// �v���p�e�B�̒l���擾
	printf("'** Print num\n");
	hr = getProperty(pIDispatch, "num", &result);
	printf("hr=%xh num=%d\n", hr, result.lVal);

	// �v���p�e�B�ɒl���Z�b�g
	printf("'** str = \"���W���W�������\"\n");
	bstr = SysAllocString(T2OLE("���W���W�������"));
	VariantInit(&var);	var.vt = VT_BSTR;	var.bstrVal = bstr;
	hr = putProperty(pIDispatch, "str", &var);
	printf("hr=%xh\n", hr);
	SysFreeString(bstr);

	// ���\�b�h���Ă�
	printf("'** str = \"���N\" : Print funcType1(1999, str), str\n");
	bstr = SysAllocString(T2OLE("���N"));
	VARIANTARG varg[2];
	VariantInit(&varg[0]);	varg[0].vt = VT_BSTR|VT_BYREF;	varg[0].pbstrVal = &bstr;
	VariantInit(&varg[1]);	varg[1].vt = VT_I4;				varg[1].lVal = 1999;
	hr = invokeMethod(pIDispatch, "funcType1", varg, 2, &result);
	printf("hr = %xh return=%d str=%s\n", hr, result.lVal, OLE2A(*varg[0].pbstrVal));
	SysFreeString(*varg[0].pbstrVal);

	// �����[�X
	pIDispatch->Release();		// �g���I������烊���[�X��
	return 0;
}
/*
�u�a�T�� VbProject �v���W�F�N�g  VbClass �N���X���W���[�� �Ƃ��č쐬�B
���̂h�^�e�͔��ɒP�������A�唼�̗v�]�𖞂����قǂ̍L���ėp�I�ȋ��ł���B
�����̃T�u���[�`���̒��͉��ł��悢�B
--------------------------------------------------------------------
Public str As String
Public num As Long

'*** �^�C�v�O�F�����Ȃ��A�߂�l�Ȃ�
Public Sub funcType0()
    num = 0
    str = ""
End Sub

'*** �^�C�v�P�F���� Long(ByVal)��String(ByRef) �߂�lLong
Public Function funcType1(ByVal n As Long, ByRef s As String) As Long
    num = n
    s = str + s
    funcType1 = Len(s)
End Function
--------------------------------------------------------------------
�n�k�d�|�b�n�l�I�u�W�F�N�g�r���[�A�ł͈ȉ��̂悤�Ɍ�����B
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
Invoke()�ŕԂ����l
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
