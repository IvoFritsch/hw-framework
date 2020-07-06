/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates.updateOperations;

import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.ContadorPassavel;
import restFramework.tableUpdates.TipoOperacao;

/**
 *
 * @author 0186779
 */
public abstract class UpdateOperation {
    
    protected String n1;
    protected Object v1;
    protected TipoOperacao tipoOperacao;

    public UpdateOperation(String nomeCampo) {
        this.n1 = nomeCampo;
    }
    
    public String getClause(ContadorPassavel index) {
        index.cont++;
        
        return tipoOperacao.getValue()
                .replace("<n1>", n1)
                .replace("<v1>", n1+index.cont);
    }
    
    public void addParameters(NamedPreparedStatement query, ContadorPassavel index) {
        index.cont++;
        if (v1 == null){
            return;
        }
        if(v1 instanceof String){
            query.setParameter(n1+index.cont+"_updt", (String) v1);
        } else if(v1 instanceof Boolean){
            query.setParameter(n1+index.cont+"_updt", (Boolean) v1);
        } else if(v1 instanceof Long){
            query.setParameter(n1+index.cont+"_updt", (Long) v1);
        } else if(v1 instanceof Integer){
            query.setParameter(n1+index.cont+"_updt", (Integer) v1);
        } else if(v1 instanceof Double){
            query.setParameter(n1+index.cont+"_updt", (Double) v1);
        }
    }

    protected void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    
}
