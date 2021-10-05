# Introduction to the Judoscript Language

## Language design
The design of the Judoscript language blends two distinct flavors. First and foremost, it is a general purpose scripting language like any modern programming languages, using dynamic typing and dot notation for accessing object members. Supporting both procedural and object-oriented paradigms, it sports static class declaration and inheritance. Various syntactic sugars and a versatile <code>print</code> statement typify its scripting language nature.

Judoscript is fully capable of scripting Java resources. Its static typing feature was introduced primarily for invoking overloaded Java methods.

The second flavor, which distances Judoscript from the rest of the crowd, is its embodiment of a number of native domain-specific statements, each carrying a very different look. Some return a value, others not. Most of these special statements can be used as regular statements anywhere in the program.

Expressions can be embedded in the domain-specific statements wherever appropriate. Expressions can also be embedded in textual literals like strings and XML values, effectively making them textual templates.

## Language features I: a potent scripting language

### The Base language
The Judoscript base language is almost identical to JavaScript for variable and constant declarations, flow control statements, and dot notation for accessing object members. In addition to the two C++-style comments, it has a third form, <code>/? ?/</code>, useful to disable a chunk of code. Function parameters can have default values, and invocation can take named parameter values. Like in JavaScript, functions are also objects and can be passed as values. Dynamic programming is available via the <code>eval</code> and <code>evalExternal</code> commands. [\[1\]](./judoscript.com-v2/books/judoscript-0.9/chapters/basic.html) [\[2\]](./judoscript.com-v2/books/judoscript-0.9/chapters/datatypes.html) [\[3\]](./judoscript.com-v2/books/judoscript-0.9/chapters/flow.html)

### Object oriented programming
Unlike JavaScript, user-defined classes are declared statically, but the data members are dynamic. Multiple inheritance is supported, and programmers should handle potential diamond inheritance issues. Class instances can be transposed to another type. [\[4\]](./judoscript.com-v2/articles/fxn_cls_etc.html)

<pre>
class Node
{
  constructor {
    assert(name != null);  // mandates that 'name' must be initialized
    children = [];
  }
  function addChild child { children.add(child); }
  function toString { return name; }
}
</pre>

### Code organization

Judoscript has a <code>!include</code> directive to include other scriptlets. This is the only mechanism to share code written in Judoscript.

The lack of support for Java-like ''package'' or ''module'' in other languages is intentional, in order to discourage creating libraries written in Judoscript. Reusable libraries are encouraged to be written in Java, and let Judoscript (or any Java scripting languages) to use.

## Language features II: Java scripting
Java scripting is a first-class citizen in Judoscript. Listed here are some of the most important Java scripting features. Refer to the documentation to see all the uses and examples. [\[5\]](./judoscript.com-v2/books/judoscript-0.9/chapters/java.html)

### Scripting Java Objects
<pre>
a = new java::java.util.HashMap;
a.put('date',      Date(2001,1,1));
a.put('integer',   1);
a.put('double',    10.01);
a.put('string',    'Hello, World!');
a.put('Dimension', new java::java.awt.Dimension(10,20));
for k in a.keys() {
  println k:>10, ' = ', a.get(k);
}

props = new java::Properties(
          a.b.c.d.e.f.g = 'a thru g',
          h.i.j.k.l.m.n = 'h thru n'
        );
println props;
</pre>

### Aliasing Java static methods
The capability to alias Java static methods as Judoscritp functions is an important mechanism, responsible for many built-in functions.

<pre>
function prop for java::System.getProperty();
function now  for java::System.currentTimeMillis();
function rt   for java::Runtime.getRuntime();

println now(), nl;
println prop('java.class.path'), nl;
rt().gc();
</pre>

### Using Java classes as values
Java classes are also object values, and can be assigned to variables or constants.

<pre>
const #Types = java::java.sql.Types;
fields = [ 'BIT', 'TINYINT', 'SMALLINT', 'INTEGER', 'BIGINT', 'FLOAT', 'REAL',
  'DOUBLE', 'NUMERIC', 'DECIMAL', 'CHAR', 'VARCHAR', 'LONGVARCHAR', 'DATE',
  'TIME', 'TIMESTAMP', 'BINARY', 'VARBINARY', 'LONGVARBINARY', 'NULL', 'OTHER',
  'JAVA_OBJECT', 'DISTINCT', 'STRUCT', 'ARRAY', 'BLOB', 'CLOB', 'REF'
];

