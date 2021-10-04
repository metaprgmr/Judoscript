package jp.ne.so_net.ga2.no_ji.jcom;
import java.util.*;

/**
 * ReleaseManager 参照カウンタ管理クラス。
 * 解放しなければならないオブジェクトを管理します。
 *
 *	比較的簡単なプログラムでは以下の形で利用して下さい。
 *   <pre>
 *   // 比較的短命のプログラム
 *   ReleaseManager rm = new ReleaseManager();
 *   try {
 *       IDispatch foo = new IDispatch(rm ,progid);
 *       // ...
 *   } catch(JComException e) {
 *       e.printStackTrace();
 *   } finally {
 *       rm.release();
 *   }
 *   </pre>
 * また、サーバーアプリケーションや、複雑で長時間動作するアプリケーション
 * では、ReleaseManagerの生成からrelease()を行うまでに、オブジェクトを解放しな
 * ければならない場合があります。オブジェクトが解放しないと、メモリを圧迫して
 * しまうからです。その場合は、適当な処理単位をpush()とpop()で囲むことにより、
 * その中で確保されたオブジェクトを解放します。
 * push()とpop()は必ず対になるようにしてください。
 * その対を push() pop() push() pop()というふうに、複数回呼ぶことができます。
 * また、push() push() pop() pop()というふうに、ネストすることもできます。
 * 以下の例では、for文の単位でオブジェクトを解放しています。
 *      <pre>
 *        // 比較的寿命の長いプログラム
 *        ReleaseManager rm = new ReleaseManager();
 *        try {
 *            IDispatch foo = new IDispatch(rm ,progid);
 *            //  ...
 *            for(int i=0; i&lt;files.length; i++) {
 *                rm.push();
 *                // ...
 *                rm.pop(); // for文の中で生成されたオブジェクトを解放
 *            }
 *        } catch(JComException e) {
 *            e.printStackTrace();
 *        } finally {
 *            rm.release();
 *        }
 *      </pre>
 * 複数のＣＯＭを同時に扱うとき、その生成と解放のタイミングが異なる場合があります。
 * 例えば、１つのＤＢをＣＯＭとして扱い、複数のＥＸＣＥＬもまたＣＯＭとして扱う場合です。
 * ＤＢは最初に１回生成するのに対し、ＥＸＣＥＬは複数回生成することになるからです。
 * その場合は複数の<code> ReleaseManager </code>を生成し、それぞれに割り当てることにより、
 * オブジェクトの解放についてキメの細かい制御を行うことができます。
 * 別のＣＯＭに対して、同じ<code> ReleaseManager </code>を使うことも、異なる
 * <code> ReleaseManager </code>を使うこともできます。また、生成する箇所を
 * 別のブロック（メソッド、スレッド等）にすることもできます。
 *      <pre>
 *        // 寿命の異なる複数のＣＯＭを扱うプログラム（例１）
 *        ReleaseManager rmDb = new ReleaseManager();
 *        ReleaseManager rmExcel = new ReleaseManager();
 *        try {
 *            IDispatch comDB = new IDispatch(rmDb ,"foo.DB");
 *            IDispatch comExcel = new IDispatch(rmExcel ,"Excel.Application");
 *            //  ...
 *            for(int i=0; i&lt;table.length; i++) {
 *                rmExcel.push();
 *                // ...
 *                rmExcel.pop();  //ＥＸＣＥＬオブジェクトのみ解放
 *            }
 *        } catch(JComException e) {
 *            e.printStackTrace();
 *        } finally {
 *            rmExcel.release();
 *            rmDb.release();
 *        }
 *      </pre>
 *      <pre>
 *        // 寿命の異なる複数のＣＯＭを扱うプログラム(例２)
 *        ReleaseManager rmDB = new ReleaseManager();
 *        try {
 *            IDispatch comDB = new IDispatch(rmDB ,"foo.DB");
 *            rmDB.push();
 *            ReleaseManager rmExcel = new ReleaseManager();
 *            try {
 *                IDispatch comExcel = new IDispatch(rmExcel ,"Excel.Application");
 *                //  ...
 *                for(int i=0; i&lt;table.length; i++) {
 *                    rmExcel.push();
 *                    rmDB.push();
 *                    // ...
 *                    rmDB.pop();       // ＤＢオブジェクトのみ解放
 *                    rmExcel.pop();   // ＥＸＣＥＬオブジェクトも解放
 *                }
 *            } catch(JComException e) {
 *                e.printStackTrace();
 *            } finally {
 *                rmExcel.release();
 *            }
 *            rmDB.pop();          //  ＤＢオブジェクトのみ解放
 *        } catch(JComException e) {
 *            e.printStackTrace();
 *        } finally {
 *            rmDB.release();
 *        }
 *      </pre>
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.00, 2000/06/25
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 	@see     IDispatch
 	@see	IUnknown
 */
