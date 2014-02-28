package com.lubin.orm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.orm.annotation.PrimaryKey;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public void copyDBData(T ar){
		ar.setConn(this.conn);
		ar.setDbName(this.dbName);
	}
	
//	public <I>  I createAR(Class<I> clazz){
//		I instance;
//		try {
//			instance = clazz.newInstance();
//			copyDBData((ActiveRecord) instance);
//		}catch (Exception e) {
//			throw new RuntimeException("createAR",e);
//		}
//		return instance;
//	}

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
	}
	
    /**
	use update(...) method directly if primary key is not auto_increment, 
     */
    public void update()
    throws SQLException
	{
    	update(this.getConn(),this.getDbName());
	}
	
	
	public void update(Connection connection, String dataBaseName)
	    throws SQLException
	{
	    ArrayList<Object> args = new ArrayList<Object>();
	    String query = buildUpdateQuery( args, dataBaseName );
	    PreparedStatement statement = connection.prepareStatement( query );
	    if ( statement != null) {
	        bindArguments( statement, args );
	        logger().debug("Executing query '{}' with values {}", query, args);
	        statement.executeUpdate();
	    }
	}
   /* private boolean existInDatabase() {
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
     * update table xxx set field1=x1,field2=x2 ... where primaryKey=keyValue.
     */
    private String buildUpdateQuery(ArrayList<Object> args, String dataBaseName) {
        try {
            Query.UpdateQuery query = Query.update( (dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName()));
            String primaryKeyColumn = null;
            Object primaryKeyValue = null;
            for (Field field : getClass().getDeclaredFields()) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                field.setAccessible(true);
                if (primaryKey != null) {
                    primaryKeyColumn = field.getName();
                    primaryKeyValue = field.get(this);
                } else {
                    args.add(field.get(this));
                    query.set( field.getName() , "?");
                }
            }
            args.add( primaryKeyValue );
            return query.where(primaryKeyColumn).isEqualTo( "?" ).toString();
        } catch ( IllegalAccessException ignored ) {}
        return null;
    }

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

    public List<T> search()
        throws SQLException
    {
    	return search(this.getConn(),this.getDbName());
    }
    
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
	  public T find(String field, Object value)
	    throws SQLException
		{
			return find(this.getConn(),this.getDbName(), field, value);
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
    private void createResultsFromResultSet( Class<T> clazz, ResultSet resultSet, ArrayList<T> results )
        throws SQLException
    {
        while ( resultSet.next() ) {
            results.add( createResultFromRow( clazz, resultSet ) );
        }
    }

    private T createResultFromRow( Class<T> clazz, ResultSet resultSet )
        throws SQLException
    {
        try {
            return createResultFromRowWithError( clazz, resultSet );
        } catch ( Exception cause ) {
            throw new RuntimeException("Unable to execute selection query", cause);
        }
    }

    private T createResultFromRowWithError( Class<T> clazz, ResultSet resultSet )
        throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException
    {
        T instance = clazz.newInstance();
        int index = 1;
        for ( Field field : clazz.getDeclaredFields() ) {
            field.setAccessible( true );
            Object value = resultSet.getObject( index++ );
            if(!field.getType().isAssignableFrom(value.getClass()) && field.getType().isAssignableFrom(Integer.class) && value instanceof Long ){
            	field.set( instance, new Integer(((Long)value).intValue()));
            }else{
            	field.set( instance, value );
            }
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
        for ( Field field : clazz.getDeclaredFields() ) {
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            
            String fieldName = field.getName();
            if (select == null) {
                select = Query.select(fieldName);
            } else {
                select = select.and(fieldName);
            }
            
            if ( primaryKey != null) {
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
	    statement.executeUpdate();
	}
    
    
    /*
     * delete table xxx where field1=x1,field2=x2....
     */
    private String buildDeletionQuery( ArrayList<Object> args, String dataBaseName )
    {
        try {
            Query.WhereQuery whereClause = null;
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object arg = field.get(this);
                if ( arg != null) {
                    String fieldName = field.getName();
                    if (whereClause == null) {
                        whereClause = Query.where( fieldName );
                    } else {
                        whereClause.and(fieldName);
                    }
                    whereClause.isEqualTo("?");
                    args.add(arg);
                }
            }
            return Query.delete().from((dataBaseName==null?"":(dataBaseName+"."))+className2tblName(getClass().getSimpleName())) + whereClause.toString();
        } catch (IllegalAccessException ignored) {}
        return null;
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

}
