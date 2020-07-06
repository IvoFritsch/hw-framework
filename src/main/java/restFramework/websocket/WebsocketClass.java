/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.sql.Connection;

import restFramework.DaosProvider;
import restFramework.Database;
import restFramework.RestApi;

/**
 * Classe pai de todas classes gerenciadoras de websockets.
 * Utilizar essa classe sempre que for implementar um endpoint websocket nos sistemas da Haftware.<br>
 * 
 * @author Ivo
 */
public abstract class WebsocketClass {
    
    private volatile int nextClientId = 0;
    private final Map<Integer, WebsocketClient> clients = new ConcurrentHashMap<>();
    private final Set<Integer> clientesInteressados = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean aceitando = true;
    String wsAddress; 
    private volatile long lastUpdate = System.currentTimeMillis();
    
    
    boolean validaClienteInternal(WebsocketClient client){
        if(!aceitando) return false;
        return validaCliente(client);
    }
    
    synchronized void putClient(WebsocketClient client){
        client.putProperty("clientId", getNextClientId());
        client.setReadyToSend(true);
        DaosProvider daos = new DaosProvider();
        try(Connection con = Database.getTransaction()){
            daos.setConnection(con);
            clientConnected(client,daos);
            if(daos.precisaCommitar());
                con.commit();
        }catch(Exception e){
            // System.err.println("Exception no clientConnected da classe websocket "+this.getClass().getSimpleName());
        }
        clients.put(nextClientId, client);
        nextClientId++;
    }
    
    void updateLastUpdate(){
        lastUpdate = System.currentTimeMillis();
    }
    
    boolean acionaListener(Method m, EventInput inp){
        if(!aceitando) return false;
        if(getClientsCount() < 1) return false;
        updateLastUpdate();
        boolean continua = false;
        if(inp.clientesInteressados == null) continua = true;
        if(!continua)
            for (Integer clienteInteressado : inp.clientesInteressados) {
                if(this.clientesInteressados.contains(clienteInteressado)){
                    continua = true;
                    break;
                }
            }
        if(!continua) return false;
        try {
            m.invoke(this, inp);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    void removeClient(Integer clientId){
        if(!aceitando) return;
        WebsocketClient client = clients.get(clientId);
        if(client == null)
            return;
        client.setReadyToSend(false);
        clients.remove(clientId);
        clientDisconnected(client);
    }
    
    int getNextClientId() {
        return nextClientId;
    }
    
    boolean isInactive(){
        // Caso não tem mais nenhum cliente mas já teve em algum momento, esse websocket está inativo
        if(clients.isEmpty() && nextClientId > 0) return true;
        if(lastUpdate < (System.currentTimeMillis() - 600000)) return true;
        return false;
    }
    
    /**
     * Desconecta todos os clientes e encerra esse websocket
     * 
     */
    public void closeSocket(){
        if(!aceitando) return;
        // Indicamos que esse socket não está aceitando mais nada
        aceitando = false;
        // Esperamos 200 milisegundos para que as alterações nas estruturas de dados que estejam ocorrendo nesse momento finalizem
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
        }
        List<WebsocketClient> remover = new ArrayList<>();
        clients.forEach((i,c) -> remover.add(c));
        remover.forEach(c -> c.closeConnection("O socket foi fechado"));
        RestApi.getWebsocketManager().removeWsClass(wsAddress);
    }
    
    /**
     * Adiciona um cliente à lista de clientes interessados nos eventos.
     * 
     * @param cli ID do cliente
     */
    protected final void addClienteInteressado(Integer cli){
        clientesInteressados.add(cli);
    }
    
    /**
     * Remove um cliente da lista de clientes interessados nos eventos.
     * 
     * @param cli ID do cliente
     */
    protected final void removeClienteInteressado(Integer cli){
        clientesInteressados.add(cli);
    }
    
    /**
     * Retorna a quantidade de clientes conectados à esse websocket
     * 
     * @return quantidade de clientes conectados
     */
    public int getClientsCount() {
        return clients.size();
    }

    /**
     * Retorna a lista de clientes desse websocket
     * 
     * @return lista de clientes
     */
    protected Map<Integer, WebsocketClient> getClients() {
        return clients;
    }
    
    /**
     * Envia a mensagem para todos os clientes conectados à esse socket
     * 
     * @param mensagem mensagem a enviar
     */
    protected void enviaMensagemTodosClientes(WebsocketMessage mensagem){
        clients.forEach((i,c) -> {
            c.enviaMensagem(mensagem);
        });
    }
    
    /**
     * Envia a mensagem para todos os clientes conectados à esse socket, exceto o cliente c
     * 
     * @param mensagem mensagem a enviar
     * @param client Cliente a desconsiderar
     * 
     */
    protected void enviaMensagemTodosClientesExceto(WebsocketMessage mensagem, WebsocketClient client){
        clients.forEach((i,c) -> {
            if(c == client) return;
            c.enviaMensagem(mensagem);
        });
    }
    
    /**
     * Essa função é chamada automaticamente assim que um cliente se conecta à esse websocket
     * 
     * @param client Cliente que acabou de se conectar
     * @param daos Provedor da acesso ao banco
     */
    protected abstract void clientConnected(WebsocketClient client, DaosProvider daos);
    
    /**
     * Essa função é chamada automaticamente assim que um cliente se desconecta do websocket
     *   Esse método já não pode mais mandar mensagens ao cliente pois ele não receberá
     * 
     * @param client Cliente que acabou de se desconectar
     */
    protected abstract void clientDisconnected(WebsocketClient client);
    
    /**
     * Essa função é chamada automaticamente para validar se um cliente pode se conectar ao socket
     * 
     * @param client Cliente que está tentando se conectar
     * @return true se o cliente puder se conectar, false caso contrário
     */
    protected abstract boolean validaCliente(WebsocketClient client);
}