public class ReleaseManager {

	private Stack frames;
	private Stack curFrame;
	/**
		ReleaseManagerを作成します。
		解放しなければならないＣＯＭオブジェクトを管理します。
	*/
	public ReleaseManager() {
		frames = new Stack();
		curFrame = new Stack();
		frames.push(curFrame);
	}
	/**
		IUnknown を現在のスタックに追加します。
	*/
	public void add(IUnknown jcom) {
		curFrame.push(jcom);
	}

	/**
		新しいスタックを生成します。
	*/
	public void push() {
		curFrame = new Stack();
		frames.push(curFrame);
	}
	/**
		現在のスタック上のIUnknownを解放します。
		その後、1つ前のスタックに戻します。
	*/
	public void pop() {
		if(curFrame==null) return;
		// 現在のフレームに溜まったJComをreleaseする
		while( ! curFrame.empty() ) {
			((IUnknown)curFrame.pop()).release();
		}
		frames.pop();
		// フレームを一つ前に戻す
		try {
			curFrame = (Stack)frames.peek();
		} catch(EmptyStackException e) {
			curFrame = null;
		}
	}
	/**
		すべてのスタック上のIUnknownを解放します。
	*/
	public void release() {
		while( ! frames.empty() ) {
			pop();
		}
	}
	/**
		すべてのスタック上のIUnknownを解放します。
		ＪａｖａＶＭはプログラム終了時に<code> filnalize() </code>を呼ぶことを
		保証していません。
		通常は、try〜catch文でJComExceptionをキャッチし、
		finally文でrelease()を明示的に呼ぶようにしてください。
	*/
	public void finalize() {
		release();
	}

	/**
		内部で保持しているIUnknownクラス、またはそれから継承したクラスのオブジェクト
		を以下の形で表示します。
		１６進数はインターフェースのポインタ、
		括弧の中の数値は参照カウンタの数、その次はクラス名です。
		任意の箇所でのオブジェクトの状態をスナップショット的に見ることができます。
		<pre>
        {
        {
        4769e4(1)jp.ne.so_net.ga2.no_ji.jcom.excel8.ExcelApplication
        476eb8(1)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        477c98(1)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        477ed4(1)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        478004(1)jp.ne.so_net.ga2.no_ji.jcom.IUnknown
        478694(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        4788f4(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        478b30(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        478694(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        4788f4(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        478b30(2)jp.ne.so_net.ga2.no_ji.jcom.IDispatch
        }
        }</pre>
	*/
	public String toString() {
		String result = "{¥n";
		for(int i=0; i<frames.size(); i++) {
			Stack s = (Stack)frames.elementAt(i);
			result += "{¥n";
			for(int j=0; j<s.size(); j++) {
				IUnknown p = (IUnknown)s.elementAt(j);
				result += p.toString() + "¥n";
			}
			result += "}¥n";
		}
		result += "}¥n";
		return result;
	}
}

