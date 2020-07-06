/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

/**
 * Enumerado que representa os possíveis status de request retornados pelos sistemas REST da Haftware.<br>
 * 
 * @author Ivo
 */
public enum StatusResposta {
    OK, ERRO_VALIDACAO, REQUEST_INVALIDO, RECUSADO, ERRO_INTERNO, NAO_AUTORIZADO;
}
