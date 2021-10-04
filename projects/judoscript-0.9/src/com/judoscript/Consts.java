/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added TYPE3_SYMBOL="<!--".
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;


public interface Consts
{
  /////////////////////////////////////////////////////////////
  // ExceptionRuntime Constants
  //

  public static final int RTERR_INTERNAL_ERROR           = 0;
  public static final int RTERR_NO_SUCH_VALUE            = 1;
  public static final int RTERR_OBJECT_INIT              = 2;
  public static final int RTERR_INVALID_MEMBER_ACCESS    = 3;
  public static final int RTERR_INVALID_ARRAY_ACCESS     = 4;
  public static final int RTERR_INVALID_VARIABLE_NAME    = 5;
  public static final int RTERR_ILLEGAL_INDEXED_ACCESS   = 6;
  public static final int RTERR_ILLEGAL_VALUE_SETTING    = 7;
  public static final int RTERR_JAVA_EXCEPTION           = 8;
  public static final int RTERR_JAVA_METHOD_CALL         = 9;
  public static final int RTERR_JAVA_OBJECT_CREATION     = 10;
  public static final int RTERR_ILLEGAL_JUMP             = 11;
  public static final int RTERR_ASSERTION_FAILURE        = 12;
  public static final int RTERR_ILLEGAL_ARGUMENTS        = 13;
  public static final int RTERR_JDBC_FAILURE             = 14;
  public static final int RTERR_JDBC_WARNING             = 15;
  public static final int RTERR_JDBC_INVALID_COLUMN      = 16;
  public static final int RTERR_IO_FILE_OPEN_FAILURE     = 17;
  public static final int RTERR_IO_FAILURE               = 18;
  public static final int RTERR_UNDEFINED_OBJECT_TYPE    = 19;
  public static final int RTERR_CONSTANT_REDEFINITION    = 20;
  public static final int RTERR_JDBC_DRIVER_NOT_FOUND    = 21;
  public static final int RTERR_BAD_PRINT_TARGET         = 22;
  public static final int RTERR_MAILSERVER_NOT_CONNECTED = 23;
  public static final int RTERR_MAIL_MISSING_FIELD       = 24;
  public static final int RTERR_TIMER_ALREADY_EXISTS     = 25;
  public static final int RTERR_ILLEGAL_STATEMENT        = 26;
  public static final int RTERR_METHOD_NOT_FOUND         = 27;
  public static final int RTERR_CONSTANT_NOT_DEFINED     = 28;
  public static final int RTERR_UNDEFINED_GUI_EVENT      = 29;
  public static final int RTERR_JAVA_COMPILE_FAILED      = 30;
  public static final int RTERR_ENVIRONMENT_ERROR        = 31;
  public static final int RTERR_EMBEDDED_JAVA_COMPILE_FAILED = 32;
  public static final int RTERR_XML_PARSING_ERROR        = 33;
  public static final int RTERR_FS_CREATE_DIR_FAILED     = 34;
  public static final int RTERR_FS_REMOVE_DIR_FAILED     = 35;
  public static final int RTERR_FS_COPY_MOVE_FAILED      = 36;
  public static final int RTERR_INVALID_NUMBER_FORMAT    = 37;
  public static final int RTERR_USER_EXCEPTION           = 38;
  public static final int RTERR_ILLEGAL_ACCESS           = 39;
  public static final int RTERR_INVALID_JAVA_VERSION     = 40;
  public static final int RTERR_FUNCTION_ALREADY_DEFINED = 41;
  public static final int RTERR_EVALUATION_FAILED        = 42;

  public static final String[] rterr_names = {
    "INTERNAL_ERROR",
    "NO_SUCH_VALUE",
    "OBJECT_INIT",
    "INVALID_MEMBER_ACCESS",
    "INVALID_ARRAY_ACCESS",
    "INVALID_VARIABLE_NAME",
    "ILLEGAL_INDEXED_ACCESS",
    "ILLEGAL_VALUE_SETTING",
    "JAVA_EXCEPTION",
    "JAVA_METHOD_CALL",
    "JAVA_OBJECT_CREATION",
    "ILLEGAL_JUMP",
    "ASSERTION_FAILURE",
    "ILLEGAL_ARGUMENTS",
    "JDBC_FAILURE",
    "JDBC_WARNING",
    "JDBC_INVALID_COLUMN",
    "IO_FILE_OPEN_FAILURE",
    "IO_FAILURE",
    "UNDEFINED_OBJECT_TYPE",
    "CONSTANT_REDEFINITION",
    "JDBC_DRIVER_NOT_FOUND",
    "BAD_PRINT_TARGET",
    "MAILSERVER_NOT_CONNECTED",
    "MAIL_MISSING_FIELD",
    "TIMER_ALREADY_EXISTS",
    "ILLEGAL_STATEMENT",
    "METHOD_NOT_FOUND",
    "CONSTANT_NOT_DEFINED",
    "UNDEFINED_GUI_EVENT",
    "JAVA_COMPILE_FAILED",
    "ENVIRONMENT_ERROR",
    "EMBEDDED_JAVA_COMPILE_FAILED",
    "XML_PARSING_ERROR",
    "FS_CREATE_DIR_FAILED",
    "FS_REMOVE_DIR_FAILED",
    "FS_COPY_MOVE_FAILED",
    "INVALID_NUMBER_FORMAT",
    "USER_EXCEPTION",
    "ILLEGAL_ACCESS",
    "INVALID_JAVA_VERSION",
    "FUNCTION_ALREADY_DEFINED",
    "EVALUATION_FAILED"
  };

