/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 04-26-2002  JH   Added support for contains() to take regex's.
 * 06-25-2002  JH   Added isOdd and isEven user methods.
 * 07-09-2002  JH   Added fmtRoman/formatRoman() and parseIntRoman() user methods.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

//
// Named are set in VariableAdapter.java
//

public interface MethodOrdinals
{

/////////////////////////////////////////////////////////////////
// Common Function/Method Ordinals
//
  static final int BIM_COMMON_ORDINAL_BASE = 1;
  public static final int BIM_ISINT      = BIM_COMMON_ORDINAL_BASE + 1;
  public static final int BIM_ISDOUBLE   = BIM_COMMON_ORDINAL_BASE + 2;
  public static final int BIM_ISNUMBER   = BIM_COMMON_ORDINAL_BASE + 3;
  public static final int BIM_ISSTRING   = BIM_COMMON_ORDINAL_BASE + 4;
  public static final int BIM_ISDATE     = BIM_COMMON_ORDINAL_BASE + 5;
  public static final int BIM_ISJAVA     = BIM_COMMON_ORDINAL_BASE + 6;
  public static final int BIM_ISARRAY    = BIM_COMMON_ORDINAL_BASE + 7;
  public static final int BIM_ISSET      = BIM_COMMON_ORDINAL_BASE + 8;
  public static final int BIM_ISSTRUCT   = BIM_COMMON_ORDINAL_BASE + 9;
  public static final int BIM_ISSTACK    = BIM_COMMON_ORDINAL_BASE + 10;
  public static final int BIM_ISQUEUE    = BIM_COMMON_ORDINAL_BASE + 11;
  public static final int BIM_ISFUNCTION = BIM_COMMON_ORDINAL_BASE + 12;
  public static final int BIM_ISOBJECT   = BIM_COMMON_ORDINAL_BASE + 13;
  public static final int BIM_ISCOMPLEX  = BIM_COMMON_ORDINAL_BASE + 14;
  public static final int BIM_ISA        = BIM_COMMON_ORDINAL_BASE + 15;
  public static final int BIM_ISNULL     = BIM_COMMON_ORDINAL_BASE + 16;
  public static final int BIM_TYPENAME   = BIM_COMMON_ORDINAL_BASE + 17;
  public static final int BIM_TOSTRING   = BIM_COMMON_ORDINAL_BASE + 18;
  public static final int BIM_TOARRAY    = BIM_COMMON_ORDINAL_BASE + 19;

/////////////////////////////////////////////////////////////////
// ValueBase
//
  // java.lang.* class casting
         static final int BIM_JAVA_BASE = BIM_COMMON_ORDINAL_BASE + 19;
  public static final int BIM_TOBOOLEAN      = BIM_JAVA_BASE + 1;
  public static final int BIM_TOBYTE         = BIM_JAVA_BASE + 2;
  public static final int BIM_TOCHARACTER    = BIM_JAVA_BASE + 3;
  public static final int BIM_TOSHORT        = BIM_JAVA_BASE + 4;
  public static final int BIM_TOINTEGER      = BIM_JAVA_BASE + 5;
  public static final int BIM_TOLONG         = BIM_JAVA_BASE + 6;
  public static final int BIM_TOFLOAT        = BIM_JAVA_BASE + 7;
  public static final int BIM_TODOUBLE       = BIM_JAVA_BASE + 8;

  // number/math methods
         static final int BIM_NUMBER_BASE    = BIM_JAVA_BASE + 10;
//  public static final int BIM_INT            = BIM_NUMBER_BASE + 1; // use BIM_SYS_INT
//  public static final int BIM_FLOAT          = BIM_NUMBER_BASE + 2; // use BIM_SYS_FLOAT/DOUBLE
  public static final int BIM_ABS            = BIM_NUMBER_BASE + 4;
  public static final int BIM_SQRT           = BIM_NUMBER_BASE + 5;
  public static final int BIM_LOG            = BIM_NUMBER_BASE + 6;
  public static final int BIM_LOG10          = BIM_NUMBER_BASE + 7;
  public static final int BIM_LOG2           = BIM_NUMBER_BASE + 8;
  public static final int BIM_EXP            = BIM_NUMBER_BASE + 9;
  public static final int BIM_FLOOR          = BIM_NUMBER_BASE + 10;
  public static final int BIM_CEIL           = BIM_NUMBER_BASE + 11;
  public static final int BIM_ROUND          = BIM_NUMBER_BASE + 12;
  public static final int BIM_POW            = BIM_NUMBER_BASE + 13;
  public static final int BIM_SIN            = BIM_NUMBER_BASE + 14;
  public static final int BIM_COS            = BIM_NUMBER_BASE + 15;
  public static final int BIM_TAN            = BIM_NUMBER_BASE + 16;
  public static final int BIM_ASIN           = BIM_NUMBER_BASE + 17;
  public static final int BIM_ACOS           = BIM_NUMBER_BASE + 18;
  public static final int BIM_ATAN           = BIM_NUMBER_BASE + 19;
  public static final int BIM_SIN_DEG        = BIM_NUMBER_BASE + 20;
  public static final int BIM_COS_DEG        = BIM_NUMBER_BASE + 21;
  public static final int BIM_TAN_DEG        = BIM_NUMBER_BASE + 22;
  public static final int BIM_ASIN_DEG       = BIM_NUMBER_BASE + 23;
  public static final int BIM_ACOS_DEG       = BIM_NUMBER_BASE + 24;
  public static final int BIM_ATAN_DEG       = BIM_NUMBER_BASE + 25;
  public static final int BIM_DEGREE         = BIM_NUMBER_BASE + 26;
  public static final int BIM_RADIAN         = BIM_NUMBER_BASE + 27;
  public static final int BIM_GROUPNUMBER    = BIM_NUMBER_BASE + 28;
  public static final int BIM_FRACTIONDIGITS = BIM_NUMBER_BASE + 29;
  public static final int BIM_ISODD          = BIM_NUMBER_BASE + 30;
  public static final int BIM_ISEVEN         = BIM_NUMBER_BASE + 31;
  public static final int BIM_ISALPHA        = BIM_NUMBER_BASE + 32;
  public static final int BIM_ISALNUM        = BIM_NUMBER_BASE + 33;
  public static final int BIM_ISDIGIT        = BIM_NUMBER_BASE + 34;
  public static final int BIM_ISUPPER        = BIM_NUMBER_BASE + 35;
  public static final int BIM_ISLOWER        = BIM_NUMBER_BASE + 36;
  public static final int BIM_ISWHITE        = BIM_NUMBER_BASE + 37;
  public static final int BIM_NUMOFDIGITS    = BIM_NUMBER_BASE + 38;
  public static final int BIM_NUMOFHEXDIGITS = BIM_NUMBER_BASE + 39;
  public static final int BIM_NUMOFOCTALDIGITS = BIM_NUMBER_BASE + 40;

