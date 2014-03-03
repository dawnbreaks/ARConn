package com.lubin.orm.dbpojo.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SqlToJavaType  {
    private String sql2JavaTypeConf;
    private Map<String, JavaTypeInfo> typesMap;

  
    //constructor
    public SqlToJavaType(String confFile)
    {
        typesMap = new HashMap<String, JavaTypeInfo>();
        this.sql2JavaTypeConf = confFile;
        readXml();
    }
    
    private void readXml()
    {
    	
    	try {
    		final File fXmlFile = new File(sql2JavaTypeConf);
    	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	    Document doc = dBuilder.parse(fXmlFile);
    	    doc.getDocumentElement().normalize();

    	    NodeList nList = doc.getElementsByTagName("type");
    	    System.out.println("Root element :" + doc.getDocumentElement().getNodeName()+" node list length: "+nList.getLength());
    	
    	    JavaTypeInfo typeDescriptor;
    	    for (int temp = 0; temp < nList.getLength(); temp++) {
    	    	Node nNode = nList.item(temp);
 	   	       if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 	   	           Element eElement = (Element) nNode;
 	   	           typeDescriptor = new JavaTypeInfo();
 	   	           typeDescriptor.setTypeName(eElement.getAttribute("to"));
 	   	           typeDescriptor.setImportString(eElement.getAttribute("import"));
 	   	           typesMap.put(eElement.getAttribute("db"),typeDescriptor);
 	   	        }    	    	    
    	   	}
    	  } catch (Exception e) {
    		  e.printStackTrace();
    	  }
    }


    public String getJavaType(String sqlType) {
        if(typesMap.containsKey(sqlType)){
            return typesMap.get(sqlType).getTypeName();
        }
        else{
            //return "do_not_supported_"+sqlType;
            return "String";
        }
    }

    public String getImportString(String sqlType) {
        if(typesMap.containsKey(sqlType)){
            return typesMap.get(sqlType).getImportString();
        }
        else{
            return null;
        }
    }
    
    
    private static class JavaTypeInfo {
        private String typeName;
        private String importString;

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getImportString() {
            return importString;
        }

        public void setImportString(String importString) {
            this.importString = importString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JavaTypeInfo that = (JavaTypeInfo) o;

            if (importString != null ? !importString.equals(that.importString) : that.importString != null) return false;
            return !(typeName != null ? !typeName.equals(that.typeName) : that.typeName != null);

        }

        @Override
        public int hashCode() {
            int result = typeName != null ? typeName.hashCode() : 0;
            result = 31 * result + (importString != null ? importString.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "GeneratedTypeDescriptor{" +
                    "typeName='" + typeName + '\'' +
                    ", importString='" + importString + '\'' +
                    '}';
        }
    }
}
