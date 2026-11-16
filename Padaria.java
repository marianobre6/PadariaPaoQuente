package padaria;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Reúne as regras de negócio e os dados em memória.
 * Toda alteração é feita aqui: a interface e os relatórios não modificam os produtos.
 */
public class Padaria {
    public static final int MAX_PRODUTOS = 20;
    public static final int LIMITE_ESTOQUE_BAIXO = 5;
    public static final String[] DIAS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    private String nome;
    private ArrayList<Produto> produtos;
    private ArrayList<String> historico;
    private int[][] vendas;
    private double[] faturamentoPorDia;

    // Soma apenas as vendas aceitas no mês atual, enquanto o programa está aberto.
    // Sem banco de dados, os valores reiniciam quando a aplicação é fechada.
    private YearMonth mesDeReferencia;
    private double faturamentoDoMes;
    // Divide os lançamentos do mês em cinco períodos: 1-7, 8-14, 15-21, 22-28 e 29-31.
    private double[] faturamentoPorPeriodoDoMes;

    public Padaria(String nome) {
        if (nome == null || nome.isBlank()) {
            this.nome = "Padaria Pão Quente";
        } else {
            this.nome = nome.trim();
        }
        this.produtos = new ArrayList<>();
        this.historico = new ArrayList<>();
        this.vendas = new int[MAX_PRODUTOS][7];
        this.faturamentoPorDia = new double[7];
        this.mesDeReferencia = YearMonth.now();
        this.faturamentoDoMes = 0;
        this.faturamentoPorPeriodoDoMes = new double[5];
    }

    public String getNome() {
        return nome;
    }

    public String cadastrarProduto(String nome, String categoria, double preco, int quantidade) {
        if (nome == null || nome.trim().isEmpty()) {
            return "ERRO: o nome do produto não pode ficar vazio.";
        }
        String nomeTratado = nome.trim();
        if (existeProdutoComNome(nomeTratado)) {
            return "ERRO: já existe um produto chamado '" + nomeTratado + "'.";
        }
        if (produtos.size() >= MAX_PRODUTOS) {
            return "ERRO: o limite de 20 produtos foi atingido.";
        }
        if (!Double.isFinite(preco) || preco <= 0) {
            return "ERRO: o preço deve ser maior que zero.";
        }
        if (quantidade < 0) {
            return "ERRO: a quantidade inicial não pode ser negativa.";
        }

        String categoriaTratada = "Sem categoria";
        if (categoria != null && !categoria.isBlank()) {
            categoriaTratada = categoria.trim();
        }
        Produto novo = new Produto(nomeTratado, categoriaTratada, preco, quantidade);
        produtos.add(novo);
        registrarNoHistorico("Cadastro: " + novo.getNome() + " (código " + novo.getCodigo() + ")"
                + ", " + quantidade + " unidade(s), " + moeda(preco));
        return "OK: produto cadastrado com o código " + novo.getCodigo() + ".";
    }

    public Produto buscarPorCodigo(int codigo) {
        int posicao = indiceDoProduto(codigo);
        if (posicao == -1) {
            return null;
        }
        return produtos.get(posicao);
    }

    public ArrayList<Produto> buscarPorNome(String trecho) {
        ArrayList<Produto> encontrados = new ArrayList<>();
        String busca = trecho == null ? "" : trecho.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
        for (Produto produto : produtos) {
            if (produto.getNome().toLowerCase(Locale.forLanguageTag("pt-BR")).contains(busca)) {
                encontrados.add(produto);
            }
        }
        return encontrados;
    }

