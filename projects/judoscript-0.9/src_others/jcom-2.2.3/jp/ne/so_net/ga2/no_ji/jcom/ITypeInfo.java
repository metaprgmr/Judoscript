package jp.ne.so_net.ga2.no_ji.jcom;

/**
 * ITypeInfoインターフェースを扱うためのクラス
 *
 * @see     IUnknown
 * @see     JComException
 * @see     ReleaseManager
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.10, 2000/07/01
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
 */
public class ITypeInfo extends IUnknown {
    /**
		IID_ITypeInfo です。
		@see       GUID
	*/
	public static GUID IID = new GUID( 0x00020401, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );
    /**
     * ITypeInfoを作成します。
	 * 引数pITypeInfoはITypeInfoインターフェースのポインタを指定します。
     * @param     rm             参照カウンタ管理クラス
     	@param	pITypeInfo	ITypeInfoインターフェース
     * @see       ReleaseManager
	 */
	public ITypeInfo(ReleaseManager rm, int pITypeInfo) {
		super(rm, pITypeInfo);
	}
	/**
		指定したメンバIDのドキュメントを返します。
		メンバＩＤはFuncDescクラスのmemidにあります。
		MEMBERID_NIL(-1)を指定した場合はこのオブジェクトに対するドキュメントを返します。
		戻り値は４つ分の文字列の配列で、それぞれ以下の値が格納されています。
		カッコ内はExcel.Applicationの場合の値です。
		値のないものはnullになっています。
		<pre>
		戻り値[0]	bstrName        この名前。("_Application")
		戻り値[1]	btrDocString    ドキュメント(null)
		戻り値[2]	dwHelpContext    ヘルプコンテキストの番号を文字列に変えたもの("131073")
		戻り値[3]	bstrHelpFile	ヘルプファイルのフルパス("D:¥Office97¥Office¥VBAXL8.HLP")
		<pre>
	*/
	public String[] getDocumentation(int memid) throws JComException {
		return _getDocumentation(memid);
	}
	public static final int MEMBERID_NIL = -1;
	/**
		TypeAttrを返します。
		TypeAttrはITypeInfoの属性を管理するクラスです。
		@see	ITypeInfo.TypeAttr
	*/
	public TypeAttr getTypeAttr() throws JComException {
		return _getTypeAttr();
	}
	/**
		指定したindexのFuncDescを返します。
		FuncDescはメソッドの情報を管理するクラスです。
		@see	ITypeInfo.FuncDesc
	*/
	public FuncDesc getFuncDesc(int index) throws JComException {
		return _getFuncDesc(index);
	}
	/**
		指定したindexのVarDescを返します。
		VarDescは変数(通常はEnum型)の情報を管理するクラスです。
		@see	ITypeInfo.VarDesc
	*/
	public VarDesc getVarDesc(int index) throws JComException {
		return _getVarDesc(index);
	}
	/**
		このオブジェクトが属しているITypeLibを返します。
		@see	ITypeLib
	*/
	public ITypeLib getTypeLib() throws JComException {
		return new ITypeLib(rm, _getTypeLib());
	}
	/**
		このクラスがCOCLASSのとき、実際に実装している型情報を返します。
	*/
	public ITypeInfo getImplType(int index) throws JComException {
		return new ITypeInfo(rm, _getImplType(index));
	}
	/**
		hreftypeで指定されたITypeInfoを返します。
		@see ITypeInfo.ElemDesc#getTypeDesc()
	*/
	public ITypeInfo getRefTypeInfo(int hreftype) throws JComException {
		return new ITypeInfo(rm, _getRefTypeInfo(hreftype));
	}
	/**
		型情報がVT_USERDEFINEDのとき、そこからHREFTYPEを取り出します。
		HREFTYPEは16進数で表されています。
	*/
	public static int getRefTypeFromTypeDesc(String type) {
		return Integer.parseInt(type.substring(type.indexOf('(')+1, type.indexOf(')')),16);
	}
	/**
		このオブジェクトと他のオブジェクトが等しいかどうかを示します。 
		ITypeInfo.TypeAttr.IID により同一の型情報かどうかを判断しています。
		@see ITypeInfo.TypeAttr#getIID()
	*/
	public boolean equals(Object obj) {
		if(! (obj instanceof ITypeInfo)) return false;
		ITypeInfo info = (ITypeInfo)obj;
		try {
			return info.getTypeAttr().getIID().equals(this.getTypeAttr().getIID());
		} catch(JComException e) { e.printStackTrace(); }
		return false;
	}
	/**
		ITypeInfoの属性を管理するクラスです。
		GUID、関数の数、変数の数を返します。
		変数は通常は定数で、プロパティとは異なります。
		プロパティの設定、取得は関数に含まれます。
		@see	ITypeInfo
	*/
	public class TypeAttr {
		public static final int TKIND_ENUM      = 0;
		public static final int TKIND_RECORD    = 1;
		public static final int TKIND_MODULE    = 2;
		public static final int TKIND_INTERFACE = 3;
		public static final int TKIND_DISPATCH  = 4;
		public static final int TKIND_COCLASS   = 5;
		public static final int TKIND_ALIAS     = 6;
		public static final int TKIND_UNION     = 7;
		public static final int TKIND_MAX       = 8;

