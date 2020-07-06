/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import java.sql.Connection;

import restFramework.Database;

/**
 * Essa é a classe controladora dos websockets do Haftware REST Framework.
 * Utilizar essa classe sempre que se quiser abrir websockets nos sistemas da Haftware.
 * 
 * @author ivoaf
 */
@ServerEndpoint("/websocket/{websocketId}/{connectParam}")
public class WebsocketManager implements Runnable{
    
    private static Map<String,WebsocketClass> websockets;
    // A organização desse Map é conforme o seguinte:
    // Cada classe que implementa uma 'WebsocketClass' tem o seu próprio mapa de strings X métodos
    private static Map<Class<?>, Map<String, Method>> metodosWebsocket;
    // A organização desse Map é conforme o seguinte:
    // Cada classe que implementa uma 'WebsocketClass' tem o seu próprio mapa de strings X listeners
    private static Map<Class<?>, Map<String, Method>> listenersClass;
    // Esse é o mapa que é buscado sempre que um evento é disparado, representa o nome do evento x Lista de métodos que o implementam.
    private static Map<String, List<WebsocketClass>> eventsListeners;
    
    private static Executor disparadorEventos;
    

    public WebsocketManager() {
        if(websockets == null)
            websockets = new ConcurrentHashMap<>();
        if(metodosWebsocket == null)
            metodosWebsocket = new ConcurrentHashMap<>();
        if(listenersClass == null)
            listenersClass = new ConcurrentHashMap<>();
        if(eventsListeners == null)
            eventsListeners = new ConcurrentHashMap<>();
        if(disparadorEventos == null)
            disparadorEventos = Executors.newCachedThreadPool();
    }
    
    /**
     * Abre um novo endpoint websocket, que será tratado pela classe c.
     * Retorna um identificador único para esse endpoint, os clientes usam esse idenficador para se conectar ao socket criado.
     *
     * @param c Classe que irá tratar os eventos do websocket
     * @return identificador do socket recém aberto, conectar-se à ele com a url /websocket/{id}/{connectParam}, null se não pôde criar
     */
    public String abreWebsocket(WebsocketClass c){
        // Se ainda não mapeamos os métodos dessa classe
        mapeiaMetodosListenersClasse(c.getClass());
        preparaListenersClasse(c);
        String id = UUID.randomUUID().toString();
        c.wsAddress = id;
        websockets.put(id, c);
        return id;
    }
    
    /**
     * Dispara o evento indicado pelo nome no websocket indicado pelo ID.<br>
     * 
     * @param nome Nome do evento a ser disparado.
     * @param websocketId O identificador do websocket a receber o evento
     * @param conteudo Um conteúdo a passar aos listeners do evento
     * @return true se obteve sucesso, false caso contrário
     */
    public boolean triggerEvent(String nome, String websocketId, String conteudo){
        if(websocketId == null) return false;
        WebsocketClass c = websockets.get(websocketId);
        if(c == null) return false;
        Method listenerDisparar = listenersClass.get(c.getClass()).get(nome);
        if(listenerDisparar == null) return false;
        c.acionaListener(listenerDisparar, new EventInput(null, conteudo));
        return true;
    }
    
    /**
     * Dispara o evento indicado pelo nome no websocket indicado pela classe gerenciadora, todos os websockets abertos no momento com a classe gerenciadora indicada serão acionados.<br>
     * 
     * @param nome Nome do evento a ser disparado.
     * @param websocketClass A classe gerenciadora que recebe o evento
     * @param conteudo Um conteúdo a passar aos listeners do evento
     * @return true se obteve sucesso, false caso contrário
     */
    public boolean triggerEvent(String nome, Class<? extends WebsocketClass> websocketClass, String conteudo){
        if(websocketClass == null) return false;
        websockets.entrySet().stream().filter(c -> websocketClass.isAssignableFrom(c.getValue().getClass())).forEach(ws -> {
            WebsocketClass c = ws.getValue();
            Method listenerDisparar = listenersClass.get(c.getClass()).get(nome);
            if(listenerDisparar == null) return;
            c.acionaListener(listenerDisparar, new EventInput(null, conteudo));
        });
        return true;
    }
    
