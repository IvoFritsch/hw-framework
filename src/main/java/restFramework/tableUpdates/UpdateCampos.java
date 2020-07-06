/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates;

import java.util.ArrayList;
import java.util.List;
import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.ContadorPassavel;
import restFramework.tableUpdates.updateOperations.UpdateOperation;

/**
 *
 * @author 0186779
 */
public abstract class UpdateCampos {
    
    protected List<UpdateOperation> updates = new ArrayList<>();
    
    protected void addUpdate(UpdateOperation uo){
        updates.add(uo);
    }
    
    public String getSetClause(ContadorPassavel index){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (UpdateOperation update : updates) {
            sb.append(update.getClause(index));
            if(i < updates.size()-1) sb.append(", ");
            i++;
        }
        return sb.toString();
    }
    
    public void addParameters(NamedPreparedStatement query){
        ContadorPassavel index = new ContadorPassavel();
        updates.forEach(u -> {
            u.addParameters(query, index);
        });
    }
}