  // String methods
         static final int BIM_STRING_BASE = BIM_NUMBER_BASE + 40;
  public static final int BIM_CHR                     = BIM_STRING_BASE + 1;
  public static final int BIM_ASCII                   = BIM_STRING_BASE + 2;
  public static final int BIM_UNICODE                 = BIM_STRING_BASE + 3;
  public static final int BIM_CHARAT                  = BIM_STRING_BASE + 4;
  public static final int BIM_SUBSTR                  = BIM_STRING_BASE + 5;
  public static final int BIM_TRUNCATE                = BIM_STRING_BASE + 6;
  public static final int BIM_CSV                     = BIM_STRING_BASE + 7;
  public static final int BIM_TRIM                    = BIM_STRING_BASE + 8;
  public static final int BIM_INDEXOF                 = BIM_STRING_BASE + 9;
  public static final int BIM_LASTINDEXOF             = BIM_STRING_BASE + 10;
  public static final int BIM_TOLOWER                 = BIM_STRING_BASE + 11;
  public static final int BIM_TOUPPER                 = BIM_STRING_BASE + 12;
  public static final int BIM_STARTSWITH              = BIM_STRING_BASE + 13;
  public static final int BIM_ENDSWITH                = BIM_STRING_BASE + 14;
  public static final int BIM_REGIONMATCHES           = BIM_STRING_BASE + 15;
  public static final int BIM_REGIONMATCHESIGNORECASE = BIM_STRING_BASE + 16;
  public static final int BIM_REPLACE                 = BIM_STRING_BASE + 17;
  public static final int BIM_REPLACEIGNORECASE       = BIM_STRING_BASE + 18;
  public static final int BIM_REPLACETAGS             = BIM_STRING_BASE + 19;
  public static final int BIM_ENCODEURL               = BIM_STRING_BASE + 20;
  public static final int BIM_DECODEURL               = BIM_STRING_BASE + 21;
  public static final int BIM_PARSEURL                = BIM_STRING_BASE + 22;
  public static final int BIM_GETFILENAME             = BIM_STRING_BASE + 23;
  public static final int BIM_GETFILEPATH             = BIM_STRING_BASE + 24;
  public static final int BIM_GETFILEEXT              = BIM_STRING_BASE + 25;
  public static final int BIM_ENSUREENDSWITHFILESEP   = BIM_STRING_BASE + 26;
  public static final int BIM_ISEMPTY                 = BIM_STRING_BASE + 27;
  public static final int BIM_ISNOTEMPTY              = BIM_STRING_BASE + 28;
  public static final int BIM_ISBLANK                 = BIM_STRING_BASE + 29;
  public static final int BIM_ISNOTBLANK              = BIM_STRING_BASE + 30;
  public static final int BIM_NEVEREMPTY              = BIM_STRING_BASE + 31;
  public static final int BIM_STRINGCOMPARE           = BIM_STRING_BASE + 32;
  public static final int BIM_COUNT                   = BIM_STRING_BASE + 33;
  public static final int BIM_GETREADER               = BIM_STRING_BASE + 34;
  public static final int BIM_EQUALSIGNORECASE        = BIM_STRING_BASE + 35;
  public static final int BIM_CONTAINS                = BIM_STRING_BASE + 36;
  public static final int BIM_LEFT                    = BIM_STRING_BASE + 37;
  public static final int BIM_RIGHT                   = BIM_STRING_BASE + 38;
  public static final int BIM_LEFTOF                  = BIM_STRING_BASE + 39;
  public static final int BIM_RIGHTOF                 = BIM_STRING_BASE + 40;
  public static final int BIM_LEFTOFFIRSTWHITE        = BIM_STRING_BASE + 41;
  public static final int BIM_RIGHTOFFIRSTWHITE       = BIM_STRING_BASE + 42;
  public static final int BIM_GETBYTES                = BIM_STRING_BASE + 43;
  public static final int BIM_GETCHARS                = BIM_STRING_BASE + 44;
  public static final int BIM_BASE64DECODE            = BIM_STRING_BASE + 45;
  public static final int BIM_UNQUOTE                 = BIM_STRING_BASE + 46;
  public static final int BIM_TOABSOLUTEPATH          = BIM_STRING_BASE + 47;
  public static final int BIM_PARSEFIXEDPOSITION      = BIM_STRING_BASE + 48;
  public static final int BIM_WRITETOFILE             = BIM_STRING_BASE + 49;
  public static final int BIM_WRITETOZIP              = BIM_STRING_BASE + 50;
  public static final int BIM_ISASCIIONLY             = BIM_STRING_BASE + 51;
  public static final int BIM_ESCAPEHTML              = BIM_STRING_BASE + 52;
  public static final int BIM_ESCAPEJAVA              = BIM_STRING_BASE + 53;
  public static final int BIM_ESCAPEJAVASCRIPT        = BIM_STRING_BASE + 54;
  public static final int BIM_ESCAPESQL               = BIM_STRING_BASE + 55;
  public static final int BIM_ESCAPEXML               = BIM_STRING_BASE + 56;
  public static final int BIM_UNESCAPEHTML            = BIM_STRING_BASE + 57;
  public static final int BIM_UNESCAPEJAVA            = BIM_STRING_BASE + 58;
  public static final int BIM_UNESCAPEJAVASCRIPT      = BIM_STRING_BASE + 59;
  public static final int BIM_UNESCAPEXML             = BIM_STRING_BASE + 60;
  public static final int BIM_MATCHER                 = BIM_STRING_BASE + 61;
  public static final int BIM_MATCHES                 = BIM_STRING_BASE + 62;
  public static final int BIM_MATCHESSTART            = BIM_STRING_BASE + 63;
  public static final int BIM_REPLACEALL              = BIM_STRING_BASE + 64;
  public static final int BIM_REPLACEFIRST            = BIM_STRING_BASE + 65;
  public static final int BIM_SPLIT                   = BIM_STRING_BASE + 66;
  public static final int BIM_SPLITWITHMATCHES        = BIM_STRING_BASE + 67;
  public static final int BIM_SPLITWITHMATCHESONLY    = BIM_STRING_BASE + 68;
  public static final int BIM_CHOMP                   = BIM_STRING_BASE + 69;
  public static final int BIM_LINESTOARRAY            = BIM_STRING_BASE + 70;
  public static final int BIM_TOABSOLUTEURL           = BIM_STRING_BASE + 71;

