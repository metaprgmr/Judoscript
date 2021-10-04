#include <jni.h>
#include "jp_ne_so_0005fnet_ga2_no_0005fji_jcom_Com.h"
#include "atlbase.h"
#include "atlconv.h"
#include <stdio.h>
#include "jcom.h"


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_Com
 * Method:    _CLSIDFromProgID
 * Signature: (Ljava/lang/String;)Ljp/ne/so_net/ga2/no_ji/jcom/GUID;
 * **********************************************************************/
JNIEXPORT jobject JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_Com__1CLSIDFromProgID
  (JNIEnv *env, jclass cls, jstring progid)
{
	BSTR bstrProgID = jstring2BSTR(env, progid);
	CLSID clsid;
	HRESULT hr = CLSIDFromProgID(bstrProgID, &clsid);
	SysFreeString(bstrProgID);
	if(FAILED(hr)) return NULL;		// ‚È‚©‚Á‚½‚çNULL‚ð•Ô‚·
	return GUID2jobject(env, &clsid);
}


/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_Com
 * Method:    _ProgIDFromCLSID
 * Signature: (Ljp/ne/so_net/ga2/no_ji/jcom/GUID;)Ljava/lang/String;
 * **********************************************************************/
JNIEXPORT jstring JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_Com__1ProgIDFromCLSID
  (JNIEnv *env, jclass cls, jobject jclsid)
{
USES_CONVERSION;
	if(jclsid==NULL) return NULL;
	CLSID clsid;
	jobject2GUID(env, jclsid, &clsid);
/*
	BSTR bstrProgID;
	ProgIDFromCLSID(clsid, &bstrProgID); 
print_BSTR(bstrProgID);
	jstring jProgID = BSTR2jstring(env, bstrProgID);
	SysFreeString(bstrProgID);
*/
	LPOLESTR lpsz;
	ProgIDFromCLSID(clsid, &lpsz);	// NOT BSTR!!!
//for(int i=0; i<20; i++) {
//	printf("%02X ", lpsz[i]);
//}
	BSTR bstrProgID = OLE2BSTR(lpsz);
	jstring jProgID = BSTR2jstring(env, bstrProgID);
	SysFreeString(bstrProgID);
	CoTaskMemFree(lpsz);

	return jProgID;
}




/* **********************************************************************
 * Class:     jp_ne_so_0005fnet_ga2_no_0005fji_jcom_Com
 * Method:    StringFromIID
 * Signature: (Ljp/ne/so_net/ga2/no_ji/jcom/GUID;)Ljava/lang/String;
 	–¢Žg—pB‚·‚Å‚ÉJava‘¤ GUID‚ÅŽÀ‘•Ï‚Ý
 * **********************************************************************/
JNIEXPORT jstring JNICALL Java_jp_ne_so_1net_ga2_no_1ji_jcom_Com_StringFromIID
  (JNIEnv *env, jclass cls, jobject iid)
{
	// ˆø”‚©‚çIID‚ðŽæ“¾
	GUID IID;
	jobject2GUID(env, iid, &IID);
	// IID‚©‚çProgID‚ðŽæ“¾
	BSTR bstrString = NULL;
	HRESULT hr = StringFromIID(IID, &bstrString);
	if(FAILED(hr)) {
		printf("hr=%Xh \n", hr);
		return (jstring)NULL;
	}
	// BSTR‚©‚çjstring‚É•ÏŠ·
	jstring jString = BSTR2jstring(env, bstrString);
	SysFreeString(bstrString);
	return jString;
}

