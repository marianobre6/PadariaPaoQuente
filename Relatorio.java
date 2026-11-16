package padaria;

import java.util.ArrayList;
import java.util.Locale;

/** Relatórios somente de leitura. A classe não muda nenhuma informação da Padaria. */
public class Relatorio {
    private static final int LARGURA = 72;

    public static void imprimirEstoque(Padaria padaria) {
        titulo("ESTOQUE ATUAL - " + padaria.getNome());
        imprimirTabelaDeProdutos(padaria.getProdutos());
        linha();
        System.out.println("  PRODUTOS CADASTRADOS: " + padaria.getQuantidadeDeProdutos()
                + " de " + Padaria.MAX_PRODUTOS);
        System.out.println("  VALOR TOTAL EM ESTOQUE: " + moeda(padaria.valorTotalEmEstoque()));
        linha();
    }

    public static void imprimirResultadoBusca(ArrayList<Produto> encontrados, String trecho) {
        titulo("BUSCA DE PRODUTOS - '" + trecho + "'");
        if (encontrados.isEmpty()) {
            System.out.println("  Nenhum produto encontrado.");
        } else {
            System.out.println("  " + encontrados.size() + " produto(s) encontrado(s).");
            imprimirTabelaDeProdutos(encontrados);
        }
        linha();
    }

    public static void imprimirVendasDaSemana(Padaria padaria) {
        titulo("VENDAS DA SEMANA (UNIDADES) - " + padaria.getNome());
        System.out.printf("%-4s %-20s", "COD", "PRODUTO");
        for (String dia : Padaria.DIAS) {
            System.out.printf(" %5s", dia);
        }
        System.out.printf(" %6s%n", "TOTAL");
        System.out.println("-".repeat(LARGURA));
        ArrayList<Produto> produtos = padaria.getProdutos();
        int totalGeral = 0;
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            System.out.printf("%-4d %-20s", p.getCodigo(), cortar(p.getNome(), 20));
            for (int dia = 1; dia <= 7; dia++) {
                System.out.printf(" %5d", padaria.getVendas(i, dia));
            }
            int total = padaria.totalVendidoDoProduto(i);
            totalGeral += total;
            System.out.printf(" %6d%n", total);
        }
        System.out.println("-".repeat(LARGURA));
        System.out.printf("%-4s %-20s", "", "TOTAL DO DIA");
        for (int dia = 1; dia <= 7; dia++) {
            System.out.printf(" %5d", padaria.totalVendidoNoDia(dia));
        }
        System.out.printf(" %6d%n", totalGeral);
        linha();
    }

    public static void imprimirEstoqueBaixo(Padaria padaria) {
        titulo("ALERTA DE ESTOQUE - MENOS DE " + Padaria.LIMITE_ESTOQUE_BAIXO + " UNIDADES");
        ArrayList<Produto> baixos = padaria.produtosComEstoqueBaixo();
        if (baixos.isEmpty()) {
            System.out.println("  Nenhum produto com estoque baixo.");
        } else {
            imprimirTabelaDeProdutos(baixos);
            System.out.println("  ATENÇÃO: " + baixos.size() + " produto(s) precisam de reposição.");
        }
        linha();
    }

    public static void imprimirFechamento(Padaria padaria) {
        titulo("FECHAMENTO DA SEMANA - " + padaria.getNome());
        System.out.println("  Faturamento por dia:");
        for (int dia = 1; dia <= 7; dia++) {
            System.out.printf("    %-3s  %12s%n", Padaria.DIAS[dia - 1], moeda(padaria.getFaturamentoDoDia(dia)));
        }
        System.out.println("-".repeat(LARGURA));
        System.out.println("  Faturamento total da semana: " + moeda(padaria.getFaturamentoTotal()));
        Produto maisVendido = padaria.produtoMaisVendido();
        System.out.println("  Produto mais vendido: " + (maisVendido == null ? "Nenhum" : maisVendido.getNome()));
        int melhorDia = padaria.diaDeMaiorFaturamento();
        System.out.println("  Dia de maior faturamento: " + (melhorDia == 0 ? "Nenhum" : Padaria.DIAS[melhorDia - 1]));
        System.out.println("  Valor total em estoque: " + moeda(padaria.valorTotalEmEstoque()));
        System.out.println("  Produtos com estoque baixo: " + padaria.produtosComEstoqueBaixo().size());
        System.out.println("  Operações registradas: " + padaria.getHistorico().size());
        linha();
    }

    public static void imprimirHistorico(Padaria padaria) {
        titulo("HISTÓRICO DE OPERAÇÕES BEM-SUCEDIDAS");
        ArrayList<String> historico = padaria.getHistorico();
        if (historico.isEmpty()) {
            System.out.println("  Ainda não existem operações registradas.");
        } else {
            for (String entrada : historico) {
                System.out.println("  " + entrada);
            }
        }
        System.out.println("  Total de operações: " + historico.size());
        linha();
    }

    private static void imprimirTabelaDeProdutos(ArrayList<Produto> produtos) {
        System.out.printf("%-4s %-21s %-15s %12s %5s%n", "COD", "NOME", "CATEGORIA", "PREÇO", "QTD");
        System.out.println("-".repeat(LARGURA));
        if (produtos.isEmpty()) {
            System.out.println("  Nenhum produto cadastrado.");
        }
        for (Produto produto : produtos) {
            System.out.printf("%-4d %-21s %-15s %12s %5d%n",
                    produto.getCodigo(), cortar(produto.getNome(), 21),
                    cortar(produto.getCategoria(), 15), moeda(produto.getPreco()),
                    produto.getQuantidade());
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(LARGURA));
        System.out.println("  " + texto);
        System.out.println("=".repeat(LARGURA));
    }

    private static void linha() {
        System.out.println("=".repeat(LARGURA));
    }

    private static String cortar(String texto, int tamanho) {
        if (texto.length() <= tamanho) {
            return texto;
        }
        return texto.substring(0, tamanho - 3) + "...";
    }

    private static String moeda(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }
}
