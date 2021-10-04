/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-9-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util.classfile;

public interface ClassFileConsts
{
  // Constant pool entry types
  public static final int CONSTANT_Utf8               = 1;
  public static final int CONSTANT_Integer            = 3;
  public static final int CONSTANT_Float              = 4;
  public static final int CONSTANT_Long               = 5;
  public static final int CONSTANT_Double             = 6;
  public static final int CONSTANT_Class              = 7;
  public static final int CONSTANT_String             = 8;
  public static final int CONSTANT_Fieldref           = 9; 
  public static final int CONSTANT_Methodref          = 10; 
  public static final int CONSTANT_InterfaceMethodref = 11;
  public static final int CONSTANT_NameAndType        = 12;

  public static final String[] CONSTANT_Names = {
    "?0?", "Utf8", "?2?", "Integer", "Float", "Long", "Double", "Class",
    "String", "Fieldref", "Methodref", "InterfaceMethodref", "NameAndType"
  };

  // Major and minor version of the code
  public final static short MAJOR = 45; // 46, 47
  public final static short MINOR = 3;  //  0,  0

  // Java VM opcodes
  public static final short OPCODE_nop              = 0;
  public static final short OPCODE_aconst_null      = 1;
  public static final short OPCODE_iconst_m1        = 2;
  public static final short OPCODE_iconst_0         = 3;
  public static final short OPCODE_iconst_1         = 4;
  public static final short OPCODE_iconst_2         = 5;
  public static final short OPCODE_iconst_3         = 6;
  public static final short OPCODE_iconst_4         = 7;
  public static final short OPCODE_iconst_5         = 8;
  public static final short OPCODE_lconst_0         = 9;
  public static final short OPCODE_lconst_1         = 10;
  public static final short OPCODE_fconst_0         = 11;
  public static final short OPCODE_fconst_1         = 12;
  public static final short OPCODE_fconst_2         = 13;
  public static final short OPCODE_dconst_0         = 14;
  public static final short OPCODE_dconst_1         = 15;
  public static final short OPCODE_bipush           = 16;
  public static final short OPCODE_sipush           = 17;
  public static final short OPCODE_ldc              = 18;
  public static final short OPCODE_ldc_w            = 19;
  public static final short OPCODE_ldc2_w           = 20;
  public static final short OPCODE_iload            = 21;
  public static final short OPCODE_lload            = 22;
  public static final short OPCODE_fload            = 23;
  public static final short OPCODE_dload            = 24;
  public static final short OPCODE_aload            = 25;
  public static final short OPCODE_iload_0          = 26;
  public static final short OPCODE_iload_1          = 27;
  public static final short OPCODE_iload_2          = 28;
  public static final short OPCODE_iload_3          = 29;
  public static final short OPCODE_lload_0          = 30;
  public static final short OPCODE_lload_1          = 31;
  public static final short OPCODE_lload_2          = 32;
  public static final short OPCODE_lload_3          = 33;
  public static final short OPCODE_fload_0          = 34;
  public static final short OPCODE_fload_1          = 35;
  public static final short OPCODE_fload_2          = 36;
  public static final short OPCODE_fload_3          = 37;
  public static final short OPCODE_dload_0          = 38;
  public static final short OPCODE_dload_1          = 39;
  public static final short OPCODE_dload_2          = 40;
  public static final short OPCODE_dload_3          = 41;
  public static final short OPCODE_aload_0          = 42;
  public static final short OPCODE_aload_1          = 43;
  public static final short OPCODE_aload_2          = 44;
  public static final short OPCODE_aload_3          = 45;
  public static final short OPCODE_iaload           = 46;
  public static final short OPCODE_laload           = 47;
  public static final short OPCODE_faload           = 48;
  public static final short OPCODE_daload           = 49;
  public static final short OPCODE_aaload           = 50;
  public static final short OPCODE_baload           = 51;
  public static final short OPCODE_caload           = 52;
  public static final short OPCODE_saload           = 53;
  public static final short OPCODE_istore           = 54;
  public static final short OPCODE_lstore           = 55;
  public static final short OPCODE_fstore           = 56;
  public static final short OPCODE_dstore           = 57;
  public static final short OPCODE_astore           = 58;
  public static final short OPCODE_istore_0         = 59;
  public static final short OPCODE_istore_1         = 60;
  public static final short OPCODE_istore_2         = 61;
  public static final short OPCODE_istore_3         = 62;
  public static final short OPCODE_lstore_0         = 63;
  public static final short OPCODE_lstore_1         = 64;
  public static final short OPCODE_lstore_2         = 65;
  public static final short OPCODE_lstore_3         = 66;
  public static final short OPCODE_fstore_0         = 67;
  public static final short OPCODE_fstore_1         = 68;
  public static final short OPCODE_fstore_2         = 69;
  public static final short OPCODE_fstore_3         = 70;
  public static final short OPCODE_dstore_0         = 71;
  public static final short OPCODE_dstore_1         = 72;
  public static final short OPCODE_dstore_2         = 73;
  public static final short OPCODE_dstore_3         = 74;
  public static final short OPCODE_astore_0         = 75;
  public static final short OPCODE_astore_1         = 76;
  public static final short OPCODE_astore_2         = 77;
  public static final short OPCODE_astore_3         = 78;
  public static final short OPCODE_iastore          = 79;
  public static final short OPCODE_lastore          = 80;
  public static final short OPCODE_fastore          = 81;
  public static final short OPCODE_dastore          = 82;
  public static final short OPCODE_aastore          = 83;
  public static final short OPCODE_bastore          = 84;
  public static final short OPCODE_castore          = 85;
  public static final short OPCODE_sastore          = 86;
  public static final short OPCODE_pop              = 87;
  public static final short OPCODE_pop2             = 88;
  public static final short OPCODE_dup              = 89;
  public static final short OPCODE_dup_x1           = 90;
  public static final short OPCODE_dup_x2           = 91;
  public static final short OPCODE_dup2             = 92;
  public static final short OPCODE_dup2_x1          = 93;
  public static final short OPCODE_dup2_x2          = 94;
  public static final short OPCODE_swap             = 95;
  public static final short OPCODE_iadd             = 96;
  public static final short OPCODE_ladd             = 97;
  public static final short OPCODE_fadd             = 98;
  public static final short OPCODE_dadd             = 99;
  public static final short OPCODE_isub             = 100;
  public static final short OPCODE_lsub             = 101;
  public static final short OPCODE_fsub             = 102;
  public static final short OPCODE_dsub             = 103;
  public static final short OPCODE_imul             = 104;
  public static final short OPCODE_lmul             = 105;
  public static final short OPCODE_fmul             = 106;
  public static final short OPCODE_dmul             = 107;
  public static final short OPCODE_idiv             = 108;
  public static final short OPCODE_ldiv             = 109;
  public static final short OPCODE_fdiv             = 110;
  public static final short OPCODE_ddiv             = 111;
  public static final short OPCODE_irem             = 112;
  public static final short OPCODE_lrem             = 113;
  public static final short OPCODE_frem             = 114;
  public static final short OPCODE_drem             = 115;
  public static final short OPCODE_ineg             = 116;
  public static final short OPCODE_lneg             = 117;
  public static final short OPCODE_fneg             = 118;
  public static final short OPCODE_dneg             = 119;
  public static final short OPCODE_ishl             = 120;
  public static final short OPCODE_lshl             = 121;
  public static final short OPCODE_ishr             = 122;
  public static final short OPCODE_lshr             = 123;
  public static final short OPCODE_iushr            = 124;
  public static final short OPCODE_lushr            = 125;
  public static final short OPCODE_iand             = 126;
  public static final short OPCODE_land             = 127;
  public static final short OPCODE_ior              = 128;
  public static final short OPCODE_lor              = 129;
  public static final short OPCODE_ixor             = 130;
  public static final short OPCODE_lxor             = 131;
  public static final short OPCODE_iinc             = 132;
  public static final short OPCODE_i2l              = 133;
  public static final short OPCODE_i2f              = 134;
  public static final short OPCODE_i2d              = 135;
  public static final short OPCODE_l2i              = 136;
  public static final short OPCODE_l2f              = 137;
  public static final short OPCODE_l2d              = 138;
  public static final short OPCODE_f2i              = 139;
  public static final short OPCODE_f2l              = 140;
  public static final short OPCODE_f2d              = 141;
  public static final short OPCODE_d2i              = 142;
  public static final short OPCODE_d2l              = 143;
  public static final short OPCODE_d2f              = 144;
  public static final short OPCODE_i2b              = 145;
  public static final short OPCODE_i2c              = 146;
  public static final short OPCODE_i2s              = 147;
  public static final short OPCODE_lcmp             = 148;
  public static final short OPCODE_fcmpl            = 149;
  public static final short OPCODE_fcmpg            = 150;
  public static final short OPCODE_dcmpl            = 151;
  public static final short OPCODE_dcmpg            = 152;
  public static final short OPCODE_ifeq             = 153;
  public static final short OPCODE_ifne             = 154;
  public static final short OPCODE_iflt             = 155;
  public static final short OPCODE_ifge             = 156;
  public static final short OPCODE_ifgt             = 157;
  public static final short OPCODE_ifle             = 158;
  public static final short OPCODE_if_icmpeq        = 159;
  public static final short OPCODE_if_icmpne        = 160;
  public static final short OPCODE_if_icmplt        = 161;
  public static final short OPCODE_if_icmpge        = 162;
  public static final short OPCODE_if_icmpgt        = 163;
  public static final short OPCODE_if_icmple        = 164;
  public static final short OPCODE_if_acmpeq        = 165;
  public static final short OPCODE_if_acmpne        = 166;
  public static final short OPCODE_goto             = 167;
  public static final short OPCODE_jsr              = 168;
  public static final short OPCODE_ret              = 169;
  public static final short OPCODE_tableswitch      = 170;
  public static final short OPCODE_lookupswitch     = 171;
  public static final short OPCODE_ireturn          = 172;
  public static final short OPCODE_lreturn          = 173;
  public static final short OPCODE_freturn          = 174;
  public static final short OPCODE_dreturn          = 175;
  public static final short OPCODE_areturn          = 176;
  public static final short OPCODE_return           = 177;
  public static final short OPCODE_getstatic        = 178;
  public static final short OPCODE_putstatic        = 179;
  public static final short OPCODE_getfield         = 180;
  public static final short OPCODE_putfield         = 181;
  public static final short OPCODE_invokevirtual    = 182;
  public static final short OPCODE_invokespecial    = 183;
  public static final short OPCODE_invokestatic     = 184;
  public static final short OPCODE_invokeinterface  = 185;
  public static final short OPCODE_new              = 187;
  public static final short OPCODE_newarray         = 188;
  public static final short OPCODE_anewarray        = 189;
  public static final short OPCODE_arraylength      = 190;
  public static final short OPCODE_athrow           = 191;
  public static final short OPCODE_checkcast        = 192;
  public static final short OPCODE_instanceof       = 193;
  public static final short OPCODE_monitorenter     = 194;
  public static final short OPCODE_monitorexit      = 195;
  public static final short OPCODE_wide             = 196;
  public static final short OPCODE_multianewarray   = 197;
  public static final short OPCODE_ifnull           = 198;
  public static final short OPCODE_ifnonnull        = 199;
  public static final short OPCODE_goto_w           = 200;
  public static final short OPCODE_jsr_w            = 201;

