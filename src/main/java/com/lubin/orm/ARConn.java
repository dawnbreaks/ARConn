package com.lubin.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ARConn{
    private Connection conn =null;
    private String dbName =null;

	public ARConn(Connection conn, String dbName){
		this.conn = conn;
		this.dbName = dbName;
	}

    public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	
	public void beginTX() throws SQLException{
		conn.setAutoCommit(false);
	}
	
	public void commit() throws SQLException{
		conn.commit();
	}
	
	public void rollback() throws SQLException{
		conn.rollback();
	}
	
	
	public <I extends ActiveRecord<I>> void iniARObj(ActiveRecord<I> ar){
		ar.setConn(this.conn);
		ar.setDbName(this.dbName);
	}
	
	public  <I extends ActiveRecord<I>> I create(Class<I> clazz){
		I instance;
		try {
			instance = clazz.newInstance();
			iniARObj(instance);
		}catch (Exception e) {
			throw new RuntimeException("create",e);
		}
		return instance;
	}
	
   	public <I extends ActiveRecord<I>>  I find(Class<I> clazz, Object id){
		I instance;
		try {
			instance = clazz.newInstance();
			iniARObj(instance);
			System.out.print("");
			return instance.find(id);
		}catch (Exception e) {
			throw new RuntimeException("find",e);
		}
	}
   	
  	public <I extends ActiveRecord<I>>   List<I> findAll(Class<I> clazz, String columnName, Object value){
		I instance;
		try {
			instance = clazz.newInstance();
			iniARObj(instance);
			return instance.findAll(columnName,value);
		}catch (Exception e) {
			throw new RuntimeException("findAll",e);
		}
	}
  	
	public <I extends ActiveRecord<I>>  List<I> queryAll(Class<I> clazz, String criterion ){
		I instance;
		try {
			instance = clazz.newInstance();
			iniARObj(instance);
			return instance.queryAll(criterion);
		}catch (Exception e) {
			throw new RuntimeException("queryAll",e);
		}
	}

	public void close(){
		try {
			if(conn!=null && !conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}