  // File status methods
  public static final int BIM_FILE_BASE               = BIM_STRING_BASE + 71;
  public static final int BIM_FILEEXISTS              = BIM_FILE_BASE + 1;
  public static final int BIM_FILEISWRITABLE          = BIM_FILE_BASE + 2;
  public static final int BIM_FILEISREADABLE          = BIM_FILE_BASE + 3;
  public static final int BIM_FILELENGTH              = BIM_FILE_BASE + 4;
  public static final int BIM_FILETIME                = BIM_FILE_BASE + 5;
  public static final int BIM_ISFILE                  = BIM_FILE_BASE + 6;
  public static final int BIM_FILEISDIRECTORY         = BIM_FILE_BASE + 7;
  public static final int BIM_FILEISREGULAR           = BIM_FILE_BASE + 8;
  public static final int BIM_FILEISHIDDEN            = BIM_FILE_BASE + 9;
  public static final int BIM_TOOSPATH                = BIM_FILE_BASE + 10;


  // Parsing and formatting methods
         static final int BIM_FORMAT_BASE    = BIM_FILE_BASE + 10;
  public static final int BIM_PARSEINT       = BIM_FORMAT_BASE + 1;
  public static final int BIM_PARSEINTROMAN  = BIM_FORMAT_BASE + 2;
  public static final int BIM_PARSEDATE      = BIM_FORMAT_BASE + 3;
  public static final int BIM_FORMATBOOL     = BIM_FORMAT_BASE + 4;
  public static final int BIM_FORMATHEX      = BIM_FORMAT_BASE + 5;
  public static final int BIM_FORMATOCTAL    = BIM_FORMAT_BASE + 6;
  public static final int BIM_FORMATROMAN    = BIM_FORMAT_BASE + 7;
  public static final int BIM_FORMATCURRENCY = BIM_FORMAT_BASE + 8;
  public static final int BIM_FORMATDURATION = BIM_FORMAT_BASE + 9;
  public static final int BIM_UNIT           = BIM_FORMAT_BASE + 10;
  public static final int BIM_CAPITALIZEFIRSTLETTER = BIM_FORMAT_BASE + 11;
  public static final int BIM_CAPITALIZEALLFIRSTLETTERS = BIM_FORMAT_BASE + 12;

/////////////////////////////////////////////////////////////////
// bio.IODevice
//
         static final int BIM_IODEVICE_BASE = BIM_FORMAT_BASE + 12;
  public static final int BIM_SIZE              = BIM_IODEVICE_BASE + 1;
  public static final int BIM_LASTMODIFIED      = BIM_IODEVICE_BASE + 2;
  public static final int BIM_SETBIGENDIAN      = BIM_IODEVICE_BASE + 3;
  public static final int BIM_ISBIGENDIAN       = BIM_IODEVICE_BASE + 4;
  public static final int BIM_SETLITTLEENDIAN   = BIM_IODEVICE_BASE + 5;
  public static final int BIM_ISLITTLEENDIAN    = BIM_IODEVICE_BASE + 6;
  public static final int BIM_READBYTESASSTRING = BIM_IODEVICE_BASE + 7;
  public static final int BIM_TOTEXTINPUT       = BIM_IODEVICE_BASE + 8;
  public static final int BIM_TOTEXTOUTPUT      = BIM_IODEVICE_BASE + 9;

/////////////////////////////////////////////////////////////////
// bio.UserDefined
//
         static final int BIM_USERDEFINED_BASE = BIM_IODEVICE_BASE + 9;
  public static final int BIM_COPY         = BIM_USERDEFINED_BASE + 1;
  public static final int BIM_CLEAR        = BIM_USERDEFINED_BASE + 2;
  public static final int BIM_KEYS         = BIM_USERDEFINED_BASE + 3;
  public static final int BIM_KEYSSORTED   = BIM_USERDEFINED_BASE + 4;
  public static final int BIM_KEYSFILTERED = BIM_USERDEFINED_BASE + 5;
  public static final int BIM_VALUES       = BIM_USERDEFINED_BASE + 6;
  public static final int BIM_HAS          = BIM_USERDEFINED_BASE + 7;
  public static final int BIM_HASMETHOD    = BIM_USERDEFINED_BASE + 8;
  public static final int BIM_ASSERTHAS    = BIM_USERDEFINED_BASE + 9;
  public static final int BIM_REMOVE       = BIM_USERDEFINED_BASE + 10;
  public static final int BIM_GET          = BIM_USERDEFINED_BASE + 11;
  public static final int BIM_SET          = BIM_USERDEFINED_BASE + 12;
  public static final int BIM_TRANSPOSE    = BIM_USERDEFINED_BASE + 13;
  public static final int BIM_KEYSSORTEDBYVALUE            = BIM_USERDEFINED_BASE + 14;
  public static final int BIM_KEYSFILTEREDBYVALUE          = BIM_USERDEFINED_BASE + 15;
  public static final int BIM_KEYSFILTEREDANDSORTEDBYVALUE = BIM_USERDEFINED_BASE + 16;

/////////////////////////////////////////////////////////////////
// bio.ZipArchive
//
         static final int BIM_ZIPARCHIVE_BASE = BIM_USERDEFINED_BASE + 16;
  public static final int BIM_FILECOMPRESSEDSIZE = BIM_ZIPARCHIVE_BASE + 1;

/////////////////////////////////////////////////////////////////
// bio._Array and bio._Set
//
         static final int BIM_ARRAY_BASE = BIM_ZIPARCHIVE_BASE + 1;
  public static final int BIM_LASTINDEX            = BIM_ARRAY_BASE + 1;
  public static final int BIM_INSERT               = BIM_ARRAY_BASE + 2;
  public static final int BIM_PREPEND              = BIM_ARRAY_BASE + 3;
  public static final int BIM_APPEND               = BIM_ARRAY_BASE + 4;
  public static final int BIM_SUBARRAY             = BIM_ARRAY_BASE + 5;
  public static final int BIM_EXISTS               = BIM_ARRAY_BASE + 6;
  public static final int BIM_SUM                  = BIM_ARRAY_BASE + 7;
  public static final int BIM_AVG                  = BIM_ARRAY_BASE + 8;
  public static final int BIM_RANGE                = BIM_ARRAY_BASE + 9;
  public static final int BIM_SORT                 = BIM_ARRAY_BASE + 10;
  public static final int BIM_SORT_AS_STRING       = BIM_ARRAY_BASE + 11;
  public static final int BIM_SORT_AS_NUMBER       = BIM_ARRAY_BASE + 12;
  public static final int BIM_SORT_AS_DATE         = BIM_ARRAY_BASE + 13;
  public static final int BIM_REVERSE              = BIM_ARRAY_BASE + 14;
  public static final int BIM_FILTER               = BIM_ARRAY_BASE + 15;
  public static final int BIM_CONVERT              = BIM_ARRAY_BASE + 16;
  public static final int BIM_CONCAT               = BIM_ARRAY_BASE + 17;
  public static final int BIM_TOFIXEDPOSITIONSTRING= BIM_ARRAY_BASE + 18;
  public static final int BIM_PREPENDARRAY         = BIM_ARRAY_BASE + 19;
  public static final int BIM_TOSTRINGARRAY        = BIM_ARRAY_BASE + 20;
  public static final int BIM_APPENDARRAY          = BIM_ARRAY_BASE + 21;
  public static final int BIM_SETSIZE              = BIM_ARRAY_BASE + 22;
  public static final int BIM_TOOBJECTARRAY        = BIM_ARRAY_BASE + 23;
  public static final int BIM_TOBOOLEANARRAY       = BIM_ARRAY_BASE + 24;
  public static final int BIM_TOBYTEARRAY          = BIM_ARRAY_BASE + 25;
  public static final int BIM_TOCHARARRAY          = BIM_ARRAY_BASE + 26;
  public static final int BIM_TOSHORTARRAY         = BIM_ARRAY_BASE + 27;
  public static final int BIM_TOINTARRAY           = BIM_ARRAY_BASE + 28;
  public static final int BIM_TOLONGARRAY          = BIM_ARRAY_BASE + 29;
  public static final int BIM_TOFLOATARRAY         = BIM_ARRAY_BASE + 30;
  public static final int BIM_TODOUBLEARRAY        = BIM_ARRAY_BASE + 31;
  public static final int BIM_TOBOOLEANOBJECTARRAY = BIM_ARRAY_BASE + 32;
  public static final int BIM_TOBYTEOBJECTARRAY    = BIM_ARRAY_BASE + 33;
  public static final int BIM_TOCHAROBJECTARRAY    = BIM_ARRAY_BASE + 34;
  public static final int BIM_TOSHORTOBJECTARRAY   = BIM_ARRAY_BASE + 35;
  public static final int BIM_TOINTOBJECTARRAY     = BIM_ARRAY_BASE + 36;
  public static final int BIM_TOLONGOBJECTARRAY    = BIM_ARRAY_BASE + 37;
  public static final int BIM_TOFLOATOBJECTARRAY   = BIM_ARRAY_BASE + 38;
  public static final int BIM_TODOUBLEOBJECTARRAY  = BIM_ARRAY_BASE + 39;
  public static final int BIM_SUBSET               = BIM_ARRAY_BASE + 40; // _Set
  public static final int BIM_TOJAVASET            = BIM_ARRAY_BASE + 41; // _Set
  public static final int BIM_FIRST                = BIM_ARRAY_BASE + 42;
  public static final int BIM_LAST                 = BIM_ARRAY_BASE + 43;
  public static final int BIM_SAVEASLINES          = BIM_ARRAY_BASE + 44;
  public static final int BIM_LOADASLINES          = BIM_ARRAY_BASE + 45;
  public static final int BIM_GETONE               = BIM_ARRAY_BASE + 46;

/////////////////////////////////////////////////////////////////
// bio._Date
//
         static final int BIM_DATE_BASE = BIM_ARRAY_BASE + 46;
  public static final int BIM_EPOCH                = BIM_DATE_BASE + 1;  // read/write - own
  public static final int BIM_YEAR                 = BIM_DATE_BASE + 2;  // read/write
  public static final int BIM_MONTH                = BIM_DATE_BASE + 3;  // read/write
  public static final int BIM_HOUR                 = BIM_DATE_BASE + 4;  // read/write - HOUR_OF_DAY
  public static final int BIM_MINUTE               = BIM_DATE_BASE + 5;  // read/write
  public static final int BIM_SECOND               = BIM_DATE_BASE + 6;  // read/write
  public static final int BIM_MILLISECOND          = BIM_DATE_BASE + 7;  // read/write
  public static final int BIM_ZONE_OFFSET          = BIM_DATE_BASE + 8;  // read/write
  public static final int BIM_DST_OFFSET           = BIM_DATE_BASE + 9;  // read/write
  public static final int BIM_WEEK_OF_YEAR         = BIM_DATE_BASE + 10; // read only
  public static final int BIM_WEEK_OF_MONTH        = BIM_DATE_BASE + 11; // read only
  public static final int BIM_DAY_OF_MONTH         = BIM_DATE_BASE + 12; // read only
  public static final int BIM_DAY_OF_YEAR          = BIM_DATE_BASE + 13; // read only
  public static final int BIM_DAY_OF_WEEK          = BIM_DATE_BASE + 14; // read only
  public static final int BIM_DAY_OF_WEEK_IN_MONTH = BIM_DATE_BASE + 15; // read only
  public static final int BIM_IS_AM                = BIM_DATE_BASE + 16; // read only - AM_PM
  public static final int BIM_IS_PM                = BIM_DATE_BASE + 17; // read only - AM_PM
  public static final int BIM_MONTH_NAME           = BIM_DATE_BASE + 18; // read only - own
  public static final int BIM_MONTH_SHORT_NAME     = BIM_DATE_BASE + 19; // read only - own
  public static final int BIM_WEEK_NAME            = BIM_DATE_BASE + 20; // read only - own
  public static final int BIM_WEEK_SHORT_NAME      = BIM_DATE_BASE + 21; // read only - own
  public static final int BIM_FORMATDATE           = BIM_DATE_BASE + 22;
  public static final int BIM_BEFORE               = BIM_DATE_BASE + 23;
  public static final int BIM_AFTER                = BIM_DATE_BASE + 24;
  public static final int BIM_GETTIME              = BIM_DATE_BASE + 25;
  public static final int BIM_SETTIME              = BIM_DATE_BASE + 26;
  public static final int BIM_ENSUREDATE           = BIM_DATE_BASE + 27;

/////////////////////////////////////////////////////////////////
// bio._TableData
//
         static final int BIM_TABLEDATA_BASE = BIM_DATE_BASE + 27;
  public static final int BIM_ADDROW               = BIM_TABLEDATA_BASE + 1;
  public static final int BIM_SETROW               = BIM_TABLEDATA_BASE + 2;
  public static final int BIM_SETTITLES            = BIM_TABLEDATA_BASE + 3;

/////////////////////////////////////////////////////////////////
// bio._HTTP and bio.HttpService
//
         static final int BIM_HTTP_BASE = BIM_TABLEDATA_BASE + 3;
  public static final int BIM_GETURL            = BIM_HTTP_BASE + 1;
  public static final int BIM_GETHOST           = BIM_HTTP_BASE + 2;
  public static final int BIM_GETPORT           = BIM_HTTP_BASE + 3;
  public static final int BIM_GETDOMAIN         = BIM_HTTP_BASE + 4;
  public static final int BIM_GETPATH           = BIM_HTTP_BASE + 5;
  public static final int BIM_GETQUERY          = BIM_HTTP_BASE + 6;
  public static final int BIM_GETREF            = BIM_HTTP_BASE + 7;
  public static final int BIM_GETMETHOD         = BIM_HTTP_BASE + 8;
  public static final int BIM_GETINPUTSTREAM    = BIM_HTTP_BASE + 9;
  public static final int BIM_GETTEXTINPUT      = BIM_HTTP_BASE + 10;
  public static final int BIM_GETOUTPUTSTREAM   = BIM_HTTP_BASE + 11;
  public static final int BIM_GETTEXTOUTPUT     = BIM_HTTP_BASE + 12;
  public static final int BIM_STATUSCODE        = BIM_HTTP_BASE + 13;
  public static final int BIM_RESPONSEMSG       = BIM_HTTP_BASE + 14;
  public static final int BIM_GETDATEHEADER     = BIM_HTTP_BASE + 15;
  public static final int BIM_GETALLHEADERS     = BIM_HTTP_BASE + 16;
  public static final int BIM_CONNECT           = BIM_HTTP_BASE + 17;
  public static final int BIM_ADDCOOKIE         = BIM_HTTP_BASE + 18;
  public static final int BIM_LOADCOOKIES       = BIM_HTTP_BASE + 19;
  public static final int BIM_GETCOOKIES        = BIM_HTTP_BASE + 20;
  public static final int BIM_SAVECOOKIES       = BIM_HTTP_BASE + 21;
  public static final int BIM_GETCONTENTLENGTH  = BIM_HTTP_BASE + 22;
  public static final int BIM_GETCONTENTTYPE    = BIM_HTTP_BASE + 23;
  public static final int BIM_GETCONTENTBYTES   = BIM_HTTP_BASE + 24;
  public static final int BIM_PARSEFORMVARS     = BIM_HTTP_BASE + 25; // HttpService
  public static final int BIM_GETSERVERNAME     = BIM_HTTP_BASE + 26; // HttpService
  public static final int BIM_GETSERVERPORT     = BIM_HTTP_BASE + 27; // HttpService
  public static final int BIM_SERVEFILE         = BIM_HTTP_BASE + 28; // HttpService
  public static final int BIM_SERVEERROR        = BIM_HTTP_BASE + 29; // HttpService

/////////////////////////////////////////////////////////////////
// db.DBMetaData
//
         static final int BIM_DBMD_BASE = BIM_HTTP_BASE + 29;
  public static final int BIM_GETSCHEMAS           = BIM_DBMD_BASE + 1;
  public static final int BIM_GETCATALOGS          = BIM_DBMD_BASE + 2;
  public static final int BIM_GETTABLETYPES        = BIM_DBMD_BASE + 3;
  public static final int BIM_GETTYPEINFO          = BIM_DBMD_BASE + 4;
  public static final int BIM_GETPROCEDURES        = BIM_DBMD_BASE + 5;
  public static final int BIM_GETPROCEDURECOLUMNS  = BIM_DBMD_BASE + 6;
  public static final int BIM_GETTABLES            = BIM_DBMD_BASE + 7;
  public static final int BIM_GETCOLUMNS           = BIM_DBMD_BASE + 8;
  public static final int BIM_GETCOLUMNPRIVILEGES  = BIM_DBMD_BASE + 9;
  public static final int BIM_GETTABLEPRIVILEGES   = BIM_DBMD_BASE + 10;
  public static final int BIM_GETBESTROWIDENTIFIER = BIM_DBMD_BASE + 11;
  public static final int BIM_GETVERSIONCOLUMNS    = BIM_DBMD_BASE + 12;
  public static final int BIM_GETPRIMARYKEYS       = BIM_DBMD_BASE + 13;
  public static final int BIM_GETIMPORTEDKEYS      = BIM_DBMD_BASE + 14;
  public static final int BIM_GETEXPORTEDKEYS      = BIM_DBMD_BASE + 15;
  public static final int BIM_GETCROSSREFERENCE    = BIM_DBMD_BASE + 16;
  public static final int BIM_GETINDEXINFO         = BIM_DBMD_BASE + 17;
  public static final int BIM_GETUDTS              = BIM_DBMD_BASE + 18;

/////////////////////////////////////////////////////////////////
// db.DBHandle, db.QueryResult and db.UpdateResult
//
         static final int BIM_DB_BASE = BIM_DBMD_BASE + 18;
  public static final int BIM_GETSQL               = BIM_DB_BASE + 1;
  public static final int BIM_GETRESULT            = BIM_DB_BASE + 2;
  public static final int BIM_GETONERESULT         = BIM_DB_BASE + 3;
  public static final int BIM_GETRESULTSETTYPE     = BIM_DB_BASE + 4;
  public static final int BIM_GETRESULTSET         = BIM_DB_BASE + 5;
  public static final int BIM_GETRESULTSETMETADATA = BIM_DB_BASE + 6;
  public static final int BIM_GETPREPAREDSTATEMENT = BIM_DB_BASE + 7;
  public static final int BIM_GETCOLUMNATTRS       = BIM_DB_BASE + 8;
  public static final int BIM_DUMPRESULT           = BIM_DB_BASE + 9;
  public static final int BIM_EXECUTEUPDATE        = BIM_DB_BASE + 10;
  public static final int BIM_NEXT                 = BIM_DB_BASE + 11;
  public static final int BIM_HASRESULTSET         = BIM_DB_BASE + 12;

/////////////////////////////////////////////////////////////////
// db.DBBatch
//
         static final int BIM_DBBATCH_BASE = BIM_DB_BASE + 12;
  public static final int BIM_EXECUTE      = BIM_DBBATCH_BASE + 1;

/////////////////////////////////////////////////////////////////
// xml.XmlTag
//
         static final int BIM_XML_BASE = BIM_DBBATCH_BASE + 1;
  public static final int BIM_GETNAME      = BIM_XML_BASE + 1;
  public static final int BIM_GETURI       = BIM_XML_BASE + 2;
  public static final int BIM_GETLOCAL     = BIM_XML_BASE + 3;
  public static final int BIM_GETLOCALTEXT = BIM_XML_BASE + 4;
  public static final int BIM_GETRAW       = BIM_XML_BASE + 5;
  public static final int BIM_HASATTRS     = BIM_XML_BASE + 6;
  public static final int BIM_COUNTATTRS   = BIM_XML_BASE + 7;
  public static final int BIM_GETATTRNAME  = BIM_XML_BASE + 8;
  public static final int BIM_GETATTRVALUE = BIM_XML_BASE + 9;
  public static final int BIM_ISENDTAG     = BIM_XML_BASE + 10;

/////////////////////////////////////////////////////////////////
// StmtHtml$MarkupValue
//
         static final int BIM_SGML_BASE = BIM_XML_BASE + 10;
  public static final int BIM_GETROW       = BIM_SGML_BASE + 1;
  public static final int BIM_GETCOLUMN    = BIM_SGML_BASE + 2;
  public static final int BIM_ISCLOSED     = BIM_SGML_BASE + 3;
  public static final int BIM_SETNAME      = BIM_SGML_BASE + 4;
  public static final int BIM_GETTEXT      = BIM_SGML_BASE + 5;
  public static final int BIM_GETALLATTRS  = BIM_SGML_BASE + 6;


