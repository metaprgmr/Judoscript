#include "atlbase.h"
#include "atlconv.h"
#include <stdio.h>
#include <assert.h>

//VT_MFCBYREF �͂P�o�C�g�ɂ����߂邽�߁A0x40�ɂ��Ă���B
//�킴�킴VT_BYREF�ƒu�������Ă���̂��B���������B
/*
	COleDispatchDriver::InvokeHelper()���x�[�X�ɕύX�����B
	�ύX�_�͈ȉ��̒ʂ�B
	�E�����o�ł͂Ȃ��A�Ɨ������֐��ɂ����B
	�EIDispatch*�������œn���悤�ɂ����B
	�EASSERT(),TRACE()��assert(),printf()�ɂ����B
	�EAfx�n�̊֐��𓙉��ȏ����ɒu���������B
	�E��O�𓊂����ɖ߂�l��Ԃ��悤�ɂ����B
	�E�߂�lpvRet��������̂Ƃ��ɂ́ACString�ł͂Ȃ��ABSTR��Ԃ��悤�ɂ����B
	  �Ăяo������SysFreeString()���K�v�B
	�E�����A�߂�l��VARIANT�^�ɕϊ��ς݂ɂ����B
	
	
	
	O	int*	argerr;		�����̃G���[(0..�G���[�̈����̈ʒu�A-1:�G���[�Ȃ�)
*/

