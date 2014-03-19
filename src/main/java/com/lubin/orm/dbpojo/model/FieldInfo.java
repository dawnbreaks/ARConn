package com.lubin.orm.dbpojo.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class FieldInfo {

    private String name;
    private String columnName;
    private String sqlType;
    private String javaType;
    private String getter;
    private String setter;
    private List<String> annotations = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getGetter() {
        getter = "get"+StringUtils.capitalize(name);
        return getter;
    }

    public void setGetter(String getter) {
        this.getter = getter;
    }

    public String getSetter() {
        setter = "set"+StringUtils.capitalize(name);
        return setter;
    }

    public void setSetter(String setter) {
        this.setter = setter;
    }
    
	public void addAnnotations(String annotation) {
		this.annotations.add(annotation);
	}

	public List<String> getAnnotations() {
		return this.annotations;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldInfo that = (FieldInfo) o;

        if (!columnName.equals(that.columnName)) return false;
        if (!javaType.equals(that.javaType)) return false;
        if (!name.equals(that.name)) return false;
        return sqlType.equals(that.sqlType);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + columnName.hashCode();
        result = 31 * result + sqlType.hashCode();
        result = 31 * result + javaType.hashCode();
        return result;
    }
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }


}
