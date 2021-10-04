public class Foo1 extends Foo
{
  public String bar(int x) { return "int";  }
  public String bar(long x) { return "long";  }
  public String bar(double x) { return "double";  }
  public String bar(String x) { return "String"; }
  public String bar(java.sql.Date x) { return "SQLDate"; }
}

