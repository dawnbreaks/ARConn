package com.lubin.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>PrimaryKey</code> annotation indicates to the framework that a field
 * maps a column that has a primary key constraint. It is used to update existing
 * record with {@link com.lubin.orm.ActiveRecord#save()} method.
 *
 * <p>To tag a field as primary key, just add the annotation like that :</p>
 * <pre>
 * import activerecord.ActiveRecord;
 * import activerecord.annotation.PrimaryKey;
 *
 * public class Contact
 *     extends ActiveRecord&lt;Contact>
 * {
 *     {@literal @}PrimaryKey
 *     private Integer id;  //  This field is a primary key
 *
 *     //  Those fields are not primary keys
 *     private String firstName;
 *     private String lastName;
 *     private String email;
 * }
 * </pre>
 *
 * <p>
 *     By convention, a null primary key indicates that the instance object has never been
 *     perstisted in database. So:
 * </p>
 * <ul>
 *     <ol>The primary key field type MUST be a wrapper type (java.lang.Integer, java.lang.Long, java.lang.Double)
 *     and MUST NOT be a primitive type (like int, long or double)</ol>
 *     <ol>The primary key MUST NOT be set if you want to create another record, and the corresponding column
 *     MUST have an auto generated mechanism to create a default value (link auto increment, sequence, ...)</ol>
 * </ul>
 * @see com.lubin.orm.ActiveRecord#save() The insert/update routine.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PrimaryKey
{
}
