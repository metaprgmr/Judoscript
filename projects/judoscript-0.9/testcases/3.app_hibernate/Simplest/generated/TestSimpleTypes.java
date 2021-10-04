// default package

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class TestSimpleTypes implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Float theFloat;

    /** nullable persistent field */
    private Byte theByte;

    /** nullable persistent field */
    private Character theChar;

    /** nullable persistent field */
    private Short theShort;

    /** nullable persistent field */
    private Integer theInt;

    /** nullable persistent field */
    private Long theLong;

    /** nullable persistent field */
    private String theString;

    /** nullable persistent field */
    private Date theDate;

    /** full constructor */
    public TestSimpleTypes(Float theFloat, Byte theByte, Character theChar, Short theShort, Integer theInt, Long theLong, String theString, Date theDate) {
        this.theFloat = theFloat;
        this.theByte = theByte;
        this.theChar = theChar;
        this.theShort = theShort;
        this.theInt = theInt;
        this.theLong = theLong;
        this.theString = theString;
        this.theDate = theDate;
    }

    /** default constructor */
    public TestSimpleTypes() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getTheFloat() {
        return this.theFloat;
    }

    public void setTheFloat(Float theFloat) {
        this.theFloat = theFloat;
    }

    public Byte getTheByte() {
        return this.theByte;
    }

    public void setTheByte(Byte theByte) {
        this.theByte = theByte;
    }

    public Character getTheChar() {
        return this.theChar;
    }

    public void setTheChar(Character theChar) {
        this.theChar = theChar;
    }

    public Short getTheShort() {
        return this.theShort;
    }

    public void setTheShort(Short theShort) {
        this.theShort = theShort;
    }

    public Integer getTheInt() {
        return this.theInt;
    }

    public void setTheInt(Integer theInt) {
        this.theInt = theInt;
    }

    public Long getTheLong() {
        return this.theLong;
    }

    public void setTheLong(Long theLong) {
        this.theLong = theLong;
    }

    public String getTheString() {
        return this.theString;
    }

    public void setTheString(String theString) {
        this.theString = theString;
    }

    public Date getTheDate() {
        return this.theDate;
    }

    public void setTheDate(Date theDate) {
        this.theDate = theDate;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