  /////////////////////////////////////////////////////////////
  // Undefined Access Control Constants
  //
  public static final int ISSUE_LEVEL_IGNORE = 0;
  public static final int ISSUE_LEVEL_DEBUG  = 1;
  public static final int ISSUE_LEVEL_INFO   = 2;
  public static final int ISSUE_LEVEL_WARN   = 3;
  public static final int ISSUE_LEVEL_ERROR  = 4;

  /////////////////////////////////////////////////////////////
  // RuntimeContext Constants
  //

  public static final int PRINT_USER = 0;
  public static final int PRINT_OUT  = 1;
  public static final int PRINT_ERR  = 2;
  public static final int PRINT_LOG  = 3;
  public static final int PRINT_PIPE = 4;

  /////////////////////////////////////////////////////////////
  // Expr Constants
  //

  public static final int TYPE_INTERNAL  = -1001;
  public static final int TYPE_CALLABLE  = -1002;
  public static final int TYPE_CONTEXT   = -1003;
  public static final int TYPE_UNKNOWN   = -1000;
  public static final int TYPE_UNDEFINED = -2;
  public static final int TYPE_NAN       = -1;
  public static final int TYPE_NIL       = 0;
  public static final int TYPE_STRING    = 1;
  public static final int TYPE_DOUBLE    = 2;
  public static final int TYPE_INT       = 3;
  public static final int TYPE_DATE      = 4;
  public static final int TYPE_OBJECT    = 5;
  public static final int TYPE_JAVA      = 6;
  public static final int TYPE_FUNCTION  = 7;
  public static final int TYPE_ARRAY     = 8;
  public static final int TYPE_STACK     = 9;
  public static final int TYPE_QUEUE     = 10;
  public static final int TYPE_SET       = 11;
  public static final int TYPE_STRUCT    = 12;
  public static final int TYPE_COM       = 13;
  public static final int TYPE_COMPLEX   = 14;
  public static final int TYPE_WS        = 15;

  public static final int OP_CONCAT           = 1;  // @
  public static final int OP_PLUS             = 2;  // +, ++
  public static final int OP_MINUS            = 3;  // -, --
  public static final int OP_MUL              = 4;  // *
  public static final int OP_DIV              = 5;  // /
  public static final int OP_MOD              = 6;  // %

  public static final int OP_EQ               = 7;  // ==
  public static final int OP_NE               = 8;  // !=
  public static final int OP_GT               = 9;  // >
  public static final int OP_LT               = 10; // <
  public static final int OP_GE               = 11; // >=
  public static final int OP_LE               = 12; // <=

  public static final int OP_LSHIFT           = 13; // <<
  public static final int OP_RSHIFT           = 14; // >>
  public static final int OP_RUSHIFT          = 15; // >>>
  public static final int OP_COMPLEMENT       = 16; // ~

  public static final int OP_NOT              = 17; // !
  public static final int OP_AND              = 18; // &, &&
  public static final int OP_OR               = 19; // |, ||
  public static final int OP_XOR              = 20; // ^

  public static final int OP_ASSIGN           = 21; // =
  public static final int OP_COPY             = 22; // <=
  public static final int OP_CONCAT_ASSIGN    = 23; // @=
  public static final int OP_PLUS_ASSIGN      = 24; // +=
  public static final int OP_MINUS_ASSIGN     = 25; // -=
  public static final int OP_MUL_ASSIGN       = 26; // *=
  public static final int OP_DIV_ASSIGN       = 27; // /=
  public static final int OP_MOD_ASSIGN       = 28; // %=
  public static final int OP_LSHIFT_ASSIGN    = 29; // <<=
  public static final int OP_RSHIFT_ASSIGN    = 30; // >>=
  public static final int OP_RUSHIFT_ASSIGN   = 31; // >>>=
  public static final int OP_AND_ASSIGN       = 32; // &=
  public static final int OP_OR_ASSIGN        = 33; // |=
  public static final int OP_XOR_ASSIGN       = 34; // ^=
  public static final int OP_LOGIC_AND_ASSIGN = 35; // &&=
  public static final int OP_LOGIC_OR_ASSIGN  = 36; // ||=