		private GUID IID;
		private int cFuncs;
		private int cVars;
		private int cImplTypes;
		private int typekind;
		public TypeAttr(GUID IID, int typekind, int cFuncs, int cVars, int cImplTypes) {
			this.IID        = IID;
			this.typekind   = typekind;
			this.cFuncs     = cFuncs;
			this.cVars      = cVars;
			this.cImplTypes = cImplTypes;
		}
		public GUID getIID() { return IID; }
		public int getTypeKind() { return typekind; }
		public int getFuncs() { return cFuncs; }
		public int getVars() { return cVars; }
		public int getImplTypes() { return cImplTypes; }
	}


	/**
		１つの引数や戻り値などの型の情報を持ちます。
		ＣＯＭのELEMDESC構造体を表すクラスです。
		[in out]などの情報と、型 VT_PTR+VT_BSTR などの情報を持ちます。
		VT_USERDEFINEDを持つ場合、自動的にそのクラス名を取得し、
		実際の文字列に置き換えます。
		型は文字列の形で保持し、Java互換の形になります。すなわち、
		VT_PTR|VT_I4は "[I"、VT_UNKNOWNは"Ljp.ne.so_net.ga2.no_ji.jcom.IUnknown"に
		なります。
		@see	ITypeInfo.FuncDesc
	*/
	public class ElemDesc {
		public static final int IDLFLAG_FIN     = 0x01;
		public static final int IDLFLAG_FOUT    = 0x02;
		public static final int IDLFLAG_FLCID   = 0x04;
		public static final int IDLFLAG_FRETVAL = 0x08;
		private int idl;	// IDLFLAG_XXXの組み合わせ
		private String typedesc;	// 型情報 "VT_INT"
		public ElemDesc(String typedesc, int idl) {
			this.typedesc = typedesc;
			this.idl = idl;
		}
		public int getIDL() { return idl; }
		/**
			型情報を返します。以下の形式です。
			"VT_I4" "VT_BSTR" "VT_DISPATCH" "VT_PTR+VT_I4"
			"VT_SAFEARRAY+VT_I4"
			"VT_USERDEFINED(1):VBE"
			VT_USERDEFINEDの括弧内の数値は16進数で表したのhreftypeで、その数値を
			getRefTypeInfo()に渡すことにより、その値のITypeInfoを
			取得することができます。
		*/
		public String getTypeDesc() { return typedesc; }
		public String toString() {
			if(idl==0) return typedesc;
			String result = "";
			if((idl & IDLFLAG_FIN)!=0) result += "[in]";
			if((idl & IDLFLAG_FOUT)!=0) result += "[out]";
			if((idl & IDLFLAG_FLCID)!=0) result += "[lcid]";	// ???
			if((idl & IDLFLAG_FRETVAL)!=0) result += "[retval]";
			return result + typedesc;
		}
	}
	
