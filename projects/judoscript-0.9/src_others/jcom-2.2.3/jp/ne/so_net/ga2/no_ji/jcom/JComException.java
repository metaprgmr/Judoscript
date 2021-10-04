package jp.ne.so_net.ga2.no_ji.jcom;
/**
	JCom 内で例外が発生したときにスローされます。
	例外の内容についてはgetMessage()を行って下さい。
	
	
	
	
	@see Exception
	@see IUnknown
	@see IDispatch
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.00, 2000/06/25
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
*/
public class JComException extends Exception {
    /**
     * 例外を作成します。
		@see IUnknown
		@see IDispatch
	 */
	JComException(String msg) {
		super(msg);
	}
}
