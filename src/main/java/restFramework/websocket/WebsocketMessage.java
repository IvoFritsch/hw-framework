/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;
import restFramework.DaosProvider;
import restFramework.JsonManager;

/**
 * Essa classe representa uma mensagem websocket, tanto de saída como de entrada.
 * 
 * @author Ivo
 */
public class WebsocketMessage extends DaosProvider{
    
    @Expose
    private String metodo = null;
    @Expose
    private final Map<String,Object> conteudo;
    private String json = "{}";
    private boolean alterouConteudo = false;

    public WebsocketMessage() {
        conteudo = new HashMap<>();
    }

    public WebsocketMessage(String mensagem) {
        conteudo = new HashMap<>();
        alterouConteudo = true;
        conteudo.put("mensagem", mensagem);
    }
    
    /**
     * Adiciona um conteúdo na mensagem.
     * 
     * @param nome Nome(chave JSON) do conteúdo adicionar
     * @param valor Valor do conteúdo
     * @return A própria classe, para chamadas em sequencia.
     */
    public WebsocketMessage addConteudo(String nome, Object valor){
        conteudo.put(nome, valor);
        alterouConteudo = true;
        return this;
    }
	
    String getMetodo() {
        return metodo;
    }
	
	/**
     * Define o metodo dessa mensagem.
	 *
     * @param metodo O nome do método
     * @return A própria classe, para chamadas em sequencia.
     */
    public WebsocketMessage setMetodo(String metodo) {
        this.alterouConteudo = true;
        this.metodo = metodo;
        return this;
    }

	/**
     * Retorna o valor do conteúdo identificado pelo nome
	 *
     * @param nome O nome do conteúdo a retornar
     * @return O conteúdo identificado pelo nome, null se não existir
     */
    public Object getConteudo(String nome) {
        return conteudo.get(nome);
    }
    
    
    String getJson(){
        if(alterouConteudo)
            json = JsonManager.toJsonOnlyExpose(this);
        return json;
    }
    
    static WebsocketMessage fromJson(String json){
        return JsonManager.getGsonWithoutExpose().fromJson(json, WebsocketMessage.class);
    }
}
