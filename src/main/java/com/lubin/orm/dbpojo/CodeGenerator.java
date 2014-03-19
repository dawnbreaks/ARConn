package com.lubin.orm.dbpojo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.lubin.orm.dbpojo.model.ClassInfo;
import com.lubin.orm.dbpojo.model.FieldInfo;
import com.lubin.orm.dbpojo.util.NameConversion;
import com.lubin.orm.dbpojo.util.SqlToJavaType;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


public class CodeGenerator
{
    private static Logger logger = Logger.getLogger(CodeGenerator.class);
    private Connection connection;

    private Config conf = ConfigFactory.load();
    
    private SqlToJavaType sqlToJavaType =new SqlToJavaType(conf.getString("sqlToJavaType"));
    private NameConversion nameConversion = new NameConversion(conf.getString("keywordsFile"));
    
    

    public CodeGenerator(){
//    	sqlToJavaType =new SqlToJavaType(conf.getString("sqlToJavaType"));
//    	nameConversion = new NameConversion(conf.getString("keywordsFile"));
        try {
			Class.forName(conf.getString("jdbc.driver"));
			String url = conf.getString("jdbc.url");
			String user = conf.getString("jdbc.user");
			String pass = conf.getString("jdbc.pass");

			this.connection = DriverManager.getConnection(String.format(url+"&user=%s&password=%s", user, pass));
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}   
    }
  
    public final void releaseConnection(){
        try {
            if(connection!=null && !connection.isClosed()){
                logger.info("Release connection....");
                connection.close();
            }
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    
    public ClassInfo loadTableMetadata(String tableName, String schema) throws SQLException
    {
    	logger.info("Table : " + tableName + " Java Class : " + nameConversion.tableNameToClassName(tableName));
        
        DatabaseMetaData meta = connection.getMetaData();

        ResultSet pkResultset = meta.getPrimaryKeys(schema, schema, tableName);

        Set<String> pkSet = new HashSet<String> ();
        while (pkResultset.next()) {
          String columnName = pkResultset.getString("COLUMN_NAME");
          pkSet.add(columnName.toLowerCase());
        }
        pkResultset.close();
      
        
        final String sql = metadataQuerySQL(schema+"."+tableName);
        Statement statement = null;
        ResultSet rs = null;
        ResultSetMetaData metaData;
        ClassInfo pojoPrototype = new ClassInfo();
        pojoPrototype.setTableName(tableName);
        pojoPrototype.setName(nameConversion.tableNameToClassName(tableName));
        String imp;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            if(rs!=null){
                metaData = rs.getMetaData();
                if(metaData!=null){
                    FieldInfo pojoFieldPrototype;
                    for(int i=1;i<metaData.getColumnCount()+1;i++){
                        pojoFieldPrototype = new FieldInfo();

                        pojoFieldPrototype.setColumnName(metaData.getColumnName(i));
//                        pojoFieldPrototype.setName(nameConversionHelper.columnNameToIdentifier(metaData.getColumnName(i)));
                        pojoFieldPrototype.setName(metaData.getColumnName(i));//fix me!!
                        pojoFieldPrototype.setSqlType(metaData.getColumnTypeName(i));
                        pojoFieldPrototype.setJavaType(sqlToJavaType.getJavaType(metaData.getColumnTypeName(i)));
                        if(metaData.isAutoIncrement(i)){
                        	pojoFieldPrototype.addAnnotations("@AutoIncrement");
                        }
                        
                        if(pkSet.contains(metaData.getColumnName(i).toLowerCase())){
                        	pojoFieldPrototype.addAnnotations("@PrimaryKey");
                        }
                        
                        
                        imp = sqlToJavaType.getImportString(metaData.getColumnTypeName(i));
                        if(imp!=null && !pojoPrototype.getImports().contains(imp)){
                            pojoPrototype.getImports().add(imp);
                        }
                        pojoPrototype.getFields().add(pojoFieldPrototype);

                        if(logger.isDebugEnabled()){
                            logger.debug(metaData.getColumnName(i) +" => "+pojoFieldPrototype.getName()+
                                    " => "+pojoFieldPrototype.getGetter()+" => "+pojoFieldPrototype.getSetter()
                                    +" => "+pojoFieldPrototype.getSqlType()
                                    +" => "+pojoFieldPrototype.getJavaType()
                                    +" => "+metaData.getScale(i)
                                    +" => "+metaData.getPrecision(i));
                        }
                    }
                }
            }
            else{
                logger.error("Metadata empty. Probably wrong table name.");
                //return null;
            }
        } catch (SQLException e) {
            logger.error("Working with table "+ tableName+" caused error : ",e);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return pojoPrototype;
    }
    
    public final String metadataQuerySQL(String tableName)
    {
        return String.format("select * from %s where 1=2",tableName);
    }
    
    //for oracle database
//    public List<String> getAllTablesInSchema(String shemaName)
//    {
//        final String tablesInSchemaSQL = "select table_name from all_tables where owner=\'"+shemaName+"\'";
//        List<String> tables = null;
//        try {
//            Statement st = connection.createStatement();
//            ResultSet resultSet = st.executeQuery(tablesInSchemaSQL);
//            if(resultSet!=null)
//            {
//                tables = new LinkedList<String>();
//                String tableName;
//                while (resultSet.next())
//                {
//                    tableName = resultSet.getString(1);
//                    if(tableName!=null){
//                        tables.add(tableName);
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            logger.error(e);
//        }
//        return tables;
//    }

    //for mysql database
    public List<String> listAllTables(String sql)
    {
        List<String> tables = null;
        try {
            Statement st = connection.createStatement();
            ResultSet resultSet = st.executeQuery(sql);
            if(resultSet!=null)
            {
                tables = new LinkedList<String>();
                String tableName;
                while (resultSet.next())
                {
                    tableName = resultSet.getString(1);
                    if(tableName!=null){
                        tables.add(tableName);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return tables;
    }
    
    public void genereateCode(String dbName, String templateName, String path, String javaPackage) throws SQLException{
    	
    	List<String> tables = listAllTables("SHOW TABLES FROM "+dbName);
        PojoWriter pojoWriter = new PojoWriter();
        pojoWriter.setPath(path + PojoWriter.packageToDirPath(javaPackage));
        pojoWriter.setTemplateName(templateName);

        ClassInfo prototype;
        for(String table : tables)
        {
            prototype = loadTableMetadata(table,dbName);

            prototype.setJavaPackage(javaPackage);
            pojoWriter.setFileName(prototype.getName()+".java");
            pojoWriter.write(prototype);
            logger.debug("Prototype : "+prototype);
        }
    }
    
    
    public static void main(String[] args) throws SQLException
    {
    	
        CodeGenerator codeGenerator = new CodeGenerator();
        
        String dbName = "crmadmin";
        String templateName ="crmPojo.vm";
        
        String path = "src/main/java/";
        String javaPackage = "com.crm.dao.admin.model";
       
        codeGenerator.genereateCode(dbName, templateName, path, javaPackage);
        
        dbName = "client01";
        javaPackage = "com.crm.dao.admin.client";
        
        codeGenerator.genereateCode(dbName, templateName, path, javaPackage);
    }
}
