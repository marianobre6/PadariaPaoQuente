package padaria;

import java.util.ArrayList;
import java.util.Scanner;

/** Interface de console exigida no enunciado. Executar: java padaria.Aplicacao */
public class Aplicacao {
    private static final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        Padaria padaria = new Padaria("Padaria Pão Quente");
        carregarDadosIniciais(padaria);
        System.out.println("\nBem-vindo ao SISTEMA PÃO QUENTE!");
        System.out.println("Controle de estoque, vendas e fechamento semanal.");
        System.out.println("Dados de demonstração carregados com sucesso.");

        int opcao;
        do {
            mostrarMenu();
            opcao = lerInteiro("Escolha uma opção: ");
            switch (opcao) {
                case 1 -> cadastrarProduto(padaria);
                case 2 -> Relatorio.imprimirEstoque(padaria);
                case 3 -> registrarVenda(padaria);
                case 4 -> reporEstoque(padaria);
                case 5 -> alterarPreco(padaria);
                case 6 -> buscarProduto(padaria);
                case 7 -> Relatorio.imprimirVendasDaSemana(padaria);
                case 8 -> Relatorio.imprimirEstoqueBaixo(padaria);
                case 9 -> Relatorio.imprimirFechamento(padaria);
                case 10 -> Relatorio.imprimirHistorico(padaria);
                case 0 -> {
                    Relatorio.imprimirFechamento(padaria);
                    System.out.println("Programa encerrado. Obrigado por usar o Pão Quente!");
                }
                default -> System.out.println("Opção inválida. Digite um número de 0 a 10.");
            }
        } while (opcao != 0);
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("+------------------------------------------------------------------+");
        System.out.println("|                   PADARIA PÃO QUENTE                              |");
        System.out.println("|                 ESTOQUE E VENDAS - MENU                           |");
        System.out.println("+------------------------------------------------------------------+");
        System.out.println("|  1  Cadastrar produto         6  Buscar produto por nome         |");
        System.out.println("|  2  Listar estoque            7  Vendas da semana                |");
        System.out.println("|  3  Registrar venda           8  Alerta de estoque baixo        |");
        System.out.println("|  4  Repor estoque             9  Fechamento semanal             |");
        System.out.println("|  5  Alterar preço            10  Histórico de operações         |");
        System.out.println("|  0  Sair e exibir fechamento                                     |");
        System.out.println("+------------------------------------------------------------------+");
    }

    private static void cadastrarProduto(Padaria padaria) {
        System.out.println("\n--- Cadastro de produto ---");
        String nome = lerTexto("Nome do produto: ");
        System.out.print("Categoria (Enter = Sem categoria): ");
        String categoria = entrada.nextLine().trim();
        double preco = lerDecimal("Preço (R$): ");
        int quantidade = lerInteiro("Quantidade inicial: ");
        System.out.println(padaria.cadastrarProduto(nome, categoria, preco, quantidade));
    }

    private static void registrarVenda(Padaria padaria) {
        System.out.println("\n--- Registro de venda ---");
        int codigo = lerInteiro("Código do produto: ");
        int quantidade = lerInteiro("Quantidade vendida: ");
        int dia = lerInteiro("Dia (1-Seg 2-Ter 3-Qua 4-Qui 5-Sex 6-Sáb 7-Dom): ");
        System.out.println(padaria.registrarVenda(codigo, quantidade, dia));
    }

    private static void reporEstoque(Padaria padaria) {
        System.out.println("\n--- Reposição de estoque ---");
        int codigo = lerInteiro("Código do produto: ");
        int quantidade = lerInteiro("Quantidade para repor: ");
        System.out.println(padaria.reporEstoque(codigo, quantidade));
    }

    private static void alterarPreco(Padaria padaria) {
        System.out.println("\n--- Alteração de preço ---");
        int codigo = lerInteiro("Código do produto: ");
        double preco = lerDecimal("Novo preço (R$): ");
        System.out.println(padaria.alterarPreco(codigo, preco));
    }

    private static void buscarProduto(Padaria padaria) {
        System.out.println("\n--- Busca por parte do nome ---");
        String trecho = lerTexto("Digite uma palavra ou parte do nome: ");
        ArrayList<Produto> encontrados = padaria.buscarPorNome(trecho);
        Relatorio.imprimirResultadoBusca(encontrados, trecho);
    }

    /** Os cinco produtos e as oito vendas do enunciado, na ordem exata. */
    private static void carregarDadosIniciais(Padaria padaria) {
        padaria.cadastrarProduto("Pão francês", "Pães", 0.75, 200);
        padaria.cadastrarProduto("Pão de queijo", "Salgados", 4.50, 60);
        padaria.cadastrarProduto("Bolo de cenoura", "Doces", 28.00, 6);
        padaria.cadastrarProduto("Sonho", "Doces", 6.00, 25);
        padaria.cadastrarProduto("Café coado", "Bebidas", 5.00, 80);

        padaria.registrarVenda(1, 120, 1);
        padaria.registrarVenda(2, 15, 1);
        padaria.registrarVenda(5, 30, 1);
        padaria.registrarVenda(1, 60, 2);
        padaria.registrarVenda(3, 2, 2);
        padaria.registrarVenda(4, 10, 3);
        padaria.registrarVenda(2, 20, 3);
        padaria.registrarVenda(5, 25, 3);
    }

    /** Usa a leitura com try/catch fornecida no Apêndice A do professor. */
    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = entrada.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido: digite um número inteiro.");
            }
        }
    }

    private static double lerDecimal(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = entrada.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isFinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // Não interrompe o programa; imprime a mensagem abaixo e repete.
            }
            System.out.println("Valor inválido: digite um número, como 5,50.");
        }
    }

    private static String lerTexto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = entrada.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este campo não pode ficar vazio.");
        }
    }
}
