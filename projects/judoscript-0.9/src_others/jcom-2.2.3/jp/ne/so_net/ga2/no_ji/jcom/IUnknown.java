package jp.ne.so_net.ga2.no_ji.jcom;
import java.lang.reflect.*;

/**
 * IUnknownインターフェースを扱うためのクラス
 *
 * @see     IDispatch
 * @see     JComException
 * @see     ReleaseManager
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.00, 2000/06/25
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class IUnknown {
    /**
		IID_IUnknown です。
		@see       GUID
	*/
	public static GUID IID = new GUID( 0x00000000, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );

	/**
		IUnknownインターフェースのポインタを保持します。
		変更しないでください。
		IUnknown interface pointer.
		Don't change!
	*/
	protected int pIUnknown = 0;		// IUnknownインターフェースを持つオブジェクトのポインタ
	/**
		参照カウンタ管理クラス
		Reference counter management class.
	*/
	protected ReleaseManager rm = null;

    /**
     * 空のIUnknownを作成します。
	 * COMは割り当てられていないので注意
     * @param     rm             参照カウンタ管理クラス
     * @see       ReleaseManager
	 */
	public IUnknown(ReleaseManager rm) {
		this.rm = rm;
		this.pIUnknown = 0;
	}

    /**
     * IUnknownを作成します。
     * 引数で指定されたIUnknownポインタのCOMを作成します。
     * 通常、別のCOMのメソッドから返されたIUnknownポインタに対して、
     * 使用します。
     * @param     rm             参照カウンタ管理クラス
     * @param     IUnknown       IUnknownインターフェースのアドレス
     * @see       ReleaseManager
	 */
	public IUnknown(ReleaseManager rm, int pIUnknown) {
		this.rm = rm;
		this.pIUnknown = pIUnknown;
	}

    /**
     * QueryInterfaceを実行し、指定したインターフェースを取得します。
     * IID と Java側のクラスを指定して下さい。
     *  <pre>
     *  IUnknown iUnknown = (IUnknown)worksheets.get("_NewEnum");
     *  IEnumVARIANT a = (IEnumVARIANT)iUnknown.queryInterface(
     *                      IEnumVARIANT.class,
     *                      IEnumVARIANT.IID);
     *  </pre>
		2000.11.27 サポートしていないインターフェースは確実にnullを返すようにした。
     * @param     jclass         Ｊａｖａ側のクラス
     * @param     IID            インターフェースＩＤ
     * @result    指定したインターフェースを返します。
     *            失敗するとnullを返します。
     * @see       GUID
	 */
	public synchronized IUnknown queryInterface(Class jclass, GUID IID) throws JComException {
		try {
			int pIUnknown = _queryInterface(IID);
			if(pIUnknown == 0) return null;		// no interface
			// クラス名のコンストラクタを呼ぶ。引数は(ReleaseManager rm, int pIUnknown);
			Class[] param = new Class[2];
			param[0] = ReleaseManager.class;
			param[1] = Integer.TYPE;
			Constructor constructor = jclass.getConstructor(param);
			Object[] p = new Object[2];
			p[0] = rm;
			p[1] = new Integer(pIUnknown);
			IUnknown com = (IUnknown)constructor.newInstance(p);
			// ReleaseManagerに登録
			rm.add(com);
			return com;
		}
		// JComExceptionは上に投げる。
		catch(JComException e) { throw e; }
		catch(Exception e) { e.printStackTrace(); }
		return null;	
	}

    /**
     * QueryInterfaceを実行し、指定したインターフェースを取得します。
     * IID と Java側のクラス名を指定して下さい。
     * @param     classname      Ｊａｖａ側のクラス名
     * @param     IID            インターフェースＩＤ
     * @result    指定したインターフェースを返します。
     *            失敗するとnullを返します。
     * @see       GUID
     * @deprecation #queryInterface(Class, GUID)
	 */
	public synchronized IUnknown queryInterface(String classname, GUID IID)
					 throws JComException, ClassNotFoundException {
		Class jclass = Class.forName(classname);
		return queryInterface(jclass, IID);
	}

    /**
     * COMオブジェクトを解放します。
     * ReleaseManagerを使えば、明示的に呼ぶ必要はありません。
     * すでに解放されていた場合、falseを返します。
     * @result    正常終了の場合は<code> true </code>を返します。
     *            すでに解放されていた場合は、<code> false </code>を返します。
	 */
	public synchronized boolean release() {
		return _release();
	}

    /**
     * 参照カウンタを１つあげ、現在のカウンタ値を返します。
     * 通常呼ぶ必要はありません。
     * 参照カウンタを見たい場合でのみ使用します。
     * その場合はrelease()を呼んで、カウンタを下げて下さい。
     * @result    参照カウンタの値。上がった値を返します。
     * @see       #release()
	 */
	public synchronized int addRef() {
		return _addRef();
	}
	
	/**
		内部で保持しているIUnknownクラス、またはそれから継承したクラスの
		オブジェクトを以下の形で表示します。
		<pre>476eb8(1)jp.ne.so_net.ga2.no_ji.jcom.IDispatch</pre>
		１６進数はインターフェースのポインタ、
		括弧の中の数値は参照カウンタの数、その次はクラス名です。
	*/
	public String toString() {
		String result = Integer.toHexString(pIUnknown) + "(" + (addRef()-1) + ")" + getClass().getName();
		release();		// 上記addRef()でカウントアップした参照カウンタを１つ減らす。
		return result;
	}

	/**
		ReleaseManagerを返します。
		以下の形で、現在の参照カウンタ管理クラスを見ることができます。
		<pre>System.out.println(excel.getReleaseManager().toString());</pre>
     * @see       #release()
	 */
	public ReleaseManager getReleaseManager() {	return rm; }

	// ＪＮＩ
	private native int _addRef();
	private native boolean _release();
	private native int _queryInterface(GUID IID) throws JComException;
}

