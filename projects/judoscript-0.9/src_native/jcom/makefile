#
# jcom.jar Makefile for Microsoft nmake.exe
# version 2.20

# あなたの環境に合わせてコメントアウトして下さい。
# adjust your env, and comment out
#COPY_DLL=copy jcom.dll d:\java\jdk1.3\bin
#COPY_JAR=copy jcom.jar d:\java\jdk1.3\lib\ext

# 使用例
# >nmake              ; jcom.jar, jcom.dll を作ります。 
# >nmake clean        ; 作業用ファイルを削除します。
# >nmake javah        ; JNI用ヘッダを作成します。
# >nmake javadoc      ; ドキュメントを作成します。
#
# usage
# >nmake              ; make jcom.jar, jcom.dll
# >nmake clean        ; delete workfile
# >nmake javah        ; make JNI header file
# >nmake javadoc      ; make document

#################################################### all
all: jcom.jar jcom.dll

#################################################### jcom.jar
CLASSDIR=build_tmp\jp\ne\so_net\ga2\no_ji\jcom
JAVADIR=jp\ne\so_net\ga2\no_ji\jcom
JFLAGS=-g -encoding SJIS -d build_tmp
CLASSES=\
	$(CLASSDIR)\GUID.class \
	$(CLASSDIR)\IUnknown.class \
	$(CLASSDIR)\IDispatch.class \
	$(CLASSDIR)\ITypeInfo.class \
	$(CLASSDIR)\ITypeLib.class \
	$(CLASSDIR)\IEnumVARIANT.class \
	$(CLASSDIR)\IPersist.class \
	$(CLASSDIR)\Com.class \
	$(CLASSDIR)\VariantCurrency.class \
	$(CLASSDIR)\JComException.class \
	$(CLASSDIR)\ReleaseManager.class
EXCEL_CLASSDIR=$(CLASSDIR)\excel8
EXCEL_JAVADIR=$(JAVADIR)\excel8
EXCEL_CLASSES=\
	$(EXCEL_CLASSDIR)\ExcelApplication.class \
	$(EXCEL_CLASSDIR)\ExcelWorkbooks.class \
	$(EXCEL_CLASSDIR)\ExcelWorkbook.class \
	$(EXCEL_CLASSDIR)\ExcelWorksheets.class \
	$(EXCEL_CLASSDIR)\ExcelWorksheet.class \
	$(EXCEL_CLASSDIR)\ExcelRange.class \
	$(EXCEL_CLASSDIR)\ExcelFont.class \
	$(EXCEL_CLASSDIR)\XlFileFormat.class \
	$(EXCEL_CLASSDIR)\XlPageBreak.class \
	$(EXCEL_CLASSDIR)\XlSaveAsAccessMode.class \
	$(EXCEL_CLASSDIR)\XlSheetType.class 

#jcom.jar: build_tmp $(CLASSES)
jcom.jar: build_tmp $(CLASSES) $(EXCEL_CLASSES)
	cd build_tmp
	jar c0Mf ..\jcom.jar jp ../manifest.mf
	cd ..	
	$(COPY_JAR)

build_tmp:
	mkdir build_tmp

$(CLASSDIR)\IDispatch.class: $(JAVADIR)\IDispatch.java
	javac $(JFLAGS) $**

$(CLASSDIR)\GUID.class: $(JAVADIR)\GUID.java
	javac $(JFLAGS) $**

$(CLASSDIR)\IUnknown.class: $(JAVADIR)\IUnknown.java
	javac $(JFLAGS) $**

$(CLASSDIR)\ITypeInfo.class: $(JAVADIR)\ITypeInfo.java
	javac $(JFLAGS) $**

$(CLASSDIR)\ITypeLib.class: $(JAVADIR)\ITypeLib.java
	javac $(JFLAGS) $**

$(CLASSDIR)\IEnumVARIANT.class: $(JAVADIR)\IEnumVARIANT.java
	javac $(JFLAGS) $**

$(CLASSDIR)\IPersist.class: $(JAVADIR)\IPersist.java
	javac $(JFLAGS) $**

$(CLASSDIR)\JComException.class: $(JAVADIR)\JComException.java
	javac $(JFLAGS) $**