  /////////////////////////////////////////////////////////////
  // Built-in Function Ordinal
  //

  public static final int BIM_ALL__MASK         = 0x0FF0000;
  public static final int BIM_SYS__MASK         = 0x0010000;
  public static final int BIM_FS__MASK          = 0x0020000;
  public static final int BIM_DBCON__MASK       = 0x0040000;

  public static final int BIM_SYS_RANDOM = BIM_SYS__MASK | 0x0001;

  // '$$con' methods
  public static final int BIM_CLOSE          = BIM_DBCON__MASK | 1;
  public static final int BIM_COMMIT         = BIM_DBCON__MASK | 2;
  public static final int BIM_ROLLBACK       = BIM_DBCON__MASK | 3;
  public static final int BIM_NATIVESQL      = BIM_DBCON__MASK | 4;
  public static final int BIM_CLEARWARNINGS  = BIM_DBCON__MASK | 5;
  public static final int BIM_REPORTWARNINGS = BIM_DBCON__MASK | 6;
  public static final int BIM_EXECUTEQUERY   = BIM_DBCON__MASK | 7;
  public static final int BIM_EXECUTESQL     = BIM_DBCON__MASK | 8;
  public static final int BIM_EXECUTESQLFILE = BIM_DBCON__MASK | 9;
  public static final int BIM_EXECUTEBATCH   = BIM_DBCON__MASK | 10;
  public static final int BIM_GETMETADATA    = BIM_DBCON__MASK | 11;
  public static final int BIM_TABLEEXISTS    = BIM_DBCON__MASK | 12;
  public static final int BIM_PROCEXISTS     = BIM_DBCON__MASK | 13;
  public static final int BIM_UDTEXISTS      = BIM_DBCON__MASK | 14;
  public static final int BIM_OBJECTEXISTS   = BIM_DBCON__MASK | 15;
  public static final int BIM_GETOBJECTTYPE  = BIM_DBCON__MASK | 16;
  public static final int BIM_COUNTROWS      = BIM_DBCON__MASK | 17;
  public static final int BIM_DESCRIBE       = BIM_DBCON__MASK | 18;
  public static final int BIM_ADDTYPEMAP     = BIM_DBCON__MASK | 19;
  public static final int BIM_CREATEBATCH    = BIM_DBCON__MASK | 20;

