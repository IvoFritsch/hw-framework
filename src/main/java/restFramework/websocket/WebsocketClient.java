/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * Representa um cliente que está conectado à um websocket.
 * 
 * @author ivoaf
 */
public class WebsocketClient {
    private final Session session;
    private final String connectParam;
    private boolean readyToSend = false;

    WebsocketClient(Session session, String connectParam) {
        this.session = session;
        this.connectParam = connectParam;
    }
    
    void setReadyToSend(boolean readyToSend) {
        this.readyToSend = readyToSend;
    }
    
    
    /**
     * Extende a validade do cliente em mais minutos
     * 
     * @param minutos Quantidade de minutos a extender
     */
    void extendeValidade(int minutos){
        
    }
    
    /**
     * Fecha a conexão com esse cliente e o remove da lista.
     * 
     */
    public void closeConnection(){
    
    }
    
    /**
     * Fecha a conexão com esse cliente e o remove da lista enviando junta uma mensagem de motivo.
     * 
     * @param motivo Um texto com o motivo do fechamento, null para um texto vazio
     */
    public void closeConnection(String motivo){
        try{
            if(motivo == null){
                session.close();
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, motivo));
            }
        } catch(Exception e){
            
        }
    }
    
    /**
     * Coloca uma propriedade nesse cliente, as propriedade podem ser recuperadas a qualquer momento.
     * 
     * @param nome nome da propriedade
     * @param valor valor da propriedade
     */
    public void putProperty(String nome, Object valor){
        session.getUserProperties().put(nome, valor);
    }
    
    /**
     * Retorna uma propriedade desse cliente.
     * 
     * @param nome nome da propriedade
     * @return valor da propriedade, null se não existir
     */
    public Object getProperty(String nome){
        return session.getUserProperties().get(nome);
    }

    /**
     * Retorna o parâmetro de url que o cliente utilizou para se conectar à esse websocket.<br>
     * O parâmetro de URL é utilizado para a classe de alto nível diferenciar os vários clientes que se conectaram à ela.<br>
     * O parâmetro de URL vem logo depois do id do websocket: /websocket/{id}/{connectParam}<br>
     * 
     * @return parâmetro de url
     */
    public String getConnectParam() {
        return connectParam;
    }

    /**
     * Retorna o parâmetro de url que o cliente utilizou para se conectar à esse websocket casteado em formato Integer.<br>
     * 
     * @return parâmetro de url, null se não for um Integer
     */
    public Integer getConnectParamAsInteger() {
        try{
            return new Integer(connectParam);
        }catch(Exception ex){
            return null;
        }
    }
    
    /**
     * Envia uma mensagem para o cliente de maneira assíncrona.
     * 
     * @param mensagem mensagem a enviar
     */
    public void enviaMensagem(WebsocketMessage mensagem){
        if(!readyToSend) return;
        session.getAsyncRemote().sendText(mensagem.getJson());
    }
}