	/**
		メソッドの情報を管理するクラスです。
		メンバＩＤ、および呼び出し形式、引数の型や戻り値の型などを
		持ちます。
		@see	ITypeInfo.ElemDesc
		@see	ITypeInfo
	*/
	public class FuncDesc {
		private int memid;					// メンバID(0〜)
		private int invkind;				// INVOKE_XXXのいずれか
		private ElemDesc[] elemdescParam;	// 引数の型
		private ElemDesc elemdescFunc;		// 戻り値の型
		public static final int INVOKE_FUNC           = 0x01;
		public static final int INVOKE_PROPERTYGET    = 0x02;
		public static final int INVOKE_PROPERTYPUT    = 0x04;
		public static final int INVOKE_PROPERTYPUTREF = 0x08;
		/**
			メソッドの情報を生成します。
			ITypeInfo.getFuncDesc()内で使用されます。
			通常、外部からは使用しません。
			@see	ITypeInfo#getFuncDesc(int)
		*/
		public FuncDesc(int memid, int invkind, ElemDesc[] elemdescParam, ElemDesc elemdescFunc) {
			this.memid = memid;
			this.invkind = invkind;
			this.elemdescParam = elemdescParam;
			this.elemdescFunc = elemdescFunc;
		}
		/**
			メソッドの情報を表示します。
		*/
		public String toString() {
			try {
				String[] names = getNames();
				String result = "";
				switch(invkind) {
					case INVOKE_FUNC:			result += "FUNC ";
						break;
					case INVOKE_PROPERTYGET:	result += "GET ";	break;
					case INVOKE_PROPERTYPUT:	result += "PUT ";	break;
					case INVOKE_PROPERTYPUTREF:	result += "PUTREF ";	break;
				}
				result += names[0] + "(";
				if(elemdescParam!=null) {
					for(int i=0; i<elemdescParam.length; i++) {
						result += elemdescParam[i].toString() + " ";
						if(i+1<names.length) result += names[i+1];
						if(i!=elemdescParam.length-1) result += ",";
					}
				}
				result += ")"+elemdescFunc.toString();
				return result;
			}
			catch(Exception e) {}
			return null;
/*
			try {
				String[] names = getNames();
				String result = "";
				switch(invkind) {
					case INVOKE_FUNC:			result += "FUNC ";	break;
					case INVOKE_PROPERTYGET:	result += "GET ";	break;
					case INVOKE_PROPERTYPUT:	result += "PUT ";	break;
					case INVOKE_PROPERTYPUTREF:	result += "PUTREF ";	break;
				}
				result += names[0] + "(";
				if(elemdescParam!=null) {
					for(int i=0; i<elemdescParam.length; i++) {
						result += elemdescParam[i].toString() + " " + names[i+1];
						if(i!=elemdescParam.length-1) result += ",";
					}
				}
				result += ")"+elemdescFunc.toString();
				return result;
			}
			catch(Exception e) {}
			return null;
*/
		}
		/**
			メンバＩＤを返します。0以上の値です。
		*/
		public int getMemID() { return memid; }
		/**
			呼び出し形式を返します。
			INVOKE_XXXのいずれかです。
			@see	ITypeInfo.FuncDesc#INVOKE_FUNC
			@see	ITypeInfo.FuncDesc#INVOKE_PROPERTYGET
			@see	ITypeInfo.FuncDesc#INVOKE_PROPERTYPUT
			@see	ITypeInfo.FuncDesc#INVOKE_PROPERTYPUTREF
		*/
		public int getInvokeKind() { return invkind; }
		/**
			引数の情報を返します。
			引数がない場合は<code>null</code>を返します。
			@see	ITypeInfo.ElemDesc
		*/
		public ElemDesc[] getParams() { return elemdescParam; }
		/**
			戻り値の情報を返します。
			@see	ITypeInfo.ElemDesc
		*/
		public ElemDesc getFunc() { return elemdescFunc; }
		/**
			メソッドの名前、引数の名前を返します。
			どうやら、[0]がメソッド名、[1]以降が引数の名前のようです。
		*/
		public String[] getNames() throws JComException {
			return _getNames(memid);
		}
	}