    /** Retorna uma cópia da lista; alterações na lista devolvida não atingem o cadastro. */
    public ArrayList<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }

    public int getQuantidadeDeProdutos() {
        return produtos.size();
    }

    public String registrarVenda(int codigo, int unidades, int dia) {
        int posicao = indiceDoProduto(codigo);
        if (posicao < 0) {
            return "ERRO: produto de código " + codigo + " não encontrado.";
        }
        if (unidades <= 0) {
            return "ERRO: a quantidade vendida deve ser maior que zero.";
        }
        if (!diaValido(dia)) {
            return "ERRO: dia inválido. Digite um dia de 1 a 7.";
        }
        Produto produto = produtos.get(posicao);
        if (unidades > produto.getQuantidade()) {
            return "ERRO: estoque insuficiente. Disponível: " + produto.getQuantidade() + " unidade(s).";
        }

        // Todas as condições são checadas ANTES de reduzir o estoque e somar as vendas.
        double valorCobrado = unidades * produto.getPreco();
        produto.removerEstoque(unidades);
        vendas[posicao][dia - 1] += unidades; // O usuário digita 1..7; a matriz usa 0..6.
        faturamentoPorDia[dia - 1] += valorCobrado; // Preserva o preço da época da venda.
        // O mês atual acompanha as vendas sem modificar a matriz semanal.
        LocalDate dataDoRegistro = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(dataDoRegistro);
        if (!mesDeReferencia.equals(mesAtual)) {
            mesDeReferencia = mesAtual;
            faturamentoDoMes = 0;
            faturamentoPorPeriodoDoMes = new double[5];
        }
        faturamentoDoMes += valorCobrado;
        // O enunciado pede dia da semana, não data. Para o gráfico mensal,
        // usamos a data real de lançamento da venda, sem inventar datas passadas.
        int periodo = (dataDoRegistro.getDayOfMonth() - 1) / 7;
        faturamentoPorPeriodoDoMes[periodo] += valorCobrado;
        registrarNoHistorico("Venda: " + unidades + " x " + produto.getNome() + " ("
                + DIAS[dia - 1] + ") = " + moeda(valorCobrado));
        return "OK: venda registrada. Total: " + moeda(valorCobrado)
                + ". Estoque restante: " + produto.getQuantidade() + ".";
    }

    public String reporEstoque(int codigo, int unidades) {
        Produto produto = buscarPorCodigo(codigo);
        if (produto == null) {
            return "ERRO: produto de código " + codigo + " não encontrado.";
        }
        if (unidades <= 0) {
            return "ERRO: a quantidade reposta deve ser maior que zero.";
        }
        if (!produto.adicionarEstoque(unidades)) {
            return "ERRO: não foi possível repor esta quantidade.";
        }
        registrarNoHistorico("Reposição: " + unidades + " x " + produto.getNome()
                + ". Saldo: " + produto.getQuantidade());
        return "OK: estoque de " + produto.getNome() + " agora é " + produto.getQuantidade() + ".";
    }

    public String alterarPreco(int codigo, double novoPreco) {
        Produto produto = buscarPorCodigo(codigo);
        if (produto == null) {
            return "ERRO: produto de código " + codigo + " não encontrado.";
        }
        if (!Double.isFinite(novoPreco) || novoPreco <= 0) {
            return "ERRO: o preço deve ser maior que zero.";
        }
        double antigo = produto.getPreco();
        produto.setPreco(novoPreco);
        registrarNoHistorico("Preço alterado: " + produto.getNome() + " de "
                + moeda(antigo) + " para " + moeda(novoPreco));
        return "OK: preço de " + produto.getNome() + " alterado para " + moeda(novoPreco) + ".";
    }

    public int getVendas(int posicaoProduto, int dia) {
        if (posicaoProduto < 0 || posicaoProduto >= produtos.size() || !diaValido(dia)) {
            return 0;
        }
        return vendas[posicaoProduto][dia - 1];
    }

    public int totalVendidoDoProduto(int posicaoProduto) {
        if (posicaoProduto < 0 || posicaoProduto >= produtos.size()) {
            return 0;
        }
        int total = 0;
        for (int dia = 1; dia <= 7; dia++) {
            total += getVendas(posicaoProduto, dia);
        }
        return total;
    }

    public int totalVendidoNoDia(int dia) {
        if (!diaValido(dia)) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < produtos.size(); i++) {
            total += getVendas(i, dia);
        }
        return total;
    }

    public double getFaturamentoDoDia(int dia) {
        if (!diaValido(dia)) {
            return 0;
        }
        return faturamentoPorDia[dia - 1];
    }

    public double getFaturamentoTotal() {
        double total = 0;
        for (double valor : faturamentoPorDia) {
            total += valor;
        }
        return total;
    }

    /** Retorna o total de vendas aceitas neste mês durante a sessão atual. */
    public double getFaturamentoDoMes() {
        if (!mesDeReferencia.equals(YearMonth.now())) {
            return 0;
        }
        return faturamentoDoMes;
    }

    /** Valor de cada período do mês. Período 1 = dias 1 a 7; período 5 = 29 ao fim. */
    public double getFaturamentoDoPeriodoDoMes(int periodo) {
        if (periodo < 1 || periodo > 5 || !mesDeReferencia.equals(YearMonth.now())) {
            return 0;
        }
        return faturamentoPorPeriodoDoMes[periodo - 1];
    }

    public Produto produtoMaisVendido() {
        Produto melhor = null;
        int maiorQuantidade = 0;
        for (int i = 0; i < produtos.size(); i++) {
            int vendido = totalVendidoDoProduto(i);
            if (vendido > maiorQuantidade) {
                maiorQuantidade = vendido;
                melhor = produtos.get(i);
            }
        }
        return melhor;
    }

    public int diaDeMaiorFaturamento() {
        double maiorFaturamento = 0;
        int melhorDia = 0;
        for (int dia = 1; dia <= 7; dia++) {
            double valor = getFaturamentoDoDia(dia);
            if (valor > maiorFaturamento) {
                maiorFaturamento = valor;
                melhorDia = dia;
            }
        }
        return melhorDia;
    }

    public ArrayList<Produto> produtosComEstoqueBaixo() {
        ArrayList<Produto> encontrados = new ArrayList<>();
        for (Produto produto : produtos) {
            if (produto.estoqueBaixo(LIMITE_ESTOQUE_BAIXO)) {
                encontrados.add(produto);
            }
        }
        return encontrados;
    }

    public double valorTotalEmEstoque() {
        double total = 0;
        for (Produto produto : produtos) {
            total += produto.valorEmEstoque();
        }
        return total;
    }

    /** Retorna uma cópia para impedir que outras classes apaguem o histórico. */
    public ArrayList<String> getHistorico() {
        return new ArrayList<>(historico);
    }

    private int indiceDoProduto(int codigo) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getCodigo() == codigo) {
                return i;
            }
        }
        return -1;
    }

    private boolean existeProdutoComNome(String nome) {
        for (Produto produto : produtos) {
            if (produto.getNome().equalsIgnoreCase(nome)) {
                return true;
            }
        }
        return false;
    }

    private boolean diaValido(int dia) {
        return dia >= 1 && dia <= 7;
    }

    private void registrarNoHistorico(String descricao) {
        historico.add(String.format("%03d | %s", historico.size() + 1, descricao));
    }

    private static String moeda(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }
}
