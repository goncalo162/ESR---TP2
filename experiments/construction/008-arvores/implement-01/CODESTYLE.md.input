Para ficheiros JAVA deves seguir sempre as seguintes regras ao criar código:

# Geral

## Espaçamento e Chavetas

Deves utilizar sempre este tipo de chavetas
if()
{

}
Exceto em excessões descritas a baixo

Dentro de cada metodo tenta agrupar codigo do mesmo assunto junto sem espaços, caso oportuno ou muito grande (mais de 4 linhas) deixa um espaço em branco, tenta não deixar linhas sozinhas separadas dentro de metodos a não ser que a sua lógica seja muito obtusa

Em caso de uma linha ser muito grande (por exemplo stream ou função muito larga) todas as linhas devem ter o mesmo numero de tabs (+1 do que a função original)

## Auxiliar

Para classes auxiliares deves utilizar um ficheiro novo e garantir que só o original onde esta estava tem acesso a este a não ser que a classe auxiliar seja muito pequena

## Blocos por Script

Cada script deve estar dividido em diversas secções com blocos,
No final de cada bloco deves ter três linhas vazias, um comentário com o nome do próximo bloco, e uma linha vazia antes de começar o próximo:
    ex:
        //* === Construtores === *

        public X()
        {
            codigo;
        }



        //* === Logica === *

        public y()
        {
            codigo;
        }

A única exceção para este espaçamento é o primeiro bloco que deve ser sempre "Atributos e Variáveis" que deve estar diretamente abaixo em vez de três a contar da chaveta de declaração da classe:
    ex:
        public class X
        {
            //* === Atributos e Variáveis === *
            private bool x;
            private int y;

            ...
        }

Na declaração deixa apenas linhas de espaço entre elas se a lista estiver muito extensa (mais de 8 atributos) ou para separar entre atributos de tipos de dados e outras classes/interfaces, deixando apenas uma linha em brancho

Dentro do mesmo bloco métodos devem ser separados por dois espaços:
    ex:
        public int fazX()
        {

        }


        public int fazY()
        {

        }

Deves tentar reutilizar os nomes dos blocos no comentário entre todas as secções de modo a que seja facil fazer Cntrl + F nos ficheiros

Estes são alguns exemplos relevantes de nome:
Atributos e Variáveis
Construtores
Getters
Setters
Auxiliar
Lógica

## Comentários
Tenta não utilizar comentários o código deve ser claro o suficiente para que baste olhar para o titulo de uma função e o seu codigo para perceber o que faz, caso necessário utiliza

# Condições e Loops

## If

if/for etc.. devem sempre ser declarados com os parenteses ao lado (ex: if("condição"))
if não deve usar chavetas se a consequencia for apenas uma linha
    ex: if(condição)
            return;

Para mais do que uma linha deves usar sempre
if(condição)
{
    codigo 1
    codigo 2
}

No caso de existirem várias branches (elses) deves fazer o seguinte
Caso cada consequência tenha apenas uma linha e existam apenas duas condições deves não usar chavetas
    ex: if(condição)
            return x;
        else
            return y;
Caso cada consequência tenha apenas uma linha e existam mais do que duas condições deves trocar para um Switch statement
Caso contrário deves seguir sempre o seguinte, para cada consequência deves deixar uma linha vasia antes de começar a consequência nova, no caso de else if deves ter a chaveta colada ao próprio código, nos outros não
    ex: if(condição)
        {
           codigo 1

        }else if(condição 2){
            codigo 2

        }else{
            codigo3
        }

## Ciclos

Ciclos for, foreach, while, do while, etc.. devem ter sempre chavetas independentemente do numero de linhas


## Switch Statements

Switch statements devem seguir o seguinte padrão:
    ex: switch(variavel)
        {
            case x:
                codigo;
                break;

            case y:
                codigo;
                break;

            case z:
                codigo;
                break;

            default:
                codigo;
                break;
        }

## Try/catch

Para try, catch deves utilizar sempre chavetas e:
Caso existam duas seguir este formato

try
{
    codigo;

}catch (except){
    codigo;
}

Caso existam mais ou finally:

try
{
    codigo;

}catch (except){
    codigo;

}finally{
    codigo;
}

## Agrupar com o resto o método

Para todas as estruturas mencionadas aqui tenta agrupar apenas uma linha de codigo com elas antes
    ex: codigo1;
        if(condigção)
            codigo2;

No final de blocos condicionais grandes (if else, try/catch) dá espaço
    ex:
        codigo 0;

        if(condição)
        {
           codigo 1;

        }else if(condição 2){
            codigo 2;

        }else{
            codigo3;
        }

        codigo4;