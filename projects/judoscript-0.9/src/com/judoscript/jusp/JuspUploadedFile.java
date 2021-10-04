/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-20-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jusp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.fileupload.FileItem;

public class JuspUploadedFile
{
	FileItem item;

	JuspUploadedFile(FileItem fi) { item = fi; }

	public void delete() { item.delete(); }
	public byte[] get() { return item.get(); }
	public String getContentType() { return item.getContentType(); }
	public String getFieldName() { return item.getFieldName(); }
	public InputStream getInputStream() throws IOException { return item.getInputStream(); }
	public String getFileName() { return item.getName(); }
	public long getFileSize() { return item.getSize(); }
	public String getAsString() { return item.getString(); }
	public String getAsString(String encoding) throws UnsupportedEncodingException { return item.getString(encoding); }
	public void write(String file) throws Exception { item.write(new File(file)); }
	public void write(File file) throws Exception { item.write(file); }

} // end of class JuspUploadedFile.