    /**
     * Dispara o evento indicado pelo nome nos websockets abertos.<br>
     * <b>ATENÇÂO: </b>O evento pode não ser disparado imediatamente, dependendo da carga no servidor no momento,
     * o procedimento de acionar os listeners é bastante complexo, por isso o framework enfileira isso e garante que o evento será disparado 
     * o quanto antes possível, mas não imediatamente.<br>
     * 
     * @param nome Nome do evento a ser disparado.
     * @param clienteInteressado Um Integer que normalmente representa o ID do usuário interessado nesse evento
     * @param conteudo Um conteúdo a passar aos listeners do evento
     */
    public void triggerEvent(String nome, Integer clienteInteressado, String conteudo){
        ArrayList<Integer> clientesInteressados = new ArrayList<>();
        clientesInteressados.add(clienteInteressado);
        triggerEvent(nome,clientesInteressados,conteudo);
    }
    
    /**
     * Dispara o evento indicado pelo nome nos websockets abertos.<br>
     * <b>ATENÇÂO: </b>O evento pode não ser disparado imediatamente, dependendo da carga no servidor no momento,
     * o procedimento de acionar os listeners é bastante complexo, por isso o framework enfileira isso e garante que o evento será disparado 
     * o quanto antes possível, mas não imediatamente.<br>
     * 
     * @param nome Nome do evento a ser disparado.
     * @param clientesInteressados Uma lista de integer representando os usuários interessados
     * @param conteudo Um conteúdo a passar aos listeners do evento
     */
    public void triggerEvent(String nome, ArrayList<Integer> clientesInteressados, String conteudo){
        
        disparadorEventos.execute(() -> {
            if(eventsListeners.get(nome) == null) return;
            eventsListeners.get(nome).forEach(c -> {
                c.acionaListener(listenersClass.get(c.getClass()).get(nome), new EventInput(clientesInteressados, conteudo));
            });
        });
    }
    
    void removeWsClass(String wsId){
        WebsocketClass classeRemovida = websockets.remove(wsId);
        Map<String, Method> listenersRemover = listenersClass.get(classeRemovida.getClass());
        listenersRemover.forEach((n,m) -> eventsListeners.get(n).remove(classeRemovida));
    }
    
    private static synchronized void preparaListenersClasse(WebsocketClass wsc) {
        listenersClass.get(wsc.getClass()).forEach((nome,metodo) -> {
            eventsListeners.get(nome).add(wsc);
        });
    }
    
    // Adiciona na árvore de métodos todos os métodos websocket da classe
    private static synchronized void mapeiaMetodosListenersClasse(Class<?> c) {
        // Se já mapeamos os métodos dessa classe
        if(metodosWebsocket.get(c) != null){
            return;
        }
        Map<String, Method> mapaMetodos = new ConcurrentHashMap<>();
        Map<String, Method> mapaListeners = new ConcurrentHashMap<>();
        metodosWebsocket.put(c, mapaMetodos);
        listenersClass.put(c, mapaListeners);
        Method[] m = c.getDeclaredMethods();
        for (Method method : m) {
            if (method.getAnnotation(WebsocketMethod.class) != null){
                if(method.getParameterCount() != 2){
                    System.err.println("Não foi possível adicionar o metodo websocket "+c.getSimpleName()+"."+method.getName()+", o método deve ter 2 parâmetros de entrada");
                    break;
                }
                if(!WebsocketClient.class.equals(method.getParameterTypes()[0])){
                    System.err.println("Não foi possível adicionar o metodo websocket "+c.getSimpleName()+"."+method.getName()+", o primeiro parâmetro de entrada deve ser um 'WebsocketClient'");
                    break;
                }
                if(!WebsocketMessage.class.equals(method.getParameterTypes()[1])){
                    System.err.println("Não foi possível adicionar o metodo websocket "+c.getSimpleName()+"."+method.getName()+", o segundo parâmetro de entrada deve ser uma 'WebsocketMessage'");
                    break;
                }
                System.out.println("Adicionando o método websocket "+c.getSimpleName()+"."+method.getName());
                mapaMetodos.put(method.getAnnotation(WebsocketMethod.class).value(), method);
            }
            if (method.getAnnotation(EventListener.class) != null){
                if(method.getParameterCount() != 1){
                    System.err.println("Não foi possível adicionar o listener websocket "+c.getSimpleName()+"."+method.getName()+", o método deve ter 1 parâmetro de entrada");
                    break;
                }
                String nomeListener = method.getAnnotation(EventListener.class).value();
                System.out.println("Adicionando o listener websocket "+c.getSimpleName()+"."+method.getName()+" : "+nomeListener);
                mapaListeners.put(nomeListener, method);
                if(!eventsListeners.containsKey(nomeListener)){
                    eventsListeners.put(nomeListener, Collections.synchronizedList(new LinkedList<>()));
                }
            }
        }
    }
    
