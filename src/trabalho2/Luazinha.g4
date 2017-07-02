grammar Luazinha;


@members{
static String grupo = "<Coloque os RAs do seu grupo aqui>"; 
static PilhaDeTabelas pilhaDeTabelas = new PilhaDeTabelas();
}


programa : 
           trecho
           
         ;

trecho : (comando ';'?)* (ultimocomando ';'?)?
       ;

bloco : trecho
      ;

comando :  listavar1=listavar '=' listaexp 
        |  chamadadefuncao
        |  'do' bloco1=bloco 'end'
        |  'while' exp1=exp 'do' bloco2=bloco 'end'
        |  'repeat' bloco3=bloco 'until' exp2=exp
        |  'if' exp3=exp 'then' bloco4=bloco ('elseif' exp_if+=exp 'then' bloco_if+=bloco)* ('else' bloco_else=bloco)? 'end'
        |  'for' NOME '=' exp_for1=exp ',' exp_for2=exp (',' exp_for3=exp)? 'do' bloco_for=bloco 'end'
        |  'for' lista_nomes1=listadenomes 'in' lista_exp2+=listaexp 'do' bloco_for2=bloco 'end'
        |  'function' nomedafuncao cp1=corpodafuncao 
        |  'local' 'function' NOME cp2=corpodafuncao 
        |  'local' lista_nomes2=listadenomes ('=' lista_exp3+=listaexp)?
        ;

ultimocomando : 'return' (listaexp)? | 'break'
              ;

nomedafuncao returns [ String nome, boolean metodo ]
@init { $metodo = false; }
    : n1=NOME { $nome = $n1.getText(); }
      ('.' n2=NOME { $nome += "." + $n2.getText(); })*
      (':' n3=NOME { $metodo = true; $nome += "." + $n3.getText(); })?
    ;

listavar returns [ List<String> nomes ]
@init { $nomes = new ArrayList<String>(); }
    : v1=var { $nomes.add($v1.nome); }
      (',' v2=var { $nomes.add($v2.nome); }
      )*
    ;

var returns [ String nome, int linha, int coluna ]
    :  NOME { $nome = $NOME.getText(); $linha = $NOME.line; $coluna = $NOME.pos; } 
    |  expprefixo '[' exp ']'
    |  expprefixo '.' NOME
    ;

listadenomes returns [ List<String> nomes ]
@init{ $nomes = new ArrayList<String>(); }
    : n1=NOME { $nomes.add($n1.getText()); }
      (',' n2=NOME { $nomes.add($n2.getText()); } )*
    ;

listaexp : (exp ',')* exp
         ;

exp :  'nil' | 'false' | 'true' | NUMERO | CADEIA | '...' | funcao | 
       exp2=expprefixo2 | construtortabela | expB1 = exp opbin expB2 = exp | opunaria expU = exp
    ;


expprefixo : NOME ( '[' exp ']' | '.' NOME )*
           ;

expprefixo2 : var1=var | chama_func1=chamadadefuncao | '(' exp ')'
           ;

chamadadefuncao :  expprefixo args |
                   expprefixo ':' NOME args
                ;

args :  '(' (listaexp)? ')' | construtortabela | CADEIA 
     ;

funcao : 'function' corpodafuncao
       ;

corpodafuncao : '(' (listapar1=listapar)? ')' bloco 'end'
              ;

listapar : listadenomes_f=listadenomes (',' '...')? 
         | '...'
         ;

construtortabela : '{' (listadecampos)? '}'
                 ;

listadecampos : campo (separadordecampos campo)* (separadordecampos)?
              ;

campo : '[' exp_c1=exp ']' '=' exp_c2=exp | NOME '=' exp_c3=exp | exp_c4=exp
      ;

separadordecampos : ',' | ';'
                  ;

opbin : '+' | '-' | '*' | '/' | '^' | '%' | '..' | '<' | 
        '<=' | '>' | '>=' | '==' | '~=' | 'and' | 'or'
      ;

opunaria : '-' | 'not' | '#'
         ;


NOME	:	('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')*;
CADEIA	:	'\'' ~('\n' | '\r' | '\'')* '\'' | '"' ~('\n' | '\r' | '"')* '"';
NUMERO	:	('0'..'9')+ EXPOENTE? | ('0'..'9')+ '.' ('0'..'9')* EXPOENTE?
		| '.' ('0'..'9')+ EXPOENTE?;
fragment
EXPOENTE	:	('e' | 'E') ( '+' | '-')? ('0'..'9')+;
COMENTARIO
	:	'--' ~('\n' | '\r')* '\r'? '\n' {skip();};
WS	:	(' ' | '\t' | '\r' | '\n') {skip();};
