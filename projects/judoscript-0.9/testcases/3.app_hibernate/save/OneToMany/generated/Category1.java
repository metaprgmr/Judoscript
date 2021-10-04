// default package

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Category1 implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private Category1 parentCategory;

    /** persistent field */
    private Set childCategories;

    /** full constructor */
    public Category1(String name, Category1 parentCategory, Set childCategories) {
        this.name = name;
        this.parentCategory = parentCategory;
        this.childCategories = childCategories;
    }

    /** default constructor */
    public Category1() {
    }

    /** minimal constructor */
    public Category1(Set childCategories) {
        this.childCategories = childCategories;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category1 getParentCategory() {
        return this.parentCategory;
    }

    public void setParentCategory(Category1 parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Set getChildCategories() {
        return this.childCategories;
    }

    public void setChildCategories(Set childCategories) {
        this.childCategories = childCategories;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
