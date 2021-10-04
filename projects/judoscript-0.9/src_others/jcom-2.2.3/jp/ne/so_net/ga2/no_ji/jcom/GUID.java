package jp.ne.so_net.ga2.no_ji.jcom;
import java.text.NumberFormat;

/**
 * GUIDクラス
 * @author Yoshinori Watanabe(渡辺 義則)
 * @see     IUnknown
 * @see     IDispatch
	@author Yoshinori Watanabe(渡辺 義則)
	@version 2.10, 2000/07/01
	Copyright(C) Yoshinori Watanabe 1999-2000. All Rights Reserved.
*/
public class GUID {
	/*
		GUIDデータ。
		データ隠蔽しません。好きに見てください。
		いじるときには注意して。
	*/
	public int			data1;
	public short		data2;
	public short		data3;
	public byte[]		data4 = new byte[8];

	/**
		GUIDを作成します。
	*/
	public GUID(int data1, short data2, short data3,
				byte data40, byte data41, byte data42, byte data43,
				byte data44, byte data45, byte data46, byte data47) {
		this.data1 = data1;
		this.data2 = data2;
		this.data3 = data3;
		this.data4[0] = data40;
		this.data4[1] = data41;
		this.data4[2] = data42;
		this.data4[3] = data43;
		this.data4[4] = data44;
		this.data4[5] = data45;
		this.data4[6] = data46;
		this.data4[7] = data47;
	}
	/**
		GUIDを作成します。
	*/
	public GUID(int data1, short data2, short data3, byte[] data4) {
		this(data1, data2, data3, data4[0], data4[1], data4[2], data4[3], data4[4], data4[5], data4[6], data4[7]);
	}

	/**
		GUIDを作成します。
		Javaで数値を直接記述すると、通常int型になります。short やbyteにするときには
		明示的なキャストが必要になり、記述が面倒になります。
		その負荷を減らすためにこのコンストラクタを用意しました。
		data2,data3はshortの範囲の値を、data40〜data47はbyteの範囲の値を指定して下さい。
	*/
	public GUID(int data1, int data2, int data3,
				int data40, int data41, int data42, int data43,
				int data44, int data45, int data46, int data47) {
		this(data1, (short)data2, (short)data3, 
				(byte)data40, (byte)data41, (byte)data42, (byte)data43,
				(byte)data44, (byte)data45, (byte)data46, (byte)data47);
	}
	/**
		GUIDを作成します。
		Javaで数値を直接記述すると、通常int型になります。short やbyteにするときには
		明示的なキャストが必要になり、記述が面倒になります。
		その負荷を減らすためにこのコンストラクタを用意しました。
		data2,data3はshortの範囲の値を、data4[0]〜data4[7]はbyteの範囲の値を指定して下さい。
	*/
	public GUID(int data1, int data2, int data3, int[] data4) {
		this(data1, data2, data3, data4[0], data4[1], data4[2], data4[3], data4[4], data4[5], data4[6], data4[7]);
	}

	/**
		GUIDを"{FB7FDAE2-89B8-11CF-9BE8-00A0C90A632C}"の形式で表します。
	*/
	public String toString() {
		return "{"+
			GUID.toHexString(data1)+"-"+
			GUID.toHexString(data2)+"-"+
			GUID.toHexString(data3)+"-"+
			GUID.toHexString(data4[0])+GUID.toHexString(data4[1])+"-"+
			GUID.toHexString(data4[2])+GUID.toHexString(data4[3])+
			GUID.toHexString(data4[4])+GUID.toHexString(data4[5])+
			GUID.toHexString(data4[6])+GUID.toHexString(data4[7])+"}";
	}
	public boolean equals(Object obj) {
		if(!(obj instanceof GUID)) return false;
		GUID guid = (GUID)obj;
		if(guid.data1 != data1) return false;
		if(guid.data2 != data2) return false;
		if(guid.data3 != data3) return false;
		for(int i=0; i<8; i++) {
			if(guid.data4[i] != data4[i]) return false;
		}
		return true;
	}

	// あぁ〜、くそ〜、面倒くさい！ Ｃなら一発なのに・・・。
	public static String toHexString(int i) {
		String s = Integer.toHexString(i).toUpperCase();
		return ("0000000"+s).substring(s.length()-1);
	}
	public static String toHexString(short i) {
		String s = Integer.toHexString(i).toUpperCase();
		return ("000"+s).substring(s.length()-1);
	}
	public static String toHexString(byte i) {
		String s = Integer.toHexString(i).toUpperCase();
		return ("0"+s).substring(s.length()-1);
	}

	/**
		"{FB7FDAE2-89B8-11CF-9BE8-00A0C90A632C}"の形式からGUIDを作成します。
		形式が間違っていた場合はnullを返します。
	*/
	public static GUID parse(String guid) {
		try {
			/*
				 0....:....1....:....2....:....3....:....
				"{FB7FDAE2-89B8-11CF-9BE8-00A0C90A632C}"
				Byte.parseByte("C0",16); では、NumberFormatExceptionになる。
				127以上は受け付けてくれないのだ。
				そのくせ、以下のキャストは可能で、-64になる。
				byte b = (byte)192;	// -64
			*/
			return new GUID(
				(int)Long.parseLong(guid.substring(1,9),16),
				Integer.parseInt(guid.substring(10,14),16),
				Integer.parseInt(guid.substring(15,19),16),
				Integer.parseInt(guid.substring(20,22),16),
				Integer.parseInt(guid.substring(22,24),16),
				Integer.parseInt(guid.substring(25,27),16),
				Integer.parseInt(guid.substring(27,29),16),
				Integer.parseInt(guid.substring(29,31),16),
				Integer.parseInt(guid.substring(31,33),16),
				Integer.parseInt(guid.substring(33,35),16),
				Integer.parseInt(guid.substring(35,37),16));
		}catch(Exception e) { return null; }
	}

	/*
		標準的なGUIDを用意しておきます。
		適当に追加して下さい。
		設計上、ここに追加するのではなく、個々のインターフェースに対応したクラスのメンバに
		用意すべきです。
	*/
	// IID
	public static final GUID IID_IUnknown     = new GUID( 0x00000000, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );
	public static final GUID IID_IDispatch    = new GUID( 0x00020400, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );
	public static final GUID IID_IEnumVARIANT = new GUID( 0x00020404, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 );

