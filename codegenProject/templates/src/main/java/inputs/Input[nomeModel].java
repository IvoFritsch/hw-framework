<#if root.model.config.getConfig("geraFront") != "true">${root.cancelaGeracao()}</#if>

package inputs;

import model.${root.model.nome};
import restFramework.MethodInput;
import restFramework.response.HwResponse;

public class Input${root.model.nome} extends MethodInput {

  public ${root.model.nome}.Grupos acao;

<#list root.model.listaTodosCampos as campo>
<#if campo.temAConfigIgualA("showOnly", "true")>
<#continue>
</#if>
<#if campo.temAConfigIgualA("isId", "true")>
<#continue>
</#if>
  public ${campo.tipoAsString} ${campo.nome};
</#list>

  @Override
  protected void filtraCampos() {
    if ( acao == null ) acao = ${root.model.nome}.Grupos.cadastroInicial;
  }

  @Override
  protected String getMethodNamespace() {
    return null;
  }
  
  @Override
  protected void validaCampos(HwResponse res) {
    
  }

}