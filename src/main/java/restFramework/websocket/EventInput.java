/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import java.util.ArrayList;
import restFramework.DaosProvider;

/**
 *
 * @author Ivo
 */
public class EventInput extends DaosProvider {
    public ArrayList<Integer> clientesInteressados;
    public String conteudo;

    public EventInput(ArrayList<Integer> clientesInteressados, String conteudo) {
        this.clientesInteressados = clientesInteressados;
        this.conteudo = conteudo;
    }
    
    
    
}
