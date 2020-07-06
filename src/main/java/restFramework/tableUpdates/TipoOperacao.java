/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates;

/**
 *
 * @author 0186779
 */
public enum TipoOperacao {
    SET("<n1> = :<v1>_updt"),
    SET_NULL("<n1> = NULL"),
    ADD("<n1> = <n1> + :<v1>_updt"),
    SUBTRACT("<n1> = <n1> - :<v1>_updt"),
    MULTIPLY("<n1> = <n1> * :<v1>_updt"),
    DIVIDE("<n1> = <n1> / :<v1>_updt"),
    INVERT("<n1> = <n1> * -1 + 1"),
    CUSTOM_CLAUSE("<n1>"),
    CONCAT_FIM("<n1> = CONCAT(<n1>, :<v1>_updt)"),
    CONCAT_INICIO("<n1> = CONCAT(:<v1>_updt, <n1>)");
    
    private final String value;
    
    private TipoOperacao(String s) {
        value = s;
    }
    
    public String getValue() {
       return this.value;
    }
}
