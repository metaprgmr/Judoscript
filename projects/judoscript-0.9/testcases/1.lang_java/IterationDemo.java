import java.util.*;

// A class with one iterator() method
public class IterationDemo
{
  int i = 0;
  public Iterator iterator() {
    i = 0;
    return new Iterator() {
      public boolean hasNext() { return i++<3; }
      public Object next() { return "iterator()-" + i; }
      public void remove() {}
    };
  }

  // A class with one method that returns an Iterator and takes no params
  public static class WithOneIterator {
    int i = 0;
    public Iterator foo() {
      i = 0;
      return new Iterator() {
        public boolean hasNext() { return i++<3; }
        public Object next() { return "foo()-" + i; }
        public void remove() {}
      };
    }
  }

  // A class with one method that returns an Enumeration and takes no params
  public static class WithOneEnumeration {
    int i = 0;
    public Enumeration bar() {
      i = 0;
      return new Enumeration() {
        public boolean hasMoreElements() { return i++<3; }
        public Object nextElement() { return "bar()-" + i; }
      };
    }
  }
}

