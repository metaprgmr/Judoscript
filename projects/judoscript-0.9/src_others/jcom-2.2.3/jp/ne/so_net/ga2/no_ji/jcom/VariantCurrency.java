package jp.ne.so_net.ga2.no_ji.jcom;

import java.text.NumberFormat;

/**
 * VARIANT型の通貨型を定義します。
 * VARIANTではCY/CURRENCYはLONGLONGとして実装されていますが、
 * Javaでは通常通貨はdoubleっています。明示的に通貨とdoubleを
 * 区別するために、このように実装しました。
 *
 * @see     IDispatch
 * @see     JComException
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.00, 2000/06/25
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class VariantCurrency {
	double value;
	static NumberFormat price = NumberFormat.getCurrencyInstance();

    /**
     * 指定された金額でVariantCurrencyを作成します。
     * @param     value 金額
     */
	public VariantCurrency(double value) { this.value = value; }

    /**
     * VariantCurrencyを作成します。金額は０で初期化されます。
     */
	public VariantCurrency() { this(0.0); }

    /**
     * 指定した金額を設定します。
     * @param	value	金額
     */
	public void set(double value) { this.value = value; }

	/**
     * 金額を取得します。
     * @return	金額
     */
	public double get() { return value; }

    /**
     * 金額を文字列に変換します。
     * 書式は
     * <code>NumberFormat.getCurrencyInstance()</code>
     * に従います。
     * @return	文字列
     */
	public String toString() {
		return price.format(value);
	}
}
