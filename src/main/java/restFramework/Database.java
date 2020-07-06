/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCPool;

/**
 *
 * @author Pedrivo
 */
public class Database {
    
    private static String dbLocation = Utils.findHsqlmanDbLocation(RestApi.nomeProjeto);
    private static DataSource dataSource;
    private static JDBCPool pool = new JDBCPool(20);
    private static boolean finished = false;
    
    
    public static void setURL(String url){
        pool.setURL(url);
    }
    
    public static void setUser(String user){
        pool.setUser(user);
    }
    
    public static void setPassword(String password){
        pool.setPassword(password);
    }
    
    static void finish(){
        System.out.println("Localização do banco de dados: "+dbLocation);
        dataSource = pool;
        finished = true;
    }
    
    /**
     * Esse método é utilizado somente pelo framework, não utilizá-lo de fora
     * 
     * @return Conexão
     */
    public static java.sql.Connection getNormalConnection() throws SQLException{
        java.sql.Connection retorno = null;
        while(retorno == null){
            try{
                retorno = dataSource.getConnection();
            } catch(Exception e){}
        }
        return retorno;
    }
    
    public static Connection getStaticConnection() throws SQLException{
        return dataSource.getConnection();
    }
    
    public static Connection getTransaction(){
        Connection retorno = null;
        // Espera até encontrar uma conexão válida, isso só acontece no caso do banco de dados ser reiniciado, normalmente esse while passa direto
        while(retorno == null){
            try{
                retorno = dataSource.getConnection();
                retorno.setAutoCommit(false);
            } catch(Exception e){}
        }
        try {
            if(retorno.getAutoCommit())
                retorno.setAutoCommit(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return retorno;
    }

    public static boolean isFinished() {
        return finished;
    }
    
    static void closeAllConnections(){
        try {
            pool.close(2);
        } catch (Exception ex) {
        }
        
    }
    
}