#if 1
HRESULT InvokeHelper(IDispatch* pDispatch, DISPID dwDispID, WORD wFlags,
	VARIANT* pvRet, VARIANT* argList, int argcount,
	int* argerr)
{
	USES_CONVERSION;

	if (pDispatch == NULL)
	{
		printf("Warning: attempt to call Invoke with NULL pDispatch!\n");
		return -1;
	}

	// �������쐬����
	DISPPARAMS dispparams;
	memset(&dispparams, 0, sizeof dispparams);

	// determine number of arguments  �����̐��̌���
	dispparams.cArgs = argcount;

	DISPID dispidNamed = DISPID_PROPERTYPUT;
	if (wFlags & (DISPATCH_PROPERTYPUT|DISPATCH_PROPERTYPUTREF))
	{
		assert(dispparams.cArgs > 0);
		dispparams.cNamedArgs = 1;
		dispparams.rgdispidNamedArgs = &dispidNamed;
	}

	if (dispparams.cArgs != 0)
	{
		// allocate memory for all VARIANT parameters
		dispparams.rgvarg = argList;
	}

	// initialize return value  �߂�l�̏�����
	if (pvRet != NULL)
		VariantInit(pvRet);

	// initialize EXCEPINFO struct ��O���̏�����
	EXCEPINFO excepInfo;
	memset(&excepInfo, 0, sizeof excepInfo);

	*argerr = (UINT)-1;  // initialize to invalid arg

	// make the call 
	SCODE sc = pDispatch->Invoke(dwDispID, IID_NULL, 0, wFlags,
		&dispparams, pvRet, &excepInfo, (UINT*)argerr);

//printf("argerr=%d\n", argerr);

	return sc;
}
#else
HRESULT InvokeHelper(IDispatch* pDispatch, DISPID dwDispID, WORD wFlags,
	VARTYPE vtRet, void* pvRet, const BYTE* pbParamInfo, va_list argList)
{
	USES_CONVERSION;

	if (pDispatch == NULL)
	{
		printf("Warning: attempt to call Invoke with NULL pDispatch!\n");
		return -1;
	}

	// �������쐬����
	DISPPARAMS dispparams;
	memset(&dispparams, 0, sizeof dispparams);

	// determine number of arguments  �����̐��̌���
	if (pbParamInfo != NULL)
		dispparams.cArgs = lstrlenA((LPCSTR)pbParamInfo);

	DISPID dispidNamed = DISPID_PROPERTYPUT;
	if (wFlags & (DISPATCH_PROPERTYPUT|DISPATCH_PROPERTYPUTREF))
	{
		assert(dispparams.cArgs > 0);
		dispparams.cNamedArgs = 1;
		dispparams.rgdispidNamedArgs = &dispidNamed;
	}

	if (dispparams.cArgs != 0)
	{
		// allocate memory for all VARIANT parameters
		VARIANT* pArg = new VARIANT[dispparams.cArgs];
		assert(pArg != NULL);   // should have thrown exception
		dispparams.rgvarg = pArg;
		memset(pArg, 0, sizeof(VARIANT) * dispparams.cArgs);

		// get ready to walk vararg list
		const BYTE* pb = pbParamInfo;
		pArg += dispparams.cArgs - 1;   // params go in opposite order

		while (*pb != 0)
		{
			assert(pArg >= dispparams.rgvarg);

			pArg->vt = *pb; // set the variant type
//			if (pArg->vt & VT_MFCBYREF)		// VT_MFCBYREF�̃r�b�g��VT_BYREF�ɒu��������
//			{
//				pArg->vt &= ~VT_MFCBYREF;
//				pArg->vt |= VT_BYREF;
//			}
			switch (pArg->vt)
			{
			case VT_UI1:
				pArg->bVal = va_arg(argList, BYTE);
				break;
			case VT_I2:
				pArg->iVal = va_arg(argList, short);
				break;
			case VT_I4:
				pArg->lVal = va_arg(argList, long);
				break;
			case VT_R4:
				pArg->fltVal = (float)va_arg(argList, double);
				break;
			case VT_R8:
				pArg->dblVal = va_arg(argList, double);
				break;
			case VT_DATE:
				pArg->date = va_arg(argList, DATE);
				break;
			case VT_CY:
				pArg->cyVal = *va_arg(argList, CY*);
				break;
			case VT_BSTR:
				{
					LPCOLESTR lpsz = va_arg(argList, LPOLESTR);
					pArg->bstrVal = ::SysAllocString(lpsz);
//					if (lpsz != NULL && pArg->bstrVal == NULL)
//						AfxThrowMemoryException();
					assert(!(lpsz != NULL && pArg->bstrVal == NULL));
				}
				break;
//#if !defined(_UNICODE) && !defined(OLE2ANSI)
//			case VT_BSTRA:
//				{
//					LPCSTR lpsz = va_arg(argList, LPSTR);
//					pArg->bstrVal = ::SysAllocString(T2COLE(lpsz));
//					if (lpsz != NULL && pArg->bstrVal == NULL)
//						AfxThrowMemoryException();
//					pArg->vt = VT_BSTR;
//				}
//				break;
//#endif
			case VT_DISPATCH:
				pArg->pdispVal = va_arg(argList, LPDISPATCH);
				break;
			case VT_ERROR:
				pArg->scode = va_arg(argList, SCODE);
				break;
			case VT_BOOL:
				V_BOOL(pArg) = (VARIANT_BOOL)(va_arg(argList, BOOL) ? -1 : 0);
				break;
			case VT_VARIANT:
				*pArg = *va_arg(argList, VARIANT*);
				break;
			case VT_UNKNOWN:
				pArg->punkVal = va_arg(argList, LPUNKNOWN);
				break;

			case VT_I2|VT_BYREF:
				pArg->piVal = va_arg(argList, short*);
				break;
			case VT_UI1|VT_BYREF:
				pArg->pbVal = va_arg(argList, BYTE*);
				break;
			case VT_I4|VT_BYREF:
				pArg->plVal = va_arg(argList, long*);
				break;
			case VT_R4|VT_BYREF:
				pArg->pfltVal = va_arg(argList, float*);
				break;
			case VT_R8|VT_BYREF:
				pArg->pdblVal = va_arg(argList, double*);
				break;
			case VT_DATE|VT_BYREF:
				pArg->pdate = va_arg(argList, DATE*);
				break;
			case VT_CY|VT_BYREF:
				pArg->pcyVal = va_arg(argList, CY*);
				break;
			case VT_BSTR|VT_BYREF:
				pArg->pbstrVal = va_arg(argList, BSTR*);
				break;
			case VT_DISPATCH|VT_BYREF:
				pArg->ppdispVal = va_arg(argList, LPDISPATCH*);
				break;
			case VT_ERROR|VT_BYREF:
				pArg->pscode = va_arg(argList, SCODE*);
				break;
			case VT_BOOL|VT_BYREF:
				{
					// coerce BOOL into VARIANT_BOOL
					BOOL* pboolVal = va_arg(argList, BOOL*);
					*pboolVal = *pboolVal ? MAKELONG(-1, 0) : 0;
					pArg->pboolVal = (VARIANT_BOOL*)pboolVal;
				}
				break;
			case VT_VARIANT|VT_BYREF:
				pArg->pvarVal = va_arg(argList, VARIANT*);
				break;
			case VT_UNKNOWN|VT_BYREF:
				pArg->ppunkVal = va_arg(argList, LPUNKNOWN*);
				break;

			default:
				assert(FALSE);  // unknown type!
				break;
			}

			--pArg; // get ready to fill next argument
			++pb;
		}
	}

	// initialize return value  �߂�l�̏�����
	VARIANT* pvarResult = NULL;
	VARIANT vaResult;
	VariantInit(&vaResult);
	if (vtRet != VT_EMPTY)
		pvarResult = &vaResult;

	// initialize EXCEPINFO struct ��O���̏�����
	EXCEPINFO excepInfo;
	memset(&excepInfo, 0, sizeof excepInfo);

	UINT nArgErr = (UINT)-1;  // initialize to invalid arg

	// make the call 
	SCODE sc = pDispatch->Invoke(dwDispID, IID_NULL, 0, wFlags,
		&dispparams, pvarResult, &excepInfo, &nArgErr);

	// cleanup any arguments that need cleanup ���������
	if (dispparams.cArgs != 0)
	{
		VARIANT* pArg = dispparams.rgvarg + dispparams.cArgs - 1;
		const BYTE* pb = pbParamInfo;
		while (*pb != 0)
		{
			switch ((VARTYPE)*pb)
			{
//#if !defined(_UNICODE) && !defined(OLE2ANSI)
//			case VT_BSTRA:
//#endif
			case VT_BSTR:
				VariantClear(pArg);
				break;
			}
			--pArg;
			++pb;
		}
	}
	delete[] dispparams.rgvarg;

	// throw exception on failure 
	if (FAILED(sc))
	{
		VariantClear(&vaResult);
		return sc;
/*		if (sc != DISP_E_EXCEPTION)
		{
			// non-exception error code
			AfxThrowOleException(sc);
		}

		// make sure excepInfo is filled in
		if (excepInfo.pfnDeferredFillIn != NULL)
			excepInfo.pfnDeferredFillIn(&excepInfo);

		// allocate new exception, and fill it
		COleDispatchException* pException =
			new COleDispatchException(NULL, 0, excepInfo.wCode);
		assert(pException->m_wCode == excepInfo.wCode);
		if (excepInfo.bstrSource != NULL)
		{
			pException->m_strSource = excepInfo.bstrSource;
			SysFreeString(excepInfo.bstrSource);
		}
		if (excepInfo.bstrDescription != NULL)
		{
			pException->m_strDescription = excepInfo.bstrDescription;
			SysFreeString(excepInfo.bstrDescription);
		}
		if (excepInfo.bstrHelpFile != NULL)
		{
			pException->m_strHelpFile = excepInfo.bstrHelpFile;
			SysFreeString(excepInfo.bstrHelpFile);
		}
		pException->m_dwHelpContext = excepInfo.dwHelpContext;
		pException->m_scError = excepInfo.scode;

		// then throw the exception
		THROW(pException);
*/
		assert(FALSE);  // not reached
	}

	if (vtRet != VT_EMPTY)
	{
		// convert return value �߂�l��ϊ�
		if (vtRet != VT_VARIANT)
		{
			SCODE sc = VariantChangeType(&vaResult, &vaResult, 0, vtRet);
			if (FAILED(sc))
			{
				printf("Warning: automation return value coercion failed.\n");
				VariantClear(&vaResult);
//				AfxThrowOleException(sc);
				return sc;
			}
			assert(vtRet == vaResult.vt);
		}

		// copy return value into return spot!
		switch (vtRet)
		{
		case VT_UI1:
			*(BYTE*)pvRet = vaResult.bVal;
			break;
		case VT_I2:
			*(short*)pvRet = vaResult.iVal;
			break;
		case VT_I4:
			*(long*)pvRet = vaResult.lVal;
			break;
		case VT_R4:
//			*(_AFX_FLOAT*)pvRet = *(_AFX_FLOAT*)&vaResult.fltVal;
			*(float*)pvRet = vaResult.fltVal;
			break;
		case VT_R8:
//			*(_AFX_DOUBLE*)pvRet = *(_AFX_DOUBLE*)&vaResult.dblVal;
			*(double*)pvRet = vaResult.dblVal;
			break;
		case VT_DATE:
//			*(_AFX_DOUBLE*)pvRet = *(_AFX_DOUBLE*)&vaResult.date;
			*(double*)pvRet = vaResult.date;
			break;
		case VT_CY:
			*(CY*)pvRet = vaResult.cyVal;
			break;
		case VT_BSTR:
//			AfxBSTR2CString((CString*)pvRet, vaResult.bstrVal);
//			SysFreeString(vaResult.bstrVal);
			*(BSTR*)pvRet = vaResult.bstrVal;
			break;
		case VT_DISPATCH:
			*(LPDISPATCH*)pvRet = vaResult.pdispVal;
			break;
		case VT_ERROR:
			*(SCODE*)pvRet = vaResult.scode;
			break;
		case VT_BOOL:
			*(BOOL*)pvRet = (V_BOOL(&vaResult) != 0);
			break;
		case VT_VARIANT:
			*(VARIANT*)pvRet = vaResult;
			break;
		case VT_UNKNOWN:
			*(LPUNKNOWN*)pvRet = vaResult.punkVal;
			break;

		default:
			assert(FALSE);  // invalid return type specified
		}
	}
	return 0;
}
#endif