$(CLASSDIR)\Com.class: $(JAVADIR)\Com.java
	javac $(JFLAGS) $**

$(CLASSDIR)\VariantCurrency.class: $(JAVADIR)\VariantCurrency.java
	javac $(JFLAGS) $**

$(CLASSDIR)\ReleaseManager.class: $(JAVADIR)\ReleaseManager.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelApplication.class: $(EXCEL_JAVADIR)\ExcelApplication.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelWorkbooks.class: $(EXCEL_JAVADIR)\ExcelWorkbooks.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelWorkbook.class: $(EXCEL_JAVADIR)\ExcelWorkbook.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelWorksheets.class: $(EXCEL_JAVADIR)\ExcelWorksheets.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelWorksheet.class: $(EXCEL_JAVADIR)\ExcelWorksheet.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelRange.class: $(EXCEL_JAVADIR)\ExcelRange.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\ExcelFont.class: $(EXCEL_JAVADIR)\ExcelFont.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\XlFileFormat.class: $(EXCEL_JAVADIR)\XlFileFormat.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\XlPageBreak.class: $(EXCEL_JAVADIR)\XlPageBreak.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\XlSaveAsAccessMode.class: $(EXCEL_JAVADIR)\XlSaveAsAccessMode.java
	javac $(JFLAGS) $**

$(EXCEL_CLASSDIR)\XlSheetType.class: $(EXCEL_JAVADIR)\XlSheetType.java
	javac $(JFLAGS) $**


#################################################### jcom.dll
CFLAGS=/I%java_home%\include /I%java_home%\include\win32 /c
OBJDIR=build_tmp\cpp
CPPDIR=cpp
OBJS=\
	$(OBJDIR)\IUnknown.obj \
	$(OBJDIR)\IDispatch.obj \
	$(OBJDIR)\ITypeInfo.obj \
	$(OBJDIR)\ITypeLib.obj \
	$(OBJDIR)\IEnumVARIANT.obj \
	$(OBJDIR)\IPersist.obj \
	$(OBJDIR)\Com.obj \
	$(OBJDIR)\callCom.obj \
	$(OBJDIR)\VARIANT.obj \
	$(OBJDIR)\jstring.obj \
	$(OBJDIR)\guid.obj \
	$(OBJDIR)\InvokeHelper.obj \

jcom.dll: $(OBJDIR) $(OBJS)
	@echo make dll...
	link /dll $(OBJS) /OUT:jcom.dll
	$(COPY_DLL)


$(OBJDIR):
	mkdir $(OBJDIR)

$(OBJDIR)\IUnknown.obj: $(CPPDIR)\IUnknown.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\IDispatch.obj: $(CPPDIR)\IDispatch.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\ITypeInfo.obj: $(CPPDIR)\ITypeInfo.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\ITypeLib.obj: $(CPPDIR)\ITypeLib.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\IEnumVARIANT.obj: $(CPPDIR)\IEnumVARIANT.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\IPersist.obj: $(CPPDIR)\IPersist.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\Com.obj: $(CPPDIR)\Com.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\callCom.obj: $(CPPDIR)\callCom.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\VARIANT.obj: $(CPPDIR)\VARIANT.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\jstring.obj: $(CPPDIR)\jstring.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\guid.obj: $(CPPDIR)\guid.cpp
	cl $(CFLAGS) /Fo$* $**

$(OBJDIR)\InvokeHelper.obj: $(CPPDIR)\InvokeHelper.cpp
	cl $(CFLAGS) /Fo$* $**

#################################################### clean
clean:
	@echo clear temporary directory...
	deltree /y build_tmp
	mkdir build_tmp

#################################################### docs
javadoc:
	@echo clear api directory...
	deltree /y docs\api
	mkdir docs\api
	javadoc -d docs\api -sourcepath . -encoding SJIS -splitindex @packages

#################################################### javah
javah:
	@echo make C-header files ...
	cd build_tmp
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.IUnknown
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.IDispatch
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.ITypeInfo
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.ITypeLib
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.IEnumVARIANT
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.IPersist
	javah -jni -d ../cpp jp.ne.so_net.ga2.no_ji.jcom.Com
	cd ..

