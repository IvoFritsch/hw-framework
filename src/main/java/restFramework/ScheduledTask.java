/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Representa uma task agendada que está rodando no servidor.<br>
 * Essa é uma classe interna do framework
 * 
 * @author Ivo
 */
public class ScheduledTask implements Runnable{

    public Method metodo;
    public Object resource;
    public double intervalo;
    public String nome;
    public boolean transacional = false;
    public boolean runOnStartup = false;
    public String[] metodosLockNames;
    public ArrayList<ApiMethod> metodosLock = new ArrayList<>();
    public boolean horaria = true;
    public String momentoRodar;
    
    
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try (Connection transacao = Database.getTransaction()){
            metodosLock.forEach(l -> l.lock());
            try(DaosProvider daosProvider = new DaosProvider()){
                if(transacional){
                    daosProvider.transacional = true;
                    daosProvider.setConnection(transacao);
                }
                metodo.invoke(resource, daosProvider);
                if(transacional && daosProvider.precisaCommitar()){
                    transacao.commit();
                }
            }
        } catch (Exception ex) {
            if(!(ex instanceof RollbackException)){
                if(RestApi.enviarEmailsErro) enviaEmailErroInterno(ex);
                else {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    String stringErros = errors.toString();
                    stringErros = stringErros.substring(stringErros.indexOf("Caused by: ")+"Caused by: ".length());
                    System.out.println(stringErros);
                }
            }
        } finally {
            List<ApiMethod> toUnlock = (List<ApiMethod>) metodosLock.clone();
            Collections.reverse(toUnlock);
            toUnlock.forEach(l -> l.unlock());
        }
    }
    
    public int getHoraAgendada(){
        return new Integer(momentoRodar.split("[:]")[0]);
    }
    
    public int getMinutoAgendado(){
        return new Integer(momentoRodar.split("[:]")[1]);
    }
    
    public void validaHoraAgendada(){
        getHoraAgendada();
        getMinutoAgendado();
    }
    
    public int calculaDelayInicial(){
        long hoje = LocalDateTime.now().until(LocalDate.now().atTime(getHoraAgendada(), getMinutoAgendado()), ChronoUnit.SECONDS);
        long amanha = LocalDateTime.now().until(LocalDate.now().plusDays(1).atTime(getHoraAgendada(), getMinutoAgendado()), ChronoUnit.SECONDS);
        if(hoje > 0) return (int)hoje;
        return (int) amanha;
    }
    
    private void enviaEmailErroInterno(Exception e){
        String email = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "	<head>\n" +
                    "		<meta charset=\"utf-8\"/>\n" +
                    "		<title>Email automático da Haftware SI</title>\n" +
                    "		<meta name=\"viewport\" content=\"width=device-width\"/>\n" +
                    "	</head>\n" +
                    "	<body style=\"margin: 0; padding: 0; font-family: 'Verdana';\">\n" +
"		<div style=\"margin: 0 auto; max-width: 800px; padding: 10px;\">" +
                            "<h1>Ocorreu um erro durante a execução de uma task agendada no "+RestApi.nomeProjeto+":</h1>" +
                            "<p style=\"\n" +
"				font-size: 14px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: blue;\">Momento: "+new Date().toString()+"</p><br/>"+
                            "<hr/><p style=\"\n" +
"				font-size: 14px;\n" +
"				padding: 0px;\n" +
"				margin: 0px;\n" +
"				color: green;\">Nome da Task: "+"["+this.nome+"]</p><br/>"+
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
        ApiMethod.getDestinatariosEmailsErro().forEach(d -> RestApi.getEmailSender().send(d, "Ocorreu um erro interno em uma task agendada do "+RestApi.nomeProjeto, email));
    }
}