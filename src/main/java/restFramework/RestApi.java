/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.sql.Connection;

import restFramework.emailSender.EmailSender;
import restFramework.filesman.FilesManager;
import restFramework.onesignal.OnesignalConstants;
import restFramework.response.HwResponse;
import restFramework.social.FacebookUserInfo;
import restFramework.websocket.WebsocketManager;

/**
 * Classe pai das APIs REST da Haftware
 * 
 * @author Ivo
 */
public abstract class RestApi extends HttpServlet{
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    // Caso essa flag esteja setada, é porque está rodadno dentro de um ambiente de
    // containers da Haftware
    public static final boolean HW_PRODUCTION = "true".equals(System.getenv("HW_PRODUCTION"));
    public static boolean OMIT_LOGS = false;
    public static Double version = 1.0;
    
    private static Map<String, Map<String,ApiMethod>> metodos;
    private static Map<String, ApiMethod> metodosPorNomeDeFuncao;
    private static Map<String,MethodFilter> filtros;
    private static Map<String,ScheduledTask> tasks;
    private static Map<String, Supplier<JSONObject>> staticMethods;

    private static Map<String,String> resourceFiles;
    private static AuthManager authManager = null;
    private static KeyValueStorage keyValueStorage = null;
    private static EmailSender emailSender = null;
    private static SmsSender smsSender = null;
    private static WebsocketManager websocket;
    private static FilesManager filesman;
    private static ScheduledExecutorService scheduler;
    private static int initErrorsCounter;
    private static boolean readyToAccept;
    