  // Special opcodes, may be used by JVM internally
  public static final short OPCODE_breakpoint                = 202;
  public static final short OPCODE_ldc_quick                 = 203;
  public static final short OPCODE_ldc_w_quick               = 204;
  public static final short OPCODE_ldc2_w_quick              = 205;
  public static final short OPCODE_getfield_quick            = 206;
  public static final short OPCODE_putfield_quick            = 207;
  public static final short OPCODE_getfield2_quick           = 208;
  public static final short OPCODE_putfield2_quick           = 209;
  public static final short OPCODE_getstatic_quick           = 210;
  public static final short OPCODE_putstatic_quick           = 211;
  public static final short OPCODE_getstatic2_quick          = 212;
  public static final short OPCODE_putstatic2_quick          = 213;
  public static final short OPCODE_invokevirtual_quick       = 214;
  public static final short OPCODE_invokenonvirtual_quick    = 215;
  public static final short OPCODE_invokesuper_quick         = 216;
  public static final short OPCODE_invokestatic_quick        = 217;
  public static final short OPCODE_invokeinterface_quick     = 218;
  public static final short OPCODE_invokevirtualobject_quick = 219;
  public static final short OPCODE_new_quick                 = 221;
  public static final short OPCODE_anewarray_quick           = 222;
  public static final short OPCODE_multianewarray_quick      = 223;
  public static final short OPCODE_checkcast_quick           = 224;
  public static final short OPCODE_instanceof_quick          = 225;
  public static final short OPCODE_invokevirtual_quick_w     = 226;
  public static final short OPCODE_getfield_quick_w          = 227;
  public static final short OPCODE_putfield_quick_w          = 228;
  public static final short OPCODE_impdep1                   = 254;
  public static final short OPCODE_impdep2                   = 255;

