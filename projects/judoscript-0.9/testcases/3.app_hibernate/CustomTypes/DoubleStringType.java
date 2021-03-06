import java.io.Serializable;
import java.sql.*;
import net.sf.hibernate.*;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;
import net.sf.hibernate.CompositeUserType;

public class DoubleStringType implements CompositeUserType
{
  private static final int[] TYPES = { Types.VARCHAR, Types.VARCHAR };
  
  public int[] sqlTypes() { return TYPES; }
  
  public Class returnedClass() { return String[].class; }
  
  public boolean equals(Object x, Object y) {
    if (x==y) return true;
    if (x==null || y==null) return false;
    String[] sa1 = (String[])x;
    String[] sa2 = (String[])y;
    return sa1[0].equals(sa2[0]) && sa1[1].equals(sa2[1]);
  }
  
  public Object deepCopy(Object x) {
    if (x==null) return null;
    String[] result = new String[2];
    String[] input = (String[]) x;
    result[0] = input[0];
    result[1] = input[1];
    return result;
  }
  
  public boolean isMutable() { return true; }
  
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor sess, Object owner)
    throws HibernateException, SQLException
  {
    String first = (String) Hibernate.STRING.nullSafeGet(rs, names[0]);
    String second = (String) Hibernate.STRING.nullSafeGet(rs, names[1]);
    return ( first==null && second==null ) ? null : new String[] { first, second };
  }

  public void nullSafeSet(PreparedStatement st, Object val, int index, SessionImplementor sess)
    throws HibernateException, SQLException
  {
    String[] strings = (val==null) ? new String[2] : (String[])val;
    Hibernate.STRING.nullSafeSet(st, strings[0], index);
    Hibernate.STRING.nullSafeSet(st, strings[1], index+1);
  }
  
  public String[] getPropertyNames() { return new String[] { "s1", "s2" }; }

  public Type[] getPropertyTypes() { return new Type[] { Hibernate.STRING, Hibernate.STRING }; }

  public Object getPropertyValue(Object c, int p) { return ((String[])c)[p]; }

  public void setPropertyValue(Object c, int p, Object val) { ((String[])c)[p] = (String)val; }

  public Object assemble(Serializable cached, SessionImplementor sess, Object owner) {
    return deepCopy(cached);
  }

  public Serializable disassemble(Object val, SessionImplementor sess) {
    return (Serializable)deepCopy(val);
  }

} // end of class DoubleStringType.