println 'JDBC types in Java ', sysProperty('java.version'), ':';
for fld in fields {
  println fld:<13, ': ', #Types.(fld);
}
</pre>

### Extending Java classes and implementing Java interfaces
It is possible to define Judoscript classes that extend Java classes or implement Java interfaces.

<pre>
class MyHashMap extends java::HashMap
{
  // Test overriding an existing method
  int hashCode() { return super.hashCode() ^ 1; }

  // A new method.
  String[] getKeys() {
    arr = [];
    for x in this { arr.add(x); }
    return arr.toStringArray();
  }

  // A new method.
  Object[] getValues() {
    arr = [];
    for x in values() { arr.add(x); }
    return arr.toObjectArray();
  }
}
</pre>

Anonymous Java interface adapters can be created and used.

<pre>
import java.awt.Frame;
import java.awt.event.*;

f = new java::Frame;
f.addWindowListener(
  new java::WindowListener {
    void windowClosing(WindowEvent e) { exit 0; }
  }
);
f.setVisible(true);
</pre>

## Language features III: special domain supports
This part of the language is what makes Judoscript unique. Each major domain support will be touched on briefly, usually with examples.

### JDBC scripting
JDBC scripting is a central feature of Judoscript, as well as a key motivation for the language itself. It makes database operations intuitive and conveient. [\[6\]](./judoscript.com-v2/books/judoscript-0.9/chapters/jdbc.html)

Below is an example that connects to database and runs some SQL statements.

<pre>
db::connect to 'jdbc:oracle:thin:@dbsvr:1521:dbname', 'user', 'pass';
if tableExists('EMP') {
  db::sql: DROP TABLE emp;
}
db::sql {
  CREATE TABLE emp(emp_no     INTEGER PRIMARY KEY,
                   first_name VARCHAR(100),   // First Name
                   last_name  VARCHAR(100),   // Last Name
                   birth_date DATE,           -- Birth Date
                   salary     NUMBER);        -- Salary

  CREATE INDEX emp_fname ON emp(first_name);
  CREATE INDEX emp_lname ON emp(last_name);

  INSERT INTO emp(emp_no,first_name,last_name,birth_date,salary)
  VALUES( 100, 'Jim', 'Malone', to_date('1-3-1954','dd-mm-yyyy'), 86500.0);
}
println 'db::sql result: ', $_;
disconnect();
</pre>

Using stored procedures is through the <code>db:any</code> statement.

<pre>
db::any [[*
  CREATE PROCEDURE test_proc(
    param_io IN OUT NUMBER,
    param_i  IN     VARCHAR,
    param_o  OUT    VARCHAR)
  AS BEGIN
    param_o := param_i;
    IF param_io IS NOT NULL THEN
      param_io := param_io + 1;
    ELSE
      param_io := -1000;
    END IF;
  END;
*]];
</pre>

The more interesting usage is the parameterized queries and updates. Queries and updates can be run directly or preparedly.

<pre>
db::prepare ins:
  INSERT INTO emp( emp_no, first_name, last_name, birth_date, salary  )
           VALUES( :empNo, :firstName, :lastName, :birthDate, :salary )
;

for row in rows {
  db::sql ins with @empNo:Integer  = id++,
                   @firstName      = row[0],
                   @lastName       = row[1],
                   @birthDate:Date = row[2],
                   @salary:Number  = row[3];
}
</pre>

Below is a query example.
<pre>
db::query qry:
  SELECT emp_no, first_name, last_name, birth_date, salary
  FROM emp
;

println 'ID   First-Name  Last-Name    Salary    Birth-Date', nl,
        '---  ----------  -----------  --------  ----------';
while qry.next() {
  println qry[1].int()   :<5,
          qry.first_name :<12,
          qry.last_name  :<12,
          qry.salary     :*7.1, '  ',
          qry.birth_date.fmtDate('yyyy-MM-dd');
}
</pre>

### SGML and XML document processing
SGML is the generic hypertext format that includes HTML and XML. There are two APIs for processing XML documents: [[Simple API for XML]] (or SAX), and [[Document Object Model]] (or DOM). SAX processing scans the whole text as a stream, and fires events for each encountered tag or text segment. Judoscript has a cute little statement that makes use of this feature effectively for XML and SGML data. [\[7\]](./judoscript.com-v2/articles/xml.html) [\[8\]](./judoscript.com-v2/articles/http_html.html)