	/**
		内部変数の情報を管理するクラスです。
		通常、Enum型の定数に使われます。
		@see	ITypeInfo
		@see	ITypeInfo.ElemDesc
	*/
	public class VarDesc {
		private int memid;					// メンバID(0〜)
		private int varkind;				// VAR_XXXのいずれか
		private ElemDesc elemdescVar;		// 変数の型
		private Object   varValue;			// 戻り値の型
		public static final int VAR_PERINSTANCE = 0;
		public static final int VAR_STATIC      = 1;
		public static final int VAR_CONST       = 2;
		public static final int VAR_DISPATCH    = 3;
		/**
			メソッドの情報を生成します。
			ITypeInfo.getFuncDesc()内で使用されます。
			通常、外部からは使用しません。
			@see	ITypeInfo#getFuncDesc(int)
		*/
		public VarDesc(int memid, int varkind, ElemDesc elemdescVar, Object varValue) {
			this.memid = memid;
			this.varkind = varkind;
			this.elemdescVar = elemdescVar;
			this.varValue = varValue;
		}
		/**
			メソッドの情報を表示します。
		*/
		public String toString() {
			try {
				String[] names = getNames();
				String result = "";
				switch(varkind) {
					case VAR_PERINSTANCE:	result += "PERINSTANCE ";	break;
					case VAR_STATIC:		result += "STATIC ";		break;
					case VAR_CONST:			result += "CONST ";			break;
					case VAR_DISPATCH:		result += "DISPATCH ";		break;
				}
				result += elemdescVar.toString() + " " + names[0] + " = " + varValue.toString();
				return result;
			}
			catch(Exception e) {}
			return null;
		}
		/**
			メンバＩＤを返します。0以上の値です。
		*/
		public int getMemID() { return memid; }
		/**
			変数の形式を返します。
			VAR_XXXのいずれかです。
			@see	ITypeInfo.VarDesc#VAR_PERINSTANCE
			@see	ITypeInfo.VarDesc#VAR_STATIC
			@see	ITypeInfo.VarDesc#VAR_CONST
			@see	ITypeInfo.VarDesc#VAR_DISPATCH
		*/
		public int getVarKind() { return varkind; }
		/**
			変数の型の情報を返します。
			@see	ITypeInfo.ElemDesc
		*/
		public ElemDesc getVar() { return elemdescVar; }
		/**
			メソッドの名前、引数の名前を返します。
			どうやら、[0]がメソッド名、[1]以降が引数の名前のようです。
		*/
		public String[] getNames() throws JComException {
			return _getNames(memid);
		}
		/**
			変数の値を返します。
		*/
		public Object getValue() { return varValue; }
	}

	// release()はsuperのでＯＫ．

	// JNI
	private native String[] _getDocumentation(int memid) throws JComException;
	private native String[] _getNames(int memid) throws JComException;
	private native TypeAttr _getTypeAttr() throws JComException;
	private native FuncDesc _getFuncDesc(int index) throws JComException;
	private native VarDesc  _getVarDesc(int index) throws JComException;
	private native int      _getImplType(int index) throws JComException;
	private native int      _getTypeLib() throws JComException;		// ITypeInfo::GetContainingTypeLib()
	private native int      _getRefTypeInfo(int hreftype) throws JComException;		// ITypeInfo::GetRefTypeInfo()

}

/*
	FuncDesc や ElemDesc は、一応、外部から変更ができないようにしている。
	すなわち、メンバはprivateにし、get系しか対応していない。
	ただ、どちらもコンストラクタはpublicなので完全とは言えない。
	ＪＮＩ内部で生成する必要があるからです。
	デザインパターンのsingletonの考えが入っています。

	しかし、今年の夏も暑い〜。
2000-07-23
	COCLASSってなんじゃあ〜！！！！

*/
