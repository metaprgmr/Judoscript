// default package

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Foo implements Serializable {

    /** identifier field */
    private String key;

    /** nullable persistent field */
    private String[] custom;

    /** full constructor */
    public Foo(String[] custom) {
        this.custom = custom;
    }

    /** default constructor */
    public Foo() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String[] getCustom() {
        return this.custom;
    }

    public void setCustom(String[] custom) {
        this.custom = custom;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("key", getKey())
            .toString();
    }

}