    public static String corsHeader = null;
    public static String frontendBaseUrl;
    public static String backendBaseUrl;
    public static String nomeProjeto;
    public static boolean enviarEmailsErro;
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, metodos.get("GET"), "GET");
        
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, metodos.get("POST"), "POST");
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, metodos.get("DELETE"), "DELETE");
        
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, metodos.get("PUT"), "PUT");
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp){
        
        // Adiciona permissão para localhost
        String requestURL = req.getHeader("Referer");
        String corsOverride = corsHeader;
        try {
            if(requestURL != null && !requestURL.startsWith("https://www.")){
                if(requestURL.startsWith("http://localhost")){
                    corsOverride = "http://localhost:"+new URL(requestURL).getPort();
                } else if(requestURL.replace("https://", "").split("/", 2)[0].endsWith(".haftware.now.sh")){
                    corsOverride = "https://"+ requestURL.replace("https://", "").split("/", 2)[0];
                }
            }
        } catch (MalformedURLException ex){}
        // Headers para o browser não bloquear os requests de dominios diferentes
        if(corsHeader != null){
            resp.addHeader("Access-Control-Allow-Origin", corsOverride);
            resp.addHeader("Access-Control-Allow-Headers", "authId, Content-Type");
            resp.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
        }
    }

    private void doMethod(HttpServletRequest req, HttpServletResponse resp, Map<String,ApiMethod> listaMetodos, String httpMethod) throws ServletException, IOException {
        
        //System.out.println(req.getRequestURI().substring(req.getRequestURI().indexOf(req.getServletPath())+req.getServletPath().length()+1));
        long entry = System.currentTimeMillis();
        resp.setCharacterEncoding("UTF-8");
        
        // Adiciona permissão para localhost
        String requestURL = req.getHeader("Referer");
        String corsOverride = corsHeader;
        try {
            if(requestURL != null && !requestURL.startsWith("https://www.")){
                if(requestURL.startsWith("http://localhost")){
                    corsOverride = "http://localhost:"+new URL(requestURL).getPort();
                } else if(requestURL.replace("https://", "").split("/", 2)[0].endsWith(".haftware.now.sh")){
                    corsOverride = "https://"+ requestURL.replace("https://", "").split("/", 2)[0];
                }
            }
        } catch (MalformedURLException ex){}
        if(corsHeader != null){
            resp.addHeader("Access-Control-Allow-Origin", corsOverride);
            resp.addHeader("Access-Control-Allow-Headers", "authId, Content-Type");
            resp.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
        }
        resp.setContentType("application/json");
        
        // --------------------------------------------
        ApiMethod metodoChamar = null;
        String path = "";
        String pathParam = null;
        try{
            if(listaMetodos == null) throw new Exception();
            path = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getServletPath())+req.getServletPath().length()+1);
            if(path.equals("version")){
                PrintWriter writer = resp.getWriter();
                writer.write("{\"version\":" + RestApi.version + "}");
                return;
            }
            if(path.equals("locks")){
                PrintWriter writer = resp.getWriter();
                writer.write("total of "+ DaosProvider.qtdLocks() +" locks currently open");
                return;
            }
            Supplier<JSONObject> staticFunction = staticMethods.get(path);
            if(staticFunction != null){
              PrintWriter writer = resp.getWriter();
              writer.write(staticFunction.get().toString(2));
              return;
            }
            metodoChamar = listaMetodos.get(path);
            boolean useiParam = false;
            if(metodoChamar == null){
                int lastBar = path.lastIndexOf('/');
                String pathStart = path.substring(0, lastBar);
                metodoChamar = listaMetodos.get(pathStart);
                useiParam = true;
            }
            try{
                if(metodoChamar != null && metodoChamar.paramIndex > -1) pathParam = URLDecoder.decode(path.substring(metodoChamar.paramIndex), "UTF-8");
            } catch(Exception e){}
            //Caso usou parametro mas o método não aceita parêmetros
            if(useiParam && metodoChamar != null && metodoChamar.paramIndex < 0) metodoChamar = null;
        } catch (Exception e){
            metodoChamar = null;
        }
        PrintWriter writer = resp.getWriter();
        HwResponse hwResp = new HwResponse();
        if(!readyToAccept){
            hwResp.addErroInterno("O Haftware REST Framework não foi inicializado OK e por isso não está aceitando requests, verifique o log de saida do servidor para detalhes.");
            writer.write(hwResp.getJson());
            return;
        }
        if(metodoChamar == null) 
            hwResp.addErroRequest("A URL chamada não foi encontrada no servidor");
        else
            metodoChamar.invoke(hwResp, req, req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), pathParam);
        writer.write(hwResp.getJson());
        if (!OMIT_LOGS && !HW_PRODUCTION) System.out.println(httpMethod + " > "+ path + " - " + (System.currentTimeMillis() - entry)+" ms");
    }
    
    @Override
    public void init() throws ServletException {
        System.out.println("..."
                + "\n---------------------------------------------------------------------------------------------------------------------");
        System.out.println("Inicializando o Haftware REST Framework...");
        if(RestApi.HW_PRODUCTION) System.out.println("Detectado container Docker, iniciando em modo produção...");
        System.out.println("Liberando conexões SSL...");
        try{
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        initErrorsCounter = 0;
        readyToAccept = false;
        metodos = new HashMap<>();
        staticMethods = new HashMap<>();
        metodos.put("GET", new HashMap<>());
        metodos.put("POST", new HashMap<>());
        metodos.put("DELETE", new HashMap<>());
        metodos.put("PUT", new HashMap<>());
        filtros = new HashMap<>();
        tasks = new HashMap<>();
        resourceFiles = new HashMap<>();
        metodosPorNomeDeFuncao = new HashMap<>();
        System.out.println("Inicializando websockets...");
        websocket = new WebsocketManager();
        emailSender = new EmailSender();
        smsSender = new SmsSender();
        System.out.println("Carregando mensagens em multiidiomas...");
        IdiomasUtils.init(getServletContext());
        System.out.println("Configurando...");
        configura();
        tasks.forEach((n, st) -> {
            if(st.metodosLockNames == null) return;
            Arrays.sort(st.metodosLockNames);
            for (String metodoLock : st.metodosLockNames) {
                ApiMethod add = metodosPorNomeDeFuncao.get(metodoLock);
                if(add == null){
                    initErrorsCounter++;
                    System.err.println("[ERRO] A task agendada " + st.nome + " informa que deveria pausar o método "+metodoLock + ", mas esse método não existe");
                    continue;
                }
                if(st.metodosLock.contains(add)){
                    initErrorsCounter++;
                    System.err.println("[ERRO] Método "+ metodoLock+" duplicado na anotação @PausaMetodos da task agendada " + st.nome);
                    continue;
                }
                st.metodosLock.add(add);
            }
        });
        
        System.out.println("Atualizando banco de dados...");
        try(DatabaseUpdater du = new DatabaseUpdater(getServletContext())){
            
        } catch(Exception ex){
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possível completar com sucesso a atualização do banco de dados:\n"+ex.getMessage());
        }
        scheduler = Executors.newScheduledThreadPool(tasks.isEmpty() ? 1 : tasks.size());
        
        System.out.println("Inicializando controlador de autenticação...");
        try {
            setAuthManager(AuthManager.class);
        } catch (Exception ex) {
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possível inicializar o gerenciador de arquivos:\n"+ex.getMessage());
        }
        
        System.out.println("Inicializando gerenciador de arquivos...");
        try {
            filesman = new FilesManager();
        } catch (Exception ex) {
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possível inicializar o gerenciador de arquivos:\n"+ex.getMessage());
        }
        System.out.println("Inicializando conexão com banco...");
        try {
            keyValueStorage = new KeyValueStorage();
        } catch (Exception ex) {
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possível inicializar o banco de key-value:\n"+ex.getMessage());
        }
        metodos.forEach((httpMethod, list) -> {
            list.forEach((p,m) -> {
                m.putFiltros(filtros);
            });
        });
        
        if(initErrorsCounter == 0){
            System.out.println("Inicializando tasks agendadas...");
            // Inicializa o faxineiro de authIds antigos, rodando a cada 3 horas
            scheduler.scheduleAtFixedRate(authManager, 3, 3, TimeUnit.HOURS);
            // Inicializa o faxineiro de arquivos deletados, rodando a cada 2 horas
            if(filesman != null) scheduler.scheduleAtFixedRate(filesman, 2, 2, TimeUnit.HOURS);
            // Inicializa o faxineiro de websockets pendurados
            scheduler.scheduleAtFixedRate(websocket, 5, 5, TimeUnit.MINUTES);
            tasks.forEach((n,st) -> {
                int delayInicial = (int)(st.intervalo*60);
                int intervalo = (int)(st.intervalo*60);
                if(st.horaria)
                    delayInicial = st.calculaDelayInicial();
                if(st.runOnStartup) scheduler.schedule(st, 2, TimeUnit.SECONDS);
                scheduler.scheduleAtFixedRate(st, delayInicial, intervalo, TimeUnit.SECONDS);
            });
            readyToAccept = true;
            int mCount = 0;
            for ( Map<String, ApiMethod> mArr : metodos.values() ) {
                // for ( ApiMethod m : mArr.values() ) mCount++;
                mCount += mArr.values().size();
            }
            keyValueStorage.put("LAST_STARTUP", System.currentTimeMillis());
            System.out.println("Haftware REST Framework inicializou OK e está pronto para receber requests em:\n"
                    + "\t\t "+backendBaseUrl+"api/\n\n"
                            + "\t\t\t Quantidade de métodos:         "+mCount+"\n"
                            + "\t\t\t Quantidade de tasks agendadas: "+tasks.size()+"\n"
                            + "\t\t\t Quantidade de filtros:         "+filtros.size()+"\n"
                            + "---------------------------------------------------------------------------------------------------------------------");
        } else {
            readyToAccept = false;
            System.out.println("Foram detectados "+initErrorsCounter+" erros na inicialização do Haftware REST Framework\n"
                    + "\t\t Verifique acima as linhas iniciadas com '[ERRO]' para detalhes...");
        }
    }
    
    @Override
    public void destroy() {
        readyToAccept = false;
        shutdown();
        if(scheduler != null){
            System.out.println("Parando tarefas agendadas...");
            scheduler.shutdownNow();
        }
        System.out.println("Fechando conexões com o banco de dados...");
        Database.closeAllConnections();
    }
    
    protected abstract void configura();
    protected void shutdown(){};
    
    private static void setAuthManager(Class<?> cont){
        if(authManager != null) return;
        try {
            authManager = (AuthManager)cont.newInstance();
            System.out.println("Controlador de autenticação inicializado com sucesso.");
        } catch (Exception ex) {
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possível instanciar um controlador de autenticação");
        }
    }

    public static AuthManager getAuthManager() {
        return authManager;
    }

    /**
     * Chama a função especificada envolvendo-a em uma transação no banco.
     *  A função passada deve possuir como parametros um DaosProvider e um Object. Já como retorno um Object.
     * 
     * @param f Função a chamar
     * @param secondParam Segundo parâmetro a passar para a função após o DaosProvider
     * @return O retorno da função
     */
    public static Object callWithDaosProvider(BiFunction<DaosProvider, Object, Object> f, Object secondParam) {
        try (Connection transacao = Database.getTransaction()){
            Object ret = null;
            try(DaosProvider daosProvider = new DaosProvider()){
                daosProvider.transacional = true;
                daosProvider.setConnection(transacao);
                ret = f.apply(daosProvider, secondParam);
                if(daosProvider.precisaCommitar()){
                    transacao.commit();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            return ret;
        } catch (Exception ex) {
            if(!(ex instanceof RollbackException)){
                throw new RuntimeException(ex);
            }
        }
        return null;
    }
    

    /**
     * Chama a função especificada envolvendo-a em uma transação no banco.
     *  A função passada deve possuir como parametros um DaosProvider. Já como retorno um Object.
     * 
     * @param f Função a chamar
     * @return O retorno da função
     */
    public static Object callWithDaosProvider(Function<DaosProvider, Object> f) {
        try (Connection transacao = Database.getTransaction()){
            Object ret = null;
            try(DaosProvider daosProvider = new DaosProvider()){
                daosProvider.transacional = true;
                daosProvider.setConnection(transacao);
                ret = f.apply(daosProvider);
                if(daosProvider.precisaCommitar()){
                    transacao.commit();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            return ret;
        } catch (Exception ex) {
            if(!(ex instanceof RollbackException)){
                throw new RuntimeException(ex);
            }
        }
        return null;
    }
    
    protected final void addFiltersClass(Class<?> c){
        try {
            Object filtrosAdicionar = c.newInstance();
            Method[] m = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++){
                if (m[i].getAnnotation(Filtro.class) != null){
                    MethodFilter filtroAdicionar = new MethodFilter();
                    filtroAdicionar.nome = m[i].getAnnotation(Filtro.class).value();
                    filtroAdicionar.nomeCompleto = c.getSimpleName()+"."+filtroAdicionar.nome;
                    if(m[i].getParameterCount() > 0){
                        if(m[i].getParameterCount() > 2){
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar o filtro "+c.getSimpleName()+"."+filtroAdicionar.nome+", o filtro deve ter apenas 1 ou 2 parâmetros de entrada");
                            return;
                        }
                        if (!MethodInput.class.equals(m[i].getParameterTypes()[0])) {
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar o filtro "+c.getSimpleName()+"."+filtroAdicionar.nome+", o primeiro parâmetro de entrada do método deve ser da classe 'MethodInput'");
                            return;
                        }
                        if (m[i].getParameterCount() == 2 && !String.class.equals(m[i].getParameterTypes()[1])) {
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar o filtro "+c.getSimpleName()+"."+filtroAdicionar.nome+", o segundo parâmetro de entrada do método deve ser da classe 'MethodInput'");
                            return;
                        }
                    } else {
                        initErrorsCounter++;
                        System.err.println("[ERRO] Não foi possível adicionar o filtro "+c.getSimpleName()+"."+filtroAdicionar.nome+", ele deve possuir um parametro de entrada da classe 'MethodInput'");
                        return;
                    }
                    if (m[i].getParameterCount() == 2) {
                        filtroAdicionar.recebeNomeMetodo = true;
                    }
                    if(m[i].getAnnotation(SempreAplicado.class) != null){
                        filtroAdicionar.ignoravel = false;
                    }
                    if(m[i].getAnnotation(Prioritario.class) != null){
                        filtroAdicionar.prioritario = true;
                    }
                    filtroAdicionar.metodo = m[i];
                    filtroAdicionar.resource = filtrosAdicionar;
                    System.out.println("Adicionando o filtro "+c.getSimpleName()+"."+filtroAdicionar.nome);
                    filtros.put(filtroAdicionar.nome, filtroAdicionar);
                }
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
    
    protected final void addTasksClass(Class<?> c){
        
        try {
            Object tasksAdicionar = c.newInstance();
            Method[] m = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++){
                if (m[i].getAnnotation(Task.class) != null){
                    ScheduledTask taskAdicionar = new ScheduledTask();
                    taskAdicionar.nome = m[i].getName();
                    taskAdicionar.resource = tasksAdicionar;
                    try{
                        if(m[i].getAnnotation(Task.class).value().contains(":")){
                            taskAdicionar.intervalo = 1440.0;
                            taskAdicionar.horaria = true;
                            taskAdicionar.momentoRodar = m[i].getAnnotation(Task.class).value();
                            taskAdicionar.validaHoraAgendada();
                        } else {
                            taskAdicionar.intervalo = new Double(m[i].getAnnotation(Task.class).value());
                            taskAdicionar.horaria = false;
                        }
                    } catch (Exception e){
                        initErrorsCounter++;
                        System.err.println("[ERRO] Não foi possível adicionar a task agendada "+c.getSimpleName()+"."+taskAdicionar.nome+", valor da anotação 'Task' inválido");
                        return;
                    }
                    if(m[i].getParameterCount() > 0){
                        if(m[i].getParameterCount() > 1){
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar a task agendada "+c.getSimpleName()+"."+taskAdicionar.nome+", a task deve ter apenas um parâmetro de entrada");
                            return;
                        }
                        if (!DaosProvider.class.equals(m[i].getParameterTypes()[0])) {
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar a task agendada "+c.getSimpleName()+"."+taskAdicionar.nome+", o parâmetro de entrada do método deve ser da classe 'DaosProvider'");
                            return;
                        }
                    } else {
                        initErrorsCounter++;
                        System.err.println("[ERRO] Não foi possível adicionar a task agendada "+c.getSimpleName()+"."+taskAdicionar.nome+", ela deve possuir um parâmetro de entrada da classe 'DaosProvider'");
                        return;
                    }
                    if(m[i].getAnnotation(Transacional.class) != null){
                        taskAdicionar.transacional = true;
                    }
                    if(m[i].getAnnotation(RunOnStartup.class) != null){
                        taskAdicionar.runOnStartup = true;
                    }
                    if(m[i].getAnnotation(PausaMetodos.class) != null){
                        taskAdicionar.metodosLockNames = m[i].getAnnotation(PausaMetodos.class).value();
                    }
                    taskAdicionar.metodo = m[i];
                    System.out.println("Adicionando a task agendada "+c.getSimpleName()+"."+taskAdicionar.nome+
                            (taskAdicionar.horaria ? ", programada para rodar sempre às "+taskAdicionar.momentoRodar : ", com intervalo de "+taskAdicionar.intervalo+" minutos"));
                    tasks.put(taskAdicionar.nome, taskAdicionar);
                }
            }
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
    
    /**
     * Adiciona uma classe de recursos REST da API vinculada à URL indicada.<br>
     * O framework procurará todos os métodos contendo a anotação {@link Path} e os adicionara como métodos da API.<br>
     * As classes de recurso REST são acessadas atráves da URL /api/{url}/{metodo}<br>
     * 
     * @param c classe de recurso à adicionar
     */
    protected final void addResourceClass(Class<?> c){
        final int entryErros = initErrorsCounter;
        try {
            Annotation pathResource = c.getAnnotation(Path.class);
            if(pathResource == null){
                initErrorsCounter++;
                System.err.println("[ERRO] Não foi possível adicionar a classe de recurso "+c.getSimpleName()+", anotação Path não está presente");
                return;
            }
            String url = ((Path)pathResource).value();
            Object recursoAdicionar = c.newInstance();
            System.out.println("Adicionando recurso "+c.getSimpleName()+" para a URL '"+url+"'...");
            
            Method[] m = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++){
                if (m[i].getAnnotation(Path.class) != null){
                    String paramName = null;
                    ApiMethod metodoAdicionar = new ApiMethod();
                    String pathMetodo = url+"/"+m[i].getAnnotation(Path.class).value();
                    String[] parts = pathMetodo.split("/");
                    String lastPart = parts[parts.length - 1];
                    if(lastPart.startsWith("{") && lastPart.endsWith("}")){
                        pathMetodo = String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 1));
                        paramName = lastPart.replace("{", "").replace("}", "");
                        metodoAdicionar.paramIndex = pathMetodo.length() + 1;
                        String[] paramNameType = paramName.split(":");
                        paramName = paramNameType[0];
                        if(paramNameType.length > 1){
                            metodoAdicionar.paramType = paramNameType[1];
                        }else{
                            metodoAdicionar.paramType = "String";
                        }
                    }
                    // GET, POST, DELETE, PUT
                    String metodoHttp = "GET";
                    metodoAdicionar.nome = m[i].getName();
                    metodoAdicionar.nomeCompleto = c.getSimpleName()+"."+metodoAdicionar.nome;
                    metodoAdicionar.resource = recursoAdicionar;
                    metodoAdicionar.metodo = m[i];
                    metodoAdicionar.paramName = paramName;
                    if(m[i].getParameterCount() > 0){
                        if(m[i].getParameterCount() > 1){
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", o método deve ter apenas um parâmetro de entrada");
                            break;
                        }
                        if (MethodInput.class.isAssignableFrom(m[i].getParameterTypes()[0])){
                            try{
                            metodoAdicionar.classeInput = 
                                    MethodInput.class.cast(m[i].getParameterTypes()[0].newInstance()).getClass();
                            } catch (Exception e){
                                initErrorsCounter++;
                                System.err.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", não foi possível instanciar a input\n verifique se ela não é uma classe abstrata ou, caso seja uma classe  filha de outra, declare ela como static.");
                            }
                        } else {
                            initErrorsCounter++;
                            System.err.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", o parâmetro de entrada do método deve extender a classe 'MethodInput'");
                            break;
                        }
                    } else {
                        initErrorsCounter++;
                        System.err.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", ele deve possuir um parametro de entrada da classe 'MethodInput'\n Para um método que não recebe nada especial como entrada, use a classe 'InputVazia'");
                        break;
                    }
                    if(m[i].getAnnotation(POST.class) != null){
                        metodoHttp = "POST";
                    }
                    if(m[i].getAnnotation(DELETE.class) != null){
                        metodoHttp = "DELETE";
                    }
                    if(m[i].getAnnotation(PUT.class) != null){
                        metodoHttp = "PUT";
                    }
                    if(m[i].getAnnotation(Transacional.class) != null){
                        metodoAdicionar.transacional = true;
                    }
                    if(m[i].getAnnotation(SemAuth.class) != null){
                        metodoAdicionar.fazAuth = false;
                    }
                    if(m[i].getAnnotation(AplicaFiltros.class) != null){
                        metodoAdicionar.filtrosAplicar = m[i].getAnnotation(AplicaFiltros.class).value();
                    }
                    if(!metodoAdicionar.classeInput.equals(InputVazia.class) && metodoHttp.equals("GET")){
                        initErrorsCounter++;
                        System.out.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", métodos GET devem obrigatoriamente ter uma uma 'InputVazia' como entrada.");
                        break;
                    }
                    Map<String, ApiMethod> insertInto = metodos.get(metodoHttp);
                    if(insertInto.containsKey(pathMetodo)){
                        initErrorsCounter++;
                        System.err.println("[ERRO] Não foi possível adicionar o método "+c.getSimpleName()+"."+metodoAdicionar.nome+", pois o Path definido para ele já está sendo usado pelo método "+insertInto.get(url+"/"+pathMetodo).nomeCompleto);
                        break;
                    }
                    System.out.println("Adicionando o método "+c.getSimpleName()+"."+metodoAdicionar.nome+" ("+metodoHttp+") ao caminho '"+pathMetodo+"' da API");
                    insertInto.put(pathMetodo, metodoAdicionar);
                    metodosPorNomeDeFuncao.put(c.getSimpleName()+"."+metodoAdicionar.nome, metodoAdicionar);
                }
            }
        } catch (Throwable e) {
            if(entryErros == initErrorsCounter) {
                System.err.println("[ERRO] Ocorreu uma exception desconhecida durante a adição da classe de recurso "+c.getSimpleName()+":");
                e.printStackTrace();
                initErrorsCounter++;
            }
        }
    }
    
    /**
     * Lê e carrega na memória o conteúdo de um arquivo de recurso para posterior acesso.<br>
     * Os arquivos de recurso ficam na pasta web/WEB-INF/resources
     * 
     * @param name Nome do arquivo de recurso.
     */
    protected void useResourceFile(String name){
        useResourceFile(name, false);
    }
    protected void useResourceFile(String name, boolean multidiomas){
        try{
            if(multidiomas){
                String nomeArquivo = name.substring(0, name.lastIndexOf("."));
                String extensaoArquivo = name.substring(name.lastIndexOf(".")+1);
                
                IdiomasUtils.forEachIdioma(i -> {
                    try{
                        String nomeCompleto = nomeArquivo+"_"+i+"."+extensaoArquivo;
                        System.out.println(nomeCompleto);
                        resourceFiles.put(nomeCompleto, IOUtils.toString(getServletContext().getResourceAsStream("/WEB-INF/resources/"+nomeCompleto), "UTF-8"));
                    } catch(Exception e){}
                });
            } else {
                resourceFiles.put(name, IOUtils.toString(getServletContext().getResourceAsStream("/WEB-INF/resources/"+name), "UTF-8"));
            }
        }catch(Exception e){
            initErrorsCounter++;
            System.err.println("[ERRO] Não foi possivel carregar o arquivo de recurso "+name+":\n");
        }
    }
    
    /**
     * Retorna o conteudo de um arquivo de recurso carregado.
     * 
     * @param name Nome do arquivo de recurso.
     * @return Um mapa de [nome x conteúdo] dos arquivos de recurso
     */
    public static String getResourceFile(String name){
        
        String retorno = resourceFiles.get(name);
        
        if (retorno == null){
            throw new RuntimeException("Foi solicitado um arquivo de recurso que não está carregado: " + name);
        }
        return retorno;
    }
    /**
     * Retorna o conteudo de um arquivo de recurso carregado.
     * 
     * @param name Nome do arquivo de recurso.
     * @param idioma Idioma do arquivo.
     * @return Um mapa de [nome x conteúdo] dos arquivos de recurso
     */
    public static String getResourceFile(String name, String idioma){
        if(idioma == null) idioma = IdiomasUtils.idiomaDefault;
        String nomeArquivo = name.substring(0, name.lastIndexOf("."));
        String extensaoArquivo = name.substring(name.lastIndexOf(".")+1);
        String nomeCompleto = nomeArquivo+"_"+idioma+"."+extensaoArquivo;
        if(!resourceFiles.containsKey(nomeCompleto)){
            nomeCompleto = nomeArquivo+"_"+IdiomasUtils.idiomaDefault+"."+extensaoArquivo;
        }
        String retorno = resourceFiles.get(nomeCompleto);
        
        if (retorno == null){
            throw new RuntimeException("Foi solicitado um arquivo de recurso que não está carregado: " + name);
        }
        return retorno;
    }

    /**
     * Retorna o gerenciador dos websockets da api, que pode abrir e fechar websockets, assim como enviar eventos para eles.
     * 
     * @return o gerenciador de websockets rodando no servidor
     */
    public static WebsocketManager getWebsocketManager() {
        return websocket;
    }
    
    /**
     * Retorna o gerenciador de arquivos dinâmicos do framework, que controla, atualiza, exclui e gerencia todos os aspectos dos arquivos dos usuários do sistema.
     * 
     * @return o gerenciador de arquivos do framework
     */
    public static FilesManager getFilesManager() {
        return filesman;
    }
    
    /**
     * Retorna o gerenciador de armazenamento chave/valor que pode ser usado para persistir informações mesmo entre reiniciações do sistema
     * 
     * @return o gerenciador de armazenamento chave/valor
     */
    public static KeyValueStorage getKeyValueStorage() {
        return keyValueStorage;
    }
    
    /**
     * Retorna o a classe responsável por enviar emails.
     * 
     * @return enviador de emails do framework
     */
    public static EmailSender getEmailSender(){
        return emailSender;
    }
    
    
    /**
     * Retorna o a classe responsável por enviar SMS.
     * 
     * @return enviador de SMS do framework
     */
    public static SmsSender getSmsSender(){
        return smsSender;
    }
    
    public static String msg(String key, String idioma, Object... valores){
        return IdiomasUtils.getMensagem(key, idioma, valores);
    }
    
    /**
     * Retorna todos os arquivos de recurso carregados.
     * 
     * @return Um mapa de [nome x conteúdo] dos arquivos de recurso
     */
    public static Map<String, String> getResourceFiles() {
        return resourceFiles;
    }

    /**
     * Indica o header de cors a adicionar nos request de OPTIONS, null para desabilitar CORS
     * 
     * @param corsHeader valor do header
     */
    public static void setCorsHeader(String corsHeader) {
        RestApi.corsHeader = corsHeader;
    }
    
    /**
     * Indica a URL base de acesso externo ao backend projeto.
     * 
     * @param backendBaseUrl Url de acesso externo
     */
    protected static void setBackendBaseUrl(String backendBaseUrl) {
        if(!backendBaseUrl.endsWith("/"))
            backendBaseUrl = backendBaseUrl.concat("/");
        RestApi.backendBaseUrl = backendBaseUrl;
        if(frontendBaseUrl == null) frontendBaseUrl = RestApi.backendBaseUrl;
    }
    
    /**
     * Indica a URL base de acesso externo ao frontend projeto.
     * 
     * @param frontendBaseUrl Url de acesso externo
     */
    protected static void setFrontendBaseUrl(String frontendBaseUrl) {
        if(!frontendBaseUrl.endsWith("/"))
            frontendBaseUrl = frontendBaseUrl.concat("/");
        RestApi.frontendBaseUrl = frontendBaseUrl;
    }
    
    /**
     * Indica o nome do projeto que está usando o framework.
     * 
     * @param nomeProjeto Nome do projeto
     */
    protected static void setNomeProjeto(String nomeProjeto) {
        RestApi.nomeProjeto = nomeProjeto;
    }
    
    /**
     * Indica o a versão atual do sistema.
     * 
     * @param version Nome do projeto
     */
    protected static void setVersion(Double version) {
        RestApi.version = version;
    }
    
    /**
     * Define as as chaves do app do Facebook, para que o Framework consiga recuperar informações de usuário do Facebook
     *
     * @param appId ID do app do Facebook
     * @param appSecret Chave secreta do aplicativo, pode ser obtida em https://developers.facebook.com/apps/[appId]/settings/advanced/, no campo "Token de cliente"
     */
    protected void setFacebookAppTokens(String appId, String appSecret){
        FacebookUserInfo.setAppId(appId);
        FacebookUserInfo.setAppSecret(appSecret);
    }
    
    /**
     * Adiciona um email para receber os emails de erro interno que ocorrerem no sistema
     * 
     * @param email
     */
    protected void addDestinatarioEmailsErro(String email){
        ApiMethod.addDestinatarioEmailsErro(email);
    }
    
    /**
     * Define o nome do enviador dos emails, esse nome aparecerá para os usuários
     * 
     * @param nome
     */
    protected void setNomeEnviadorEmails(String nome){
        EmailSender.setFromName(nome);
    }
    
    /**
     * Define o máximo de locks que serão instanciadas por key, quanto maior o 
     * valor, mais requests simultaneos podem ser processados, mas mais memória 
     * é consumida(default: 600)
     * 
     * @param value
     */
    protected void setMaxLocksPerKey(int value){
        DaosProvider.setMaxLocksPerKey(value);
    }

    /**
     * Indica se deve ou não enviar emails de erro nos métodos, caso false, printa no console
     * 
     * @param enviarEmailsErro
     */
    public static void setEnviarEmailsErro(boolean enviarEmailsErro) {
        RestApi.enviarEmailsErro = enviarEmailsErro;
    }

    /**
     * Adiciona um método estático ao caminho indicado, o método deve retornar um JSONObject que é o corpo da resposta do método estático
     * 
     * @param method Caminho do método estático
     * @param f função responsável por suprir o método
     */
    protected void addStaticMethod(String method, Supplier<JSONObject> f){
      staticMethods.put(method, f);
    }
    
    protected void configDatabaseConnection(String url, String user, String password){
        if(Database.isFinished()){
            initErrorsCounter++;
            System.err.println("[ERRO] Tentativa de alterar a configuração do banco de dados, isso não é suportado pelo framework\n");
            return;
        }
        System.out.println("Configurando conexão com o banco de dados...");
        Database.setURL(HW_PRODUCTION ? "jdbc:hsqldb:hsql://banco-"+nomeProjeto+":7030/db" : url);
        Database.setUser(user);
        Database.setPassword(password);
        Database.finish();
    }
    
    protected void setOnesignalTokens(String appId, String authorization){
        OnesignalConstants.setAppId(appId);
        OnesignalConstants.setAuthorization(authorization);
    }

    protected void setSendGridApiKey(String key){
        EmailSender.setSendgridApiKey(key);
    }
}
