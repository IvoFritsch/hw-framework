/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;


/**
 *
 * @author Pedrivo
 */
public abstract class Dao {
    protected Connection transaction;
    protected DaosProvider instanciador;
    
    private static DaosProvider dummyInst;
    
    static{
        dummyInst = new InputVazia();
    }
    
    public Dao(){
        transaction = null;
        instanciador = dummyInst;
    }
    
    public Dao(Connection transaction, MethodInput instanciador){
        this.transaction = transaction;
        this.instanciador = instanciador;
    }
    
    protected Connection getConnection(){
        return instanciador.getConnection();
    }

    void setTransaction(Connection transaction) {
        this.transaction = transaction;
    }

    void setInstanciador(DaosProvider instanciador) {
        this.instanciador = instanciador;
    }
   
}
