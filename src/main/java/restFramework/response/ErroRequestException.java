/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

/**
 * Exception que, quando lançada, é convertida em um erro de request na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public class ErroRequestException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ErroRequestException(String message) {
        super(message);
    }
    
}