  // '$$sys' methods
  public static final int BIM_DATE                     = BIM_SYS__MASK | 1;
  public static final int BIM_SYS_TIME                 = BIM_SYS__MASK | 2;
  public static final int BIM_SYS_TIMETODAY            = BIM_SYS__MASK | 3;
  public static final int BIM_SYS_RAND                 = BIM_SYS__MASK | 4;
  public static final int BIM_SYS_GETOUT               = BIM_SYS__MASK | 6;
  public static final int BIM_SYS_GETERR               = BIM_SYS__MASK | 7;
  public static final int BIM_SYS_GETLOG               = BIM_SYS__MASK | 8;
  public static final int BIM_SYS_GETIN                = BIM_SYS__MASK | 9;
  public static final int BIM_SYS_GETINSTREAM          = BIM_SYS__MASK | 10;
  public static final int BIM_SYS_SETOUT               = BIM_SYS__MASK | 11;
  public static final int BIM_SYS_SETERR               = BIM_SYS__MASK | 12;
  public static final int BIM_SYS_SETLOG               = BIM_SYS__MASK | 13;
  public static final int BIM_SYS_SETIN                = BIM_SYS__MASK | 14;
  public static final int BIM_MAX                      = BIM_SYS__MASK | 15;
  public static final int BIM_MIN                      = BIM_SYS__MASK | 16;
  public static final int BIM_SYS_CONNECTMAILSERVER    = BIM_SYS__MASK | 17;
  public static final int BIM_SYS_DISCONNECTMAILSERVER = BIM_SYS__MASK | 18;
  public static final int BIM_SYS_TIMERHANDLER         = BIM_SYS__MASK | 19;
  public static final int BIM_SYS_SETGUILISTENER       = BIM_SYS__MASK | 20;
  public static final int BIM_SYS_GETDEFAULTDATEFORMAT = BIM_SYS__MASK | 21;
  public static final int BIM_SYS_SETDEFAULTDATEFORMAT = BIM_SYS__MASK | 22;
  public static final int BIM_SYS_COMPARE              = BIM_SYS__MASK | 23;
  public static final int BIM_SYS_LOCK                 = BIM_SYS__MASK | 24;
  public static final int BIM_SYS_UNLOCK               = BIM_SYS__MASK | 25;
  public static final int BIM_SYS_WAITFOR              = BIM_SYS__MASK | 26;
  public static final int BIM_SYS_NOTIFY               = BIM_SYS__MASK | 27;
  public static final int BIM_SYS_NOTIFYALL            = BIM_SYS__MASK | 28;
  public static final int BIM_SYS_SECRET               = BIM_SYS__MASK | 29;
  public static final int BIM_SYS_ACCEPTHTTP           = BIM_SYS__MASK | 30;
  public static final int BIM_SYS_HTTPGET              = BIM_SYS__MASK | 31;
  public static final int BIM_SYS_HTTPPOST             = BIM_SYS__MASK | 32;
  public static final int BIM_SYS_COOKIE               = BIM_SYS__MASK | 33;
  public static final int BIM_SYS_COPYSTREAM           = BIM_SYS__MASK | 34;
  public static final int BIM_SYS_REGEX                = BIM_SYS__MASK | 35;
  public static final int BIM_SYS_SETCHARSET           = BIM_SYS__MASK | 36;
  public static final int BIM_SYS_GETCHARSET           = BIM_SYS__MASK | 37;
  public static final int BIM_SYS_GETOUTSTREAM         = BIM_SYS__MASK | 38;
  public static final int BIM_SYS_ASSERT               = BIM_SYS__MASK | 39;
  public static final int BIM_SYS_THISLINE             = BIM_SYS__MASK | 40;
  public static final int BIM_SYS_THISFILE             = BIM_SYS__MASK | 41;
  public static final int BIM_SYS_ENCODE               = BIM_SYS__MASK | 42;
  public static final int BIM_SYS_SSH                  = BIM_SYS__MASK | 43;
  public static final int BIM_SYS_GETFUNCTIONS         = BIM_SYS__MASK | 44;
  public static final int BIM_SYS_GETTHREADS           = BIM_SYS__MASK | 45;
  public static final int BIM_SYS_GETENVVARS           = BIM_SYS__MASK | 46;
  public static final int BIM_SYS_GETENVVAR            = BIM_SYS__MASK | 47;
  public static final int BIM_SYS_DIFF                 = BIM_SYS__MASK | 48;
  public static final int BIM_SYS_CONVERTTOVARIABLES   = BIM_SYS__MASK | 49;
  public static final int BIM_SYS_SETVARIABLE          = BIM_SYS__MASK | 50;
  public static final int BIM_SYS_GETVARIABLE          = BIM_SYS__MASK | 51;
  public static final int BIM_SYS_EXIT                 = BIM_SYS__MASK | 52;
  public static final int BIM_SYS_GETSCRIPTPATH        = BIM_SYS__MASK | 53;
  public static final int BIM_SYS_LOOPINDEX            = BIM_SYS__MASK | 54;
  public static final int BIM_SYS_ECHO                 = BIM_SYS__MASK | 55;
  public static final int BIM_SYS_BOOLEAN              = BIM_SYS__MASK | 56;
  public static final int BIM_SYS_BYTE                 = BIM_SYS__MASK | 57;
  public static final int BIM_SYS_CHAR                 = BIM_SYS__MASK | 58;
  public static final int BIM_SYS_SHORT                = BIM_SYS__MASK | 59;
  public static final int BIM_SYS_INT                  = BIM_SYS__MASK | 60;
  public static final int BIM_SYS_LONG                 = BIM_SYS__MASK | 61;
  public static final int BIM_SYS_FLOAT                = BIM_SYS__MASK | 62;
  public static final int BIM_SYS_DOUBLE               = BIM_SYS__MASK | 63;
  public static final int BIM_SYS_ANTCALL              = BIM_SYS__MASK | 64;
  public static final int BIM_SYS_EVAL                 = BIM_SYS__MASK | 65;
  public static final int BIM_SYS_EVALSEPARATE         = BIM_SYS__MASK | 66;
  public static final int BIM_SYS_EVALFILE             = BIM_SYS__MASK | 67;
  public static final int BIM_SYS_EVALFILESEPARATE     = BIM_SYS__MASK | 68;
  public static final int BIM_SYS_DB_DISCONNECT        = BIM_SYS__MASK | 69;
  // Hibernate
  public static final int BIM_HIB_ADD_CLASS            = BIM_SYS__MASK | 70;
  public static final int BIM_HIB_ADD_RESOURCE         = BIM_SYS__MASK | 71;



