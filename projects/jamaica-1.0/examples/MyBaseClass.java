
public class MyBaseClass extends java.io.PrintWriter
{
  protected String fooBar = null;

  protected MyBaseClass(java.io.Writer out) { super(out); }

  public String getData() { return fooBar; }

  public void close() {
    System.out.println("MyBaseClass::close()");
    super.close();
  }

}