  // Internal use
  public static final short  UNDEFINED = -1;
  public static final short  UNKNOWN   = -2;
         static final short  UNK       = UNKNOWN;

  public static final byte T_U1 = 1;
  public static final byte T_U2 = 2;
  public static final byte T_U4 = 4;
  public static final byte T_S1 = -1;
  public static final byte T_S2 = -2;
  public static final byte T_S4 = -4;

  static final short[] OPTYPES_NONE   = {};
  static final short[] OPTYPES_U1     = {T_U1};
  static final short[] OPTYPES_U2     = {T_U2};
  static final short[] OPTYPES_S2     = {T_S2};
  static final short[] OPTYPES_S4     = {T_S4};
  static final short[] OPTYPES_U1U1   = {T_U1,T_U1};
  static final short[] OPTYPES_U2U1U1 = {T_U2,T_U1,T_U1};
  static final short[] OPTYPES_U2U1   = {T_U2,T_U1};

  public static final short M_UNK    = UNKNOWN;
  public static final short M_NON    = 0;
  public static final short M_VAL    = 1;
  public static final short M_VAR    = 2;
  public static final short M_JMP    = 3;
  public static final short M_TYP    = 4;
  public static final short M_CLS    = 5;
  public static final short M_FLD    = 6;
  public static final short M_MTD    = 7;
  public static final short M_VARVAL = 8;
  public static final short M_CLSVAL = 9;
  public static final short M_IFS    = 10;
  public static final short M_LOD    = 11;
  public static final short M_ITF    = 12; // invokeinterface