	public static void main(String[] args) {
		System.out.println("IID_IUnknown="+GUID.IID_IUnknown.toString());
		System.out.println("IID_IDispatch="+GUID.IID_IDispatch.toString());
		System.out.println("IID_IEnumVARIANT="+GUID.IID_IEnumVARIANT.toString());
		// 同じ値が表示されればＯＫ
		GUID tmp1 = GUID.parse(GUID.IID_IUnknown.toString());
		System.out.println(tmp1);
		GUID tmp2 = GUID.parse(GUID.IID_IDispatch.toString());
		System.out.println(tmp2);
		GUID tmp3 = GUID.parse(GUID.IID_IEnumVARIANT.toString());
		System.out.println(tmp3);
	}
}
/*
{00000000-0000-0000-C000-000000000046}
{00020400-0000-0000-C000-000000000046}
{00020404-0000-0000-C000-000000000046}

static const IID _IID_IUnknown =
	{ 0x00000000, 0x0000, 0x0000, { 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 } };

0x9f6aa700, 0xd188, 0x11cd, 0xad, 0x48, 0x0, 0xaa, 0x0, 0x3c, 0x9c, 0xb6);

'DEFINE_GUID' を検索中...
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(99):DEFINE_GUID(CATID_ActiveScript, 0xf0b7a1a1, 0x9847, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(102):DEFINE_GUID(CATID_ActiveScriptParse, 0xf0b7a1a2, 0x9847, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(105):DEFINE_GUID(IID_IActiveScript, 0xbb1a2ae1, 0xa4f9, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(108):DEFINE_GUID(IID_IActiveScriptParse, 0xbb1a2ae2, 0xa4f9, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(111):DEFINE_GUID(IID_IActiveScriptSite, 0xdb01a1e3, 0xa42b, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(114):DEFINE_GUID(IID_IActiveScriptSiteWindow, 0xd10f6761, 0x83e9, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ACTIVSCP.H(117):DEFINE_GUID(IID_IActiveScriptError, 0xeae1ba61, 0xa4ed, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥ASPTLB.H(7):DEFINE_GUID(LIBID_ASPTypeLibrary,0xD97A6DA0L,0xA85C,0x11CF,0x83,0xAE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(12):DEFINE_GUID(IID_IStringList,0xD97A6DA0L,0xA85D,0x11CF,0x83,0xAE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(63):DEFINE_GUID(IID_IRequestDictionary,0xD97A6DA0L,0xA85F,0x11DF,0x83,0xAE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(113):DEFINE_GUID(IID_IRequest,0xD97A6DA0L,0xA861,0x11CF,0x93,0xAE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(168):DEFINE_GUID(CLSID_Request,0x920C25D0L,0x25D9,0x11D0,0xA5,0x5F,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(174):DEFINE_GUID(IID_IReadCookie,0x71EAF260L,0x0CE0,0x11D0,0xA5,0x3E,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(225):DEFINE_GUID(IID_IWriteCookie,0xD97A6DA0L,0xA862,0x11CF,0x84,0xAE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(280):DEFINE_GUID(IID_IResponse,0xD97A6DA0L,0xA864,0x11CF,0x83,0xBE,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(349):DEFINE_GUID(CLSID_Response,0x46E19BA0L,0x25DD,0x11D0,0xA5,0x5F,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(355):DEFINE_GUID(IID_ISessionObject,0xD97A6DA0L,0xA865,0x11CF,0x83,0xAF,0x00,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(410):DEFINE_GUID(CLSID_Session,0x509F8F20L,0x25DE,0x11D0,0xA5,0x5F,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(416):DEFINE_GUID(IID_IApplicationObject,0xD97A6DA0L,0xA866,0x11CF,0x83,0xAE,0x10,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(469):DEFINE_GUID(CLSID_Application,0x7C3BAF00L,0x25DE,0x11D0,0xA5,0x5F,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(475):DEFINE_GUID(IID_IServer,0xD97A6DA0L,0xA867,0x11CF,0x83,0xAE,0x01,0xA0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(529):DEFINE_GUID(CLSID_Server,0xA506D160L,0x25E0,0x11D0,0xA5,0x5F,0x00,0xA0,0xC9,0x0C,0x20,0x91);
D:¥DevStudio¥VC¥include¥ASPTLB.H(535):DEFINE_GUID(IID_IScriptingContext,0xD97A6DA0L,0xA868,0x11CF,0x83,0xAE,0x00,0xB0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥ASPTLB.H(588):DEFINE_GUID(CLSID_ScriptingContext,0xD97A6DA0L,0xA868,0x11CF,0x83,0xAE,0x11,0xB0,0xC9,0x0C,0x2B,0xD8);
D:¥DevStudio¥VC¥include¥BASETYPS.H(191)://      Example: DEFINE_GUID(GUID_XXX, a, b, c, ...);
D:¥DevStudio¥VC¥include¥BASETYPS.H(206):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥include¥BASETYPS.H(210):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥include¥BASETYPS.H(216):    DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥D3D.H(32):DEFINE_GUID( IID_IDirect3D,             0x3BBA0080,0x2421,0x11CF,0xA3,0x1A,0x00,0xAA,0x00,0xB9,0x33,0x56 );
D:¥DevStudio¥VC¥include¥D3D.H(33):DEFINE_GUID( IID_IDirect3DTexture,      0x2CDCD9E0,0x25A0,0x11CF,0xA3,0x1A,0x00,0xAA,0x00,0xB9,0x33,0x56 );
D:¥DevStudio¥VC¥include¥D3D.H(34):DEFINE_GUID( IID_IDirect3DLight,        0x4417C142,0x33AD,0x11CF,0x81,0x6F,0x00,0x00,0xC0,0x20,0x15,0x6E );
D:¥DevStudio¥VC¥include¥D3D.H(35):DEFINE_GUID( IID_IDirect3DMaterial,     0x4417C144,0x33AD,0x11CF,0x81,0x6F,0x00,0x00,0xC0,0x20,0x15,0x6E );
D:¥DevStudio¥VC¥include¥D3D.H(36):DEFINE_GUID( IID_IDirect3DExecuteBuffer,0x4417C145,0x33AD,0x11CF,0x81,0x6F,0x00,0x00,0xC0,0x20,0x15,0x6E );
D:¥DevStudio¥VC¥include¥D3D.H(37):DEFINE_GUID( IID_IDirect3DViewport,     0x4417C146,0x33AD,0x11CF,0x81,0x6F,0x00,0x00,0xC0,0x20,0x15,0x6E );
D:¥DevStudio¥VC¥include¥D3DRM.H(24):DEFINE_GUID(IID_IDirect3DRM,    0x2bc49361, 0x8327, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(76):DEFINE_GUID(CLSID_CDirect3DRMDevice,        0x4fa3568e, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(77):DEFINE_GUID(CLSID_CDirect3DRMViewport,      0x4fa3568f, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(78):DEFINE_GUID(CLSID_CDirect3DRMFrame,         0x4fa35690, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(79):DEFINE_GUID(CLSID_CDirect3DRMMesh,          0x4fa35691, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(80):DEFINE_GUID(CLSID_CDirect3DRMMeshBuilder,   0x4fa35692, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(81):DEFINE_GUID(CLSID_CDirect3DRMFace,          0x4fa35693, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(82):DEFINE_GUID(CLSID_CDirect3DRMLight,         0x4fa35694, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(83):DEFINE_GUID(CLSID_CDirect3DRMTexture,       0x4fa35695, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(84):DEFINE_GUID(CLSID_CDirect3DRMWrap,          0x4fa35696, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(85):DEFINE_GUID(CLSID_CDirect3DRMMaterial,      0x4fa35697, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(86):DEFINE_GUID(CLSID_CDirect3DRMAnimation,     0x4fa35698, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(87):DEFINE_GUID(CLSID_CDirect3DRMAnimationSet,  0x4fa35699, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(88):DEFINE_GUID(CLSID_CDirect3DRMUserVisual,    0x4fa3569a, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(89):DEFINE_GUID(CLSID_CDirect3DRMShadow,        0x4fa3569b, 0x623f, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(94):DEFINE_GUID(IID_IDirect3DRMObject,          0xeb16cb00, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(95):DEFINE_GUID(IID_IDirect3DRMDevice,          0xe9e19280, 0x6e05, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(96):DEFINE_GUID(IID_IDirect3DRMViewport,        0xeb16cb02, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(97):DEFINE_GUID(IID_IDirect3DRMFrame,           0xeb16cb03, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(98):DEFINE_GUID(IID_IDirect3DRMVisual,          0xeb16cb04, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(99):DEFINE_GUID(IID_IDirect3DRMMesh,            0xa3a80d01, 0x6e12, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(100):DEFINE_GUID(IID_IDirect3DRMMeshBuilder,     0xa3a80d02, 0x6e12, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(101):DEFINE_GUID(IID_IDirect3DRMFace,            0xeb16cb07, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(102):DEFINE_GUID(IID_IDirect3DRMLight,           0xeb16cb08, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(103):DEFINE_GUID(IID_IDirect3DRMTexture,         0xeb16cb09, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(104):DEFINE_GUID(IID_IDirect3DRMWrap,            0xeb16cb0a, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(105):DEFINE_GUID(IID_IDirect3DRMMaterial,        0xeb16cb0b, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(106):DEFINE_GUID(IID_IDirect3DRMAnimation,       0xeb16cb0d, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(107):DEFINE_GUID(IID_IDirect3DRMAnimationSet,    0xeb16cb0e, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(108):DEFINE_GUID(IID_IDirect3DRMDeviceArray,     0xeb16cb10, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(109):DEFINE_GUID(IID_IDirect3DRMViewportArray,   0xeb16cb11, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(110):DEFINE_GUID(IID_IDirect3DRMFrameArray,      0xeb16cb12, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(111):DEFINE_GUID(IID_IDirect3DRMVisualArray,     0xeb16cb13, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(112):DEFINE_GUID(IID_IDirect3DRMLightArray,      0xeb16cb14, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(113):DEFINE_GUID(IID_IDirect3DRMPickedArray,     0xeb16cb16, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(114):DEFINE_GUID(IID_IDirect3DRMFaceArray,       0xeb16cb17, 0xd271, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(115):DEFINE_GUID(IID_IDirect3DRMUserVisual,      0x59163de0, 0x6d43, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMOBJ.H(116):DEFINE_GUID(IID_IDirect3DRMShadow,          0xaf359780, 0x6ba3, 0x11cf, 0xac, 0x4a, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥D3DRMWIN.H(24):DEFINE_GUID(IID_IDirect3DRMWinDevice,   0xc5016cc0, 0xd273, 0x11ce, 0xac, 0x48, 0x0, 0x0, 0xc0, 0x38, 0x25, 0xa1);
D:¥DevStudio¥VC¥include¥DBDAOID.H(22):    DEFINE_GUID(name, l, 0, 0x10, 0x80,0,0,0xAA,0,0x6D,0x2E,0xA4)
D:¥DevStudio¥VC¥include¥DDRAW.H(33):DEFINE_GUID( CLSID_DirectDraw,			0xD7B70EE0,0x4340,0x11CF,0xB0,0x63,0x00,0x20,0xAF,0xC2,0xCD,0x35 );
D:¥DevStudio¥VC¥include¥DDRAW.H(34):DEFINE_GUID( CLSID_DirectDrawClipper,           0x593817A0,0x7DB3,0x11CF,0xA2,0xDE,0x00,0xAA,0x00,0xb9,0x33,0x56 );
D:¥DevStudio¥VC¥include¥DDRAW.H(35):DEFINE_GUID( IID_IDirectDraw,			0x6C14DB80,0xA733,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60 );
D:¥DevStudio¥VC¥include¥DDRAW.H(36):DEFINE_GUID( IID_IDirectDraw2,                  0xB3A6F3E0,0x2B43,0x11CF,0xA2,0xDE,0x00,0xAA,0x00,0xB9,0x33,0x56 );
D:¥DevStudio¥VC¥include¥DDRAW.H(37):DEFINE_GUID( IID_IDirectDrawSurface,		0x6C14DB81,0xA733,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60 );
D:¥DevStudio¥VC¥include¥DDRAW.H(38):DEFINE_GUID( IID_IDirectDrawSurface2,		0x57805885,0x6eec,0x11cf,0x94,0x41,0xa8,0x23,0x03,0xc1,0x0e,0x27 );
D:¥DevStudio¥VC¥include¥DDRAW.H(40):DEFINE_GUID( IID_IDirectDrawPalette,		0x6C14DB84,0xA733,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60 );
D:¥DevStudio¥VC¥include¥DDRAW.H(41):DEFINE_GUID( IID_IDirectDrawClipper,		0x6C14DB85,0xA733,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60 );
D:¥DevStudio¥VC¥include¥DPLAY.H(300):DEFINE_GUID( IID_IDirectPlay, 0x5454e9a0, 0xdb65, 0x11ce, 0x92, 0x1c, 0x00, 0xaa, 0x00, 0x6c, 0x49, 0x72);
D:¥DevStudio¥VC¥include¥DSOUND.H(26):DEFINE_GUID(CLSID_DirectSound,
D:¥DevStudio¥VC¥include¥DSOUND.H(30):DEFINE_GUID(IID_IDirectSound,0x279AFA83,0x4981,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60);
D:¥DevStudio¥VC¥include¥DSOUND.H(32):DEFINE_GUID(IID_IDirectSoundBuffer,0x279AFA85,0x4981,0x11CE,0xA5,0x21,0x00,0x20,0xAF,0x0B,0xE5,0x60);
D:¥DevStudio¥VC¥include¥EXCHEXT.H(545):    DEFINE_GUID(name, 0x00020D00 | (b), 0, 0, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥EXCHFORM.H(39):    DEFINE_GUID(name, 0x00020D00 | (b), 0, 0, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥EXDISP.H(15):DEFINE_GUID(LIBID_SHDocVw,0xEAB22AC0,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥EXDISP.H(30):DEFINE_GUID(IID_IWebBrowser,0xEAB22AC1,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥EXDISP.H(102):DEFINE_GUID(DIID_DWebBrowserEvents,0xEAB22AC2,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥EXDISP.H(173):DEFINE_GUID(CLSID_WebBrowser,0xEAB22AC3,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥EXDISP.H(179):DEFINE_GUID(IID_IWebBrowserApp,0x0002DF05,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥EXDISP.H(273):DEFINE_GUID(CLSID_InternetExplorer,0x0002DF01,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥INITGUID.H(18):#ifndef DEFINE_GUID
D:¥DevStudio¥VC¥include¥INITGUID.H(22):#undef DEFINE_GUID
D:¥DevStudio¥VC¥include¥INITGUID.H(36):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥include¥ISGUIDS.H(12):DEFINE_GUID(CLSID_InternetShortcut,       0xFBF23B40L, 0xE3F0, 0x101B, 0x84, 0x88, 0x00, 0xAA, 0x00, 0x3E, 0x56, 0xF8);
D:¥DevStudio¥VC¥include¥ISGUIDS.H(14):DEFINE_GUID(IID_IUniformResourceLocatorA, 0xFBF23B80L, 0xE3F0, 0x101B, 0x84, 0x88, 0x00, 0xAA, 0x00, 0x3E, 0x56, 0xF8);
D:¥DevStudio¥VC¥include¥ISGUIDS.H(15):DEFINE_GUID(IID_IUniformResourceLocatorW, 0xCABB0DA0L, 0xDA57, 0x11CF, 0x99, 0x74, 0x00, 0x20, 0xAF, 0xD7, 0x97, 0x62);
D:¥DevStudio¥VC¥include¥MSSTKPPG.H(17):DEFINE_GUID(CLSID_StockFontPage, 0x7ebdaae0, 0x8120, 0x11cf, 0x89, 0x9f, 0x0, 0xaa, 0x0, 0x68, 0x8b, 0x10);
D:¥DevStudio¥VC¥include¥MSSTKPPG.H(20):DEFINE_GUID(CLSID_StockColorPage, 0x7ebdaae1, 0x8120, 0x11cf, 0x89, 0x9f, 0x0, 0xaa, 0x0, 0x68, 0x8b, 0x10);
D:¥DevStudio¥VC¥include¥MSSTKPPG.H(23):DEFINE_GUID(CLSID_StockPicturePage, 0x7ebdaae2, 0x8120, 0x11cf, 0x89, 0x9f, 0x0, 0xaa, 0x0, 0x68, 0x8b, 0x10);
D:¥DevStudio¥VC¥include¥MTX.H(516):DEFINE_GUID( IID_IObjectControl, 0x51372aec, 
D:¥DevStudio¥VC¥include¥OBJBASE.H(414)://      Example: DEFINE_GUID(GUID_XXX, a, b, c, ...);
D:¥DevStudio¥VC¥include¥OBJBASE.H(429):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥include¥OBJBASE.H(433):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥include¥OBJBASE.H(439):    DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥OBJSAFE.H(87):DEFINE_GUID(IID_IObjectSafety, 0xcb5bdc81, 0x93c1, 0x11cf, 0x8f, 0x20, 0x0, 0x80, 0x5f, 0x2c, 0xd0, 0x64);
D:¥DevStudio¥VC¥include¥OLECTL.H(38):DEFINE_GUID(IID_IPropertyFrame,
D:¥DevStudio¥VC¥include¥OLECTL.H(45):DEFINE_GUID(CLSID_CFontPropPage, 
D:¥DevStudio¥VC¥include¥OLECTL.H(47):DEFINE_GUID(CLSID_CColorPropPage, 
D:¥DevStudio¥VC¥include¥OLECTL.H(49):DEFINE_GUID(CLSID_CPicturePropPage, 
D:¥DevStudio¥VC¥include¥OLECTL.H(56):DEFINE_GUID(CLSID_PersistPropset,
D:¥DevStudio¥VC¥include¥OLECTL.H(58):DEFINE_GUID(CLSID_ConvertVBX,
D:¥DevStudio¥VC¥include¥OLECTL.H(64):DEFINE_GUID(CLSID_StdFont, 
D:¥DevStudio¥VC¥include¥OLECTL.H(66):DEFINE_GUID(CLSID_StdPicture, 
D:¥DevStudio¥VC¥include¥OLECTL.H(73):DEFINE_GUID(GUID_HIMETRIC,
D:¥DevStudio¥VC¥include¥OLECTL.H(75):DEFINE_GUID(GUID_COLOR,
D:¥DevStudio¥VC¥include¥OLECTL.H(77):DEFINE_GUID(GUID_XPOSPIXEL,
D:¥DevStudio¥VC¥include¥OLECTL.H(79):DEFINE_GUID(GUID_YPOSPIXEL,
D:¥DevStudio¥VC¥include¥OLECTL.H(81):DEFINE_GUID(GUID_XSIZEPIXEL,
D:¥DevStudio¥VC¥include¥OLECTL.H(83):DEFINE_GUID(GUID_YSIZEPIXEL,
D:¥DevStudio¥VC¥include¥OLECTL.H(85):DEFINE_GUID(GUID_XPOS,
D:¥DevStudio¥VC¥include¥OLECTL.H(87):DEFINE_GUID(GUID_YPOS,
D:¥DevStudio¥VC¥include¥OLECTL.H(89):DEFINE_GUID(GUID_XSIZE,
D:¥DevStudio¥VC¥include¥OLECTL.H(91):DEFINE_GUID(GUID_YSIZE,
D:¥DevStudio¥VC¥include¥OLECTL.H(95):DEFINE_GUID(GUID_TRISTATE,
D:¥DevStudio¥VC¥include¥OLECTL.H(99):DEFINE_GUID(GUID_OPTIONVALUEEXCLUSIVE,
D:¥DevStudio¥VC¥include¥OLECTL.H(101):DEFINE_GUID(GUID_CHECKVALUEEXCLUSIVE,
D:¥DevStudio¥VC¥include¥OLECTL.H(103):DEFINE_GUID(GUID_FONTNAME,
D:¥DevStudio¥VC¥include¥OLECTL.H(105):DEFINE_GUID(GUID_FONTSIZE,
D:¥DevStudio¥VC¥include¥OLECTL.H(107):DEFINE_GUID(GUID_FONTBOLD,
D:¥DevStudio¥VC¥include¥OLECTL.H(109):DEFINE_GUID(GUID_FONTITALIC,
D:¥DevStudio¥VC¥include¥OLECTL.H(111):DEFINE_GUID(GUID_FONTUNDERSCORE,
D:¥DevStudio¥VC¥include¥OLECTL.H(113):DEFINE_GUID(GUID_FONTSTRIKETHROUGH,
D:¥DevStudio¥VC¥include¥OLECTL.H(115):DEFINE_GUID(GUID_HANDLE,
D:¥DevStudio¥VC¥include¥RECGUIDS.H(9):DEFINE_GUID(IID_IReconcileInitiator, 0x99180161L, 0xDA16, 0x101A, 0x93, 0x5C, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00);
D:¥DevStudio¥VC¥include¥RECGUIDS.H(10):DEFINE_GUID(IID_IReconcilableObject, 0x99180162L, 0xDA16, 0x101A, 0x93, 0x5C, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00);
D:¥DevStudio¥VC¥include¥RECGUIDS.H(11):DEFINE_GUID(IID_INotifyReplica,      0x99180163L, 0xDA16, 0x101A, 0x93, 0x5C, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00);
D:¥DevStudio¥VC¥include¥RECGUIDS.H(12):DEFINE_GUID(IID_IBriefcaseInitiator, 0x99180164L, 0xDA16, 0x101A, 0x93, 0x5C, 0x44, 0x45, 0x53, 0x54, 0x00, 0x00);
D:¥DevStudio¥VC¥include¥RICHOLE.H(156):DEFINE_GUID(IID_IRichEditOle,         0x00020D00, 0, 0, 0xC0,0,0,0,0,0,0,0x46);
D:¥DevStudio¥VC¥include¥RICHOLE.H(157):DEFINE_GUID(IID_IRichEditOleCallback, 0x00020D03, 0, 0, 0xC0,0,0,0,0,0,0,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(10):#define DEFINE_SHLGUID(name, l, w1, w2) DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥SHLGUID.H(55):DEFINE_GUID(IID_IShellView2, 0x88E39E80L, 0x3578, 0x11CF, 0xAE, 0x69, 0x08, 0x00, 0x2B, 0x2E, 0x12, 0x62);
D:¥DevStudio¥VC¥include¥SHLGUID.H(79):DEFINE_GUID(CLSID_InternetShortcut,       0xFBF23B40L, 0xE3F0, 0x101B, 0x84, 0x88, 0x00, 0xAA, 0x00, 0x3E, 0x56, 0xF8);
D:¥DevStudio¥VC¥include¥SHLGUID.H(80):DEFINE_GUID(IID_IUniformResourceLocator,  0xFBF23B80L, 0xE3F0, 0x101B, 0x84, 0x88, 0x00, 0xAA, 0x00, 0x3E, 0x56, 0xF8);
D:¥DevStudio¥VC¥include¥SHLGUID.H(84):DEFINE_GUID(LIBID_SHDocVw,0xEAB22AC0,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(85):DEFINE_GUID(IID_IShellExplorer,0xEAB22AC1,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(86):DEFINE_GUID(DIID_DShellExplorerEvents,0xEAB22AC2,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(87):DEFINE_GUID(CLSID_ShellExplorer,0xEAB22AC3,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(88):DEFINE_GUID(IID_ISHItemOC,0xEAB22AC4,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(89):DEFINE_GUID(DIID_DSHItemOCEvents,0xEAB22AC5,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(90):DEFINE_GUID(CLSID_SHItemOC,0xEAB22AC6,0x30C1,0x11CF,0xA7,0xEB,0x00,0x00,0xC0,0x5B,0xAE,0x0B);
D:¥DevStudio¥VC¥include¥SHLGUID.H(91):DEFINE_GUID(IID_DHyperLink,0x0002DF07,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(92):DEFINE_GUID(IID_DIExplorer,0x0002DF05,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(93):DEFINE_GUID(DIID_DExplorerEvents,0x0002DF06,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(94):DEFINE_GUID(CLSID_InternetExplorer,0x0002DF01,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(95):DEFINE_GUID(CLSID_StdHyperLink,0x0002DF09,0x0000,0x0000,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46);
D:¥DevStudio¥VC¥include¥SHLGUID.H(100):DEFINE_GUID(CLSID_FileTypes, 0xB091E540, 0x83E3, 0x11CF, 0xA7,0x13,0x00,0x20,0xAF,0xD7,0x97,0x62);
D:¥DevStudio¥VC¥include¥VFW.H(1961):    DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥include¥WPGUID.H(11):DEFINE_GUID(CLSID_WPProvider,0x3151e2e0, 0x6c4f, 0x11cf, 0x86, 0xb1, 0x0, 0xaa, 0x0, 0x60, 0xf8, 0x6c);
D:¥DevStudio¥VC¥include¥WPGUID.H(14):DEFINE_GUID(IID_IWPProvider, 0x73779EC0L, 0x6C4B, 0x11CF, 0x86, 0xB1, 0x00, 0xAA, 0x00, 0x60, 0xF8, 0x6C);
D:¥DevStudio¥VC¥include¥WPGUID.H(17):DEFINE_GUID(IID_IWPSite, 0x5261F720L, 0x6C4C, 0x11CF, 0x86, 0xB1, 0x00, 0xAA, 0x00, 0x60, 0xF8, 0x6C);
D:¥DevStudio¥VC¥include¥WPOBJ.H(14):DEFINE_GUID(LIBID_WPObj,0x536ABCA0,0x9240,0x11CF,0x9E,0xD3,0x00,0xAA,0x00,0x4C,0x12,0x0C);
D:¥DevStudio¥VC¥include¥WPOBJ.H(16):DEFINE_GUID(IID_IWPObj,0xEDD8BBC0,0x9240,0x11CF,0x9E,0xD3,0x00,0xAA,0x00,0x4C,0x12,0x0C);
D:¥DevStudio¥VC¥include¥WPOBJ.H(65):DEFINE_GUID(CLSID_WPObj,0x53DEFDE0,0x9222,0x11CF,0x9E,0xD3,0x00,0xAA,0x00,0x4C,0x12,0x0C);
D:¥DevStudio¥VC¥include¥objmodel¥ADDGUID.H(11):// NOTE!!!  This file uses the DEFINE_GUID macro.  If you #include
D:¥DevStudio¥VC¥include¥objmodel¥ADDGUID.H(24):DEFINE_GUID(IID_IDSAddIn, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(11):// NOTE!!!  This file uses the DEFINE_GUID macro.  If you #include
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(27):DEFINE_GUID(IID_IApplication,
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(31):DEFINE_GUID(IID_IApplicationEvents, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(35):DEFINE_GUID(CLSID_Application, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(43):DEFINE_GUID(IID_IGenericDocument, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(51):DEFINE_GUID(IID_IDocuments,
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(59):DEFINE_GUID(IID_IGenericWindow,
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(67):DEFINE_GUID(IID_IWindows, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(75):DEFINE_GUID(IID_IGenericProject, 
D:¥DevStudio¥VC¥include¥objmodel¥APPGUID.H(83):DEFINE_GUID(IID_IProjects,
D:¥DevStudio¥VC¥include¥objmodel¥BLDGUID.H(11):// NOTE!!!  This file uses the DEFINE_GUID macro.  If you #include
D:¥DevStudio¥VC¥include¥objmodel¥BLDGUID.H(27):DEFINE_GUID(IID_IBuildProject,
D:¥DevStudio¥VC¥include¥objmodel¥BLDGUID.H(35):DEFINE_GUID(IID_IConfiguration,
D:¥DevStudio¥VC¥include¥objmodel¥BLDGUID.H(43):DEFINE_GUID(IID_IConfigurations,
D:¥DevStudio¥VC¥include¥objmodel¥DBGGUID.H(11):// NOTE!!!  This file uses the DEFINE_GUID macro.  If you #include
D:¥DevStudio¥VC¥include¥objmodel¥DBGGUID.H(27):DEFINE_GUID(IID_IDebugger,
D:¥DevStudio¥VC¥include¥objmodel¥DBGGUID.H(31):DEFINE_GUID(IID_IDebuggerEvents,
D:¥DevStudio¥VC¥include¥objmodel¥DBGGUID.H(39):DEFINE_GUID(IID_IBreakpoint,
D:¥DevStudio¥VC¥include¥objmodel¥DBGGUID.H(47):DEFINE_GUID(IID_IBreakpoints,
D:¥DevStudio¥VC¥include¥objmodel¥TEXTGUID.H(11):// NOTE!!!  This file uses the DEFINE_GUID macro.  If you #include
D:¥DevStudio¥VC¥include¥objmodel¥TEXTGUID.H(28):DEFINE_GUID(IID_ITextDocument,
D:¥DevStudio¥VC¥include¥objmodel¥TEXTGUID.H(36):DEFINE_GUID(IID_ITextSelection,
D:¥DevStudio¥VC¥include¥objmodel¥TEXTGUID.H(44):DEFINE_GUID(IID_ITextWindow,
D:¥DevStudio¥VC¥include¥objmodel¥TEXTGUID.H(52):DEFINE_GUID(IID_ITextEditor,
D:¥DevStudio¥VC¥mfc¥src¥FILECORE.CPP(42):#undef DEFINE_GUID
D:¥DevStudio¥VC¥mfc¥src¥FILECORE.CPP(45):#define DEFINE_GUID(name, l, w1, w2, b1, b2, b3, b4, b5, b6, b7, b8) ¥
D:¥DevStudio¥VC¥mfc¥src¥FILECORE.CPP(49):#define DEFINE_OLEGUID(name, l, w1, w2) DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥mfc¥src¥FILECORE.CPP(50):#define DEFINE_SHLGUID(name, l, w1, w2) DEFINE_GUID(name, l, w1, w2, 0xC0,0,0,0,0,0,0,0x46)
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(11):DEFINE_GUID(IID_ICursor,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(15):DEFINE_GUID(IID_ICursorMove,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(19):DEFINE_GUID(IID_ICursorScroll,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(23):DEFINE_GUID(IID_ICursorUpdateARow,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(27):DEFINE_GUID(IID_INotifyDBEvents,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(31):DEFINE_GUID(IID_ICursorFind,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(35):DEFINE_GUID(IID_IEntryID,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(39):DEFINE_GUID(_GUID_NAMEONLY,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(43):DEFINE_GUID(_DBBMKGUID,
D:¥DevStudio¥VC¥mfc¥src¥OCDBID.H(47):DEFINE_GUID(_DBCIDGUID,
D:¥DevStudio¥VC¥mfc¥src¥OLEBIND.H(18):DEFINE_GUID(IID_IBoundObject,
D:¥DevStudio¥VC¥mfc¥src¥OLEBIND.H(20):DEFINE_GUID(IID_IBoundObjectSite,
// Interfaces required by COM headers
struct __declspec(uuid("00000000-0000-0000-c000-000000000046")) IUnknown;
struct __declspec(uuid("00020400-0000-0000-c000-000000000046")) IDispatch;

#if !defined(_COM_NO_STANDARD_GUIDS_)

// Interfaces:
struct __declspec(uuid("00000001-0000-0000-c000-000000000046")) IClassFactory;
struct __declspec(uuid("00000002-0000-0000-c000-000000000046")) IMalloc;
struct __declspec(uuid("00000003-0000-0000-c000-000000000046")) IMarshal;
struct __declspec(uuid("00000004-0000-0000-c000-000000000046")) IRpcChannel;
struct __declspec(uuid("00000005-0000-0000-c000-000000000046")) IRpcStub;
struct __declspec(uuid("00000006-0000-0000-c000-000000000046")) IStubManager;
struct __declspec(uuid("00000007-0000-0000-c000-000000000046")) IRpcProxy;
struct __declspec(uuid("00000008-0000-0000-c000-000000000046")) IProxyManager;
struct __declspec(uuid("00000009-0000-0000-c000-000000000046")) IPSFactory;
struct __declspec(uuid("0000000a-0000-0000-c000-000000000046")) ILockBytes;
struct __declspec(uuid("0000000b-0000-0000-c000-000000000046")) IStorage;
struct __declspec(uuid("0000000c-0000-0000-c000-000000000046")) IStream;
struct __declspec(uuid("0000000d-0000-0000-c000-000000000046")) IEnumSTATSTG;
struct __declspec(uuid("0000000e-0000-0000-c000-000000000046")) IBindCtx;
struct __declspec(uuid("0000000f-0000-0000-c000-000000000046")) IMoniker;
struct __declspec(uuid("00000010-0000-0000-c000-000000000046")) IRunningObjectTable;
struct __declspec(uuid("00000011-0000-0000-c000-000000000046")) IInternalMoniker;
struct __declspec(uuid("00000012-0000-0000-c000-000000000046")) IRootStorage;
struct __declspec(uuid("00000013-0000-0000-c000-000000000046")) IDfReserved1;
struct __declspec(uuid("00000014-0000-0000-c000-000000000046")) IDfReserved2;
struct __declspec(uuid("00000015-0000-0000-c000-000000000046")) IDfReserved3;
struct __declspec(uuid("00000016-0000-0000-c000-000000000046")) IMessageFilter;
struct __declspec(uuid("00000018-0000-0000-c000-000000000046")) IStdMarshalInfo;
struct __declspec(uuid("00000019-0000-0000-c000-000000000046")) IExternalConnection;
struct __declspec(uuid("0000001d-0000-0000-c000-000000000046")) IMallocSpy;
struct __declspec(uuid("00000020-0000-0000-c000-000000000046")) IMultiQI;
struct __declspec(uuid("00000026-0000-0000-c000-000000000046")) IStub;
struct __declspec(uuid("00000027-0000-0000-c000-000000000046")) IProxy;
struct __declspec(uuid("00000100-0000-0000-c000-000000000046")) IEnumUnknown;
struct __declspec(uuid("00000101-0000-0000-c000-000000000046")) IEnumString;
struct __declspec(uuid("00000102-0000-0000-c000-000000000046")) IEnumMoniker;
struct __declspec(uuid("00000103-0000-0000-c000-000000000046")) IEnumFORMATETC;
struct __declspec(uuid("00000104-0000-0000-c000-000000000046")) IEnumOLEVERB;
struct __declspec(uuid("00000105-0000-0000-c000-000000000046")) IEnumSTATDATA;
struct __declspec(uuid("00000106-0000-0000-c000-000000000046")) IEnumGeneric;
struct __declspec(uuid("00000107-0000-0000-c000-000000000046")) IEnumHolder;
struct __declspec(uuid("00000108-0000-0000-c000-000000000046")) IEnumCallback;
struct __declspec(uuid("00000109-0000-0000-c000-000000000046")) IPersistStream;
struct __declspec(uuid("0000010a-0000-0000-c000-000000000046")) IPersistStorage;
struct __declspec(uuid("0000010b-0000-0000-c000-000000000046")) IPersistFile;
struct __declspec(uuid("0000010c-0000-0000-c000-000000000046")) IPersist;
struct __declspec(uuid("0000010d-0000-0000-c000-000000000046")) IViewObject;
struct __declspec(uuid("0000010e-0000-0000-c000-000000000046")) IDataObject;
struct __declspec(uuid("0000010f-0000-0000-c000-000000000046")) IAdviseSink;
struct __declspec(uuid("00000110-0000-0000-c000-000000000046")) IDataAdviseHolder;
struct __declspec(uuid("00000111-0000-0000-c000-000000000046")) IOleAdviseHolder;
struct __declspec(uuid("00000112-0000-0000-c000-000000000046")) IOleObject;
struct __declspec(uuid("00000113-0000-0000-c000-000000000046")) IOleInPlaceObject;
struct __declspec(uuid("00000114-0000-0000-c000-000000000046")) IOleWindow;
struct __declspec(uuid("00000115-0000-0000-c000-000000000046")) IOleInPlaceUIWindow;
struct __declspec(uuid("00000116-0000-0000-c000-000000000046")) IOleInPlaceFrame;
struct __declspec(uuid("00000117-0000-0000-c000-000000000046")) IOleInPlaceActiveObject;
struct __declspec(uuid("00000118-0000-0000-c000-000000000046")) IOleClientSite;
struct __declspec(uuid("00000119-0000-0000-c000-000000000046")) IOleInPlaceSite;
struct __declspec(uuid("0000011a-0000-0000-c000-000000000046")) IParseDisplayName;
struct __declspec(uuid("0000011b-0000-0000-c000-000000000046")) IOleContainer;
struct __declspec(uuid("0000011c-0000-0000-c000-000000000046")) IOleItemContainer;
struct __declspec(uuid("0000011d-0000-0000-c000-000000000046")) IOleLink;
struct __declspec(uuid("0000011e-0000-0000-c000-000000000046")) IOleCache;
struct __declspec(uuid("0000011f-0000-0000-c000-000000000046")) IOleManager;
struct __declspec(uuid("00000120-0000-0000-c000-000000000046")) IOlePresObj;
struct __declspec(uuid("00000121-0000-0000-c000-000000000046")) IDropSource;
struct __declspec(uuid("00000122-0000-0000-c000-000000000046")) IDropTarget;
struct __declspec(uuid("00000123-0000-0000-c000-000000000046")) IDebug;
struct __declspec(uuid("00000124-0000-0000-c000-000000000046")) IDebugStream;
struct __declspec(uuid("00000125-0000-0000-c000-000000000046")) IAdviseSink2;
struct __declspec(uuid("00000126-0000-0000-c000-000000000046")) IRunnableObject;
struct __declspec(uuid("00000127-0000-0000-c000-000000000046")) IViewObject2;
struct __declspec(uuid("00000128-0000-0000-c000-000000000046")) IOleCache2;
struct __declspec(uuid("00000129-0000-0000-c000-000000000046")) IOleCacheControl;
struct __declspec(uuid("0000012a-0000-0000-c000-000000000046")) IContinue;
struct __declspec(uuid("00000138-0000-0000-c000-000000000046")) IPropertyStorage;
struct __declspec(uuid("00000139-0000-0000-c000-000000000046")) IEnumSTATPROPSTG;
struct __declspec(uuid("0000013a-0000-0000-c000-000000000046")) IPropertySetStorage;
struct __declspec(uuid("0000013b-0000-0000-c000-000000000046")) IEnumSTATPROPSETSTG;
struct __declspec(uuid("0000013d-0000-0000-c000-000000000046")) IClientSecurity;
struct __declspec(uuid("0000013e-0000-0000-c000-000000000046")) IServerSecurity;
struct __declspec(uuid("00000140-0000-0000-c000-000000000046")) IClassActivator;
struct __declspec(uuid("00020401-0000-0000-c000-000000000046")) ITypeInfo;
struct __declspec(uuid("00020402-0000-0000-c000-000000000046")) ITypeLib;
struct __declspec(uuid("00020403-0000-0000-c000-000000000046")) ITypeComp;
struct __declspec(uuid("00020404-0000-0000-c000-000000000046")) IEnumVARIANT;
struct __declspec(uuid("00020405-0000-0000-c000-000000000046")) ICreateTypeInfo;
struct __declspec(uuid("00020406-0000-0000-c000-000000000046")) ICreateTypeLib;
struct __declspec(uuid("0002040e-0000-0000-c000-000000000046")) ICreateTypeInfo2;
struct __declspec(uuid("0002040f-0000-0000-c000-000000000046")) ICreateTypeLib2;
struct __declspec(uuid("00020410-0000-0000-c000-000000000046")) ITypeChangeEvents;
struct __declspec(uuid("00020411-0000-0000-c000-000000000046")) ITypeLib2;
struct __declspec(uuid("00020412-0000-0000-c000-000000000046")) ITypeInfo2;
struct __declspec(uuid("00020430-0000-0000-c000-000000000046")) StdOle;
struct __declspec(uuid("00020d00-0000-0000-c000-000000000046")) IRichEditOle;
struct __declspec(uuid("00020d03-0000-0000-c000-000000000046")) IRichEditOleCallback;
struct __declspec(uuid("0002e000-0000-0000-c000-000000000046")) IEnumGUID;
struct __declspec(uuid("0002e011-0000-0000-c000-000000000046")) IEnumCATEGORYINFO;
struct __declspec(uuid("0002e012-0000-0000-c000-000000000046")) ICatRegister;
struct __declspec(uuid("0002e013-0000-0000-c000-000000000046")) ICatInformation;
struct __declspec(uuid("6d5140c1-7436-11ce-8034-00aa006009fa")) IServiceProvider;
struct __declspec(uuid("37d84f60-42cb-11ce-8135-00aa004bb851")) IPersistPropertyBag;
struct __declspec(uuid("55272a00-42cb-11ce-8135-00aa004bb851")) IPropertyBag;
struct __declspec(uuid("3127ca40-446e-11ce-8135-00aa004bb851")) IErrorLog;
struct __declspec(uuid("9bfbbc00-eff1-101a-84ed-00aa00341d07")) IBoundObject;
struct __declspec(uuid("9bfbbc01-eff1-101a-84ed-00aa00341d07")) IBoundObjectSite;
struct __declspec(uuid("9bfbbc02-eff1-101a-84ed-00aa00341d07")) IPropertyNotifySink;
struct __declspec(uuid("01e44665-24ac-101b-84ed-08002b2ec713")) IPropertyPage2;
struct __declspec(uuid("376bd3aa-3845-101b-84ed-08002b2ec713")) IPerPropertyBrowsing;
struct __declspec(uuid("99caf010-415e-11cf-8814-00aa00b569f5")) IFillLockBytes;
struct __declspec(uuid("bef6e002-a874-101a-8bba-00aa00300cab")) IFont;
struct __declspec(uuid("bef6e003-a874-101a-8bba-00aa00300cab")) IFontDisp;
struct __declspec(uuid("7bf80980-bf32-101a-8bbb-00aa00300cab")) IPicture;
struct __declspec(uuid("7bf80981-bf32-101a-8bbb-00aa00300cab")) IPictureDisp;
struct __declspec(uuid("1c2056cc-5ef4-101b-8bc8-00aa003e3b29")) IOleInPlaceObjectWindowless;
struct __declspec(uuid("79eac9c0-baf9-11ce-8c82-00aa004ba90b")) IBinding;
struct __declspec(uuid("79eac9c1-baf9-11ce-8c82-00aa004ba90b")) IBindStatusCallback;
struct __declspec(uuid("79eac9c2-baf9-11ce-8c82-00aa004ba90b")) IHlinkSite;
struct __declspec(uuid("79eac9c3-baf9-11ce-8c82-00aa004ba90b")) IHlink;
struct __declspec(uuid("79eac9c4-baf9-11ce-8c82-00aa004ba90b")) IHlinkTarget;
struct __declspec(uuid("79eac9c5-baf9-11ce-8c82-00aa004ba90b")) IHlinkFrame;
struct __declspec(uuid("79eac9c6-baf9-11ce-8c82-00aa004ba90b")) IEnumHLITEM;
struct __declspec(uuid("79eac9c7-baf9-11ce-8c82-00aa004ba90b")) IHlinkBrowseContext;
struct __declspec(uuid("79eac9c9-baf9-11ce-8c82-00aa004ba90b")) IPersistMoniker;
struct __declspec(uuid("79eac9cb-baf9-11ce-8c82-00aa004ba90b")) IBindStatusCallbackMsg;
struct __declspec(uuid("79eac9cd-baf9-11ce-8c82-00aa004ba90b")) IBindProtocol;
struct __declspec(uuid("79eac9d0-baf9-11ce-8c82-00aa004ba90b")) IAuthenticate;
struct __declspec(uuid("79eac9d1-baf9-11ce-8c82-00aa004ba90b")) ICodeInstall;
struct __declspec(uuid("79eac9d2-baf9-11ce-8c82-00aa004ba90b")) IHttpNegotiate;
struct __declspec(uuid("79eac9d3-baf9-11ce-8c82-00aa004ba90b")) IAsyncMoniker;
struct __declspec(uuid("79eac9d5-bafa-11ce-8c82-00aa004ba90b")) IWindowForBindingUI;
struct __declspec(uuid("79eac9d6-bafa-11ce-8c82-00aa004ba90b")) IWinInetInfo;
struct __declspec(uuid("79eac9d7-bafa-11ce-8c82-00aa004ba90b")) IHttpSecurity;
struct __declspec(uuid("79eac9d8-bafa-11ce-8c82-00aa004ba90b")) IWinInetHttpInfo;
struct __declspec(uuid("1cf2b120-547d-101b-8e65-08002b2bd119")) IErrorInfo;
struct __declspec(uuid("22f03340-547d-101b-8e65-08002b2bd119")) ICreateErrorInfo;
struct __declspec(uuid("df0b3d60-548f-101b-8e65-08002b2bd119")) ISupportErrorInfo;
struct __declspec(uuid("d10f6761-83e9-11cf-8f20-00805f2cd064")) IActiveScriptSiteWindow;
struct __declspec(uuid("cb5bdc81-93c1-11cf-8f20-00805f2cd064")) IObjectSafety;
struct __declspec(uuid("db01a1e3-a42b-11cf-8f20-00805f2cd064")) IActiveScriptSite;
struct __declspec(uuid("eae1ba61-a4ed-11cf-8f20-00805f2cd064")) IActiveScriptError;
struct __declspec(uuid("bb1a2ae1-a4f9-11cf-8f20-00805f2cd064")) IActiveScript;
struct __declspec(uuid("bb1a2ae2-a4f9-11cf-8f20-00805f2cd064")) IActiveScriptParse;
struct __declspec(uuid("d5f78c80-5252-11cf-90fa-00aa0042106e")) ITargetFrame;
struct __declspec(uuid("742b0e01-14e6-101b-914e-00aa00300cab")) ISimpleFrameSite;
struct __declspec(uuid("a9d758a0-4617-11cf-95fc-00aa00680db4")) IProgressNotify;
struct __declspec(uuid("0e6d4d90-6738-11cf-9608-00aa00680db4")) ILayoutStorage;
struct __declspec(uuid("548793c0-9e74-11cf-9655-00a0c9034923")) ITargetEmbedding;
struct __declspec(uuid("1008c4a0-7613-11cf-9af1-0020af6e72f4")) IChannelHook;
struct __declspec(uuid("894ad3b0-ef97-11ce-9bc9-00aa00608e01")) IOleUndoUnit;
struct __declspec(uuid("a1faf330-ef97-11ce-9bc9-00aa00608e01")) IOleParentUndoUnit;
struct __declspec(uuid("b3e7c340-ef97-11ce-9bc9-00aa00608e01")) IEnumOleUndoUnits;
struct __declspec(uuid("d001f200-ef97-11ce-9bc9-00aa00608e01")) IOleUndoManager;
struct __declspec(uuid("a6bc3ac0-dbaa-11ce-9de3-00aa004bb851")) IProvideClassInfo2;
struct __declspec(uuid("3af24290-0c96-11ce-a0cf-00aa00600ab8")) IAdviseSinkEx;
struct __declspec(uuid("3af24292-0c96-11ce-a0cf-00aa00600ab8")) IViewObjectEx;
struct __declspec(uuid("fc4801a1-2ba9-11cf-a229-00aa003d7352")) IBindHost;
struct __declspec(uuid("fc4801a3-2ba9-11cf-a229-00aa003d7352")) IObjectWithSite;
struct __declspec(uuid("b722bcc5-4e68-101b-a2bc-00aa00404770")) IOleDocument;
struct __declspec(uuid("b722bcc6-4e68-101b-a2bc-00aa00404770")) IOleDocumentView;
struct __declspec(uuid("b722bcc7-4e68-101b-a2bc-00aa00404770")) IOleDocumentSite;
struct __declspec(uuid("b722bcc8-4e68-101b-a2bc-00aa00404770")) IEnumOleDocumentViews;
struct __declspec(uuid("b722bcc9-4e68-101b-a2bc-00aa00404770")) IPrint;
struct __declspec(uuid("b722bcca-4e68-101b-a2bc-00aa00404770")) IContinueCallback;
struct __declspec(uuid("b722bccb-4e68-101b-a2bc-00aa00404770")) IOleCommandTarget;
struct __declspec(uuid("f29f6bc0-5021-11ce-aa15-00006901293f")) IROTData;
struct __declspec(uuid("9f6aa700-d188-11cd-ad48-00aa003c9cb6")) ICursor;
struct __declspec(uuid("acff0690-d188-11cd-ad48-00aa003c9cb6")) ICursorMove;
struct __declspec(uuid("bb87e420-d188-11cd-ad48-00aa003c9cb6")) ICursorScroll;
struct __declspec(uuid("d14216a0-d188-11cd-ad48-00aa003c9cb6")) ICursorUpdateARow;
struct __declspec(uuid("db526cc0-d188-11cd-ad48-00aa003c9cb6")) INotifyDBEvents;
struct __declspec(uuid("e01d7850-d188-11cd-ad48-00aa003c9cb6")) ICursorFind;
struct __declspec(uuid("e4d19810-d188-11cd-ad48-00aa003c9cb6")) IEntryID;
struct __declspec(uuid("0c733a30-2a1c-11ce-ade5-00aa0044773d")) ISequentialStream;
struct __declspec(uuid("7fd52380-4e07-101b-ae2d-08002b2ec713")) IPersistStreamInit;
struct __declspec(uuid("49384070-e40a-11ce-b2c9-00aa00680937")) IOverlappedStream;
struct __declspec(uuid("521a28f0-e40b-11ce-b2c9-00aa00680937")) IOverlappedCompletion;
struct __declspec(uuid("d5f569d0-593b-101a-b569-08002b2dbf7a")) IPSFactoryBuffer;
struct __declspec(uuid("d5f56a34-593b-101a-b569-08002b2dbf7a")) IRpcProxyBuffer;
struct __declspec(uuid("d5f56afc-593b-101a-b569-08002b2dbf7a")) IRpcStubBuffer;
struct __declspec(uuid("d5f56b60-593b-101a-b569-08002b2dbf7a")) IRpcChannelBuffer;
struct __declspec(uuid("f4f569d0-593b-101a-b569-08002b2dbf7a")) IServerHandler;
struct __declspec(uuid("f4f569d1-593b-101a-b569-08002b2dbf7a")) IClientSiteHandler;
struct __declspec(uuid("922eada0-3424-11cf-b670-00aa004cd6d8")) IOleInPlaceSiteWindowless;
struct __declspec(uuid("9c2cad80-3424-11cf-b670-00aa004cd6d8")) IOleInPlaceSiteEx;
struct __declspec(uuid("55980ba0-35aa-11cf-b671-00aa004cd6d8")) IPointerInactive;
struct __declspec(uuid("b196b283-bab4-101a-b69c-00aa00341d07")) IProvideClassInfo;
struct __declspec(uuid("b196b284-bab4-101a-b69c-00aa00341d07")) IConnectionPointContainer;
struct __declspec(uuid("b196b285-bab4-101a-b69c-00aa00341d07")) IEnumConnectionPoints;
struct __declspec(uuid("b196b286-bab4-101a-b69c-00aa00341d07")) IConnectionPoint;
struct __declspec(uuid("b196b287-bab4-101a-b69c-00aa00341d07")) IEnumConnections;
struct __declspec(uuid("b196b288-bab4-101a-b69c-00aa00341d07")) IOleControl;
struct __declspec(uuid("b196b289-bab4-101a-b69c-00aa00341d07")) IOleControlSite;
struct __declspec(uuid("b196b28a-bab4-101a-b69c-00aa00341d07")) IPropertyFrame;
struct __declspec(uuid("b196b28b-bab4-101a-b69c-00aa00341d07")) ISpecifyPropertyPages;
struct __declspec(uuid("b196b28c-bab4-101a-b69c-00aa00341d07")) IPropertyPageSite;
struct __declspec(uuid("b196b28d-bab4-101a-b69c-00aa00341d07")) IPropertyPage;
struct __declspec(uuid("b196b28f-bab4-101a-b69c-00aa00341d07")) IClassFactory2;
struct __declspec(uuid("f77459a0-bf9a-11cf-ba4e-00c04fd70816")) IMimeInfo;
struct __declspec(uuid("89bcb740-6119-101a-bcb7-00dd010655af")) IFilter;
struct __declspec(uuid("bd1ae5e0-a6ae-11ce-bd37-504200c10000")) IPersistMemory;
struct __declspec(uuid("3c374a41-bae4-11cf-bf7d-00aa006946ee")) IUrlHistoryStg;
struct __declspec(uuid("3c374a42-bae4-11cf-bf7d-00aa006946ee")) IEnumSTATURL;
struct __declspec(uuid("cf51ed10-62fe-11cf-bf86-00a0c9034836")) IQuickActivate;

// CoClasses:
struct __declspec(uuid("00000017-0000-0000-c000-000000000046")) StdMarshal;
struct __declspec(uuid("0000001b-0000-0000-c000-000000000046")) IdentityUnmarshal;
struct __declspec(uuid("0000001c-0000-0000-c000-000000000046")) InProcFreeMarshaler;
struct __declspec(uuid("0000030c-0000-0000-c000-000000000046")) PSGenObject;
struct __declspec(uuid("0000030d-0000-0000-c000-000000000046")) PSClientSite;
struct __declspec(uuid("0000030e-0000-0000-c000-000000000046")) PSClassObject;
struct __declspec(uuid("0000030f-0000-0000-c000-000000000046")) PSInPlaceActive;
struct __declspec(uuid("00000310-0000-0000-c000-000000000046")) PSInPlaceFrame;
struct __declspec(uuid("00000311-0000-0000-c000-000000000046")) PSDragDrop;
struct __declspec(uuid("00000312-0000-0000-c000-000000000046")) PSBindCtx;
struct __declspec(uuid("00000313-0000-0000-c000-000000000046")) PSEnumerators;
struct __declspec(uuid("00000315-0000-0000-c000-000000000046")) Picture_Metafile;
struct __declspec(uuid("00000315-0000-0000-c000-000000000046")) StaticMetafile;
struct __declspec(uuid("00000316-0000-0000-c000-000000000046")) Picture_Dib;
struct __declspec(uuid("00000316-0000-0000-c000-000000000046")) StaticDib;
struct __declspec(uuid("00000319-0000-0000-c000-000000000046")) Picture_EnhMetafile;
struct __declspec(uuid("00000330-0000-0000-c000-000000000046")) AllClasses;
struct __declspec(uuid("00000331-0000-0000-c000-000000000046")) LocalMachineClasses;
struct __declspec(uuid("00000332-0000-0000-c000-000000000046")) CurrentUserClasses;
struct __declspec(uuid("0002e005-0000-0000-c000-000000000046")) StdComponentCategoriesMgr;
struct __declspec(uuid("fb8f0821-0164-101b-84ed-08002b2ec713")) PersistPropset;
struct __declspec(uuid("fb8f0822-0164-101b-84ed-08002b2ec713")) ConvertVBX;
struct __declspec(uuid("79eac9d0-baf9-11ce-8c82-00aa004ba90b")) StdHlink;
struct __declspec(uuid("79eac9d1-baf9-11ce-8c82-00aa004ba90b")) StdHlinkBrowseContext;
struct __declspec(uuid("79eac9e0-baf9-11ce-8c82-00aa004ba90b")) StdURLMoniker;
struct __declspec(uuid("79eac9e1-baf9-11ce-8c82-00aa004ba90b")) StdURLProtocol;
struct __declspec(uuid("79eac9e2-baf9-11ce-8c82-00aa004ba90b")) HttpProtocol;
struct __declspec(uuid("79eac9e3-baf9-11ce-8c82-00aa004ba90b")) FtpProtocol;
struct __declspec(uuid("79eac9e4-baf9-11ce-8c82-00aa004ba90b")) GopherProtocol;
struct __declspec(uuid("79eac9e5-baf9-11ce-8c82-00aa004ba90b")) HttpSProtocol;
struct __declspec(uuid("79eac9e6-baf9-11ce-8c82-00aa004ba90b")) MkProtocol;
struct __declspec(uuid("79eac9e7-baf9-11ce-8c82-00aa004ba90b")) FileProtocol;
struct __declspec(uuid("79eac9f1-baf9-11ce-8c82-00aa004ba90b")) PSUrlMonProxy;
struct __declspec(uuid("0be35200-8f91-11ce-9de3-00aa004bb851")) CFontPropPage;
struct __declspec(uuid("0be35201-8f91-11ce-9de3-00aa004bb851")) CColorPropPage;
struct __declspec(uuid("0be35202-8f91-11ce-9de3-00aa004bb851")) CPicturePropPage;
struct __declspec(uuid("0be35203-8f91-11ce-9de3-00aa004bb851")) StdFont;
struct __declspec(uuid("0be35204-8f91-11ce-9de3-00aa004bb851")) StdPicture;

結構、いろんなGUIDがあるなぁ〜。
機械的に出せる仕組みはないかなぁ〜。
ＯＬＥ／ＣＯＭビューアやレジストリには情報が入っているのだけれど、
そこだけ抜き出せればいいのだが・・・。

typedef struct _GUID {
	unsigned long        Data1; 
    unsigned short       Data2;
    unsigned short       Data3; 
    unsigned char        Data4[8];
} GUID; 
*/
