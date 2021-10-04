package com.judoscript.parser.helper;

import java.io.Reader;
import java.io.InputStream;
import com.judoscript.util.JavaCharStream;

public class JudoCharStream extends JavaCharStream implements com.judoscript.parser.CharStream
{
  public JudoCharStream(InputStream is) { super(is); unescapeUnicode = true; }
  public JudoCharStream(Reader r)       { super(r);  unescapeUnicode = true; }
}

