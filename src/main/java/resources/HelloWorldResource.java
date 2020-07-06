/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import restFramework.GET;
import restFramework.InputVazia;
import restFramework.Path;
import restFramework.SemAuth;

/**
 *
 * @author Pedrivo
 */
@Path("hello")
public class HelloWorldResource {
    
    @Path("world")
    @GET
    @SemAuth
    public void helloWorld(InputVazia inp){
        inp.resp.addConteudo("mensagem", "Olá mundo "+inp.getUrlParam("nome", "Pessoa"));
    }
}
