package jp.ne.so_net.ga2.no_ji.jcom;

/**
 * ＣＯＭのグローバルな関数をサポートするクラス
 *
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.10, 2000/07/01
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class Com {
	public static GUID getCLSIDFromProgID(String ProgID) {
		return _CLSIDFromProgID(ProgID);
	}
	public static String getProgIDFromCLSID(GUID CLSID) {
		return _ProgIDFromCLSID(CLSID);
	}


	private static native GUID _CLSIDFromProgID(String ProgID);
	private static native String _ProgIDFromCLSID(GUID CLSID);
	// jcom.dllを読み込みます。
    static {
        System.loadLibrary("jcom");
    }
}