  /////////////////////////////////////////////////////////////
  // Pragma Constants
  //

  public static final int PRAGMA_UNKNOWN      = -1;
  public static final int PRAGMA_INCLUDEPATH  = 0; // root paths for non-absolute include files
  public static final int PRAGMA_JDBCDRIVER   = 1; // explicitly load this JDBC driver
  public static final int PRAGMA_ASSERTION    = 3; // t/F: enable/disable assertion.
  public static final int PRAGMA_GUILISTENER  = 4; // syntax: [alias:]interface:class
  public static final int PRAGMA_JAVACOMPILER = 5; // default is "javac"
  public static final int PRAGMA_CRYPTOCLASS  = 6; // default is "com.judoscript.util.PBEWithMD5AndDES"

  public static final String[] pragma_names = {
    "includePaths",
    "jdbcDriver",
    "assertion",
    "guiListener",
    "javaCompiler",
    "cryptoClass",
    null
  };

  /////////////////////////////////////////////////////////////
  // StmtDynamic Constants
  //

  public static final int DYNAMIC_EVAL         = 0;
  public static final int DYNAMIC_EVALEXTERNAL = 1;
  public static final int DYNAMIC_EVALFILE     = 2;

  /////////////////////////////////////////////////////////////
  // Java Type Constants
  //

  public static final int JAVA_ANY             = '\0';
  public static final int JAVA_STRING          = 'X';
  public static final int JAVA_BOOLEAN         = 'Z';
  public static final int JAVA_BYTE            = 'B';
  public static final int JAVA_CHAR            = 'C';
  public static final int JAVA_SHORT           = 'S';
  public static final int JAVA_INT             = 'I';
  public static final int JAVA_LONG            = 'J';
  public static final int JAVA_FLOAT           = 'F';
  public static final int JAVA_DOUBLE          = 'D';
  public static final int JAVA_CURRENCY        = 'c';
  public static final int JAVA_BOOLEAN_O       = '!';
  public static final int JAVA_BYTE_O          = '@';
  public static final int JAVA_CHAR_O          = '#';
  public static final int JAVA_SHORT_O         = '$';
  public static final int JAVA_INT_O           = '%';
  public static final int JAVA_LONG_O          = '^';
  public static final int JAVA_FLOAT_O         = '&';
  public static final int JAVA_DOUBLE_O        = '*';
  public static final int JAVA_DATE_O          = '(';
  public static final int JAVA_SQL_DATE_O      = ')';
  public static final int JAVA_SQL_TIME_O      = '_';
  public static final int JAVA_SQL_TIMESTAMP_O = '+';

  public static final Integer JAVA_ANY_I       = new Integer(JAVA_ANY);
  public static final Integer JAVA_STRING_I    = new Integer(JAVA_STRING);
  public static final Integer JAVA_BOOLEAN_I   = new Integer(JAVA_BOOLEAN);
  public static final Integer JAVA_BYTE_I      = new Integer(JAVA_BYTE);
  public static final Integer JAVA_CHAR_I      = new Integer(JAVA_CHAR);
  public static final Integer JAVA_SHORT_I     = new Integer(JAVA_SHORT);
  public static final Integer JAVA_INT_I       = new Integer(JAVA_INT);
  public static final Integer JAVA_LONG_I      = new Integer(JAVA_LONG);
  public static final Integer JAVA_FLOAT_I     = new Integer(JAVA_FLOAT);
  public static final Integer JAVA_DOUBLE_I    = new Integer(JAVA_DOUBLE);
  public static final Integer JAVA_CURRENCY_I  = new Integer(JAVA_CURRENCY);

  /////////////////////////////////////////////////////////////
  // IODevice Constants
  //

  public static final int IO_INPUTSTREAM    = 1;
  public static final int IO_OUTPUTSTREAM   = 2;
  public static final int IO_TEXTINPUTFILE  = 3;
  public static final int IO_TEXTOUTPUTFILE = 4;
  public static final int IO_RANDOMACCESS   = 5;
  public static final int IO_GZIPPED_INPUTFILE   = 6;
  public static final int IO_GZIPPED_OUTPUTFILE  = 7;
  public static final int IO_GZIPPED_TEXTINPUTFILE  = 8;
  public static final int IO_GZIPPED_TEXTOUTPUTFILE = 9;

  /////////////////////////////////////////////////////////////
  // DBConnect Constants
  //

  public static final String OPTION_JDBC_URL = "?jdbc?url";
  public static final String OPTION_JDBC_USERNAME = "?jdbc?username";
  public static final String OPTION_JDBC_PASSWORD = "?jdbc?password";

  /////////////////////////////////////////////////////////////
  // System-wide Object Names
  //

