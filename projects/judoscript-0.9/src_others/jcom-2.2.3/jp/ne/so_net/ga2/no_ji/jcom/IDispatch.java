package jp.ne.so_net.ga2.no_ji.jcom;

/**
 * IDispatchインターフェースを扱うためのクラス
 *
 * @see     IUnknown
 * @see     JComException
 * @see     ReleaseManager
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.10, 2000/08/23
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class IDispatch extends IUnknown {
    /**
		IID_IDispatch です。
		@see       GUID
	*/
	public static GUID IID = new GUID( 0x00020400, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );
	public static final int METHOD         = 1;
	public static final int PROPERTYGET    = 2;
	public static final int PROPERTYPUT    = 4;
	public static final int PROPERTYPUTREF = 8;

    /**
     * IDispatchを作成します。
     * 引数で指定されたProgIDのCOMオブジェクトを作成します。
     *        <pre>
     *        ReleaseManager rm = new ReleaseManager();
     *        try {
     *            IDispatch excel = new IDispatch(rm ,"Excel.Application");
     *            excel.put("Visible", new Boolean(true));  // 'デフォルトはFalse(表示しない)
     *            // ...
     *            excel.invoke("Quit", null);
     *        } catch(JComException e) {
     *            e.printStackTrace();
     *        } finally {
     *            rm.release();
     *        }</pre>
     * @param     rm     	参照カウンタ管理クラス
     * @param	  ProgID	プログラムＩＤ。Excelの場合"Excel.Application"と指定します。
     * @see       #create(String)
     * @see       ReleaseManager
	 */
	public IDispatch(ReleaseManager rm, String ProgID) throws JComException {
		super(rm);
		create(ProgID);
	}

    /**
     * IDispatchを作成します。
     * 引数で指定されたIDispatchポインタのCOMを作成します。
     * 通常、別のCOMのメソッドから返されたIDispatchポインタに対して、
     * 使用します。
     * @param     rm             参照カウンタ管理クラス
     * @param     pIDispatch     IDispatchインターフェースのアドレス
     * @see       #create(String)
     * @see       ReleaseManager
	 */
	public IDispatch(ReleaseManager rm, int pIDispatch) {
		super(rm, pIDispatch);
	}

    /**
     * IDispatchを作成します。
     * 引数で指定されたIDispatchと同一のＣＯＭを管理します。
     * 通常、別のCOMオブジェクトのメソッドで返されたIDispatchに対して、
     * 使用します。
     * ReleaseManagerは、引数dispの持つ同じものが継承されます。
     * @see       #create(String)
	 */
	public IDispatch(IDispatch disp) {
		super(disp.rm, disp.pIUnknown);
	}

    /**
     * ProgIDからIDispatchインターフェースを作成します。
     * すでに作成されていた場合、例外を発生させます。
     * @param     ProgID COMのプログラムID
     * @exception JComException <BR>
     *              "Already COM allocated" すでにCOMが割り当てられている。<BR>
     *              "createInstance() failed HRESULT=0x%XL" ＣＯＭの作成に失敗した。
     */
	public synchronized void create(String ProgID) throws JComException {
		_create(ProgID);
		if(rm!=null) rm.add(this);
	}

    /**
     * プロパティの値を取得します。
     * プロパティの型とＪａｖａの型との対応は以下の通りです。
     * <pre>
     *   VT_EMPTY    null
     *   VT_I4       Integer
     *   VT_R8       Double
     *   VT_BOOL     Boolean
     *   VT_BSTR     String
     *   VT_DATE     Date
     *   VT_CY       VariantCurrency
     *   VT_DISPATCH IDispatch
     *   VT_UNKNOWN  IUnknown
     * </pre>
     *	<pre>IDispatch xlBooks = (IDispatch)xlApp.get("Workbooks");</pre>
	 *  <pre>Boolean visible = xlApp.gut("Visible");</pre>
     * @param     property プロパティ名
     * @return    取得された値
     * @exception JComException <BR>
     *              "IDispatch not created"   IDispatchが作成されていない。<BR>
     *              "getProperty() failed HRESULT=0x%XL" ＣＯＭの呼び出しに失敗した。<BR>
     *              "cannot convert VT=0x%X" 未対応のVARIANT型が返された。
     * @see       #get(String,Object[])
     * @see       #invoke(String,Object[])
     * @see       #put(String,Object)
     */
/*	public synchronized Object get(String property) throws JComException {
		Object ret = _get(property);
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}
*/
	public synchronized Object get(String property) throws JComException {
		int dispid = _getIDsOfNames(property);
		Object ret = _invoke(dispid, PROPERTYGET, null);
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}


    /**
     * プロパティの値を取得します。プロパティの値の取得に引数が必要な場合に使用します。
     * プロパティの型とＪａｖａの型との対応はIDispatch.get(property)を参照してください。
     * 引数の渡し方はIDispatch.invoke()を参照してください。
     * @param     property プロパティ名
     * @param     args 引数の配列
     * @return    取得された値
     * @exception JComException <BR>
     *              "IDispatch not created"   IDispatchが作成されていない。<BR>
     *              "Invalid argument(index=%d)" 引数が不正。もしくは未対応の型が渡された。<BR>
     *              "getPropertyArg() failed HRESULT=0x%XL" ＣＯＭの呼び出しに失敗した。<BR>
     *              "cannot convert VT=0x%X" 未対応のVARIANT型が返された。
     * @see       #get(String)
     * @see       #invoke(String,Object[])
     * @see       #put(String,Object)
     */