  public static final int BIM_PWD                  = BIM_FS__MASK | 1;
  public static final int BIM_CD                   = BIM_FS__MASK | 2;
  public static final int BIM_PUSHD                = BIM_FS__MASK | 3;
  public static final int BIM_POPD                 = BIM_FS__MASK | 4;
  public static final int BIM_CREATETEMPFILE       = BIM_FS__MASK | 5;
  public static final int BIM_OPENFILE             = BIM_FS__MASK | 6;
  public static final int BIM_READLINE             = BIM_FS__MASK | 7;
  public static final int BIM_READPIPE             = BIM_FS__MASK | 8;
  public static final int BIM_OPENTEXTFILE         = BIM_FS__MASK | 9;
  public static final int BIM_OPENRANDOMACCESSFILE = BIM_FS__MASK | 10;
  public static final int BIM_OPENGZIPPEDFILE      = BIM_FS__MASK | 11;
  public static final int BIM_OPENGZIPPEDTEXTFILE  = BIM_FS__MASK | 12;
  public static final int BIM_CREATETREEOUTPUT     = BIM_FS__MASK | 13;

} // end of interface MethodOrdinals.


/*[judo]
const #MethodOrdinals = javaclass com.judoscript.MethodOrdinals;
const #VariableAdapter = javaclass com.judoscript.VariableAdapter;

fieldValues = new struct;

fields = #MethodOrdinals.class.getFields();
for f in fields {
  if f.getName().endsWith('_BASE') { continue; }
  fieldValues.(f.getInt(null)) = f.getName();
}

methods = new sortedMap;

bimMap = #VariableAdapter.bimMap;
for x in bimMap.keys() {
  ordName = fieldValues.(bimMap.(x));
  arr = methods.(ordName);
  if arr == null { arr = {}; methods.(ordName) = arr; }
  arr.add(x);
}

for ord in methods.keys() {
  . ord:<36, methods.(ord);
}

catch: $_.printInternalStackTrace();
[judo]*/

