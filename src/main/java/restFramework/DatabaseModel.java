/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

/**
 *
 * @author Ivo
 */
public abstract class DatabaseModel {
    private DaosProvider daosProvider;
    private Dao instantiator;

    public void setDaosProvider(DaosProvider daosProvider) {
        this.daosProvider = daosProvider;
    }

    public void setInstantiator(Dao instantiator) {
        this.instantiator = instantiator;
    }
    
    public void save(){
        
    }

    @Override
    public String toString() {
        return "DatabaseModel [daosProvider=" + daosProvider + ", instantiator=" + instantiator + "]";
    }

    
    
}
