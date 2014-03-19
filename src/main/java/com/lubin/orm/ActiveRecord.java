package com.lubin.orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.orm.annotation.AutoIncrement;
import com.lubin.orm.annotation.PrimaryKey;


public abstract class ActiveRecord<T extends ActiveRecord<T>>
{
    private Connection conn =null;
    private String dbName =null;


    public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	/*
	 * database name
	 */
	public String getDbName() {
		return dbName;
	}

	
	/*
	 * database.table name
	 */
	public String getTableName() {
		return dbName+"."+className2tblName(getClass().getSimpleName());
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public void copyDBData(ActiveRecord<T> ar){
		ar.setConn(this.conn);
		ar.setDbName(this.dbName);
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

	@SuppressWarnings("unchecked")
    private Class<T> clazz = (Class<T>) getClass();

    private Logger logger() {
        return LoggerFactory.getLogger(getClass());
    }

    
    /**
	use insert(...) method directly if  primary key is not auto_increment, 
     */
    public void insert()
    throws SQLException
	{
    	insert(this.getConn(),this.getDbName());
	}
	
	
	public void insert(Connection connection, String dataBaseName)
	    throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    String query = buildInsertionQuery( args, dataBaseName );
	    PreparedStatement statement = connection.prepareStatement( query );
	    if ( statement != null) {
	        bindArguments( statement, args );
	        logger().debug("Executing query '{}' with values {}", query, args);
	        statement.executeUpdate();
	    }
	    
    	for (Field field : getClass().getDeclaredFields()) {
            PrimaryKey primaryKeyField = field.getAnnotation(PrimaryKey.class);
            AutoIncrement autoIncrement = field.getAnnotation(AutoIncrement.class);
            field.setAccessible(true);
            
            if(primaryKeyField != null && autoIncrement!=null) {
            	
             	String getLastInsertIdSql = "select LAST_INSERT_ID();";
         	    ResultSet resultSet = connection.createStatement().executeQuery(getLastInsertIdSql);
         	    if(resultSet.next()){
         	    	Object lastInsertId = resultSet.getLong(1);
         	    	try{
         	    		if(!field.getType().isAssignableFrom(lastInsertId.getClass()) && field.getType().isAssignableFrom(Integer.class)){
                         	field.set(this, new Integer(((Long)lastInsertId).intValue()));
                         }else{
                         	field.set(this, lastInsertId);
                         } 
         	    	}catch (Exception e) {
         	    		throw new RuntimeException("fetch LAST_INSERT_ID got failure",e);
         	    	}
         	    	break;
         	    }
         	    
            }
        } 	 
	}
	

    public void update()
    throws SQLException
	{
    	update(this.getConn(),this.getDbName());
	}
	
	
    /*
     * update tbl set f1=x1,....  where pk1=x1 and pk2=x2
     */
	public void update(Connection connection, String dataBaseName)
	    throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    String query = buildUpdateQuery( args, dataBaseName );
	    PreparedStatement statement = connection.prepareStatement( query );
	    if ( statement != null) {
	        bindArguments( statement, args );
	        logger().debug("Executing query '{}' with values {}", query, args);
	        int c = statement.executeUpdate();
	        if(c ==0){
	        	throw new RuntimeException("ActiveRecord update nothing|query="+query);
	        }
	    }
	}
	
	/*
    @SuppressWarnings("unused")
	private boolean existInDatabase() {
        for (Field field : getClass().getDeclaredFields()) {
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
            	AutoIncrement autoIncrement = field.getAnnotation(AutoIncrement.class);	

                field.setAccessible(true);
                try {
                    if( field.get(this) != null && autoIncrement !=null) {
                    	return true;
                    }else if( field.get(this) == null && autoIncrement !=null) {
                    	return false;
                    }else if( field.get(this) == null && autoIncrement ==null) {
                    	throw new RuntimeException("primary key should not be null!");
                    }else if( field.get(this) != null && autoIncrement ==null) {
                    	throw new RuntimeException("can not determine whether data exists in DB, please call the insert/update method directly if the primary key is not auto increment !");
                    }
                    	
                } catch ( IllegalAccessException ignored ) {throw new RuntimeException("Illegal Access",ignored);}
            }
        }
        return false;
    }

*/

