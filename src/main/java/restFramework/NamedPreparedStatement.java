/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ivo
 */
@SuppressWarnings({"rawtypes", "unchecked"}) 
public class NamedPreparedStatement implements AutoCloseable{
    /** The statement this object is wrapping. */
    private final PreparedStatement statement;

    /** Maps parameter names to arrays of ints which are the parameter indices. 
*/
    private final Map indexMap;


    /**
     * Creates a NamedParameterStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) 
prepareStatement}.
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedPreparedStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap();
        String parsedQuery = parse(query, indexMap);
        statement = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS);        
    }


    /**
     * Parses a query with named parameters.  The parameter-index mappings are 
put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This 
method is non-private so JUnit code can
     * test it.
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    static final String parse(String query, Map paramMap) {
        // I was originally using regular expressions, but they didn't work well 
        // parameter-like strings inside quotes.
        int length = query.length();
        StringBuilder parsedQuery=new StringBuilder(length);
        boolean inSingleQuote=false;
        boolean inDoubleQuote=false;
        int index=1;

        for(int i=0;i<length;i++) {
            char c=query.charAt(i);
            if(inSingleQuote) {
                if(c=='\'') {
                    inSingleQuote=false;
                }
            } else if(inDoubleQuote) {
                if(c=='"') {
                    inDoubleQuote=false;
                }
            } else {
                if(c=='\'') {
                    inSingleQuote=true;
                } else if(c=='"') {
                    inDoubleQuote=true;
                } else if(c==':' && i+1<length &&
                        Character.isJavaIdentifierStart(query.charAt(i+1))) {
                    int j=i+2;
                    while(j<length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name=query.substring(i+1,j);
                    c='?'; // replace the parameter with a question mark
                    i+=name.length(); // skip past the end if the parameter

                    List indexList=(List)paramMap.get(name);
                    if(indexList==null) {
                        indexList=new LinkedList();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));

                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        for(Iterator itr=paramMap.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry=(Map.Entry)itr.next();
            List list=(List)entry.getValue();
            int[] indexes=new int[list.size()];
            int i=0;
            for(Iterator itr2=list.iterator(); itr2.hasNext();) {
                Integer x = (Integer)itr2.next();
                indexes[i++] = x.intValue();
            }
            
            entry.setValue(indexes);
        }

        return parsedQuery.toString();
    }


    /**
     * Returns the indexes for a parameter.
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private int[] getIndexes(String name) {
        int[] indexes=(int[])indexMap.get(name);
        if(indexes==null) {
            return new int[]{};
        }
        return indexes;
    }

    public void setParameter(String name, Integer value){
        try {
            setInt(name, value);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setParameter(String name, String value){
        try {
            setString(name, value);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setParameter(String name, Long value){
        try {
            setLong(name, value);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setParameter(String name, Double value){
        try {
            setDouble(name, value);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setParameter(String name, Boolean value){
        try {
            setBoolean(name, value);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setParameter(String name, Number value){
        if(value instanceof Long){
            setParameter(name, (Long) value);
        } else if(value instanceof Integer){
            setParameter(name, (Integer) value);
        } else if(value instanceof Double){
            setParameter(name, (Double) value);
        }
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    public void setString(String name, String value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
            if(value == null) statement.setNull(indexes[i], Types.NULL);
            else statement.setString(indexes[i], value);
        }
    }

    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    public void setBoolean(String name, Boolean value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
            if(value == null) statement.setNull(indexes[i], Types.NULL);
            else statement.setInt(indexes[i], value ? 1 : 0);
        }
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    public void setDouble(String name, Double value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
            if(value == null) statement.setNull(indexes[i], Types.NULL);
            else statement.setDouble(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, Integer value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
            if(value == null) statement.setNull(indexes[i], Types.NULL);
            else statement.setInt(indexes[i], value);
        }
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setLong(String name, Long value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
            if(value == null) statement.setNull(indexes[i], Types.NULL);
            else statement.setLong(indexes[i], value);
        }
    }


    /**
     * Returns the underlying statement.
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }


    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return statement.execute();
    }


    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }


    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE 
statement;
     * or an SQL statement that returns nothing, such as a DDL statement.
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    
    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    /**
     * Closes the statement.
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    @Override
    public void close() throws SQLException {
        statement.close();
    }


    /**
     * Adds the current set of parameters as a batch entry.
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException {
        statement.addBatch();
    }


    /**
     * Executes all of the batched statements.
     * 
     * See {@link Statement#executeBatch()} for details.
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }
}
