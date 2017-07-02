/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho2;
/**
 *
 * @author Matheus
 */
public class Calculador extends LuazinhaBaseVisitor<String> {
    PilhaDeTabelas escopos = new PilhaDeTabelas();


    @Override
    public String visitPrograma(LuazinhaParser.ProgramaContext ctx) {
        escopos.empilhar(new TabelaDeSimbolos("global"));
        visitTrecho(ctx.trecho());
        escopos.desempilhar();
        return null;
    }

    @Override
    public String visitTrecho(LuazinhaParser.TrechoContext ctx) {
        String valor = "";
        for(LuazinhaParser.ComandoContext cont:ctx.comando())
        {
            valor+=(visitComando(cont));
        }
        if(ctx.ultimocomando()!=null)
        {
            valor+=visitUltimocomando(ctx.ultimocomando());
        }

        return valor;
    }

    @Override
    public String visitBloco(LuazinhaParser.BlocoContext ctx) {
        return visitTrecho(ctx.trecho());
    }

    @Override
    public String visitComando(LuazinhaParser.ComandoContext ctx) {
        //Amarra todas as variveis de uma lista
        if(ctx.listavar1!= null) //listavar '=' listaexp
        {
            TabelaDeSimbolos escopoAtual;
            escopoAtual = escopos.topo();
            for(String nome : ctx.listavar1.nomes)
            {
                if(escopos.existeSimbolo(nome) == false)
                    escopoAtual.adicionarSimbolo(nome, "variavel");
            }

        }
        //Visita chamada de funcao
        if(ctx.chamadadefuncao()!=null) //chamadadefuncao
        {
            visitChamadadefuncao(ctx.chamadadefuncao());
        }
        //Corpo do comando do - while
        if(ctx.bloco1!=null) //'do' bloco1=bloco 'end'
        {
            return visitBloco(ctx.bloco1);
        }
        //VOLTAR AQUI
        if(ctx.exp1 != null) //'while' exp1=exp 'do' bloco2=bloco 'end'
        {
            visitExp(ctx.exp1);
            visitBloco(ctx.bloco2);
        }
        if(ctx.bloco3 != null) //'repeat' bloco3=bloco 'until' exp2=exp
        {
            visitBloco(ctx.bloco3);
            visitExp(ctx.exp2);
        }
        if(ctx.bloco4!=null) //'if' exp3=exp 'then' bloco4=bloco ('elseif' exp4=exp 'then' bloco5=bloco)* ('else' bloco6=bloco)? 'end'
        {
            visitExp(ctx.exp3);
            visitBloco(ctx.bloco4);
            for(LuazinhaParser.ExpContext cont : ctx.exp_if)
            {
                visitExp(cont);
            }
            for(LuazinhaParser.BlocoContext cont : ctx.bloco_if)
            {
                visitBloco(cont);
            }
            if(ctx.bloco_else!=null)
            {
                visitBloco(ctx.bloco_else);
            }
        }

        if(ctx.exp_for1 != null) //'for' NOME '=' exp_for1=exp ',' exp_for2=exp (',' exp_for3=exp)? 'do' bloco_for=bloco 'end'
        {

            TabelaDeSimbolos t2 = new TabelaDeSimbolos("for");
            escopos.empilhar(t2);
            escopos.topo().adicionarSimbolo(ctx.NOME().getText(), "variavel");
            visitExp(ctx.exp_for1);
            visitExp(ctx.exp_for2);
            if(ctx.exp_for3!=null)
                visitExp(ctx.exp_for1);
            visitBloco(ctx.bloco_for);
            escopos.desempilhar();
        }

        if(ctx.lista_nomes1 != null) //'for' lista_nomes1=listadenomes 'in' lista_exp2+=listaexp 'do' bloco_for2=bloco 'end'
        {
            TabelaDeSimbolos t2 = new TabelaDeSimbolos("for");
            escopos.empilhar(t2);
            escopos.topo().adicionarSimbolos(ctx.lista_nomes1.nomes, "variavel");
            visitListaexp(ctx.listaexp);
            visitBloco(ctx.bloco_for2);
            escopos.desempilhar();
        }
        if(ctx.cp1 != null) //'function' nomedafuncao corpodafuncao
        {
            TabelaDeSimbolos t2 = new TabelaDeSimbolos(visitNomedafuncao(ctx.nomedafuncao()));
            escopos.empilhar(t2);
            if (ctx.nomedafuncao().metodo) {
                escopos.topo().adicionarSimbolo("self", "parametro");
            }
            visitCorpodafuncao(ctx.cp1);
            escopos.desempilhar();
        }
        if(ctx.cp2!=null) //'local' 'function' NOME cp2=corpodafuncao
        {
            TabelaDeSimbolos t2 = new TabelaDeSimbolos(ctx.NOME().getText());
            escopos.empilhar(t2);
            visitCorpodafuncao(ctx.cp2);
            escopos.desempilhar();
        }
        if(ctx.lista_nomes2!=null) //'local' lista_nomes2=listadenomes ('=' lista_exp3=listaexp)?
        {
            escopos.topo().adicionarSimbolos(ctx.lista_nomes2.nomes, "variavel");
            if(ctx.lista_exp3 != null)
            {
                visitListaexp(ctx.listaexp);
            }
        }
        return null;
    }