  public static final String DEFAULT_HIBERNATE_NAME = "$$hib";
  public static final String DEFAULT_CONNECTION_NAME = "$$con";
  public static final String SYS_NAME   = "$$sys";
  public static final String ARGS_NAME  = "$$args";
  public static final String PARSER_NAME = "$$parser";
  public static final String CONTEXT_NAME  = "$$context";
  public static final String ANNOTATION_NAME  = "$$annotation";
  public static final String TIMER_NAME = "$$timer";
  public static final String FS_RESULT_NAME = "$$fs_result";
  public static final String LOCAL_NAME = "$$local";
  public static final String LOCALTEXT_NAME = "$$localText";
  public static final String BSF_NAME = "$$bsf";
  public static final String LABEL_SCHEDULE = "_?schedule?";
  public static final String SENDMAIL_NAME = "_?sendmail?";
  public static final String THIS_NAME   = "$_";
  public static final String PARENT_NAME = "$__";
  public static final String THREAD_PREFIX = "?t?";
  public static final String LAMBDA_PREFIX = "?l?";
  public static final String NODE_PREFIX = "?n?";
  public static final String ADAPTER_PREFIX = "__adpt__";
  public static final String TEMPVAR_PREFIX = "$?_?";

  /////////////////////////////////////////////////////////////
  // XML/HTML/SGML Constants
  //

  public static final String DEFAULT_NS_SYMBOL = ">DEFNS";
  public static final String XML_EVENT_PREFIX  = ">";
  public static final String ANY_TAG_SYMBOL    = "<>";
  public static final String ANY_TEXT_SYMBOL   = "><";
  public static final String TYPE1_SYMBOL      = "<!";
  public static final String TYPE2_SYMBOL      = "<?";
  public static final String TYPE3_SYMBOL      = "<!--";
  public static final String TEXT_PREFIX       = "?";
  public static final int HTTP_HEAD = 1;
  public static final int HTTP_GET  = 2;
  public static final int HTTP_POST = 3;

  /////////////////////////////////////////////////////////////
  // FileSystem Action Constants
  //

  public static final int FS_NONE                 = 0;
  public static final int FS_LIST                 = 1;
  public static final int FS_LIST_COUNT           = 2;
  public static final int FS_LIST_SIZE            = 3;
  public static final int FS_LIST_COMPRESSED_SIZE = 4;
  public static final int FS_LIST_COUNTLINES      = 5;
  public static final int FS_LIST_COUNTWORDS      = 6;
  public static final int FS_REMOVE               = 7;
  public static final int FS_SETFILETIME          = 8;
  public static final int FS_SETREADONLY          = 9;
  public static final int FS_SETREADWRITE         = 10;
  public static final int FS_ADD_TO_CLASSPATH     = 11;
  public static final int FS_DO                   = 12;
  public static final int FS_COPY                 = 13;
  public static final int FS_MOVE                 = 14;
  public static final int FS_REMOVEDIR            = 15;
  public static final int FS_MAKEDIR              = 16;

  public static final int FS_LIST_BY_NONE = 0;
  public static final int FS_LIST_BY_NAME = 1;
  public static final int FS_LIST_BY_DATE = 2;
  public static final int FS_LIST_BY_SIZE = 3;
  public static final int FS_LIST_BY_EXT  = 4;

  public static final int FS_LIST_PER_NONE= 0;
  public static final int FS_LIST_PER_FILE= 1;

  /////////////////////////////////////////////////////////////
  // Miscellaneous
  //

  public static final int RULEENGINE_MAX_ITERATIONS = 400;
  public static final long FOREVER = 54 * 365 * 24 * 3600 * 1000; // over 50 years
  public static final int MAX_READ_FILE_LENGTH = 10 * 1024 * 1024;

  /////////////////////////////////////////////////////////////
  // Extraordinaly Database Types
  //

  public static final int PREPARED_STMT_CALL = 0x0FFF9FFF; // totally randomly chosen.

  /////////////////////////////////////////////////////////////
  // Oracle Types -- refer to oracle.jdbc.OracleTypes
  //

  public static final int ORACLE_NUMBER = java.sql.Types.NUMERIC;
  public static final int ORACLE_RAW    = java.sql.Types.BINARY;
  public static final int ORACLE_ROWID             = -8;  // OracleTypes.ROWID
  public static final int ORACLE_CURSOR            = -10; // OracleTypes.CURSOR
  public static final int ORACLE_BFILE             = -13; // OracleTypes.BFILE
  public static final int ORACLE_PLSQL_INDEX_TABLE = -14; // OracleTypes.PLSQL_INDEX_TABLE
  public static final int ORACLE_FIXED_CHAR        = 999; // OracleTypes.FIXED_CHAR
                                                          // don't want any hint of dependencies.

} // end of interface Consts.