  public static InstInfo[] instructions = {
      //                                            operand operand        stk stk
      //        mnemonic         opcode               bytes types          con pro
      //       ----------------  ------------------      -- ------------    --  --
  new InstInfo("nop",            OPCODE_nop,              0,OPTYPES_NONE,    0,  0),
  new InstInfo("aconst_null",    OPCODE_aconst_null,      0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_m1",      OPCODE_iconst_m1,        0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_0",       OPCODE_iconst_0,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_1",       OPCODE_iconst_1,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_2",       OPCODE_iconst_2,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_3",       OPCODE_iconst_3,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_4",       OPCODE_iconst_4,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("iconst_5",       OPCODE_iconst_5,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("lconst_0",       OPCODE_lconst_0,         0,OPTYPES_NONE,    0,  2),
  new InstInfo("lconst_1",       OPCODE_lconst_1,         0,OPTYPES_NONE,    0,  2),
  new InstInfo("fconst_0",       OPCODE_fconst_0,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("fconst_1",       OPCODE_fconst_1,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("fconst_2",       OPCODE_fconst_2,         0,OPTYPES_NONE,    0,  1),
  new InstInfo("dconst_0",       OPCODE_dconst_0,         0,OPTYPES_NONE,    0,  2),
  new InstInfo("dconst_1",       OPCODE_dconst_1,         0,OPTYPES_NONE,    0,  2),
  new InstInfo("bipush",         OPCODE_bipush,           1,OPTYPES_U1,      0,  1, M_VAL),
  new InstInfo("sipush",         OPCODE_sipush,           2,OPTYPES_U2,      0,  1, M_VAL),
  new InstInfo("ldc",            OPCODE_ldc,              1,OPTYPES_U1,      0,  1, M_IFS),
  new InstInfo("ldc_w",          OPCODE_ldc_w,            2,OPTYPES_U2,      0,  1, M_IFS),
  new InstInfo("ldc2_w",         OPCODE_ldc2_w,           2,OPTYPES_U2,      0,  2, M_LOD),
  new InstInfo("iload",          OPCODE_iload,            1,OPTYPES_U1,      0,  1, M_VAR),
  new InstInfo("lload",          OPCODE_lload,            1,OPTYPES_U1,      0,  2, M_VAR),
  new InstInfo("fload",          OPCODE_fload,            1,OPTYPES_U1,      0,  1, M_VAR),
  new InstInfo("dload",          OPCODE_dload,            1,OPTYPES_U1,      0,  2, M_VAR),
  new InstInfo("aload",          OPCODE_aload,            1,OPTYPES_U1,      0,  1, M_VAR),
  new InstInfo("iload_0",        OPCODE_iload_0,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("iload_1",        OPCODE_iload_1,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("iload_2",        OPCODE_iload_2,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("iload_3",        OPCODE_iload_3,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("lload_0",        OPCODE_lload_0,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("lload_1",        OPCODE_lload_1,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("lload_2",        OPCODE_lload_2,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("lload_3",        OPCODE_lload_3,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("fload_0",        OPCODE_fload_0,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("fload_1",        OPCODE_fload_1,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("fload_2",        OPCODE_fload_2,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("fload_3",        OPCODE_fload_3,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("dload_0",        OPCODE_dload_0,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("dload_1",        OPCODE_dload_1,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("dload_2",        OPCODE_dload_2,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("dload_3",        OPCODE_dload_3,          0,OPTYPES_NONE,    0,  2),
  new InstInfo("aload_0",        OPCODE_aload_0,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("aload_1",        OPCODE_aload_1,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("aload_2",        OPCODE_aload_2,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("aload_3",        OPCODE_aload_3,          0,OPTYPES_NONE,    0,  1),
  new InstInfo("iaload",         OPCODE_iaload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("laload",         OPCODE_laload,           0,OPTYPES_NONE,    2,  2),
  new InstInfo("faload",         OPCODE_faload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("daload",         OPCODE_daload,           0,OPTYPES_NONE,    2,  2),
  new InstInfo("aaload",         OPCODE_aaload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("baload",         OPCODE_baload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("caload",         OPCODE_caload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("saload",         OPCODE_saload,           0,OPTYPES_NONE,    2,  1),
  new InstInfo("istore",         OPCODE_istore,           1,OPTYPES_U1,      1,  0, M_VAR),
  new InstInfo("lstore",         OPCODE_lstore,           1,OPTYPES_U1,      2,  0, M_VAR),
  new InstInfo("fstore",         OPCODE_fstore,           1,OPTYPES_U1,      1,  0, M_VAR),
  new InstInfo("dstore",         OPCODE_dstore,           1,OPTYPES_U1,      2,  0, M_VAR),
  new InstInfo("astore",         OPCODE_astore,           1,OPTYPES_U1,      1,  0, M_VAR),
  new InstInfo("istore_0",       OPCODE_istore_0,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("istore_1",       OPCODE_istore_1,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("istore_2",       OPCODE_istore_2,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("istore_3",       OPCODE_istore_3,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("lstore_0",       OPCODE_lstore_0,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("lstore_1",       OPCODE_lstore_1,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("lstore_2",       OPCODE_lstore_2,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("lstore_3",       OPCODE_lstore_3,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("fstore_0",       OPCODE_fstore_0,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("fstore_1",       OPCODE_fstore_1,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("fstore_2",       OPCODE_fstore_2,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("fstore_3",       OPCODE_fstore_3,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("dstore_0",       OPCODE_dstore_0,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("dstore_1",       OPCODE_dstore_1,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("dstore_2",       OPCODE_dstore_2,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("dstore_3",       OPCODE_dstore_3,         0,OPTYPES_NONE,    2,  0),
  new InstInfo("astore_0",       OPCODE_astore_0,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("astore_1",       OPCODE_astore_1,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("astore_2",       OPCODE_astore_2,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("astore_3",       OPCODE_astore_3,         0,OPTYPES_NONE,    1,  0),
  new InstInfo("iastore",        OPCODE_iastore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("lastore",        OPCODE_lastore,          0,OPTYPES_NONE,    4,  0),
  new InstInfo("fastore",        OPCODE_fastore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("dastore",        OPCODE_dastore,          0,OPTYPES_NONE,    4,  0),
  new InstInfo("aastore",        OPCODE_aastore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("bastore",        OPCODE_bastore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("castore",        OPCODE_castore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("sastore",        OPCODE_sastore,          0,OPTYPES_NONE,    3,  0),
  new InstInfo("pop",            OPCODE_pop,              0,OPTYPES_NONE,    1,  0),
  new InstInfo("pop2",           OPCODE_pop2,             0,OPTYPES_NONE,    2,  0),
  new InstInfo("dup",            OPCODE_dup,              0,OPTYPES_NONE,    1,  2),
  new InstInfo("dup_x1",         OPCODE_dup_x1,           0,OPTYPES_NONE,    2,  3),
  new InstInfo("dup_x2",         OPCODE_dup_x2,           0,OPTYPES_NONE,    3,  4),
  new InstInfo("dup2",           OPCODE_dup2,             0,OPTYPES_NONE,    2,  4),
  new InstInfo("dup2_x1",        OPCODE_dup2_x1,          0,OPTYPES_NONE,    3,  5),
  new InstInfo("dup2_x2",        OPCODE_dup2_x2,          0,OPTYPES_NONE,    4,  6),
  new InstInfo("swap",           OPCODE_swap,             0,OPTYPES_NONE,    2,  2),
  new InstInfo("iadd",           OPCODE_iadd,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("ladd",           OPCODE_ladd,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("fadd",           OPCODE_fadd,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("dadd",           OPCODE_dadd,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("isub",           OPCODE_isub,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lsub",           OPCODE_lsub,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("fsub",           OPCODE_fsub,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("dsub",           OPCODE_dsub,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("imul",           OPCODE_imul,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lmul",           OPCODE_lmul,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("fmul",           OPCODE_fmul,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("dmul",           OPCODE_dmul,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("idiv",           OPCODE_idiv,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("ldiv",           OPCODE_ldiv,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("fdiv",           OPCODE_fdiv,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("ddiv",           OPCODE_ddiv,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("irem",           OPCODE_irem,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lrem",           OPCODE_lrem,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("frem",           OPCODE_frem,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("drem",           OPCODE_drem,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("ineg",           OPCODE_ineg,             0,OPTYPES_NONE,    1,  1),
  new InstInfo("lneg",           OPCODE_lneg,             0,OPTYPES_NONE,    2,  2),
  new InstInfo("fneg",           OPCODE_fneg,             0,OPTYPES_NONE,    1,  1),
  new InstInfo("dneg",           OPCODE_dneg,             0,OPTYPES_NONE,    2,  2),
  new InstInfo("ishl",           OPCODE_ishl,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lshl",           OPCODE_lshl,             0,OPTYPES_NONE,    3,  2),
  new InstInfo("ishr",           OPCODE_ishr,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lshr",           OPCODE_lshr,             0,OPTYPES_NONE,    3,  2),
  new InstInfo("iushr",          OPCODE_iushr,            0,OPTYPES_NONE,    2,  1),
  new InstInfo("lushr",          OPCODE_lushr,            0,OPTYPES_NONE,    3,  2),
  new InstInfo("iand",           OPCODE_iand,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("land",           OPCODE_land,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("ior",            OPCODE_ior,              0,OPTYPES_NONE,    2,  1),
  new InstInfo("lor",            OPCODE_lor,              0,OPTYPES_NONE,    4,  2),
  new InstInfo("ixor",           OPCODE_ixor,             0,OPTYPES_NONE,    2,  1),
  new InstInfo("lxor",           OPCODE_lxor,             0,OPTYPES_NONE,    4,  2),
  new InstInfo("iinc",           OPCODE_iinc,             2,OPTYPES_U1U1,    0,  0, M_VARVAL),
  new InstInfo("i2l",            OPCODE_i2l,              0,OPTYPES_NONE,    1,  2),
  new InstInfo("i2f",            OPCODE_i2f,              0,OPTYPES_NONE,    1,  1),
  new InstInfo("i2d",            OPCODE_i2d,              0,OPTYPES_NONE,    1,  2),
  new InstInfo("l2i",            OPCODE_l2i,              0,OPTYPES_NONE,    2,  1),
  new InstInfo("l2f",            OPCODE_l2f,              0,OPTYPES_NONE,    2,  1),
  new InstInfo("l2d",            OPCODE_l2d,              0,OPTYPES_NONE,    2,  2),
  new InstInfo("f2i",            OPCODE_f2i,              0,OPTYPES_NONE,    1,  1),
  new InstInfo("f2l",            OPCODE_f2l,              0,OPTYPES_NONE,    1,  2),
  new InstInfo("f2d",            OPCODE_f2d,              0,OPTYPES_NONE,    1,  2),
  new InstInfo("d2i",            OPCODE_d2i,              0,OPTYPES_NONE,    2,  1),
  new InstInfo("d2l",            OPCODE_d2l,              0,OPTYPES_NONE,    2,  2),
  new InstInfo("d2f",            OPCODE_d2f,              0,OPTYPES_NONE,    2,  1),
  new InstInfo("i2b",            OPCODE_i2b,              0,OPTYPES_NONE,    1,  1),
  new InstInfo("i2c",            OPCODE_i2c,              0,OPTYPES_NONE,    1,  1),
  new InstInfo("i2s",            OPCODE_i2s,              0,OPTYPES_NONE,    1,  1),
  new InstInfo("lcmp",           OPCODE_lcmp,             0,OPTYPES_NONE,    4,  1),
  new InstInfo("fcmpl",          OPCODE_fcmpl,            0,OPTYPES_NONE,    2,  1),
  new InstInfo("fcmpg",          OPCODE_fcmpg,            0,OPTYPES_NONE,    2,  1),
  new InstInfo("dcmpl",          OPCODE_dcmpl,            0,OPTYPES_NONE,    4,  1),
  new InstInfo("dcmpg",          OPCODE_dcmpg,            0,OPTYPES_NONE,    4,  1),
  new InstInfo("ifeq",           OPCODE_ifeq,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("ifne",           OPCODE_ifne,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("iflt",           OPCODE_iflt,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("ifge",           OPCODE_ifge,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("ifgt",           OPCODE_ifgt,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("ifle",           OPCODE_ifle,             2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("if_icmpeq",      OPCODE_if_icmpeq,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_icmpne",      OPCODE_if_icmpne,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_icmplt",      OPCODE_if_icmplt,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_icmpge",      OPCODE_if_icmpge,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_icmpgt",      OPCODE_if_icmpgt,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_icmple",      OPCODE_if_icmple,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_acmpeq",      OPCODE_if_acmpeq,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("if_acmpne",      OPCODE_if_acmpne,        2,OPTYPES_S2,      2,  0, M_JMP),
  new InstInfo("goto",           OPCODE_goto,             2,OPTYPES_S2,      0,  0, M_JMP),
  new InstInfo("jsr",            OPCODE_jsr,              2,OPTYPES_S2,      0,  1, M_JMP),
  new InstInfo("ret",            OPCODE_ret,              1,OPTYPES_U1,      0,  0, M_VAR),
  new InstInfo("tableswitch",    OPCODE_tableswitch,    UNK,OPTYPES_NONE,    1,  0, M_UNK),
  new InstInfo("lookupswitch",   OPCODE_lookupswitch,   UNK,OPTYPES_NONE,    1,  0, M_UNK),
  new InstInfo("ireturn",        OPCODE_ireturn,          0,OPTYPES_NONE,    1,  0),
  new InstInfo("lreturn",        OPCODE_lreturn,          0,OPTYPES_NONE,    2,  0),
  new InstInfo("freturn",        OPCODE_freturn,          0,OPTYPES_NONE,    1,  0),
  new InstInfo("dreturn",        OPCODE_dreturn,          0,OPTYPES_NONE,    2,  0),
  new InstInfo("areturn",        OPCODE_areturn,          0,OPTYPES_NONE,    1,  0),
  new InstInfo("return",         OPCODE_return,           0,OPTYPES_NONE,    0,  0),
  new InstInfo("getstatic",      OPCODE_getstatic,        2,OPTYPES_U2,      0,UNK, M_FLD),
  new InstInfo("putstatic",      OPCODE_putstatic,        2,OPTYPES_U2,    UNK,  0, M_FLD),
  new InstInfo("getfield",       OPCODE_getfield,         2,OPTYPES_U2,      1,UNK, M_FLD),
  new InstInfo("putfield",       OPCODE_putfield,         2,OPTYPES_U2,    UNK,  0, M_FLD),
  new InstInfo("invokevirtual",  OPCODE_invokevirtual,    2,OPTYPES_U2,    UNK,UNK, M_MTD),
  new InstInfo("invokespecial",  OPCODE_invokespecial,    2,OPTYPES_U2,    UNK,UNK, M_MTD),
  new InstInfo("invokestatic",   OPCODE_invokestatic,     2,OPTYPES_U2,    UNK,UNK, M_MTD),
  new InstInfo("invokeinterface",OPCODE_invokeinterface,  4,OPTYPES_U2U1U1,UNK,UNK, M_ITF),
  new InstInfo("<illegal>",      UNDEFINED,             UNK,OPTYPES_NONE,    0,  0, M_UNK),
  new InstInfo("new",            OPCODE_new,              2,OPTYPES_U2,      0,  1, M_CLS),
  new InstInfo("newarray",       OPCODE_newarray,         1,OPTYPES_U1,      1,  1, M_TYP),
  new InstInfo("anewarray",      OPCODE_anewarray,        2,OPTYPES_U2,      1,  1, M_CLS),
  new InstInfo("arraylength",    OPCODE_arraylength,      0,OPTYPES_NONE,    1,  1),
  new InstInfo("athrow",         OPCODE_athrow,           0,OPTYPES_NONE,    1,  1),
  new InstInfo("checkcast",      OPCODE_checkcast,        2,OPTYPES_U2,      1,  1, M_CLS),
  new InstInfo("instanceof",     OPCODE_instanceof,       2,OPTYPES_U2,      1,  1, M_CLS),
  new InstInfo("monitorenter",   OPCODE_monitorenter,     0,OPTYPES_NONE,    1,  0),
  new InstInfo("monitorexit",    OPCODE_monitorexit,      0,OPTYPES_NONE,    1,  0),
  new InstInfo("wide",           OPCODE_wide,           UNK,OPTYPES_U1,      0,  0, M_UNK),
  new InstInfo("multianewarray", OPCODE_multianewarray,   3,OPTYPES_U2U1,  UNK,  1, M_CLSVAL),
  new InstInfo("ifnull",         OPCODE_ifnull,           2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("ifnonnull",      OPCODE_ifnonnull,        2,OPTYPES_S2,      1,  0, M_JMP),
  new InstInfo("goto_w",         OPCODE_goto_w,           4,OPTYPES_S4,      0,  0, M_JMP),
  new InstInfo("jsr_w",          OPCODE_jsr_w,            4,OPTYPES_S4,      0,  1, M_JMP)
  };

} // end of class ClassFileConsts.

/*[judo]

const #cfc = javaclass com.judoscript.util.classfile.ClassFileConsts;
insts = #cfc.instructions;

_end = '); }';
for inst in insts {
  _start = '  public void _' @ inst.mnemonic @ '(';
  _addinst = ') ';
  if (inst.operand_meaning==#cfc.M_VAR) || (inst.operand_meaning==#cfc.M_VARVAL) {
    _addinst @= 'throws BadClassFormatException ';
  }
  _addinst @= '{ addInst(OPCODE_' @ inst.mnemonic;
  switch inst.operand_meaning {
  case #cfc.M_NON:   . _start, _addinst, _end; break;
  case #cfc.M_VAL:   . _start, 'byte val',     _addinst, ', val',                    _end; break;
  case #cfc.M_VAR:   . _start, 'String var',   _addinst, ', getLocalVarIndex(var)',  _end; break;
  case #cfc.M_JMP:   . _start, 'String label', _addinst, ', label',                  _end; break;
  case #cfc.M_TYP:   . _start, 'String type',  _addinst, ', getPrimitiveType(type)', _end; break;
  case #cfc.M_CLS:   . _start, 'String cls',   _addinst, ', cfw.cpClass(cls)',       _end; break;
  case #cfc.M_FLD:   . _start, 'String cls, String fld, String desc', _addinst,
                       ', cfw.cpFieldRef(cls,fld,desc)', _end; break;
  case #cfc.M_MTD:
  case #cfc.M_ITF:   . _start, 'String cls, String mthd, String desc', _addinst,
                       ', cfw.cpMethodRef(cls,mthd,desc)', _end; break;
  case #cfc.M_VARVAL:. _start, 'String var, int val', _addinst, ', getLocalVarIndex(var), val', _end; break;
  case #cfc.M_CLSVAL:. _start, 'String cls, int val', _addinst, ', cfw.cpClass(cls), val',      _end; break;
  case #cfc.M_IFS:   . _start, 'int i',   _addinst, ', val',                       _end;
                     . _start, 'float f', _addinst, ', Float.floatToIntBits(f)',   _end;
                     . _start, 'String s',_addinst, ', cfw.cpString(s)',           _end; break;
  case #cfc.M_LOD:   . _start, 'long l',  _addinst, ', l',                         _end;
                     . _start, 'double d',_addinst, ', Double.doubleToLongBits(d)',_end; break;
  default:           . '// ', inst.mnemonic; break;
  }
}

[judo]*/