The next example shows the essence of HTML scraping. With this, writing a web site crawler is not far from reality!

<pre>
do 'http://www.judoscript.org' as sgml {
    &lt;a>:  if $_.href != null { println 'Found a hyperlink:', $_.href; }
    &lt;p>:  println 'Found a paragraph tag.';
    TEXT: println 'Found some text:', $_.length();
}
</pre>

The following example processes the XML data that is appended at the end of the script.
<pre>
do $$local as xml
{
&lt;book>:       println ($_.hardcover=='true')?"Hard":"Soft", '-cover Book ------------';
&lt;date>TEXT:   println '  Date: ', $_, ' (', $_.type, ')';
&lt;title>TEXT:  println ' Title: ', $_;
&lt;author>TEXT: println 'Author: ', $_;
&lt;isbn>TEXT:   println '  ISBN: ', $_;
}
EndScript -------------------------------------------------------
&lt;booklist>
 &lt;book>
  &lt;title> UNIX in a Nutshell &lt;/title>
  &lt;author> Daniel Gilly &lt;/author>
  &lt;publisher> O&apos;Reilly &amp; Associates, Inc. &lt;/publisher>
  &lt;isbn> 1-56592-001-5 &lt;/isbn>
  &lt;date type="first edition"> 1986-12 &lt;/date>
  &lt;date type="second edition"> 1992-06 &lt;/date>
 &lt;/book>
 &lt;book hardcover="true">
  &lt;title> Advanced Programming in the UNIX Environment &lt;/title>
  &lt;author> Richard Stevens &lt;/author>
  &lt;publisher> Addison-Wesley &lt;/publisher>
  &lt;isbn> 0-201-56317-7 &lt;/isbn>
  &lt;date type="copyright"> 1993 &lt;/date>
  &lt;date type="twelfth printing"> 1996-12 &lt;/date>
 &lt;/book>
&lt;/booklist>
</pre>

Judoscript also supports DOM and XSLT processing of XML documents.

### Apache Ant scripting
Apache Ant has been _the_ build system for Java for a long time; companies and developers have been creating many extensions called _Task_s, in Ant parlance, and the Ant ecosystem becomes a treasure trove of all kinds of utilities, either general purpose or proprietary. Judoscript has included a mechanism to directly invoke any Ant tasks. Below is an example. [\[9\]](./judoscript.com-v2/books/judoscript-0.9/chapters/ant.html)

<pre>
a = 'ants';
anttask::&lt;echo message="Hello, ${a}!"/>
anttask::&lt;echo>Hello, more ${a}!&lt;/echo>
</pre>

There are ways to run native executables in Judoscript, but you can also resort to Ant scripting, as shown below.

<pre>
src = '.';
anttask::&lt;exec dir="${src}" executable="cmd.exe" os="Windows 2000" output="dir.txt">
  &lt;arg line="/c dir"/>
&lt;/exec>
</pre>

### Unified file system and archive scripting

File manipulation is one of the most common tasks for information workers. Judoscript strives to provide a consistent experience dealing with files in both file systems and archives such as ZIP files and TAR balls. [\[10\]](./judoscript.com-v2/books/judoscript-0.9/chapters/fs.html)

The following examples inspects files in a ZIP archive.

<pre>
listFiles '*' in 'awebapp.zip' dirOnly;
for x in $_ {
  // get status for each directory
  listFiles '*.java, *.jj' in x recursive size compressedSize;
  println $_[0]:>8, '  ', $_[1]:>8, '  ', x;
}
</pre>

The next example copies files into a ZIP archive. By slightly changing the expressions, you can copy files to another location in the file system.

<pre>
copy '*.java, *.properties' in 'C:/temp/classes/' recursive
     into 'judo.jar' store manifest
