#include <jni.h>

#include "atlbase.h"
#include "atlconv.h"
#include "JCom.h"


/* **********************************************************************
 * Java�̃I�u�W�F�N�g��VARIANT�^�ɕϊ�
 * **********************************************************************/
int	jobject2VARIANT(JNIEnv* env, jobject obj, VARIANT* var)
{
	VariantInit(var);

	// null �Ȃ� VT_EMPTY ��ݒ�
	if(obj == NULL) {
//		var->vt = VT_EMPTY;
		var->vt = VT_NULL;
		return 0;	// ����I��
	}

	// Byte �Ȃ� VT_UI1 ��ݒ�
	jclass clsByte = env->FindClass("java/lang/Byte");
	if(env->IsInstanceOf(obj, clsByte)) {
		jmethodID method = env->GetMethodID(clsByte, "byteValue", "()B");
		jbyte val = env->CallByteMethod(obj, method);
		var->vt = VT_UI1;
		var->bVal = (BYTE)val;
		return 0;	// ����I��
	}

	// Short �Ȃ� VT_I2 ��ݒ�
	jclass clsShort = env->FindClass("java/lang/Short");
	if(env->IsInstanceOf(obj, clsShort)) {
		jmethodID method = env->GetMethodID(clsShort, "shortValue", "()S");
		jshort val = env->CallShortMethod(obj, method);
		var->vt = VT_I2;
		var->iVal = val;
		return 0;	// ����I��
	}

	// Integer �Ȃ� VT_I4 ��ݒ�
	jclass clsInteger = env->FindClass("java/lang/Integer");
	if(env->IsInstanceOf(obj, clsInteger)) {
		jmethodID method = env->GetMethodID(clsInteger, "intValue", "()I");
		jint val = env->CallIntMethod(obj, method);
		var->vt = VT_I4;
		var->lVal = val;
		return 0;	// ����I��
	}

	// Float �Ȃ� VT_R4 ��ݒ�
	jclass clsFloat = env->FindClass("java/lang/Float");
	if(env->IsInstanceOf(obj, clsFloat)) {
		jmethodID method = env->GetMethodID(clsFloat, "floatValue", "()F");
		jfloat val = env->CallFloatMethod(obj, method);
		var->vt = VT_R4;
		var->fltVal = val;
		return 0;	// ����I��
	}

	// Double �Ȃ� VT_R8 ��ݒ�
	jclass clsDouble = env->FindClass("java/lang/Double");
	if(env->IsInstanceOf(obj, clsDouble)) {
		jmethodID method = env->GetMethodID(clsDouble, "doubleValue", "()D");
		jdouble val = env->CallDoubleMethod(obj, method);
		var->vt = VT_R8;
		var->dblVal = val;
		return 0;	// ����I��
	}

	// Boolean �Ȃ� VT_BOOL ��ݒ�
	jclass clsBoolean = env->FindClass("java/lang/Boolean");
	if(env->IsInstanceOf(obj, clsBoolean)) {
		jmethodID method = env->GetMethodID(clsBoolean, "booleanValue", "()Z");
		jboolean val = env->CallBooleanMethod(obj, method);
		var->vt = VT_BOOL;
		var->boolVal = val ? VARIANT_TRUE : VARIANT_FALSE;
		return 0;	// ����I��
	}

	// String �Ȃ� VT_BSTR ��ݒ�
	jclass clsString = env->FindClass("java/lang/String");
	if(env->IsInstanceOf(obj, clsString)) {
		var->vt = VT_BSTR;
		var->bstrVal = jstring2BSTR(env, (jstring)obj);
		return 0;	// ����I��
	}

	// Date �Ȃ� VT_DATE ��ݒ�
	jclass clsDate = env->FindClass("java/util/Date");
	if(env->IsInstanceOf(obj, clsDate)) {
		jmethodID method = env->GetMethodID(clsDate, "getTime", "()J");
		jlong val = env->CallLongMethod(obj, method);
		var->vt = VT_DATE;
		TIME_ZONE_INFORMATION timeinfo;
		GetTimeZoneInformation(&timeinfo);
		var->date = double(val)/(24*60*60*1000)+25569.0-(timeinfo.Bias/(60.0*24.0));
		return 0;	// ����I��
	}

	// VariantCurrency �Ȃ� VT_CY ��ݒ�
	jclass clsCurrency = env->FindClass(CLASS_VARIANT_CURRENCY);
	if(env->IsInstanceOf(obj, clsCurrency)) {
		jmethodID method = env->GetMethodID(clsCurrency, "get", "()D");
		jdouble val = env->CallDoubleMethod(obj, method);
		var->vt = VT_CY;
		var->cyVal.int64 = __int64(val*10000.0);
		return 0;	// ����I��
	}

	// IDispatch �Ȃ� VT_DISPATCH ��ݒ�
	// IUnknown����ɔ��f���Ȃ���΂Ȃ�Ȃ��B
	jclass clsIDispatch = env->FindClass(CLASS_IDISPATCH);
	if(env->IsInstanceOf(obj, clsIDispatch)) {
		jfieldID fieldID = env->GetFieldID(clsIDispatch, "pIUnknown", "I"); 
		var->vt = VT_DISPATCH;
		var->pdispVal = (IDispatch*)env->GetIntField(obj, fieldID);
		return 0;	// ����I��
	}

	// IUnknown �Ȃ� VT_UNKNOWN ��ݒ�
	jclass clsIUnknown = env->FindClass(CLASS_IUNKNOWN);
	if(env->IsInstanceOf(obj, clsIUnknown)) {
		jfieldID fieldID = env->GetFieldID(clsIUnknown, "pIUnknown", "I"); 
		var->vt = VT_UNKNOWN;
		var->punkVal = (IUnknown*)env->GetIntField(obj, fieldID);
		return 0;	// ����I��
	}

	// �i���������̔z��͎Q�Ƃ��邾���B
	// �l�̓q�[�v�Ɏ���ēn���B�Ȃ������āH �����̃R����������I
	// byte[] �Ȃ� VT_UI1|VT_BYREF ��ݒ�
	jclass clsByteArray = env->FindClass("[B");
	if(env->IsInstanceOf(obj, clsByteArray)) {
		jbyte* ary = env->GetByteArrayElements((jbyteArray)obj, NULL);
		BYTE* p = (BYTE*)malloc(sizeof(BYTE));
		*p = (BYTE)ary[0];
		var->vt = VT_UI1|VT_BYREF;
		var->pbVal = p;
		env->ReleaseByteArrayElements((jbyteArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// short[] �Ȃ� VT_I2|VT_BYREF ��ݒ�
	jclass clsShortArray = env->FindClass("[S");
	if(env->IsInstanceOf(obj, clsShortArray)) {
		jshort* ary = env->GetShortArrayElements((jshortArray)obj, NULL);
		short* p = (short*)malloc(sizeof(short));
		*p = (short)ary[0];
		var->vt = VT_I2|VT_BYREF;
		var->piVal = p;
		env->ReleaseShortArrayElements((jshortArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// int[] �Ȃ� VT_I4|VT_BYREF ��ݒ�
	// �i���������̔z��͎Q�Ƃ��邾���B
	// �l�̓q�[�v�Ɏ���ēn���B�Ȃ������āH �����̃R����������I
	jclass clsIntArray = env->FindClass("[I");
	if(env->IsInstanceOf(obj, clsIntArray)) {
		jint* ary = env->GetIntArrayElements((jintArray)obj, NULL);
		long* p = (long*)malloc(sizeof(long));
		*p = (long)ary[0];
		var->vt = VT_I4|VT_BYREF;
		var->plVal = p;
		env->ReleaseIntArrayElements((jintArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// float[] �Ȃ� VT_R4|VT_BYREF ��ݒ�
	jclass clsFloatArray = env->FindClass("[F");
	if(env->IsInstanceOf(obj, clsFloatArray)) {
		jfloat* ary = env->GetFloatArrayElements((jfloatArray)obj, NULL);
		float* p = (float*)malloc(sizeof(float));
		*p = (float)ary[0];
		var->vt = VT_R4|VT_BYREF;
		var->pfltVal = p;
		env->ReleaseFloatArrayElements((jfloatArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// double[] �Ȃ� VT_R8|VT_BYREF ��ݒ�
	jclass clsDoubleArray = env->FindClass("[D");
	if(env->IsInstanceOf(obj, clsDoubleArray)) {
		jdouble* ary = env->GetDoubleArrayElements((jdoubleArray)obj, NULL);
		double* p = (double*)malloc(sizeof(double));
		*p = (double)ary[0];
		var->vt = VT_R8|VT_BYREF;
		var->pdblVal = p;
		env->ReleaseDoubleArrayElements((jdoubleArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// boolean[] �Ȃ� VT_BOOL|VT_BYREF ��ݒ�
	jclass clsBooleanArray = env->FindClass("[Z");
	if(env->IsInstanceOf(obj, clsBooleanArray)) {
		jboolean* ary = env->GetBooleanArrayElements((jbooleanArray)obj, NULL);
		VARIANT_BOOL* p = (VARIANT_BOOL*)malloc(sizeof(VARIANT_BOOL));
		*p = (ary[0]!=JNI_FALSE) ? VARIANT_TRUE : VARIANT_FALSE;
		var->vt = VT_BOOL|VT_BYREF;
		var->pboolVal = p;
		env->ReleaseBooleanArrayElements((jbooleanArray)obj, NULL, JNI_ABORT);
		return 0;	// ����I��
	}

	// String[] �Ȃ� VT_BSTR|VT_BYREF ��ݒ�
	jclass clsStringArray = env->FindClass("[Ljava/lang/String;");
	if(env->IsInstanceOf(obj, clsStringArray)) {
		jobject val = env->GetObjectArrayElement((jobjectArray)obj, 0);
		BSTR bstrVal = jstring2BSTR(env, (jstring)val);
		var->vt = VT_BSTR|VT_BYREF;
		var->pbstrVal = &bstrVal;
		return 0;	// ����I��
	}

	printf("ERROR!!! cannot convert jobject to VARIANT.\n");
	return -1;
}


// �o���A���g���������B(?)
void	VARIANT_free(VARIANT* var)
{
	switch(var->vt) {
		case VT_BSTR:	// String ��ݒ�
			SysFreeString(var->bstrVal);
			break;
		case VT_DISPATCH:	// JCom ��ݒ�
			break;
		case VT_UI1|VT_BYREF:
			free(var->pbVal);
			break;
		case VT_I2|VT_BYREF:
			free(var->piVal);
			break;
		case VT_I4|VT_BYREF:
			free(var->plVal);
			break;
		case VT_R4|VT_BYREF:
			free(var->pfltVal);
			break;
		case VT_R8|VT_BYREF:
			free(var->pdblVal);
			break;
		case VT_BOOL|VT_BYREF:
			free(var->pboolVal);
			break;
		case VT_BSTR|VT_BYREF:
			SysFreeString(*var->pbstrVal);
			break;
		default:
			break;
	}
}



int	VARIANT2jobject(JNIEnv* env, jobject obj, VARIANT* var, jobject* result)
{
	switch(var->vt) {
		case VT_EMPTY:		// null ��ݒ�
			*result = NULL;
			return 0;	// ����I��
			break;

		case VT_UI1:	// Byte ��ݒ�
		{
			jclass clsByte = env->FindClass("java/lang/Byte");
			jmethodID method = env->GetMethodID(clsByte, "<init>", "(B)V");
			*result = env->NewObject(clsByte, method, (jbyte)var->bVal);
			return 0;	// ����I��
			break;
		}
		case VT_I2:		// Short ��ݒ�
		{
			jclass clsShort = env->FindClass("java/lang/Short");
			jmethodID method = env->GetMethodID(clsShort, "<init>", "(S)V");
			*result = env->NewObject(clsShort, method, (jshort)var->iVal);
			return 0;	// ����I��
			break;
		}
		case VT_I4:		// Integer ��ݒ�
		{
			jclass clsInteger = env->FindClass("java/lang/Integer");
			jmethodID method = env->GetMethodID(clsInteger, "<init>", "(I)V");
			*result = env->NewObject(clsInteger, method, (jint)var->lVal);
			return 0;	// ����I��
			break;
		}
		case VT_R4:		// Float ��ݒ�
		{
			jclass clsFloat = env->FindClass("java/lang/Float");
			jmethodID method = env->GetMethodID(clsFloat, "<init>", "(F)V");
			*result = env->NewObject(clsFloat, method, (jfloat)var->fltVal);
			return 0;	// ����I��
			break;
		}
		case VT_R8:		// Double ��ݒ�
		{
			jclass clsDouble = env->FindClass("java/lang/Double");
			jmethodID method = env->GetMethodID(clsDouble, "<init>", "(D)V");
			*result = env->NewObject(clsDouble, method, (jdouble)var->dblVal);
			return 0;	// ����I��
			break;
		}
		case VT_BOOL:	// Boolean ��ݒ�
		{
			jclass clsBoolean = env->FindClass("java/lang/Boolean");
			jmethodID method = env->GetMethodID(clsBoolean, "<init>", "(Z)V");
			*result = env->NewObject(clsBoolean, method, var->boolVal ? JNI_TRUE : JNI_FALSE);
			return 0;	// ����I��
			break;
		}

		case VT_BSTR:	// String ��ݒ�
		{
			jclass clsString = env->FindClass("java/lang/String");
			*result = BSTR2jstring(env, var->bstrVal);
			return 0;	// ����I��
			break;
		}

		case VT_DATE:	// java.util.Date ��ݒ�
		{
			/*	DATE��double�Ŏ�������A1.0��1���ɑΉ����A
				0.0 �� 1900/01/00 00:00:00 �ɂȂ��Ă���B
				����AJava.util.Date�� long�Ŏ�������A1msec�ɑΉ����A
				1970/01/01 00:00:00 GMT�ɑΉ�����B
				��������Ⴄ���A�^���Ⴄ�B
				�����āA�Ȃɂ�蒍�ӂ��Ȃ���΂Ȃ�Ȃ��̂́ADATE�^�ɂ�
				���P�[�����l������Ă��Ȃ��B�܂�A���̓��t�͂ǂ̍��ł�
				���Ԃ��͗��p�ґ��ŊǗ����Ȃ���΂Ȃ�Ȃ��B
			*/
			jclass clsDate = env->FindClass("java/util/Date");
			jmethodID method = env->GetMethodID(clsDate, "<init>", "(J)V");
			double date19000100 = var->date;
			TIME_ZONE_INFORMATION timeinfo;
			GetTimeZoneInformation(&timeinfo);
			double date19700101GMT = date19000100+(timeinfo.Bias/(60.0*24.0))-25569.0;
			*result = env->NewObject(clsDate, method, (jlong)__int64(date19700101GMT*24*60*60*1000));
			return 0;	// ����I��
			break;
		}

		case VT_CY:	// VariantCurrency ��ݒ�
		{
			/*	CY,CURRENCY��LONGLONG�Ŏ�������A10�i����10000��1\(1$)�ɑΉ�����B
				Java�ɂ͒ʉ݌^���Ȃ��B�]���āA�Ǝ��̒ʉ݌^ VariantCurrency�����A
				�����Ԃ��悤�ɂ����B
			*/
			jclass clsCurrency = env->FindClass(CLASS_VARIANT_CURRENCY);
			jmethodID method = env->GetMethodID(clsCurrency, "<init>", "(D)V");
			*result = env->NewObject(clsCurrency, method, (jdouble)(var->cyVal.int64/10000.0));
			return 0;	// ����I��
			break;
		}
		case VT_UNKNOWN:	// IUnknown �ŕԂ��B
		{
			// IUnknown(ReleaseManager rm, int pIUnknown)�B
			jclass clsIUnknown = env->FindClass(CLASS_IUNKNOWN);
			jmethodID method = env->GetMethodID(clsIUnknown, "<init>", "(L"CLASS_RELEASEMANAGER";I)V");
			// this.rm �����o��
			jfieldID rmID = env->GetFieldID(clsIUnknown, "rm", "L"CLASS_RELEASEMANAGER";");
			jobject rm = env->GetObjectField(obj, rmID);
			// �R���X�g���N�^
			*result = env->NewObject(clsIUnknown, method, rm, (jint)(int)var->punkVal);
			return 0;	// ����I��
			break;
		}
		case VT_DISPATCH:	// IDispatch ��ݒ�
		{
			jclass clsIDispatch = env->FindClass(CLASS_IDISPATCH);
			jmethodID method = env->GetMethodID(clsIDispatch, "<init>", "(L"CLASS_RELEASEMANAGER";I)V");
			jfieldID rmID = env->GetFieldID(clsIDispatch, "rm", "L"CLASS_RELEASEMANAGER";");
			jobject rm = env->GetObjectField(obj,  rmID);
			*result = env->NewObject(clsIDispatch, method, rm, (jint)var->pdispVal);

			return 0;	// ����I��
			break;
		}
		case VT_I4|VT_BYREF:	// int[] ��ݒ�
		{
			jintArray intArray = env->NewIntArray(1);
			env->SetIntArrayRegion(intArray, 0, 1, (jint*)(var->plVal));
		
			*result = intArray;
			return 0;	// ����I��
			break;
		}
		case VT_R8|VT_BYREF:	// double[] ��ݒ�
		{
			jdoubleArray doubleArray = env->NewDoubleArray(1);
			env->SetDoubleArrayRegion(doubleArray, 0, 1, (jdouble*)(var->pdblVal));
			*result = doubleArray;
			return 0;	// ����I��
			break;
		}
		case VT_R4|VT_BYREF:	// float[] ��ݒ�
		{
			jfloatArray floatArray = env->NewFloatArray(1);
			env->SetFloatArrayRegion(floatArray, 0, 1, (jfloat*)(var->pfltVal));
			*result = floatArray;
			return 0;	// ����I��
			break;
		}
		case VT_BOOL|VT_BYREF:	// boolean[] ��ݒ�
		{
			jbooleanArray booleanArray = env->NewBooleanArray(1);
			jboolean val = ((*var->pboolVal)!=VARIANT_FALSE) ? JNI_TRUE : JNI_FALSE;
			env->SetBooleanArrayRegion(booleanArray, 0, 1, &val);
			*result = booleanArray;
			return 0;	// ����I��
			break;
		}
		case VT_BSTR|VT_BYREF:	// String[] ��ݒ�
		{
			jstring str = BSTR2jstring(env, *var->pbstrVal);
			jclass clsString = env->FindClass("java/lang/String");
			jobjectArray strArray = env->NewObjectArray(1, clsString, str);
			*result = strArray;
			return 0;	// ����I��
			break;
		}

		default:
			printf("jcom.dll: no support VT_");
			print_VARTYPE(var->vt);
			printf("\n");
			break;
	}

	*result = NULL;
	return var->vt;
}


//	������result�́A�����̔z��ɂȂ��Ă���B�����ɒ��ځA�l���Z�b�g����B
//  invoke()��BYREF�̈����ɑ΂��āA�l���t�B�[�h�o�b�N������B
int	feedbackVARIANT2jobject(JNIEnv* env, jobject obj, VARIANT* var, jobject result)
{
	switch(var->vt) {
		case VT_UI1|VT_BYREF:	// byte[] ��ݒ�
		{
			env->SetByteArrayRegion((jbyteArray)(result), 0, 1, (jbyte*)(var->pbVal));
			return 0;	// ����I��
			break;
		}
		case VT_I2|VT_BYREF:	// short[] ��ݒ�
		{
			env->SetShortArrayRegion((jshortArray)(result), 0, 1, (jshort*)(var->piVal));
			return 0;	// ����I��
			break;
		}
		case VT_I4|VT_BYREF:	// int[] ��ݒ�
		{
			env->SetIntArrayRegion((jintArray)(result), 0, 1, (jint*)(var->plVal));
			return 0;	// ����I��
			break;
		}
		case VT_R4|VT_BYREF:	// float[] ��ݒ�
		{
			env->SetFloatArrayRegion((jfloatArray)result, 0, 1, (jfloat*)(var->pfltVal));
			return 0;	// ����I��
			break;
		}
		case VT_R8|VT_BYREF:	// double[] ��ݒ�
		{
			env->SetDoubleArrayRegion((jdoubleArray)result, 0, 1, (jdouble*)(var->pdblVal));
			return 0;	// ����I��
			break;
		}
		case VT_BOOL|VT_BYREF:	// boolean[] ��ݒ�
		{
			jboolean val = ((*var->pboolVal)!=VARIANT_FALSE) ? JNI_TRUE : JNI_FALSE;
			env->SetBooleanArrayRegion((jbooleanArray)result, 0, 1, &val);
			return 0;	// ����I��
			break;
		}
		case VT_BSTR|VT_BYREF:	// String[] ��ݒ�
		{
			jstring str = BSTR2jstring(env, *var->pbstrVal);
			env->SetObjectArrayElement((jobjectArray)result, 0, str);
			return 0;	// ����I��
			break;
		}

		default:
			printf("jcom.dll: no support VT_");
			print_VARTYPE(var->vt);
			printf("\n");
			break;
	}

	return var->vt;
}


//------------------------------------------------------------------------------
//	VARTYPE�^(unsinged short)��\��
void print_VARTYPE(VARTYPE vt)
{
	switch(vt) {
	case VT_EMPTY:				printf("EMPTY");			break;
	case VT_NULL:				printf("NULL");				break;
	case VT_I2:					printf("I2");				break;
	case VT_I4:					printf("I4");				break;
	case VT_R4:					printf("R4");				break;
	case VT_R8:					printf("R8");				break;
	case VT_CY:					printf("CY");				break;
	case VT_DATE:				printf("DATE");				break;
	case VT_BSTR:				printf("BSTR");				break;
	case VT_DISPATCH:			printf("DISPATCH");			break;
	case VT_ERROR:				printf("ERROR");			break;
	case VT_BOOL:				printf("BOOL");				break;
	case VT_VARIANT:			printf("VARIANT");			break;
	case VT_UNKNOWN:			printf("UNKNOWN");			break;
	case VT_DECIMAL:			printf("DECIMAL");			break;
	case VT_I1:					printf("I1");				break;
	case VT_UI1:				printf("UI1");				break;
	case VT_UI2:				printf("UI2");				break;
	case VT_UI4:				printf("UI4");				break;
	case VT_I8:					printf("I8");				break;
	case VT_UI8:				printf("UI8");				break;
	case VT_INT:				printf("INT");				break;
	case VT_UINT:				printf("UINT");				break;
	case VT_VOID:				printf("VOID");				break;
	case VT_HRESULT:			printf("HRESULT");			break;
	case VT_PTR:				printf("PTR");				break;
	case VT_SAFEARRAY:			printf("SAFEARRAY");		break;
	case VT_CARRAY:				printf("CARRAY");			break;
	case VT_USERDEFINED:		printf("USERDEFINED");		break;
	case VT_LPSTR:				printf("LPSTR");			break;
	case VT_LPWSTR:				printf("LPWSTR");			break;
	case VT_FILETIME:			printf("FILETIME");			break;
	case VT_BLOB:				printf("BLOB");				break;
	case VT_STREAM:				printf("STREAM");			break;
	case VT_STORAGE:			printf("STORAGE");			break;
	case VT_STREAMED_OBJECT:	printf("STREAMED_OBJECT");	break;
	case VT_STORED_OBJECT:		printf("STORED_OBJECT");	break;
	case VT_BLOB_OBJECT:		printf("BLOB_OBJECT");		break;
	case VT_CF:					printf("CF");				break;
	case VT_CLSID:				printf("CLSID");			break;
	default:					printf("??? VARTYPE=%Xh.", vt);
    };
}
/*
COleDispatchDriver::InvokeHelper()�ł͈ȉ��̌^�̂�
�T�|�[�g���Ă���B�]���āA�������T�|�[�g���Ă����
�����̂ł͂Ȃ����낤���B

VT_EMPTY	void
VT_I2		short
VT_I4		long
VT_R4		float
VT_R8		double
VT_CY		CY
VT_DATE		DATE
VT_BSTR		BSTR		// OLECHAR*
VT_DISPATCH	LPDISPATCH
VT_ERROR	SCODE		// ULONG
VT_BOOL		BOOL
VT_VARIANT	VARIANT
VT_UNKNOWN	LPUNKNOWN


Use VARIANTARG to describe arguments passed within DISPPARAMS,
 and VARIANT to specify variant data that cannot be passed by reference .
The VARIANT type cannot have the VT_BYREF bit set.
VARIANTs can be passed by value, even if VARIANTARGs cannot. 

To simplify extracting values from VARIANTARGs, 
Automation provides a set of functions for manipulating this type.
Use of these functions is strongly recommended to ensure 
that applications apply consistent coercion rules. 

governs 
*/
/*
2000/08/15 �Ȃ��AVT_BYREF�̂Ƃ��ɂ͒l���q�[�v�Ɋm�ۂ��Ă��邩�H
�܂��A�|�C���^�����������\�����Ȃ��̂��ǂ������킩��Ȃ������B
���̖��O�̂Ƃ���A�u�Q�Ɓv�Ȃ�|�C���^�̒l�͂����Ȃ��ł��낤�B
�������A�|�C���^�œn���Ă������A�ς��邱�Ƃ��\�ł���B
�Ⴆ�΁ABSTR�Œl���ς��ꍇ�͏\���ς��\���͂���B

����ɁA�X�^�b�N��ɂƂ邱�Ƃ͂ł��Ȃ��B���̊֐����I������Ƃ��ɁA
�X�^�b�N��Ɏ��ꂽ�l�́A�X�R�[�v����O��Ă��܂�����ł���B

�i���������Ɏ��̂͂���̂�����A���̃|�C���^�݂̂�n���΂悢��
�v����������Ȃ����A������_���Bint��double�Ȃ�Ƃ������A������
�Ȃǂ͒l���̂��̂̌`�����Ⴄ�̂ŁA���̂܂ܓn���킯�ɂ͂����Ȃ��B

���̊֐���IDispatch::Invoke()�Ŏg���Ă���A�Ă΂ꂽ��ɁA
�q�[�v�Ŋm�ۂ����l��������Ă��悤�ɂ���B
enum VARENUM {
    VT_EMPTY = 0,
    VT_NULL = 1,
    VT_I2 = 2,
    VT_I4 = 3,
    VT_R4 = 4,
    VT_R8 = 5,
    VT_CY = 6,
    VT_DATE = 7,
    VT_BSTR = 8,
    VT_DISPATCH = 9,
    VT_ERROR = 10,
    VT_BOOL = 11,
    VT_VARIANT = 12,
    VT_UNKNOWN = 13,
    VT_DECIMAL = 14,
    VT_I1 = 16,
    VT_UI1 = 17,
    VT_UI2 = 18,
    VT_UI4 = 19,
    VT_I8 = 20,
    VT_UI8 = 21,
    VT_INT = 22,
    VT_UINT = 23,
    VT_VOID = 24,
    VT_HRESULT  = 25,
    VT_PTR = 26,
    VT_SAFEARRAY = 27,
    VT_CARRAY = 28,
    VT_USERDEFINED = 29,
    VT_LPSTR = 30,
    VT_LPWSTR = 31,
    VT_FILETIME = 64,
    VT_BLOB = 65,
    VT_STREAM = 66,
    VT_STORAGE = 67,
    VT_STREAMED_OBJECT = 68,
    VT_STORED_OBJECT = 69,
    VT_BLOB_OBJECT = 70,
    VT_CF = 71,
    VT_CLSID = 72,
    VT_VECTOR = 0x1000,
    VT_ARRAY = 0x2000,
    VT_BYREF = 0x4000,
    VT_RESERVED = 0x8000,
    VT_ILLEGAL = 0xffff,
    VT_ILLEGALMASKED = 0xfff,
    VT_TYPEMASK = 0xfff
};

*/
