/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.PrintWriter;
import java.io.StringWriter;
import restFramework.response.HwResponse;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.http.HttpServletRequest;
import restFramework.response.ErroRequestException;

/**
 * Representa um metodo da API REST, essa classe é interna e não precisa se alterada
 * Os campos dessa classe são publicos por questões de performance
 * 
 * @author Ivo
 */
class ApiMethod {
    public Object resource;
    public Class<? extends MethodInput> classeInput;
    public String nome;
    public String nomeCompleto;
    public Method metodo;
    public String paramName;
    public int paramIndex = -1;
    public String paramType;
    public boolean transacional = false;
    public boolean fazAuth = true;
    public String[] filtrosAplicar = null;
    private List<MethodFilter> filtrosAplicaveis;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final List<String> destinatariosEmailsErro = new ArrayList<>();
    
    void invoke(HwResponse hwResponse, HttpServletRequest req, String json, String pathParam){
        hwResponse.req = req;
        Object pathParamToAdd = null;
        if(paramName != null && (pathParam == null || pathParam.isEmpty())){
            hwResponse.addErroRequest("Não foi passado o "+paramName+" na URL.");
            return;
        }
        if(pathParam != null){
            try{
                switch (paramType){
                    case "String": 
                        pathParamToAdd = pathParam;
                        break;
                    case "Integer":
                    case "int": 
                        pathParamToAdd = new Integer(pathParam);
                        break;
                    case "Long":
                    case "long": 
                        pathParamToAdd = new Long(pathParam);
                        break;
                    case "Double":
                    case "double": 
                        pathParamToAdd = new Double(pathParam);
                        break;
                    case "Boolean":
                    case "boolean": 
                        pathParamToAdd = pathParam.equals("true");
                        break;
                }
            }catch(Exception e){
                hwResponse.addErroRequest("Parâmetro "+paramName+" passado com tipo inválido, esperado "+paramType+".");
                return;
            }
        }
        if(!InputVazia.class.equals(classeInput) && (json == null || json.isEmpty())){
            hwResponse.addErroRequest("Esse método requer algum conteúdo de entrada para funcionar, mas nada foi recebido.");
            return;
        }
        AuthInfo authInfo = null;
        String authId = null;
        if(fazAuth){
            authId = req.getHeader("authId");
            authInfo = autentica(authId, hwResponse, req);
            if (authInfo == null) return;
        }
        try (Connection transacao = Database.getTransaction()){
            lock.readLock().lock();
            //System.out.println("Criada transacao: "+transacao );
            try(MethodInput input = (InputVazia.class.equals(classeInput) ? classeInput.newInstance() : MethodInput.fromJson(json, classeInput, req, paramName, pathParamToAdd))){
                
                if(input == null){
                    hwResponse.addErroRequest("O conteúdo enviado não é um JSON válido ou não está no formato de entrada esperado, STOP 1");
                    return;
                }
                
                input.setConnection(transacao);
                try(MethodContext ctx = new MethodContext(input)){
                    
                    if(input.parametros == null){
                        input.parametros = new HashMap<>();
                        if(paramName != null) {
                            input.parametros.put(paramName, pathParamToAdd);
                        }
                    }
                    try {
                        hwResponse.setMethodNamespace(input.getMethodNamespace());
                    } catch(UnsupportedOperationException e){}

                    input.req = req;
                    input.resp = hwResponse;
                    input.idUsuAutenticado = fazAuth ? authInfo.idUsuario : null;
                    input.authInfo = authInfo;
                    input.authId = authId;
                    input.nomeMetodo = nomeCompleto;
                    input.rawBody = json != null ? json : "";
                    if(transacional){
                        input.transacional = true;
                    }
                    try{
                        input.validaCampos(hwResponse);
                    } catch(NullPointerException e){
                        hwResponse.addErroRequest("O conteúdo enviado não é um JSON válido ou não está no formato de entrada esperado, STOP 2");
                        hwResponse.setStopPoint("INPUT_validaCampos_exception");
                        return;
                    } catch(ErroRequestException e){
                        hwResponse.addErroRequest(e.getMessage());
                    }
                    if(!hwResponse.isOk()){
                        hwResponse.setStopPoint("INPUT_validaCampos_validation");
                        return;
                    }
                    if(!aplicaFiltros(input)) return;
                    try {
                        metodo.invoke(resource, input);
                    } catch(Exception e){
                        if(e.getCause() instanceof ErroRequestException){
                            hwResponse.addErroRequest(e.getCause().getMessage());
                        } else {
                            throw e;
                        }
                    }
                    if(input.precisaCommitar() && !transacional){
                        hwResponse.addErroInterno("O método da API não pôde finalizar corretamente.");
                        hwResponse.setStopPoint("METHOD_commit_not_transactional");
                    }
                    if(transacional && input.precisaCommitar()){
                        transacao.commit();
                    }
                }
            }
        } catch (Exception ex) {
            if(!(ex.getCause() instanceof RollbackException)){
                hwResponse.addErroInterno("Ocorreu um erro interno no servidor. Caso esse erro persista, informe a Haftware");
                hwResponse.setStopPoint("METHOD_exception");
                if(RestApi.enviarEmailsErro) enviaEmailErroInterno(ex, req, json);
                else {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    String stringErros = errors.toString();
                    stringErros = stringErros.substring(stringErros.indexOf("Caused by: ")+"Caused by: ".length());
                    System.out.println(stringErros);
                }
            } else hwResponse.setStopPoint("METHOD_rollback");
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void lock(){
        lock.writeLock().lock();
    }
    
    public void unlock(){
        lock.writeLock().unlock();
    }
    
    // Aplica todos os filtros aplicaveis para este método
    private boolean aplicaFiltros(MethodInput inp) throws Exception{
        for(MethodFilter f : filtrosAplicaveis){
            f.aplicaFiltro(inp,nomeCompleto);
            if(!inp.resp.isOk()){
                inp.resp.setStopPoint("FILTER_"+f.nomeCompleto);
                return false;
            }
        }
        // Passou todos os filtros, retorna OK
        return true;
    }
    
    private AuthInfo autentica(String authId, HwResponse hwResponse, HttpServletRequest req){
        if(authId == null){
            hwResponse.addErroAutenticacao("Não foi passado o ID de autenticação 'authId'");
            return null;
        }
        AuthInfo authInfo = RestApi.getAuthManager().authIdValido(authId, req);
        if(authInfo == null){
            hwResponse.addErroAutenticacao("Autenticação inválida");
            return null;
        }
        return authInfo;
    }
    
    public void putFiltros(Map<String,MethodFilter> filtros){
        if(filtrosAplicar == null){
            filtrosAplicar =  new String[0];
        }
        this.filtrosAplicaveis = new ArrayList<>();
        filtros.forEach((n,f) -> {
            if(consideraFiltro(f)) {
                if(f.prioritario)
                    this.filtrosAplicaveis.add(0,f);
                else
                    this.filtrosAplicaveis.add(f);
            }
        });
    }
    
    private boolean consideraFiltro(MethodFilter filtro){
        if (!filtro.ignoravel){
            return true;
        }
        for (String filtroIgnorar : filtrosAplicar) {
            if(filtroIgnorar.equals(filtro.nome)){
                return true;
            }
        }
        return false;
    }

    static void addDestinatarioEmailsErro(String destinatarioEmailsErro) {
        ApiMethod.destinatariosEmailsErro.add(destinatarioEmailsErro);
    }

    static List<String> getDestinatariosEmailsErro() {
        return destinatariosEmailsErro;
    }
    
    private void enviaEmailErroInterno(Exception e, HttpServletRequest req,String json){
        String email = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "	<head>\n" +
                    "		<meta charset=\"utf-8\"/>\n" +
                    "		<title>Email automático da Haftware SI</title>\n" +
                    "		<meta name=\"viewport\" content=\"width=device-width\"/>\n" +
                    "	</head>\n" +
                    "	<body style=\"margin: 0; padding: 0; font-family: 'Verdana';\">\n" +
"		<div style=\"margin: 0 auto; max-width: 800px; padding: 10px;\">" +
                            "<h1>Ocorreu um erro interno no "+RestApi.nomeProjeto+":</h1>" +
                            "<p style=\"\n" +
"				font-size: 14px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: blue;\">Momento: "+new Date().toString()+"</p><br/>"+
                            "<p style=\"\n" +
"				font-size: 14px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: blue;\">URL: "+"["+req.getMethod()+"]"+req.getRequestURL().append('?').append(req.getQueryString()).toString()+"</p><br/>"+
                            "<hr/><p style=\"\n" +
"				font-size: 14px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: green;\">Input:<br/>"+JsonManager.identaJson(json)+"</p><br/>"+
                            "<hr/><p style=\"\n" +
"				font-size: 12px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: red;" + 
                                "text-align: left;\">Pilha: <br/>"+Utils.getFullStackTraceHtml(e)+"</p><br/>\n" +
"			<p style=\"\n" +
"				font-size: 11px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: grey;\n" +
"				text-align: right;\">Esse é um email enviado automaticamente pela Haftware Sistemas de Internet ltda., não é necessário responder.</p>\n" +
"			<hr style=\"margin-top: 1px;\"/>"
        + "</div>"+
                            
                    "   </body>\n" +
                    "</html>";
        destinatariosEmailsErro.forEach(d -> RestApi.getEmailSender().send(d, "Ocorreu um erro interno no servidor do "+RestApi.nomeProjeto, email));
    }
    
    
}