[[*
  Manifest-Version: 1.0
  Main-Class: judo
  Created-By: James Jianbo Huang (c) 2001-(* #year *)
*]]
;
</pre>

Another example for ZIP with finer control.

<pre>
zf = createZip('~/archives/work-'+Date().fmtDate('yyyyMMdd')+'.zip');

copy '*' in 'c:/judo/src/' except '*/alfa*, */beta*, */save/*'
  recursive noHidden echo
  into zf under 'src/';

copy '*' in 'c:/judo/docs/' except '*/alfa*, */beta*, */save/*'
  recursive noHidden echo
  into zf under 'docs/';

copy '*' in 'c:/judo/examples/' except '*/alfa*, */beta*, */save/*'
  recursive noHidden echo
  into zf under 'examples/';

zf.close();
</pre>

And you can copy internet resources to file systems or archives, shown below.

<pre>
tar = createTar('yahoo_comp.tar.gz');
zip = createZip('yahoo_comp.zip');
urls = [
  'http://dir.yahoo.com/Computers_and_Internet/index.html',
  'http://dir.yahoo.com/Computers_and_Internet/Internet/index.html',
  'http://dir.yahoo.com/Computers_and_Internet/Internet/WAIS/index.html'
];
for u in urls {
  copy u into tar keepDirs;
  copy u into zip keepDirs;
}
tar.close();
zip.close();
</pre>

### SOAP web service scripting

In the times of Judoscript, the SOAP web service was the industry standard for remote invocations before the RESTful API emerged. Judoscript makes it easy to consume SOAP web services. [\[11\]](./judoscript.com-v2/books/judoscript-0.9/chapters/wsdl.html)

<pre>
svc = wsdl::'http://coldrooster.com/dhtmldude/mathservice.asmx?WSDL';
println '3 + 2 = ', svc.Add(3, 2);
println '3 - 2 = ', svc.Subtract(3, 2);
println '3 * 2 = ', svc.Multiply(3, 2);
println '3 / 2 = ', svc.Divide(3, 2);
</pre>

### Other application features

Judoscript has all kinds of programming goodies to cover most of the everyday tasks for information workers, such as executing OS native executables, sending E-mails, running scheduled jobs, accessing internet resources via HTTP, SSH, SCP, launching your own HTTP server, etc. [\[12\]](./judoscript.com-v2/articles/sched_exec_mail.html)

The Microsoft COM scripting capability opens door to the Microsoft Office documents, using their DOM to access and manipulate data therein, and integrate with other data stores and sources. [\[13\]](./judoscript.com-v2/articles/activex.html)

## Implementation

The Judoscript language grammar and its parser are done with the JavaCC tool. Many a complicated domain-specific statement constitutes a sub-language of its own that warrants an embedded sub-parser. Other such statements and some syntactic sugars require native methods in the JavaCC grammar file. Because of these complexities, the parser directly produces the internal representation of the program, readily executable by the language engine.

There are many supporting libraries included in the language runtime; some are from third parties, others are developed internally.

## References

1. [Judoscript Basic Program Structure](./judoscript.com-v2/books/judoscript-0.9/chapters/basic.html)
2. [Judoscript Basic Data Types and Expressions](./judoscript.com-v2/books/judoscript-0.9/chapters/datatypes.html)
3. [Judoscript Basic Flow Control Statements](./judoscript.com-v2/books/judoscript-0.9/chapters/flow.html)
4. [Judoscript Functions, Classes, Threads and Eval](./judoscript.com-v2/articles/fxn_cls_etc.html)
5. [Judoscript Java Scripting](./judoscript.com-v2/books/judoscript-0.9/chapters/java.html)
6. [Judoscript Introduction to JDBC Scripting](./judoscript.com-v2/books/judoscript-0.9/chapters/jdbc.html)
7. [Judoscript XML Scripting](./judoscript.com-v2/articles/xml.html)
8. [Judoscript HTML and SGML Scripting](./judoscript.com-v2/articles/http_html.html)
9. [Judoscript Ant Scripting and Scripting Ant](./judoscript.com-v2/books/judoscript-0.9/chapters/ant.html)
10. [Judoscript File System and Archives](./judoscript.com-v2/books/judoscript-0.9/chapters/fs.html)
11. [Judoscript SOAP Web Service Scripting](./judoscript.com-v2/books/judoscript-0.9/chapters/wsdl.html)
12. [Judoscript Schedule, Execute, E-Mail, SSH, SCP](./judoscript.com-v2/articles/sched_exec_mail.html)
13. [Judoscript COM Scripting](./judoscript.com-v2/articles/activex.html)


## Quick Links

* [The Story and Philosophy of Judoscript](./story_philosophy.md)
* [Reflections on _the Judoscript Idea_](./judoscript_idea_reflections.md)
* Live [reconstructed version 2 judoscript.com site](judoscript.com-v2)
* Live [reconstructed version 1 judoscript.com site](judoscript.com-v1)
* [Building the language software and website](./build_judoscript_website.md)
* The [Home page](./)