    //VISIT ULTIMO COMANDO(?)

    @Override
    public String visitNomedafuncao(LuazinhaParser.NomedafuncaoContext ctx) {
        return ctx.nome;
    }

    @Override
    public String visitListavar(LuazinhaParser.ListavarContext ctx) {
        if(ctx.nomes != null){
            TabelaDeSimbolos escopoAtual;
            escopoAtual = escopos.topo();
            for(String nome : ctx.nomes)
            {
                if(escopos.existeSimbolo(nome) == false)
                    escopoAtual.adicionarSimbolo(nome, "variavel");
            }
        }
        return null;
    }

    @Override
    public String visitVar(LuazinhaParser.VarContext ctx) {
        if(ctx.NOME() != null){
            TabelaDeSimbolos escopoAtual;
            escopoAtual = escopos.topo();
            if(escopos.existeSimbolo(ctx.nome) == false)
                escopoAtual.adicionarSimbolo(ctx.nome, "variavel");
        }
        return null;
    }

    //VISIT LISTA DE NOMES(?)

    @Override
    public String visitListaexp(LuazinhaParser.ListaexpContext ctx) {
        for(LuazinhaParser.ExpContext cont : ctx.exp())
        {
            visitExp(cont);
        }
        return null;
    }

    @Override
    public String visitExp(LuazinhaParser.ExpContext ctx) {

        if(ctx.exp2 != null)
        {
            visitExpprefixo2(ctx.exp2);
        }

        return null;
    }

    //VISIT EXPPREFIXO(?)

    @Override
    public String visitExpprefixo2(LuazinhaParser.Expprefixo2Context ctx) {
        if(ctx.var1 !=null)
        {
            visitVar(ctx.var1);
        }
        if(ctx.chama_func1!=null)
        {
            visitChamadadefuncao(ctx.chama_func1);
        }
        return null;
    }

    @Override
    public String visitChamadadefuncao(LuazinhaParser.ChamadadefuncaoContext ctx) {
        return super.visitChamadadefuncao(ctx);
    }

    //VISIT ARGS(?)

    //VISIT FUNCAO(?)

    @Override
    public String visitCorpodafuncao(LuazinhaParser.CorpodafuncaoContext ctx) {
        if(ctx.listapar1!=null)
        {
          visitListapar(ctx.listapar1);
        }
        if(ctx.bloco()!=null)
        {
            visitBloco(ctx.bloco());
        }
        return null;
    }

    @Override
    public String visitListapar(LuazinhaParser.ListaparContext ctx) {
         if(ctx.listadenomes_f!=null)
         {
           TabelaDeSimbolos escopoAtual;
           escopoAtual = escopos.topo();
           escopoAtual.adicionarSimbolos(ctx.listadenomes_f.nomes, "parametro");
         }
         return null;
    }

    //VISIT CONSTRUTORTABELA

    //VISIT LISTADECAMPOS

    @Override
    public String visitCampo(LuazinhaParser.CampoContext ctx) {
        return null;
    }

      @Override
    public String visitSeparadordecampos(LuazinhaParser.SeparadordecamposContext ctx) {
        return ctx.getText();
    }

     @Override
    public String visitOpbin(LuazinhaParser.OpbinContext ctx) {
       return ctx.getText();
    }

    @Override
    public String visitOpunaria(LuazinhaParser.OpunariaContext ctx) {
       return ctx.getText();
    }




}
