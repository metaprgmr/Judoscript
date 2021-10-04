package jp.ne.so_net.ga2.no_ji.jcom;
import java.lang.reflect.*;

/**
 * IPersistインターフェースを扱うためのクラス
 	このクラスはCLSIDを取得するためだけにあります。
 	以下の方法で、このインターフェースをサポートしている
 	ＣＯＭオブジェクトに対してCLSID、もしくはProgIDを
 	取得することが可能です。ただし、すべてのオブジェクトが
 	このインターフェースをサポートしているわけではありません。
	Excelの場合、Excel.Applicationでは使えません。
	Excel.Sheet, Excel.Chart がバージョン付きの形でProgIDを
	返します。("Excel.Chart.8"の形式)
	あと、ワードのWord.Documentなども返します。
	以下はProgIDを取得するためのサンプルです。
	<PRE>
 *   public static String getProgID(IUnknown unknown) {
 *       try {
 *           IPersist persist = (IPersist)unknown.queryInterface(IPersist.class, IPersist.IID);
 *           if(persist==null) return null;
 *           GUID clsid = persist.getClassID();
 *           return Com.getProgIDFromCLSID(clsid);
 *       }
 *       catch(JComException e) { e.printStackTrace(); }
 *       return null;
 *   }</PRE>
 *
 * @see     IUnknown
 * @see     JComException
 * @see     ReleaseManager
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.21, 2000/11/27
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class IPersist extends IUnknown {
    /**
		IID_IPersist です。0000010c-0000-0000-C000-000000000046
		@see       GUID
	*/
	public static GUID IID = new GUID( 0x0000010C, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );

    /**
     * IPersistを作成します。
     * 引数で指定されたIPersistポインタのCOMを作成します。
     * 通常、別のCOMのメソッドから返されたIPersistポインタに対して、
     * 使用します。
     * @param     rm             参照カウンタ管理クラス
     * @param     pIPersist     IPersistインターフェースのアドレス
     * @see       ReleaseManager
	 */
	public IPersist(ReleaseManager rm, int pIPersist) {
		super(rm, pIPersist);
	}

	/**
		CLSIDを返します。
	*/
	public synchronized GUID getClassID() throws JComException {
		return _getClassID();
	}

	// release()はsuperのでＯＫ．

	// ＪＮＩ
	private native GUID _getClassID() throws JComException;
}

