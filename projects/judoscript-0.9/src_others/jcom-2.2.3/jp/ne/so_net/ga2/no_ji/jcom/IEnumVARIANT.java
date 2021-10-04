package jp.ne.so_net.ga2.no_ji.jcom;

/**
 *  コレクションオブジェクトを扱うためのクラス
 * IEnumVARIANTインターフェースにはClone()というメソッドがありますが、
 * それには対応していません。Next(),Reset(),Skip()にのみ対応しています。
 * また、Next()には利用目的に合わせ、２種類の関数を用意しています。
 *
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.00, 2000/06/25
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 * @see     IUnknown
 */
public class IEnumVARIANT extends IUnknown {
    /**
		IID_IEnumVARIANT です。
		@see       GUID
	*/
	public static GUID IID = new GUID( 0x00020404, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );

    /**
     * IEnumVARIANTを作成します。
	 * 引数pIEnumVARIANTはIUnknown.queryInterface()を使って
	 	取得した値を指定します。
     * @param     rm             参照カウンタ管理クラス
     	@param	pIEnumVARIANT	pIEnumVARIANTインターフェース
     * @see       ReleaseManager
	 */
	public IEnumVARIANT(ReleaseManager rm, int pIEnumVARIANT) {
		super(rm, pIEnumVARIANT);
	}

	/**
		１つ次のオブジェクトを取り出します。
		次のオブジェクトがない場合はnullを返します。
	*/
	public synchronized Object next() throws JComException {
		Object ret = _next();
		if(rm!=null && (ret instanceof IUnknown)) {
			rm.add((IUnknown)ret);
		}
		return ret;
	}

	/**
		指定した数だけ次のオブジェクトを取り出します。
		残りが少ない場合は、指定した数以下になる場合があります。
		配列の要素数に注意して下さい。
		celtは１以上の数を指定して下さい。
		@param	celt	取得するオブジェクトの数(1〜)
	*/
	public synchronized Object[] next(int celt) throws JComException {
		Object[] ary = _next(celt);
		if(rm!=null) {
			for(int i=0; i<ary.length; i++) {
				if(ary[i] instanceof IUnknown) {
					rm.add((IUnknown)ary[i]);
				}
			}
		}
		return ary;
	}

	/**
		最初からやり直します。
		カーソルを最初に移動します。
	*/
	public synchronized void reset() throws JComException {
		_reset();
	}

	/**
		指定した数だけオブジェクトをスキップさせます。
		celtは１以上の数を指定して下さい。
		@param	celt	スキップさせる数(1〜)
	*/
	public synchronized void skip(int celt) throws JComException {
		_skip(celt);
	}

	// JNI
	private native Object _next() throws JComException;
	private native Object[] _next(int celt) throws JComException;
	private native void _reset() throws JComException;
	private native void _skip(int celt) throws JComException;
}
