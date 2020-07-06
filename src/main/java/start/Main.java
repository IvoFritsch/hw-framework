/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package start;

import javax.servlet.annotation.WebServlet;

import resources.HelloWorldResource;
import restFramework.RestApi;

/**
 *
 * @author Ivo
 */
@WebServlet(urlPatterns = "/api/*")
public class Main extends RestApi{
    private static final long serialVersionUID = 1L;

    @Override
    protected void configura() {
        
        setNomeProjeto("hw-framework");
        setVersion(1.00);
        if(RestApi.HW_PRODUCTION){
            // Endereço do servidor principal
            setBackendBaseUrl("http://localhost:8080/");
            setFrontendBaseUrl("http://localhost:9999/");
        } else {
            // Endereço do servidor de testes
            setBackendBaseUrl("http://localhost:8080/");
            setFrontendBaseUrl("http://localhost:9999/");
            setCorsHeader("*");
        }
        // ---------------------------------------
        // Adiciona classes com métodos de filtro dos métodos da API
        addFiltersClass(Filtros.class);
        // ---------------------------------------
        // Adiciona classes com métodos de tasks agendadas da API
        addTasksClass(Tasks.class);
        // ---------------------------------------
        // Adiciona todas as classes de recurso da API
        addResourceClass(HelloWorldResource.class);
        // ---------------------------------------
        // Adiciona uma lista de emails para receberem emaisls automático de erro quando estiver rodando em produção
        setEnviarEmailsErro(RestApi.HW_PRODUCTION);
        // addDestinatarioEmailsErro("<email_1@gmail.com>");
        // ---------------------------------------
        // Nome do remetente dos emails
        setNomeEnviadorEmails("Haftware SI");
        // ---------------------------------------
        // Informações de conexão com o banco
        configDatabaseConnection("jdbc:hsqldb:hsql://localhost:7030/hw-framework", 
                                    "SA", "");
        // ---------------------------------------
        // Chave de API do Sendgrid
        // setSendGridApiKey("<api_key>");
        // ---------------------------------------
        // Chave de API Facebook
        // setFacebookAppTokens("<app_id>", "<app_secret>");
    }

    @Override
    protected void shutdown() {
    }
}