    @OnOpen
    public void onOpen(@PathParam("websocketId") final String websocketId, @PathParam("connectParam") final String connectParam, final Session session) {
        //session.setMaxIdleTimeout(3000000);
        session.setMaxIdleTimeout(25000);
        //System.out.println("   oi: "+session.getId());
        WebsocketClass c = websockets.get(websocketId);
        session.getUserProperties().put("websocketId", websocketId);
        //  Caso não exista uma classe para tratar esse websocket
        if(c == null){
            throw new RuntimeException("Esse não é um websocket válido e aberto para conexões.");
        }
        WebsocketClient novoClient = new WebsocketClient(session, connectParam);
        if(!c.validaClienteInternal(novoClient)){
            throw new RuntimeException("A classe gerenciadora desse websocket não permitiu o ingresso.");
        }
        c.putClient(new WebsocketClient(session, connectParam));
        c.updateLastUpdate();
    }
    
    @OnClose
    public void onClose(final Session session){
        //System.out.println("tchau: "+session.getId());
        String websocketId = (String)session.getUserProperties().get("websocketId");
        WebsocketClass c = websockets.get(websocketId);
        if(c==null) return;
        c.removeClient((Integer)session.getUserProperties().get("clientId"));
    }
    
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, throwable.getLocalizedMessage()));
        } catch (Exception ex) {
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session){
        String websocketId = (String)session.getUserProperties().get("websocketId");
        WebsocketClass c = websockets.get(websocketId);
        if(c==null) {
            try {
                session.close();
            } catch (IOException ex) {}
            return;
        }
        if("ping".equals(message)) {
            return;
        }
        WebsocketMessage wsMessage = null;
        try{
            wsMessage = WebsocketMessage.fromJson(message);
        } catch (Exception e){
            session.getAsyncRemote().sendText("{\"erro\":\"Json inaceitável\"}");
            return;
        }
        Method metodo = null;
        if(wsMessage.getMetodo() != null)
            metodo = metodosWebsocket.get(c.getClass()).get(wsMessage.getMetodo());
        if(metodo == null){
            session.getAsyncRemote().sendText("{\"erro\":\"Método não existe nesse websocket: "+wsMessage.getMetodo()+"\"}");
            return;
        }
        try(Connection con = Database.getTransaction()){
            wsMessage.setConnection(con);
            metodo.invoke(c, c.getClients().get((Integer)session.getUserProperties().get("clientId")), wsMessage);
            c.updateLastUpdate();
            if(wsMessage.precisaCommitar())
                con.commit();
        } catch (Exception ex) {
            System.err.println("Exception no método websocket "+c.getClass().getSimpleName()+"."+metodo.getName());
        }
    }

    /**
     * Retorna a quantidade de sockets abertos no servidor nesse momento
     * 
     * @return quantidade de sockets abertos no servidor nesse momento
     */
    public int getQtdWebsockets() {
        return websockets.size();
    }

    /**
     * Retorna se o websocket indicado existe.
     * 
     * @param websocketId ID do websocket a testar
     * @return true se o websocket existe, false caso contrario.
     */
    public boolean websocketExists(String websocketId) {
        if(websocketId == null) return false;
        WebsocketClass c = websockets.get(websocketId);
        return c != null && !c.isInactive();
    }
    
    /**
     * Esse método agendado serve para fazer a limpeza de sockets que estão inativos.
     * 
     */
    @Override
    public void run() {
       List<String> fechar = new ArrayList<>(websockets.size() / 3);
       websockets.forEach((id,c) -> {
           //System.out.println("Aberto:  "+id);
           if(c.isInactive()) fechar.add(id);
       });
       
       fechar.forEach(f -> websockets.get(f).closeSocket());
    }
    
    
    
}
