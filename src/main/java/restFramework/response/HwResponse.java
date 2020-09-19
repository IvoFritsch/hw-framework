/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import restFramework.JsonManager;
import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import restFramework.Utils;

/**
 * Essa classe representa a estrutura de um resposta nos sistemas REST da Haftware.<br>
 * Essa classe tem sua contrapartida em javascript, a HW_Response.js<br>
 * Sempre usar essa classe para retornar respostas nas APIs rest da Haftware.<br>
 *
 * @author Ivo
 */
public class HwResponse {

    @Expose
    private StatusResposta status;
    @Expose
    // ATENCAO: Essa lista só é preenchida no método colocaErros()
    private List<Erro> erros;
    @Expose
    private Map<String,Object> conteudo;
    @Expose
    private MensagemAlerta mensagemAlerta;
    @Expose
    private String redirectTo;
    @Expose
    private String STOP_POINT;
    
    public HttpServletRequest req;
    private ErroValidacaoCampos erroValidacaoCampos;
    private ErroAutenticacao erroAutenticacao;
    private ErroRequest erroRequest;
    private ErroAutorizacao erroAutorizacao;
    private ErroInterno erroInterno;
    private String methodNamespace = "";
    private boolean setouMensagemRedirect = false;

    /**
     * Constrói um HwResponse vazia e com status = OK.
     * 
     */
    public HwResponse() {
        this.status = StatusResposta.OK;
    }
    
    /**
     * Indica se esta HwResponse está OK.<br>
     * Qualquer adição de erro na HwResponse faz com que o status deixe de ser OK e vire outra coisa, dependendo do erro adicionado.<br>
     *
     * @return true se estive OK, false caso contrário
     */
    public boolean isOk(){
        return this.status == StatusResposta.OK;
    }
    
    /**
     * Define uma mensagem de alerta a ser enviada juntamente com a resposta.
     *
     * @param mensagem A mensagem de alerta
     */
    public void setMensagemAlerta(MensagemAlerta mensagem){
        mensagemAlerta = mensagem;
    }
    
    
    /**
     * Define uma mensagem de alerta a ser enviada juntamente com a resposta.
     *
     * @param mensagem A mensagem de alerta
     * @return A mensagem para setar mais configurações.
     */
    public MensagemAlerta setMensagemAlerta(String mensagem){
        mensagemAlerta = new MensagemAlerta().mensagem(mensagem);
        return mensagemAlerta;
    }

    /**
     * (Equivalente ao setMensagemAlerta) Define uma mensagem de alerta a ser enviada juntamente com a resposta.
     *
     * @param mensagem A mensagem de alerta
     * @return A mensagem para setar mais configurações.
     */
    public MensagemAlerta setMensagem(String mensagem){
        return setMensagemAlerta(mensagem);
    }
    
    /**
     * Envia uma URL de redirect juntamente com a resposta.
     *
     * @param url A URL de redirect
     */
    public void sendRedirect(String url){
        redirectTo = url;
    }
    
    /**
     * Envia uma URL de redirect e prepara uma flash-message de alerta para o próximo request.<br>
     * Esse método faz com que, nessa HwResponse, seja enviada uma URL de redirect, 
     * preparando uma mensagem de alerta a ser enviada na próxima HwResponse subsequente desse mesmo cliente, em um intervalo de, no máximo, 5 segundos.<br>
     * Isso tem um efeito muito parecido com o "flash-redirect" do Spring.<br>
     *
     * @param url A URL de redirect
     * @param mensagem A mensagem de alerta
     */
    public void sendRedirect(String url, MensagemAlerta mensagem){
        redirectTo = url;
        req.getSession().setAttribute("redirectMessage", mensagem.withDate(new Date()));
        setouMensagemRedirect = true;
    }
    
    /**
     * Adiciona um erro de validação em um campo recebido na request.
     *
     * @param campo Nome do campo com erro, desde o root do JSON, ex.: "cliente.id"
     * @param codigo codigo O código que melhor representa o erro, se estiver em dúvida sobre qual utilizar, use "CodigoErroCampo.VALOR_INVALIDO"
     * @param mensagem A mensagem de erro no campo, essa mensagem normalmente é exibida diretamente ao usuário, deixar ela "bonita"
     */
    public void addErroValidacaoCampo(String campo, CodigoErroCampo codigo, String mensagem) {
        status = StatusResposta.ERRO_VALIDACAO;
        if (erroValidacaoCampos == null) {
            erroValidacaoCampos = new ErroValidacaoCampos();
        }
        erroValidacaoCampos.addCampo(methodNamespace+campo, codigo, mensagem);
    }
    