    /*
     * update table xxx set field1=x1,field2=x2 ... where pk1=v1 and pk2=v2....
     */
    private String buildUpdateQuery(ArrayList<Object> args, String dataBaseName) {
        try {
            Query.UpdateQuery query = Query.update( (dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName()));
            Query.WhereQuery whereClause = null;
  
            ArrayList<Object> pkValues = new ArrayList<Object>();
            ArrayList<String> pkNames = new ArrayList<String>();
            
            for (Field field : getClass().getDeclaredFields()) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                field.setAccessible(true);
                if (primaryKey != null) {
                	pkNames.add(field.getName());
                	pkValues.add(field.get(this));
                } else {
                    args.add(field.get(this));
                    query.set( field.getName() , "?");
                }
           }
            
           if(pkNames.isEmpty()){
        	   throw new RuntimeException(getClass().getName()+ " did not has a primary key!");
           }
           
           for(String keyName : pkNames){
        	   if(whereClause==null){
        		   whereClause = query.where(keyName).isEqualTo( "?" );
        	   }else{
        		   whereClause = whereClause.and(keyName).isEqualTo( "?" );
        	   }
           }
           args.addAll( pkValues );
           return whereClause.toString();
           
        } catch ( IllegalAccessException ignored ) {}
        return null;
    }

    
    /*
     * ignore null value.
     * insert into tbl(f1,f2....)values(v1,v2.....);
     */
    private String buildInsertionQuery( ArrayList<Object> args, String dataBaseName )
    {
        try {
            Query.InsertionQuery insert = Query.insertInto( (dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName()) );
            for (Field field : getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if(field.get(this)!=null){
                    args.add(field.get(this));
                    insert.column( field.getName() ).value("?");
                }
            }
            return insert.toString();
        } catch ( IllegalAccessException ignored ) {}
        return null;
    }

    private void bindArguments( PreparedStatement statement, ArrayList<Object> args )
            throws SQLException
    {
        int index = 1;
        for (Object arg : args) {
            statement.setObject(index++, arg);
        }
    }

    @Deprecated
    public List<T> search()
        throws SQLException
    {
    	return search(this.getConn(),this.getDbName());
    }
    
    @Deprecated
    public List<T> search(Connection connection, String dataBaseName)
    throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    String query = buildSelectionQuery( args, dataBaseName );
		PreparedStatement statement = connection.prepareStatement( query );
	    bindArguments( statement, args );
	    logger().debug("Executing query '{}' with values {}", query, args);
	
		ResultSet resultSet = statement.executeQuery();
	    ArrayList<T> results = new ArrayList<T>();
	    createResultsFromResultSet( clazz, resultSet, results );
	    return results;
	}
    
    
    /*
     * find object by primary key!
     */
    public T find(Object id)
    throws SQLException
	{
		return find(id,this.getConn(),this.getDbName());
	}
	
	public T find(Object id, Connection connection, String dataBaseName)
	throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    args.add(id);
	    
	    String query = buildFindQuery( dataBaseName );
		PreparedStatement statement = connection.prepareStatement( query );
	    bindArguments( statement, args );

		ResultSet resultSet = statement.executeQuery();
	    ArrayList<T> results = new ArrayList<T>();
	    createResultsFromResultSet( clazz, resultSet, results );
	    if(!results.isEmpty()){
	    	return results.get(0);
	    }else{
	    	return null;
	    }
	}
    
	 /*
	  * find object by non-primary key.
	  */
	public T find(String columnName, Object value)
    throws SQLException
	{
		return find(this.getConn(),this.getDbName(), columnName, value);
	}
		
	public T find(Connection connection, String dataBaseName,String columnName, Object value)
	throws SQLException
	{
		List<T> resList = findAll(connection, dataBaseName, columnName, value);
		if(resList == null || resList.isEmpty())
			return null;
		else{
			return resList.get(0);	
		}
	}
		
	public List<T> findAll(String columnName, Object value)
	throws SQLException
	{
		return findAll(this.getConn(),this.getDbName(), columnName, value);
	}
		
	public List<T> findAll(Connection connection, String dataBaseName,String columnName, Object value)
	throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    args.add(value);
	    
	    String query = buildFindQuery( dataBaseName, columnName);
		PreparedStatement statement = connection.prepareStatement( query );
	    bindArguments( statement, args );

		ResultSet resultSet = statement.executeQuery();
	    ArrayList<T> results = new ArrayList<T>();
	    createResultsFromResultSet( clazz, resultSet, results );
	    return results;
	}
		
		
	public List<T> queryAll(String criterion)
	throws SQLException
	{
		return queryAll(this.getConn(),this.getDbName(), criterion);
	}
	
	/*
	 * select * from tbl where criterion.
	 */
	public List<T> queryAll(Connection connection, String dataBaseName, String criterion)
	throws SQLException
	{
		Query.SelectionQuery select = null;
		for ( Field field : clazz.getDeclaredFields() ) {

            String fieldName = field.getName();
            if (select == null) {
                select = Query.select(fieldName);
            } else {
                select = select.and(fieldName);
            }
        }

		String query = select.from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())).toString()+" where "+criterion;

		PreparedStatement statement = connection.prepareStatement( query );
		ResultSet resultSet = statement.executeQuery();
	    ArrayList<T> results = new ArrayList<T>();
	    createResultsFromResultSet( clazz, resultSet, results );
	    return results;
	}
	
    private  void createResultsFromResultSet( Class<T> clazz, ResultSet resultSet, ArrayList<T> results )
        throws SQLException
    {
        while ( resultSet.next() ) {
            results.add( createResultFromRow( clazz, resultSet ) );
        }
    }

    private  T createResultFromRow( Class<T> clazz, ResultSet resultSet )
    throws SQLException
    {
        try {
            return createResultFromRowWithError( clazz, resultSet );
        } catch ( Exception cause ) {
            throw new RuntimeException("Unable to execute selection query", cause);
        }
    }

    private  T createResultFromRowWithError( Class<T> clazz, ResultSet resultSet )
    throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        T instance = clazz.newInstance();
        int index = 1;
        for ( Field field : clazz.getDeclaredFields() ) {
            field.setAccessible( true );
            Object value = null;
            if(field.getType().isAssignableFrom(Long.class)){
            	 value = resultSet.getLong( index++ );
            }else if(field.getType().isAssignableFrom(Integer.class)){
            	value = resultSet.getInt( index++ );
            }else{
            	value = resultSet.getObject( index++ );
            }
           
            field.set( instance, value );
            
//            if(!field.getType().isAssignableFrom(value.getClass()) && field.getType().isAssignableFrom(Integer.class) && value instanceof Long ){
//            	field.set( instance, new Integer(((Long)value).intValue()));
//            }else{
//            	field.set( instance, value );
//            }
        }
        
        copyDBData(instance);
        return instance;
    }


    /*
     * xxx1 and xxx2 not equal to null 
     * select * from table where field = xxx1 and field2 = xx2;
     */
    private String buildSelectionQuery( ArrayList<Object> args, String dataBaseName)
    {
        try {
            Query.SelectionQuery select = null;
            Query.WhereQuery whereClause = null;
            for ( Field field : clazz.getDeclaredFields() ) {
                field.setAccessible(true);
                Object arg = field.get( this );
                String fieldName = field.getName();
                if (select == null) {
                    select = Query.select(fieldName);
                } else {
                    select = select.and(fieldName);
                }
                if ( arg != null) {
                    if (whereClause == null) {
                        whereClause = Query.where( fieldName );
                    } else {
                        whereClause.and(fieldName);
                    }
                    whereClause.isEqualTo("?");
                    args.add(arg);
                }
            }
            return select.from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())).toString() + whereClause.toString();
        } catch (IllegalAccessException ignored) {}
        return null;
    }


    /*
     * select field1,field2.... from table where primaryKey=xx;
     */
    private String buildFindQuery(String dataBaseName)
    {
        Query.SelectionQuery select = null;
        Query.WhereQuery whereClause = null;
        int pkCount = 0;
        for ( Field field : clazz.getDeclaredFields() ) {
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            
            String fieldName = field.getName();
            if (select == null) {
                select = Query.select(fieldName);
            } else {
                select = select.and(fieldName);
            }
            
            if ( primaryKey != null) {
            	pkCount++;
                if(pkCount>1){
                	throw new RuntimeException(getClass().getName()+ " has more than two primary keys!");
                }
                if (whereClause == null) {
                    whereClause = Query.where( fieldName );
                } else {
                    whereClause.and(fieldName);
                }
            
                whereClause.isEqualTo("?");
            }
        }

        return select.from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())).toString() + whereClause.toString();
    }


    //find object by non-primary key.
    private String buildFindQuery(String dataBaseName, String columnName)
    {
        Query.SelectionQuery select = null;
        Query.WhereQuery whereClause = null;
        for ( Field field : clazz.getDeclaredFields() ) {

            String fieldName = field.getName();
            if (select == null) {
                select = Query.select(fieldName);
            } else {
                select = select.and(fieldName);
            }
        }
        
        if (whereClause == null) {
            whereClause = Query.where( columnName );
        } else {
            whereClause.and(columnName);
        }
        whereClause.isEqualTo("?");

        return select.from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())).toString() + whereClause.toString();
    }

    public void delete()
        throws SQLException
    {
        delete(this.getConn(),this.getDbName());
    }


    public void delete(Connection connection, String dataBaseName)
    throws SQLException
    {
        ArrayList<Object> args = new ArrayList<Object>();
        String query = buildDeletionQuery( args, dataBaseName);

    	PreparedStatement statement = connection.prepareStatement(query);
        bindArguments(statement, args);
        logger().debug("Executing query '{}' with arguments {}", query, args);
        int c = statement.executeUpdate();
        if(c ==0){
        	throw new RuntimeException("ActiveRecord delete nothing|query="+query);
        }
    }


    /*
     * delete table xxx where field1=x1,field2=x2....
     */
    private String buildDeletionQuery( ArrayList<Object> args, String dataBaseName )
    {
        try {
            Query.WhereQuery whereClause = null;
            boolean foundPK=false;
            for (Field field : getClass().getDeclaredFields()) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                field.setAccessible(true);
                if (primaryKey != null) {
                	foundPK = true;
                	if(whereClause==null){
                		whereClause = Query.where(field.getName()).isEqualTo( "?" );
            		}else{
            			whereClause = whereClause.and(field.getName()).isEqualTo( "?" );
            		}
                	args.add(field.get(this));
                }
            }
            if(!foundPK){
            	throw new RuntimeException(getClass().getName()+" did not have primary key!");
            }
           
           return Query.delete().from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())) + whereClause.toString();
        } catch (IllegalAccessException ignored) {}
        return null;
    }


    @SuppressWarnings("unused")
    private String tblName2ClassName(String tblName){
    	StringBuffer res = new StringBuffer();
    	int len = tblName.length();
    	for(int i=0;i<len;i++){
    		char c = tblName.charAt(i);
    		if(i ==0 && c <='z' &&  c>= 'a'){// upper first char
    			res.append((char)(c-32));
    		}else if(i ==0){
    			res.append(c);
    		}else if(c =='_'){
    			i++;
    			c = tblName.charAt(i);
    			if(c <='z' &&  c>= 'a'){
    				res.append((char)(c-32));
    			}
    		}else{
    			res.append(c);
    		}
    	}
    	return res.toString();
    }


    private String className2tblName(String className){
    	StringBuffer res = new StringBuffer();
    	int len = className.length();
    	for(int i=0;i<len;i++){
    		char c = className.charAt(i);
    		if(i ==0 && c <='Z' &&  c>= 'A'){// lower first char
    			res.append((char)(c+32));
    		}else if(c <='Z' &&  c>= 'A'){
    			res.append('_');
    			res.append((char)(c+32));
    		}else{
    			res.append(c);
    		}
    	}
    	return res.toString();
    }

}
