package com.lubin.orm.dbpojo.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class ClassInfo {
    private String name;
    private String javaPackage;
    private String tableName;
    private List<FieldInfo> fields;
    private List<String> imports;
    private String modelItem;

    public ClassInfo()
    {
        fields = new LinkedList<FieldInfo>();
        imports = new LinkedList<String>();
    }

    public String getModelItem() {
        return modelItem;
    }

    public void setModelItem(String modelItem) {
        this.modelItem = modelItem;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJavaPackage() {
        return javaPackage;
    }

    public void setJavaPackage(String javaPackage) {
        this.javaPackage = javaPackage;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