    /**
     * Adiciona um erro de autenticação no request.
     *
     * @param mensagem A mensagem de erro de autenticação
     */
    public void addErroAutenticacao(String mensagem){
        status = StatusResposta.RECUSADO;
        if(erroAutenticacao == null){
            erroAutenticacao = new ErroAutenticacao(mensagem);
        }
    }
    
    /**
     * Adiciona um erro de request inválido, quando o request não pôde ser aceito como um todo, não devido à um campo especifico.
     *
     * @param mensagem A mensagem de erro de request
     */
    public void addErroRequest(String mensagem){
        status = StatusResposta.REQUEST_INVALIDO;
        if(erroRequest == null){
            erroRequest = new ErroRequest(mensagem);
        }
    }
    
    /**
     * Adiciona um erro de não autorizado, quando o usuário não tem autorização de acessar um recurso específico
     *
     * @param mensagem A mensagem de erro
     */
    public void addErroAutorizacao(String mensagem){
        status = StatusResposta.NAO_AUTORIZADO;
        if(erroAutorizacao == null){
            erroAutorizacao = new ErroAutorizacao(mensagem);
        }
    }
    
    /**
     * Adiciona um erro interno, indicando que ocorreu alguma Exception no processamento, normalmente só o próprio framework utiliza essa função.
     *
     * @param mensagem A mensagem de erro interno
     */
    public void addErroInterno(String mensagem){
        status = StatusResposta.ERRO_INTERNO;
        if(erroInterno == null){
            erroInterno = new ErroInterno(mensagem);
        }
    }
    
    /**
     * Adiciona um erro interno, indicando que ocorreu alguma Exception no processamento, normalmente só o próprio framework utiliza essa função.
     *
     */
    public void addErroInterno(){
        status = StatusResposta.ERRO_INTERNO;
        if(erroInterno == null){
            erroInterno = new ErroInterno();
        }
    }
    
    /**
     * Adiciona um objeto, identificado pelo nome, para ser enviado na resposta ao front-end
     *
     * @param nome Nome(chave JSON) que identifica o conteúdo
     * @param conteudo Conteúdo em si
     * @return A própria classe, para chamadas em sequencia.
     */
    public HwResponse addConteudo(String nome, Object conteudo){
        if(this.conteudo == null) 
            this.conteudo = new HashMap<>();
        this.conteudo.put(nome,conteudo);
        return this;
    }
    
    public <T> T getConteudo(String nome, Class<T> type){
        if(this.conteudo == null) return null;
        return type.cast(this.conteudo.get(nome));
    }
    
    public Object getConteudo(String nome){
        if(this.conteudo == null) return null;
        return this.conteudo.get(nome);
    }
    
    /**
     * Monta e retorna essa HwResponse no formato JSON.
     *
     * @return O JSON que representa essa HwResponse
     */
    public String getJson(){
        colocaErros();
        if(!setouMensagemRedirect){
            if (req != null){
                MensagemAlerta mensagemRedirect = (MensagemAlerta)req.getSession().getAttribute("redirectMessage");
                if(mensagemRedirect != null){
                    if(Utils.getDateDiff(mensagemRedirect.criacao, new Date(),TimeUnit.SECONDS) < 5){
                        mensagemAlerta = mensagemRedirect;
                    }
                    req.getSession().removeAttribute("redirectMessage");
                }
            }
        }
        return JsonManager.toJsonOnlyExpose(this);
    }

    public void setStopPoint(String STOP_POINT) {
        this.STOP_POINT = STOP_POINT;
    }
    
    private void colocaErros(){
        if(status == StatusResposta.OK) return;
        erros = new ArrayList<>();
        if(erroValidacaoCampos != null) erros.add(erroValidacaoCampos);
        if(erroAutenticacao != null) erros.add(erroAutenticacao);
        if(erroRequest != null) erros.add(erroRequest);
        if(erroInterno != null) erros.add(erroInterno);
    }

    public void setMethodNamespace(String methodNamespace) {
        if(methodNamespace == null) return;
        if(!methodNamespace.isEmpty()) methodNamespace += ".";
        this.methodNamespace = methodNamespace;
    }
}
