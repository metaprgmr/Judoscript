/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-22-2002  JH   Fixed the problem of SGML comment containing '>'.
 * 06-23-2002  JH   Parses <.../>, too; Markup has a selfClosed flag.
 * 07-26-2004  JH   Use FSA for parsing.
 * 07-26-2004  JH   Parse <![CDATA[ ... ]]> as text.
 * 11-14-2004  JH   Added JSP parsing.
 * 11-14-2004  JH   Uses the JavaCC generated class of
 *                  com.judoscript.parser.JavaCharStream.
 * 07-30-2005  JH   Added rushToTag() and also did special treatment
 *                  for <script>.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.util.Stack;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class MarkupParser
{
  boolean attr_casesens;
  boolean ignoreText;
  boolean ignoreEmptyText;
  boolean trimText;
  boolean wantSpecial;
  boolean unescape = false;
  transient boolean lastWasScript = false; // this mechanism may be expanded to handle
                                           // arbitrary tag pairs, not just <script>.

  JavaCharStream mureader;
  StringBuffer read_buf;

  public MarkupParser(Object inputStream, boolean ignoreText, boolean trimText,
                      boolean ignoreEmptyText, boolean attr_casesens)
  {
    if (inputStream instanceof InputStream)
      this.mureader = new JavaCharStream((InputStream)inputStream);
    else if (inputStream instanceof Reader)
      this.mureader = new JavaCharStream((Reader)inputStream);
    else
      this.mureader = (JavaCharStream)inputStream; // default

    this.ignoreText      = ignoreText;
    this.trimText        = trimText;
    this.ignoreEmptyText = ignoreEmptyText;
    this.attr_casesens   = attr_casesens;
    wantSpecial          = false;
    read_buf             = new StringBuffer(1024);
  }

  public void setUnescape(boolean set) { unescape = set; }
  public void setWantSpecial(boolean set) { wantSpecial = set; }
  public void setAttrCaseSens(boolean sensitive) { attr_casesens = sensitive; }

  /**
   * Reads a single element per call. It can be a markup or a text string.
   * <em>Special treatment for <code>&lt;script&gt;</code> tags: contents
   * between the opening and closing tags are parsed differently to
   * accomodate <code>&lt;</code> occurrences. The only catch now is that
   * <code>&lt;script&gt;</code> can not happen anywhere within the code
   * such as string literals.</em>
   *
   *@return a Markup object.
   *        If is a markup, its "name" key is available; for plain text,
   *        no "name" key, and "text" key contains the text.
   *        Returns null if no more tags nor text.
   */
  public Markup next() throws IOException {
    Markup markup;

    if (lastWasScript) {
      lastWasScript = false;
      if (ignoreText) {
        rushToTag("/script");
        return next();
      } else {
        markup = new Markup(true);
        markup.setText(rushToTag("/script", true));
        markup.setIsText();
        return markup;
      }
    }

    int state = 0;
    read_buf.setLength(0);

    markup = new Markup(attr_casesens); // , mureader.getLine(), mureader.getColumn());

    // Parse with an FSA: see "fsa_markup.gif".

    try {
      char ch = readChar();
      markup.setRow(mureader.getLine());
      markup.setColumn(mureader.getColumn());

      read_buf.append(ch);
      if (ch == '<')
        state = 2;
      else
        state = 1;

lp:   while (true) {
        ch = readChar();

        switch(state) {
        case 1:  if (ch == '<') {
                   state = 0;
                   break lp;     // Got: TEXT
                 }
                 break; // append

        case 2:  if (Character.isLetter(ch) || ch=='_' || ch=='/') {
                   state = 3;
                 } else if (ch == '?') {
                   state = 7;
                 } else if (ch == '!') {
                   state = 9;
                 } else {
                   state = 1; // not a tag any more; treat as text.
                 }
                 break; // append

        case 3:  switch (ch) {
                 case '/':  state = 4; break;
                 case '"':  state = 5; break;
                 case '\'': state = 6; break;
                 case '>':  break lp;   // Got: <...>
                 default:   break;
                 }
                 break; // append

        case 4:  if (ch == '>') {
                   break lp;    // Got: <...>
                 } else {
                   state = 3;
                 }
                 break; // append

        case 5:  if (ch == '"') {
                   state = 3;
                 }
                 break; // append

        case 6:  if (ch == '\'') {
                   state = 3;
                 }
                 break; // append

        case 7:  if (ch == '?') {
                   state = 8;
                 }
                 break; // append

        case 8:  if (ch == '>') {
                   break lp;    // Got: <?...?>
                 } else {
                   state = 7;
                 }
                 break; // append

        case 9:  if (ch == '>') {
                   break lp;   // Got: <!...>
                 } else if (ch == '[') {
                   state = 11;
                 } else if (ch == '-') {
                   state = 20;
                 }
                 break; // append

        case 10: if (ch == '>') {
                   break lp;   // Got: <!...>
                 }
                 break; // append

        case 11: if (ch == 'C') {
                   state = 12;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 12: if (ch == 'D') {
                   state = 13;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 13: if (ch == 'A') {
                   state = 14;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 14: if (ch == 'T') {
                   state = 15;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 15: if (ch == 'A') {
                   state = 16;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 16: if (ch == '[') {
                   state = 17;
                 } else {
                   state = 10;
                 }
                 break; // append

        case 17: if (ch == ']') {
                   state = 18;
                 }
                 break; // append

        case 18: if (ch == ']') {
                   state = 19;
                 } else {
                   state = 17;
                 }
                 break; // append

        case 19: if (ch == '>') {
                   break lp;    // Got: <![CDATA[...]]>
                 } else {
                   state = 17;
                 }
                 break; // append

        case 20: if (ch == '-') {
                   state = 21;
                 } else if (ch == '>') {
                   break lp;    // Got: <!...>
                 } else {
                   state = 10;
                 }
                 break; // append


        case 21: switch (ch) {
                 case '-':  state = 24; break;
                 }
                 break; // append

        case 24: if (ch == '-') {
                   state = 25;
                 } else {
                   state = 21;
                 }
                 break; // append

        case 25: switch (ch) {
                 case '>': break lp;    // Got: <!--...-->
                 case '-': break;
                 default:  state = 21; break;
                 }
                 break; // append
        }
        read_buf.append(ch);
      }
      if (state != 0)
        read_buf.append('>');
      else // text
        mureader.backup(1);
    } catch (IOException e) {
      if (read_buf.length() == 0)
        return null;
      markup.setIsText();
      markup.setText(read_buf.toString());
      read_buf.setLength(0);

      return markup;
    }

    String s = read_buf.toString();
    read_buf.setLength(0);
    boolean isText = false;
    int end;

    // Now, s is either <...>, <!...>, <?..?>, <![CDATA[...]]> or TEXT.
    if (s.startsWith("<![CDATA[") && s.endsWith("]]>")) {
      //
      // <![CDATA[...]]>
      //
      s = s.substring(9, s.length() - 3);
      markup.setIsText();
    } else {
      if (s.startsWith("<!") && s.endsWith(">")) {
        //
        // <!...>
        //
        if (!wantSpecial)
          return next();
        
        if (s.startsWith("<!--") && s.endsWith("-->") && s.length() >= 7) {
          markup.setIsSpecial("<!--", "-->");
          markup.setText(s.substring(4, s.length()-3));
        } else {
          markup.setIsSpecial("<!", ">");
          markup.setText(s.substring(2, s.length()-1));
        }

      } else if (s.startsWith("<?") && s.endsWith("?>")) {
        //
        // <?...?>
        //
        if (!wantSpecial)
          return next();

        markup.setIsSpecial("<?", "?>");
        markup.setText(s.substring(2, s.length()-2));

      } else if (s.startsWith("<") && s.endsWith(">")) {
        //
        // <...>
        //

        int len = 1;

        if (s.endsWith("/>")) {
          markup.setSelfClosed();
          ++len;
        } else if (s.startsWith("</")) {
          markup.setClosed();
        }
        s = s.substring(0, s.length() - len);
        len = s.length();

        for (end=1; (end<len) && !isWhite(s.charAt(end)); end++);
        // Got the markup name
        String name = s.substring(1,end);
        if (!attr_casesens)
          name = name.toLowerCase();
        markup.setName(name);

        getAttrs(markup, s.substring(end).trim());

      } else {
        //
        // TEXT
        //
        markup.setIsText();
      }
    }

    if (markup.isText()) { // for both <![CDATA[...]]> and TEXT.
      if (trimText)
        s = s.trim();
      if (unescape)
        s = StringEscapeUtils.unescapeHtml(s);
      if (ignoreText || ignoreEmptyText && StringUtils.isBlank(s))
        return next();

      markup.setName(s);
    }

    if (markup.isA("script") && !markup.isClosed())
      lastWasScript = true;

    return markup;
  }

  public void rushToTag(String tagName) throws IOException {
     rushToTag(new String[]{ tagName }, false);
  }

  public String rushToTag(String tagName, boolean needText) throws IOException {
     return rushToTag(new String[]{ tagName }, needText);
  }

  public void rushToTag(String[] tagNames) throws IOException {
     rushToTag(tagNames, false);
  }

  /**
   * Rushes to the intended tag and returns any text prior to the tag
   * from the current position. The tag itself is not returned and
   * will be immediately available for a <code>next()</code> call.
   * If the tag does not exist, the whole document will be passed.
   */
  public String rushToTag(String[] tagNames, boolean needText) throws IOException {
    int cnt = (tagNames==null) ? 0 : tagNames.length;
    if (cnt == 0)
      return null;

    int i;
    if (!attr_casesens) {
      for (i=0; i<cnt; ++i)
        tagNames[i] = tagNames[i].toLowerCase();
    }

    int state = 0;
    read_buf.setLength(0);

    // Parse with an FSA.
    try {
lp:   while (true) {
        char ch = readChar();

        switch(state) {
        case 0:
          switch(ch) {
// -- For javascript regexps, " and ' may occur and screw up the parsing.
// -- So for now, having the intended tag(s) enclosed in string and regexp
// -- literals is forbidden.
/*
          case '"':  state = 1; break;
          case '\'': state = 2; break;
*/

          case '<': // process right here
            for (i=0; i<cnt; ++i) {
              String x = tagNames[i];
              int len = x.length();
              int j;
              for (j=0; j<len; ++j) {
                char c = readChar();
                if (!attr_casesens)
                  c = Character.toLowerCase(c);
                if (c != x.charAt(j))
                  break;
              }
              if (j >= len) { // Got it!
                mureader.backup(len + 1);
                break lp;
              } else {
                mureader.backup(j + 1);
              }
            }
            // Haven't got it.
            // Now we have effectively skipped that '<'.
            ch = '<';
            break;
          }
          break;
/*
        case 1:  // In ".." string.
          if (ch == '"') {
            state = 0;
          } else if (ch == '\\') {
            read_buf.append('\\');
            ch = readChar();
          }
          break;

        case 2:  // In '..' string.
          if (ch == '\'') {
            state = 0;
          } else if (ch == '\\') {
            read_buf.append('\\');
            ch = readChar();
          }
          break;
*/
        }
        if (needText)
          read_buf.append(ch);
      }
    } catch(IOException ioe) {
      // Expect this when eof is encountered.
    }

    if (needText) {
      String ret = read_buf.toString();
      read_buf.setLength(0);
      return ret;
    } else {
      return null;
    }
  }

  /**
   * Reads a single JSP-like tag. The tags read are:
   * <ul><li><code>&lt;%= %></code>
   *     <li><code>&lt;%@ %></code>
   *     <li><code>&lt;%! %></code>
   *     <li><code>&lt;%-- %></code>
   *     <li><code>&lt;% %></code>
   * </ul>
   *
   *@return a Markup object of type TYPE_SPECIAL.
   *        The Markup.specialTagName is the name, and the
   *        Markup.name holds the text for relevant tags.
   */
  public Markup nextJsp() throws IOException {
    char ch;
    int state = 0;
    read_buf.setLength(0);

    Markup markup = new Markup(true);

    markup.type = Markup.TYPE_SPECIAL;
    markup.endTag = "%>";

    // Parse with an FSA: see "fsa4jsp.judo".
    try {
loop:
      while(true) {
        ch = readChar();

        switch(state) {
        case 0:
          if (ch == '<') {
            state=1;
            continue loop;
          }
          break; // append, for text.
        case 1:
          if (ch == '%') {
            if (read_buf.length() == 0) {
              state=2;
              continue loop; // we are in!
            } else {
              markup.setIsText();
              markup.setName(read_buf.toString());
              mureader.backup(2); // get ready for the tag.
              return markup;
            }
          } else {
            read_buf.append('<');
            state=0;
          }
          break; // append, for text.
        case 2:
          switch(ch) {
          case '%' : state=3; continue loop;
          case '"' : state=4; break;
          case '\'': state=6; break;
          }
          break; // append
        case 3:
          if (ch == '>') {
            break loop;
          } else {
            read_buf.append('%');
            state=2;
          }
          break;
        case 4:
          switch(ch) {
          case '"' : state=2; break;
          case '\\': state=5; break;
          }
          break; // append
        case 5:
          state=4;
          break; // append
        case 6:
          switch(ch) {
          case '\n':
          case '\r':
          case '\'': state=2; break;
          case '\\': state=7; break;
          }
          break;
        case 7:
          state=6;
          break; // append
        }
        read_buf.append(ch);
      }

      String s = read_buf.toString();
      read_buf.setLength(0);

//      if (StringUtils.isBlank(s))
//        return null;

      ch = s.charAt(0);
      if (ch=='@' || ch=='!' || ch=='=') {
        markup.specialTagName = "<%" + ch; // '@' case may be append with a name.
        s = s.substring(1);
      } else if (ch=='-' && s.startsWith("--") && s.endsWith("--")) {
        markup.specialTagName = "<%--";
        s = s.substring(2, s.length()-2);
      } else {
        markup.specialTagName = "<%";
      }

      if (ch != '@') {
        markup.name = s;
      } else {
        markup.type = Markup.TYPE_SPECIAL;
        markup.specialTagName = "<%@";
        s = s.trim();
        int idx = s.indexOf("=");
        if (idx < 0) {
          markup.name = s;
        } else {
          String x = s.substring(0, idx).trim();
          int ptr = x.length() - 1;
          while ((ptr>=0) && !isWhite(x.charAt(ptr))) // go back to the first non-whitespaces
            --ptr;
          if (ptr > 0) {
            markup.name = x.substring(0, ptr).trim();
            getAttrs(markup, s.substring(ptr));
          } else { // like <%@abcde= %>
            markup.name = x.substring(0, idx).trim();
          }
        }
      }

    } catch(IOException e) {
      if (read_buf.length() == 0)
        return null;
      markup.setIsText();
      markup.setName(read_buf.toString());
    }

    return markup;
  }

  char readChar() throws IOException {
    try {
      char c = mureader.readChar();
      if (c == 0)
        throw new IOException();
      return c;
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new IOException();
    }
  }

  void getAttrs(Markup markup, String s) {
    int ptr = 0;
    int len = s.length();
    
    // The challenge is to handle attribute values with and without quotes.
    while (ptr < len) {
      while ((ptr<len) && isWhite(s.charAt(ptr))) ++ptr; // skip whitespaces
      if (ptr >= len)
        break;
      // looking for name
      int start = ptr;
      String name;
      while ((ptr<len) && (s.charAt(ptr)!='=')) ++ptr;
      if (ptr >= len) { // no '=' till ptr.
        name = s.substring(start).trim();
        if (!attr_casesens)
          name = name.toLowerCase();
        markup.addAttr(name, null);
        break;
      }
      // ptr pointing to '='.
      name = s.substring(start, ptr++).trim();
      if (!attr_casesens)
        name = name.toLowerCase();
      while ((ptr<len) && isWhite(s.charAt(ptr))) ++ptr; // skip whitespaces
      if (ptr >= len) { // nothing for this '='.
        markup.addAttr(name, null);
        break;
      }
      char qch = s.charAt(ptr);
      if ((qch == '"') || (qch == '\'')) { // got quote; this is easier.
        start = ++ptr;
        while ((ptr<len) && (s.charAt(ptr) != qch)) ++ptr;
        if (ptr >= len) {
          markup.addAttr(name, s.substring(start));
          break;
        }
        markup.addAttr(name, s.substring(start, ptr++));
        continue;
      }
      // So, no quote after '='.
      // Look for the next '=' then trace back for the name.
      // This means <a href=?q=bad> is illegal -- it must be quoted.
      start = ptr;
      while ((ptr<len) && (s.charAt(ptr) != '=')) ++ptr;
      if (ptr >= len) {
        String s1 = s.substring(start).trim();
        markup.addAttr(name, s1);
        break;
      }
      --ptr;
      while ((ptr>start) && isWhite(s.charAt(ptr))) --ptr; // skip whitespaces
      while ((ptr>start) && !isWhite(s.charAt(ptr))) --ptr;
      if (ptr > start)
        markup.addAttr(name, s.substring(start, ptr).trim());
    }
  }

  final static boolean isWhite(char c) { return Character.isWhitespace(c); }

  public static Markup parseXMLTree(Object inputStream) throws IOException {
    MarkupParser mp = new MarkupParser(inputStream, false, false, true, true);

    Markup root = mp.next();
    if (root == null || !root.isTag())
      throw new IOException("Expect an XML/SGML tag.");

    Stack nodeStack = new Stack();
    if (!root.isClosed())
      nodeStack.push(root);

    while (!nodeStack.isEmpty()) {
      Markup m = mp.next();
      if (m == null)
        break;
      if (m.isSelfClosed()) { // includes text.
        ((Markup)nodeStack.peek()).addChild(m);
      } else if (m.isClosedTag()) {
        nodeStack.pop();
      } else if (m.isTag()) {
        ((Markup)nodeStack.peek()).addChild(m);
        nodeStack.push(m);
      }
    }

    return root;
  }

/*
  public static void main(String[] args) {
    if (args.length <= 0) {
      System.out.println("Prints out all the tags.");
      System.out.println("Usage: java com.judoscript.util.MarkupParser filename");
      System.exit(0);
    }

    try {
      Markup t = MarkupParser.parseXMLTree(new ReaderMarkupReader(new FileReader(args[0])));
      t.printTree();

//    MarkupParser parser = new MarkupParser(new FileReader(args[0]), false, false, true, false);
//    parser.wantSpecial = true;
//    while (true) {
//      Markup mu = parser.next();
//      if (mu == null)
//        break;

//      System.out.print(mu.toString());
//      System.out.print("[");
//      System.out.print(mu.getRow());
//      System.out.print(",");
//      System.out.print(mu.getColumn());
//      System.out.println("]");
//    }

    } catch(Exception e) { e.printStackTrace(); }
  }
*/

} // end of class MarkupParser.
