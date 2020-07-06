/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;

/**
 *
 * @author Ivo
 */
@SuppressWarnings({ "resource" })
public class MethodContext implements AutoCloseable {
    
    private static final ThreadLocal<MethodContext> holder = new ThreadLocal<>();
    
    private final DaosProvider daosProvider;
    
    public MethodContext(DaosProvider daosProvider){
        this.daosProvider = daosProvider;
        holder.set(this);
    }
    
    public static Connection getConnection(){
        return holder.get().daosProvider.getConnection();
    }
    
    public static DaosProvider getDaosProvider(){
        return holder.get().daosProvider;
    }
    
    public static void setPrecisaCommitar(){
        holder.get().daosProvider.precisaCommitar = true;
    }
    
    @Override
    public void close() throws Exception {
        holder.remove();
    }
}