/*	public synchronized Object get(String property, Object[] args) throws JComException {
		Object ret = _get(property, args);
		// ＣＯＭオブジェクトが返されたら、それを参照カウンタ管理クラスに追加する。
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}
*/
	public synchronized Object get(String property, Object[] args) throws JComException {
		int dispid = _getIDsOfNames(property);
		Object ret = _invoke(dispid, PROPERTYGET, args);
		// ＣＯＭオブジェクトが返されたら、それを参照カウンタ管理クラスに追加する。
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}


    /**
     * プロパティに値を設定します。
     * プロパティの型とＪａｖａの型との対応はIDispatch.get(property)を参照してください。
			<pre>xlApp.put("Visible", new Boolean(true));</pre>
			<pre>xlRange.put("Value","JComすごいぞ！(^o^)");</pre>
     * @param     property プロパティ名
     * @param     val 設定する値
     * @exception JComException <BR>
     *              "IDispatch not created"   IDispatchが作成されていない。<BR>
     *              "Invalid argument" 引数が不正。もしくは未対応の型が渡された。<BR>
     *              "putProperty() failed HRESULT=0x%XL" ＣＯＭの呼び出しに失敗した。<BR>
     * @see       #get(String)
     * @see       #get(String,Object[])
     * @see       #invoke(String,Object[])
     */
/*	public synchronized void   put(String property, Object val) throws JComException {
		_put(property, val);
	}
*/
	public synchronized void   put(String property, Object val) throws JComException {
		int dispid = _getIDsOfNames(property);
		Object[] args = new Object[1];
		args[0] = val;
		_invoke(dispid, PROPERTYPUT, args);
	}



    /**
     * メソッドを呼び出します。
     * プロパティの型とＪａｖａの型との対応はJCom.get(property)を参照してください。
     * <pre>
			Object[] arglist = new Object[3];
			arglist[0] = new Boolean(false);
			arglist[1] = null;
			arglist[2] = new Boolean(false);
			xlBook.method("Close", arglist);
     * </pre>
     * @param     method メソッド名
     * @param     args   引数
     * @exception JComException <BR>
     *              "IDispatch not created"   IDispatchが作成されていない。<BR>
     *              "Invalid argument(index=%d)" 引数が不正。もしくは未対応の型が渡された。<BR>
     *              "invokeMethod() failed HRESULT=0x%XL" ＣＯＭの呼び出しに失敗した。<BR>
     *              "cannot convert VT=0x%X" 未対応のVARIANT型が返された。
     * @see       #get(String)
     * @see       #get(String,Object[])
     * @see       #put(String,Object)
	 */
/*
	public synchronized Object method(String method, Object[] args) throws JComException {
		Object ret = _method(method, args);
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}
*/
	public synchronized Object method(String method, Object[] args) throws JComException {
		int dispid = _getIDsOfNames(method);
		Object ret = _invoke(dispid, METHOD, args);
		if((rm!=null) && (ret instanceof IUnknown))
			rm.add((IUnknown)ret);
		return ret;
	}

    /**
		method()を参照してください。
		@see	#method(String,Object[])
		@deprecated	method(String,Object[])に置き換わりました。
	 */
	public synchronized Object invoke(String method, Object[] args) throws JComException {
		return method(method, args);
	}

    /**
		メソッド、プロパティの設定・取得を行います。
		プロパティの型とＪａｖａの型との対応はJCom.get(property)を参照してください。
		<pre>
			Object[] arglist = new Object[3];
			arglist[0] = new Boolean(false);
			arglist[1] = null;
			arglist[2] = new Boolean(false);
			xlBook.invoke("Close", IDispatch.METHOD, arglist);
		</pre>
		@param     method メソッド名
		@param     args   引数
		@exception JComException <BR>
		             "IDispatch not created"   IDispatchが作成されていない。<BR>
		             "Invalid argument(index=%d)" 引数が不正。もしくは未対応の型が渡された。<BR>
		             "invokeMethod() failed HRESULT=0x%XL" ＣＯＭの呼び出しに失敗した。<BR>
		             "cannot convert VT=0x%X" 未対応のVARIANT型が返された。
		@see       #get(String)
		@see       #get(String,Object[])
		@see       #put(String,Object)
		@see       #method(String,Object[])
	*/
	public synchronized Object invoke(String name, int wFlags, Object[] pDispParams) throws JComException {
		int dispid = _getIDsOfNames(name);
		return _invoke(dispid, wFlags, pDispParams);
	}

    /**
    	ITypeInfoを取得します。
	*/
	public synchronized ITypeInfo getTypeInfo() throws JComException {
		int pITypeInfo = _getTypeInfo();
		return new ITypeInfo(rm, pITypeInfo);
	}

	// release()はsuperのでＯＫ．

	// JNI
	private native void   _create(String ProgID) throws JComException;
	private native Object _get(String property) throws JComException;
	private native Object _get(String property, Object[] args) throws JComException;
	private native void   _put(String property, Object val) throws JComException;
	private native Object _method(String method, Object[] args) throws JComException;
	private native int    _getTypeInfo() throws JComException;
	private native Object _invoke(int dispIdMember, int wFlags, Object[] pDispParams) throws JComException;
	private native int    _getIDsOfNames(String name) throws JComException;

	// jcom.dllを読み込みます。
    static {
        System.loadLibrary("jcom");
    }
}

