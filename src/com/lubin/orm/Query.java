package com.lubin.orm;

import com.google.common.base.Joiner;

import java.util.*;

class Query
{
    static SelectionQuery select( String selection )
    {
        return new SelectionQuery(selection);
    }

    static DeletionQuery delete()
    {
        return new DeletionQuery();
    }

    static InsertionQuery insertInto( String tableName )
    {
        return new InsertionQuery(tableName);
    }

    static WhereQuery where( String name )
    {
        return new WhereQuery("", name);
    }

    public static UpdateQuery update( String tableName ) {
        return new UpdateQuery( tableName );
    }

    static class SelectionQuery
    {
        List<String> selections = new ArrayList<String>();

        SelectionQuery( String selection )
        {
            selections.add(selection);
        }

        SelectionQuery and( String selection )
        {
            selections.add(selection);
            return this;
        }

        public String toString() {
            return "SELECT " + Joiner.on(", ").join( selections );
        }

        FromQuery from( String tableName )
        {
            return new FromQuery(this.toString(), tableName);
        }
    }

    static class FromQuery
    {
        private String startStatement;

        private List<String> tableNames = new ArrayList<String>();

        FromQuery( String startStatement, String firstTableName )
        {
            this.startStatement = startStatement;
            tableNames.add(firstTableName);
        }

        public String toString() {
            return startStatement + " FROM " + Joiner.on(", ").join(tableNames);
        }

        WhereQuery where(String firstName) {
            return new WhereQuery(this.toString(), firstName);
        }
    }

    static class WhereQuery
    {
        private StringBuilder builder = new StringBuilder();

        WhereQuery( String startStatement, String firstName ) {
            this.builder.append(startStatement).append( " WHERE " ).append( firstName );
        }

        public String toString() {
            return builder.toString();
        }

        public WhereQuery isEqualTo( String operand )
        {
            builder.append(" = ").append( operand );
            return this;
        }
        
        public WhereQuery operation( String operation )
        {
            builder.append(" ").append( operation ).append(" ");
            return this;
        }

        public WhereQuery and( String name )
        {
            builder.append(" AND ").append( name );
            return this;
        }
    }

    static class DeletionQuery
    {
        public String toString() {
            return "DELETE";
        }

        public FromQuery from( String table )
        {
            return new FromQuery(this.toString(), table);
        }
    }

    static class InsertionQuery
    {
        private String table;
        private List<String> columns = new ArrayList<String>();
        private List<String> values = new ArrayList<String>();

        InsertionQuery(String table) {
            this.table = table;
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder("INSERT INTO ")
                    .append(table)
                    .append( " (" );
            Joiner joiner = Joiner.on(", ");
            buffer.append(joiner.join(columns));
            buffer.append(") VALUES (");
            buffer.append(joiner.join(values));
            buffer.append(")");
            return buffer.toString();
        }

        InsertionQuery column( String column )
        {
            columns.add(column);
            return this;
        }

        InsertionQuery value( String value )
        {
            values.add(value);
            return this;
        }
    }

    static class UpdateQuery
    {
        private String table;
        private LinkedHashMap<String, String> setClause = new LinkedHashMap<String, String>();

        UpdateQuery(String table) {
            this.table = table;
        }

        UpdateQuery set(String column, String value) {
            setClause.put(column, value);
            return this;
        }

        WhereQuery where( String firstColumn ) {
            return new WhereQuery( toString(), firstColumn );
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder("UPDATE " + table + " SET ");
            Iterator<Map.Entry<String, String>> columnsIterator = setClause.entrySet().iterator();
            while ( columnsIterator.hasNext() ) {
                Map.Entry<String, String> column = columnsIterator.next();
                buffer.append(column.getKey());
                buffer.append(" = ");
                buffer.append(column.getValue());
                if (columnsIterator.hasNext()) {
                    buffer.append(", ");
                }
            }
            return buffer.toString();
        }
    }
}
