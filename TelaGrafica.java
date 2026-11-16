package padaria;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Interface visual do sistema, feita com Java Swing.
 * As regras de estoque e vendas continuam na classe Padaria.
 * Esta tela é opcional: o enunciado exige o menu em console.
 */
public class TelaGrafica extends JFrame {
    // Paleta centralizada: só as colunas do gráfico usam azul.
    private static final Color ROXO_ESCURO = new Color(52, 29, 90);
    private static final Color ROXO = new Color(109, 62, 178);
    private static final Color ROXO_CLARO = new Color(241, 234, 250);
    private static final Color AZUL_GRAFICO = new Color(45, 113, 217);
    private static final Color FUNDO = new Color(249, 248, 252);
    private static final Color TEXTO = new Color(47, 39, 63);
    private static final Color CINZA = new Color(110, 104, 121);
    private static final Color LINHA = new Color(228, 225, 235);
    private static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 29);
    private static final Font FONTE_NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    private final Padaria padaria;
    private final CardLayout telas = new CardLayout();
    private final JPanel conteudo = new JPanel(telas);
    private final JLabel[] indicadores = new JLabel[5];
    private final JLabel status = new JLabel("Pronto para operar");
    private final JTextArea aviso = new JTextArea();
    private final DefaultTableModel modeloEstoque = new DefaultTableModel(
            new String[]{"Código", "Produto", "Categoria", "Preço", "Em estoque", "Valor estocado"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final DefaultTableModel modeloVendas = new DefaultTableModel(
            new String[]{"Produto", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabelaEstoque = new JTable(modeloEstoque);
    private final JTable tabelaVendas = new JTable(modeloVendas);
    private final JTextArea textoFechamento = new JTextArea();
    private final JTextArea textoHistorico = new JTextArea();
    private final JTextArea textoEstoqueBaixo = new JTextArea();
    private final JComboBox<String> selecionarProduto = new JComboBox<>();
    private final JComboBox<String> selecionarDia = new JComboBox<>(new String[]{
            "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira",
            "Sexta-feira", "Sábado", "Domingo"});
    private final JSpinner unidadesVenda = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
    private final PainelGrafico grafico = new PainelGrafico();
    private final PainelGraficoMensal graficoMensal = new PainelGraficoMensal();
    private final JLabel totalDoGraficoMensal = new JLabel("—");

    public TelaGrafica() {
        super("Padaria Pão Quente | Controle de Estoque e Vendas");
        padaria = new Padaria("Padaria Pão Quente");
        carregarDadosIniciais();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1300, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FUNDO);
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarAreaPrincipal(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        atualizarTudo();
        telas.show(conteudo, "Visão geral");
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(ROXO_ESCURO);
        cabecalho.setBorder(new EmptyBorder(19, 30, 19, 32));
        JLabel marca = new JLabel("PÃO QUENTE    /    PAINEL DE GESTÃO");
        marca.setFont(new Font("SansSerif", Font.BOLD, 22));
        marca.setForeground(Color.WHITE);
        cabecalho.add(marca, BorderLayout.WEST);
        JLabel direito = new JLabel("SISTEMA DEMONSTRATIVO  •  JAVA 17+");
        direito.setForeground(new Color(226, 216, 243));
        direito.setFont(new Font("SansSerif", Font.BOLD, 11));
        cabecalho.add(direito, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarAreaPrincipal() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(FUNDO);
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Color.WHITE);
        lateral.setPreferredSize(new Dimension(220, 700));
        lateral.setBorder(new EmptyBorder(25, 15, 20, 15));
        JLabel tituloMenu = new JLabel("NAVEGAÇÃO");
        tituloMenu.setForeground(CINZA);
        tituloMenu.setFont(new Font("SansSerif", Font.BOLD, 11));
        tituloMenu.setBorder(new EmptyBorder(0, 12, 12, 0));
        lateral.add(tituloMenu);
        String[] nomes = {"Visão geral", "Gráfico mensal", "Estoque", "Registrar venda", "Movimentações", "Relatórios", "Histórico"};
        for (String nome : nomes) {
            JButton botao = new JButton(nome);
            botao.setHorizontalAlignment(SwingConstants.LEFT);
            botao.setFocusPainted(false);
            botao.setBorder(new EmptyBorder(14, 14, 14, 10));
            botao.setBackground(Color.WHITE);
            botao.setForeground(TEXTO);
            botao.setFont(new Font("SansSerif", Font.BOLD, 14));
            botao.setMaximumSize(new Dimension(200, 52));
            botao.setAlignmentX(LEFT_ALIGNMENT);
            botao.addActionListener((ActionEvent e) -> {
                telas.show(conteudo, nome);
                for (java.awt.Component componente : lateral.getComponents()) {
                    if (componente instanceof JButton b) {
                        b.setBackground(b == botao ? ROXO_CLARO : Color.WHITE);
                        b.setForeground(b == botao ? ROXO : TEXTO);
                    }
                }
                atualizarTudo();
            });
            lateral.add(botao);
            lateral.add(Box.createVerticalStrut(5));
            if (nome.equals("Visão geral")) {
                botao.setBackground(ROXO_CLARO);
                botao.setForeground(ROXO);
            }
        }
        lateral.add(Box.createVerticalGlue());
        JLabel limite = new JLabel("SEM BANCO DE DADOS");
        limite.setForeground(CINZA);
        limite.setFont(new Font("SansSerif", Font.PLAIN, 10));
        limite.setBorder(new EmptyBorder(10, 5, 0, 0));
        lateral.add(limite);
        principal.add(lateral, BorderLayout.WEST);

        conteudo.setBackground(FUNDO);
        conteudo.add(criarDashboard(), "Visão geral");
        conteudo.add(criarPaginaGraficoMensal(), "Gráfico mensal");
        conteudo.add(criarEstoque(), "Estoque");
        conteudo.add(criarVendas(), "Registrar venda");
        conteudo.add(criarMovimentacoes(), "Movimentações");
        conteudo.add(criarRelatorios(), "Relatórios");
        conteudo.add(criarHistorico(), "Histórico");
        principal.add(conteudo, BorderLayout.CENTER);
        return principal;
    }

    private JPanel criarDashboard() {
        JPanel pagina = novaPagina("Visão geral", "Acompanhe a semana da padaria em tempo real.");
        JPanel corpo = new JPanel(new BorderLayout(0, 22));
        corpo.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        String[] rotulos = {"FATURAMENTO DA SEMANA", "FATURAMENTO DO MÊS", "VALOR EM ESTOQUE", "PRODUTOS", "ESTOQUE BAIXO"};
        for (int i = 0; i < 5; i++) {
            JPanel card = branco(new BorderLayout());
            JLabel rotulo = new JLabel(rotulos[i]);
            rotulo.setForeground(CINZA);
            rotulo.setFont(new Font("SansSerif", Font.BOLD, 10));
            indicadores[i] = new JLabel("—");
            indicadores[i].setFont(new Font("SansSerif", Font.BOLD, 25));
            indicadores[i].setForeground(i == 4 ? ROXO : ROXO_ESCURO);
            card.add(rotulo, BorderLayout.NORTH);
            card.add(indicadores[i], BorderLayout.CENTER);
            cards.add(card);
        }
        corpo.add(cards, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        JPanel graficoCard = branco(new BorderLayout());
        JLabel graficoTitulo = new JLabel("Faturamento por dia");
        graficoTitulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        graficoTitulo.setForeground(TEXTO);
        graficoCard.add(graficoTitulo, BorderLayout.NORTH);
        grafico.setPreferredSize(new Dimension(510, 300));
        graficoCard.add(grafico, BorderLayout.CENTER);
        centro.add(graficoCard, BorderLayout.CENTER);
        JPanel alertaCard = branco(new BorderLayout(0, 15));
        alertaCard.setPreferredSize(new Dimension(250, 250));
        JLabel alertaTitulo = new JLabel("ATENÇÃO AO ESTOQUE");
        alertaTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        alertaTitulo.setForeground(ROXO_ESCURO);
        alertaCard.add(alertaTitulo, BorderLayout.NORTH);
        aviso.setEditable(false);
        aviso.setLineWrap(true);
        aviso.setWrapStyleWord(true);
        aviso.setOpaque(false);
        aviso.setForeground(TEXTO);
        aviso.setFont(FONTE_NORMAL);
        alertaCard.add(aviso, BorderLayout.CENTER);
        JButton irEstoque = botaoDestaque("Ver produtos");
        irEstoque.addActionListener(e -> telas.show(conteudo, "Estoque"));
        alertaCard.add(irEstoque, BorderLayout.SOUTH);
        centro.add(alertaCard, BorderLayout.EAST);
        corpo.add(centro, BorderLayout.CENTER);
        JLabel legenda = new JLabel("Os dados são temporários e são atualizados após cada operação. Ao fechar o programa, a sessão é reiniciada.");
        legenda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        legenda.setForeground(CINZA);
        corpo.add(legenda, BorderLayout.SOUTH);
        pagina.add(corpo, BorderLayout.CENTER);
        return pagina;
    }

    /** Uma página separada: o gráfico semanal permanece na Visão geral. */
    private JPanel criarPaginaGraficoMensal() {
        String mes = YearMonth.now().format(
                DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR")));
        JPanel pagina = novaPagina("Gráfico mensal", "Faturamento registrado em " + mes + ".");

        JPanel corpo = new JPanel(new BorderLayout(0, 16));
        corpo.setOpaque(false);

        JPanel resumo = branco(new BorderLayout(12, 8));
        JLabel tituloTotal = new JLabel("FATURAMENTO DO MÊS");
        tituloTotal.setForeground(CINZA);
        tituloTotal.setFont(new Font("SansSerif", Font.BOLD, 12));
        totalDoGraficoMensal.setForeground(ROXO_ESCURO);
        totalDoGraficoMensal.setFont(new Font("SansSerif", Font.BOLD, 28));
        resumo.add(tituloTotal, BorderLayout.NORTH);
        resumo.add(totalDoGraficoMensal, BorderLayout.CENTER);
        corpo.add(resumo, BorderLayout.NORTH);

        JPanel card = branco(new BorderLayout(0, 15));
        JLabel titulo = new JLabel("Faturamento por períodos do mês");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        card.add(titulo, BorderLayout.NORTH);
        card.add(graficoMensal, BorderLayout.CENTER);
        corpo.add(card, BorderLayout.CENTER);

        JLabel explicacao = new JLabel(
                "Períodos: 1–7, 8–14, 15–21, 22–28 e 29–fim. Vendas agrupadas pela data em que foram lançadas.");
        explicacao.setForeground(CINZA);
        explicacao.setFont(new Font("SansSerif", Font.PLAIN, 12));
        corpo.add(explicacao, BorderLayout.SOUTH);

        pagina.add(corpo, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarEstoque() {
        JPanel pagina = novaPagina("Controle de estoque", "Cadastre, consulte e encontre produtos rapidamente.");
        JPanel corpo = branco(new BorderLayout(0, 18));
        JPanel barra = new JPanel(new BorderLayout(15, 0));
        barra.setOpaque(false);
        JTextField filtro = new JTextField();
        filtro.setFont(FONTE_NORMAL);
        filtro.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINHA), new EmptyBorder(11, 14, 11, 14)));
        filtro.setToolTipText("Digite parte do nome e pressione Enter");
        barra.add(filtro, BorderLayout.CENTER);
        TableRowSorter<DefaultTableModel> ordenacao = new TableRowSorter<>(modeloEstoque);
        tabelaEstoque.setRowSorter(ordenacao);
        filtro.addActionListener(e -> filtrarEstoque(filtro.getText(), ordenacao));
        JButton buscar = botaoSimples("Buscar");
        buscar.addActionListener(e -> filtrarEstoque(filtro.getText(), ordenacao));
        JButton cadastrar = botaoDestaque("+ Novo produto");
        cadastrar.addActionListener(e -> dialogoCadastrar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        botoes.setOpaque(false);
        botoes.add(buscar);
        botoes.add(cadastrar);
        barra.add(botoes, BorderLayout.EAST);
        corpo.add(barra, BorderLayout.NORTH);
        estilizarTabela(tabelaEstoque);
        corpo.add(new JScrollPane(tabelaEstoque), BorderLayout.CENTER);
        JLabel rodape = new JLabel("Limite de 20 produtos  •  Códigos automáticos  •  Produtos não podem ser excluídos");
        rodape.setForeground(CINZA);
        rodape.setFont(new Font("SansSerif", Font.PLAIN, 12));
        corpo.add(rodape, BorderLayout.SOUTH);
        pagina.add(corpo, BorderLayout.CENTER);
        return pagina;
    }

    private void filtrarEstoque(String trecho, TableRowSorter<DefaultTableModel> ordenacao) {
        ordenacao.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(trecho), 1));
        status.setText("Busca realizada: " + tabelaEstoque.getRowCount() + " produto(s) encontrado(s).");
    }

    private JPanel criarVendas() {
        JPanel pagina = novaPagina("Registrar uma venda", "Baixa automática no estoque e atualização do faturamento diário.");
        JPanel externo = new JPanel(new BorderLayout());
        externo.setOpaque(false);
        JPanel formulario = branco(new GridLayout(0, 1, 0, 10));
        formulario.setPreferredSize(new Dimension(560, 380));
        formulario.add(rotuloCampo("PRODUTO"));
        selecionarProduto.setFont(FONTE_NORMAL);
        formulario.add(selecionarProduto);
        formulario.add(rotuloCampo("QUANTIDADE"));
        formulario.add(unidadesVenda);
        formulario.add(rotuloCampo("DIA DA SEMANA"));
        selecionarDia.setFont(FONTE_NORMAL);
        formulario.add(selecionarDia);
        JButton registrar = botaoDestaque("CONFIRMAR VENDA");
        registrar.setPreferredSize(new Dimension(400, 50));
        registrar.addActionListener(e -> {
            int indice = selecionarProduto.getSelectedIndex();
            if (indice < 0) {
                mensagem("ERRO: selecione um produto.");
                return;
            }
            Produto p = padaria.getProdutos().get(indice);
            int quantidade = ((Number) unidadesVenda.getValue()).intValue();
            mensagem(padaria.registrarVenda(p.getCodigo(), quantidade, selecionarDia.getSelectedIndex() + 1));
        });
        formulario.add(registrar);
        JPanel alinhamento = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alinhamento.setOpaque(false);
        alinhamento.add(formulario);
        externo.add(alinhamento, BorderLayout.NORTH);
        pagina.add(externo, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarMovimentacoes() {
        JPanel pagina = novaPagina("Movimentações de produtos", "Reponha quantidades e atualize os preços sem alterar vendas passadas.");
        JPanel opcoes = new JPanel(new GridLayout(1, 2, 18, 0));
        opcoes.setOpaque(false);
        JPanel reposicao = branco(new BorderLayout(0, 20));
        reposicao.add(blocoTexto("REPOSIÇÃO DE ESTOQUE", "Adicione novas unidades ao saldo do produto.\nA alteração será registrada no histórico."), BorderLayout.NORTH);
        JButton repor = botaoDestaque("+ Repor estoque");
        repor.addActionListener(e -> dialogoRepor());
        reposicao.add(repor, BorderLayout.SOUTH);
        opcoes.add(reposicao);
        JPanel precos = branco(new BorderLayout(0, 20));
        precos.add(blocoTexto("ALTERAR PREÇO", "O novo preço vale para próximas vendas.\nO faturamento já registrado é preservado."), BorderLayout.NORTH);
        JButton alterar = botaoDestaque("Alterar preço de produto");
        alterar.addActionListener(e -> dialogoPreco());
        precos.add(alterar, BorderLayout.SOUTH);
        opcoes.add(precos);
        JPanel externo = new JPanel(new BorderLayout());
        externo.setOpaque(false);
        externo.add(opcoes, BorderLayout.NORTH);
        pagina.add(externo, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarRelatorios() {
        JPanel pagina = novaPagina("Relatórios semanais", "Dados provenientes da matriz de vendas e do vetor de faturamento.");
        javax.swing.JTabbedPane abas = new javax.swing.JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 13));
        estilizarTabela(tabelaVendas);
        abas.addTab("Matriz de vendas", new JScrollPane(tabelaVendas));
        abas.addTab("Fechamento financeiro", areaTexto(textoFechamento));
        abas.addTab("Estoque baixo", areaTexto(textoEstoqueBaixo));
        pagina.add(abas, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarHistorico() {
        JPanel pagina = novaPagina("Histórico de operações", "Lista numerada de todos os cadastros, vendas, reposições e alterações aceitas.");
        pagina.add(areaTexto(textoHistorico), BorderLayout.CENTER);
        return pagina;
    }

    private JScrollPane areaTexto(JTextArea area) {
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setForeground(TEXTO);
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(20, 22, 20, 22));
        return new JScrollPane(area);
    }

    private JPanel criarRodape() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(ROXO_CLARO);
        painel.setBorder(new EmptyBorder(11, 28, 11, 28));
        status.setForeground(ROXO_ESCURO);
        status.setFont(new Font("SansSerif", Font.BOLD, 12));
        painel.add(status, BorderLayout.WEST);
        JLabel versao = new JLabel("IBMR • ALGORITMOS E PROGRAMAÇÃO • INTERFACE JAVA SWING");
        versao.setForeground(CINZA);
        versao.setFont(new Font("SansSerif", Font.PLAIN, 10));
        painel.add(versao, BorderLayout.EAST);
        return painel;
    }

    private JPanel novaPagina(String titulo, String subtitulo) {
        JPanel pagina = new JPanel(new BorderLayout(0, 23));
        pagina.setBackground(FUNDO);
        pagina.setBorder(new EmptyBorder(28, 29, 28, 29));
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4));
        textos.setOpaque(false);
        JLabel grande = new JLabel(titulo);
        grande.setFont(FONTE_TITULO);
        grande.setForeground(TEXTO);
        textos.add(grande);
        JLabel pequeno = new JLabel(subtitulo);
        pequeno.setFont(FONTE_NORMAL);
        pequeno.setForeground(CINZA);
        textos.add(pequeno);
        pagina.add(textos, BorderLayout.NORTH);
        return pagina;
    }

    private JPanel branco(java.awt.LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINHA), new EmptyBorder(20, 20, 20, 20)));
        return painel;
    }

    private JButton botaoDestaque(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(ROXO);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setOpaque(true);
        botao.setFont(new Font("SansSerif", Font.BOLD, 13));
        botao.setBorder(new EmptyBorder(12, 17, 12, 17));
        return botao;
    }

    private JButton botaoSimples(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(ROXO_CLARO);
        botao.setForeground(ROXO_ESCURO);
        botao.setFocusPainted(false);
        botao.setFont(new Font("SansSerif", Font.BOLD, 13));
        botao.setBorder(new EmptyBorder(12, 17, 12, 17));
        return botao;
    }

    private JLabel rotuloCampo(String nome) {
        JLabel rotulo = new JLabel(nome);
        rotulo.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotulo.setForeground(CINZA);
        return rotulo;
    }

    private JPanel blocoTexto(String titulo, String descricao) {
        JPanel painel = new JPanel(new GridLayout(2, 1, 0, 12));
        painel.setOpaque(false);
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        tituloLabel.setForeground(ROXO_ESCURO);
        painel.add(tituloLabel);
        JTextArea descricaoTexto = new JTextArea(descricao);
        descricaoTexto.setEditable(false);
        descricaoTexto.setOpaque(false);
        descricaoTexto.setLineWrap(true);
        descricaoTexto.setWrapStyleWord(true);
        descricaoTexto.setFont(FONTE_NORMAL);
        descricaoTexto.setForeground(CINZA);
        painel.add(descricaoTexto);
        return painel;
    }

    private void estilizarTabela(JTable tabela) {
        tabela.setFont(FONTE_NORMAL);
        tabela.setRowHeight(38);
        tabela.setGridColor(LINHA);
        tabela.setShowVerticalLines(false);
        tabela.setSelectionBackground(new Color(231, 221, 247));
        tabela.setSelectionForeground(TEXTO);
        tabela.setFillsViewportHeight(true);
        JTableHeader header = tabela.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(ROXO_ESCURO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 42));
    }

    private void dialogoCadastrar() {
        JTextField nome = new JTextField();
        JTextField categoria = new JTextField();
        JTextField preco = new JTextField();
        JTextField quantidade = new JTextField();
        Object[] campos = {"Nome:", nome, "Categoria (opcional):", categoria,
                "Preço em reais (ex.: 3,50):", preco, "Quantidade inicial:", quantidade};
        int opcao = JOptionPane.showConfirmDialog(this, campos, "Cadastrar novo produto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            double valor = numero(preco.getText());
            int saldo = Integer.parseInt(quantidade.getText().trim());
            mensagem(padaria.cadastrarProduto(nome.getText(), categoria.getText(), valor, saldo));
        } catch (NumberFormatException e) {
            mensagem("ERRO: informe preço e quantidade válidos.");
        }
    }

    private void dialogoRepor() {
        JTextField codigo = new JTextField();
        JTextField unidades = new JTextField();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Código do produto:", codigo,
                "Unidades para adicionar:", unidades}, "Repor estoque",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            mensagem(padaria.reporEstoque(Integer.parseInt(codigo.getText().trim()),
                    Integer.parseInt(unidades.getText().trim())));
        } catch (NumberFormatException e) {
            mensagem("ERRO: o código e a quantidade devem ser números inteiros.");
        }
    }

    private void dialogoPreco() {
        JTextField codigo = new JTextField();
        JTextField preco = new JTextField();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Código do produto:", codigo,
                "Novo preço em reais:", preco}, "Alterar preço",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            mensagem(padaria.alterarPreco(Integer.parseInt(codigo.getText().trim()), numero(preco.getText())));
        } catch (NumberFormatException e) {
            mensagem("ERRO: informe código e preço válidos.");
        }
    }

    private double numero(String texto) {
        double valor = Double.parseDouble(texto.trim().replace(',', '.'));
        if (!Double.isFinite(valor)) {
            throw new NumberFormatException();
        }
        return valor;
    }

    private void mensagem(String texto) {
        boolean sucesso = texto.startsWith("OK:");
        status.setText(texto);
        JOptionPane.showMessageDialog(this, texto, sucesso ? "Operação realizada" : "Operação recusada",
                sucesso ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        atualizarTudo();
    }

    private void atualizarTudo() {
        indicadores[0].setText(moeda(padaria.getFaturamentoTotal()));
        indicadores[1].setText(moeda(padaria.getFaturamentoDoMes()));
        indicadores[2].setText(moeda(padaria.valorTotalEmEstoque()));
        indicadores[3].setText(padaria.getQuantidadeDeProdutos() + " / " + Padaria.MAX_PRODUTOS);
        indicadores[4].setText(String.valueOf(padaria.produtosComEstoqueBaixo().size()));
        ArrayList<Produto> baixo = padaria.produtosComEstoqueBaixo();
        if (baixo.isEmpty()) {
            aviso.setText("Tudo em ordem!\n\nTodos os produtos possuem estoque suficiente.");
        } else {
            StringBuilder alerta = new StringBuilder("Precisam de reposição:\n\n");
            for (Produto produto : baixo) {
                alerta.append("• ").append(produto.getNome()).append(" — ")
                        .append(produto.getQuantidade()).append(" un.\n");
            }
            aviso.setText(alerta.toString());
        }
        modeloEstoque.setRowCount(0);
        ArrayList<Produto> produtos = padaria.getProdutos();
        for (Produto p : produtos) {
            modeloEstoque.addRow(new Object[]{p.getCodigo(), p.getNome(), p.getCategoria(),
                    moeda(p.getPreco()), p.getQuantidade(), moeda(p.valorEmEstoque())});
        }
        int selecionado = selecionarProduto.getSelectedIndex();
        selecionarProduto.removeAllItems();
        for (Produto p : produtos) {
            selecionarProduto.addItem(p.getCodigo() + " - " + p.getNome() + "   |   "
                    + p.getQuantidade() + " un.   |   " + moeda(p.getPreco()));
        }
        if (selecionado >= 0 && selecionado < selecionarProduto.getItemCount()) {
            selecionarProduto.setSelectedIndex(selecionado);
        }
        modeloVendas.setRowCount(0);
        int[] totais = new int[7];
        int totalGeral = 0;
        for (int i = 0; i < produtos.size(); i++) {
            Object[] linha = new Object[9];
            linha[0] = produtos.get(i).getNome();
            for (int dia = 1; dia <= 7; dia++) {
                int valor = padaria.getVendas(i, dia);
                linha[dia] = valor;
                totais[dia - 1] += valor;
            }
            linha[8] = padaria.totalVendidoDoProduto(i);
            totalGeral += (int) linha[8];
            modeloVendas.addRow(linha);
        }
        Object[] ultimaLinha = new Object[9];
        ultimaLinha[0] = "TOTAL";
        for (int dia = 1; dia <= 7; dia++) {
            ultimaLinha[dia] = totais[dia - 1];
        }
        ultimaLinha[8] = totalGeral;
        modeloVendas.addRow(ultimaLinha);
        textoFechamento.setText(gerarFechamento());
        textoEstoqueBaixo.setText(gerarBaixoEstoque());
        StringBuilder historico = new StringBuilder("HISTÓRICO DE OPERAÇÕES — PÃO QUENTE\n\n");
        for (String linha : padaria.getHistorico()) {
            historico.append(linha).append('\n');
        }
        historico.append("\nTotal: ").append(padaria.getHistorico().size()).append(" operações aceitas.");
        textoHistorico.setText(historico.toString());
        grafico.repaint();
        totalDoGraficoMensal.setText(moeda(padaria.getFaturamentoDoMes()));
        graficoMensal.repaint();
    }

    private String gerarFechamento() {
        StringBuilder texto = new StringBuilder("FECHAMENTO DA SEMANA — ")
                .append(padaria.getNome()).append("\n")
                .append("=".repeat(55)).append("\n\nFATURAMENTO POR DIA\n\n");
        for (int dia = 1; dia <= 7; dia++) {
            String nomeDia = Padaria.DIAS[dia - 1];
            String faturamento = moeda(padaria.getFaturamentoDoDia(dia));
            texto.append(String.format("%-8s %15s%n", nomeDia, faturamento));
        }
        Produto maior = padaria.produtoMaisVendido();
        int melhorDia = padaria.diaDeMaiorFaturamento();
        texto.append("\n").append("-".repeat(55))
                .append("\nFaturamento total:       ").append(moeda(padaria.getFaturamentoTotal()))
                .append("\nProduto mais vendido:   ").append(maior == null ? "Nenhum" : maior.getNome())
                .append("\nDia de maior receita:   ").append(melhorDia == 0 ? "Nenhum" : Padaria.DIAS[melhorDia - 1])
                .append("\nValor total em estoque: ").append(moeda(padaria.valorTotalEmEstoque()))
                .append("\nProdutos com estoque baixo: ").append(padaria.produtosComEstoqueBaixo().size())
                .append("\nOperações registradas: ").append(padaria.getHistorico().size());
        return texto.toString();
    }

    private String gerarBaixoEstoque() {
        StringBuilder texto = new StringBuilder("PRODUTOS COM MENOS DE ")
                .append(Padaria.LIMITE_ESTOQUE_BAIXO).append(" UNIDADES\n\n");
        if (padaria.produtosComEstoqueBaixo().isEmpty()) {
            return texto.append("Nenhum produto com estoque baixo.").toString();
        }
        for (Produto p : padaria.produtosComEstoqueBaixo()) {
            texto.append(String.format("#%02d  %-25s  %d unidade(s)%n",
                    p.getCodigo(), p.getNome(), p.getQuantidade()));
        }
        return texto.toString();
    }

    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    private void carregarDadosIniciais() {
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

    private final class PainelGrafico extends JPanel {
        private PainelGrafico() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int esquerdo = 55, direito = 24, topo = 26, base = 44;
            int largura = Math.max(1, w - esquerdo - direito);
            int altura = Math.max(1, h - topo - base);
            double maior = 0;
            for (int dia = 1; dia <= 7; dia++) {
                maior = Math.max(maior, padaria.getFaturamentoDoDia(dia));
            }
            maior = Math.max(100, maior);
            gg.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int y = topo + altura - altura * i / 4;
                gg.setColor(LINHA);
                gg.setStroke(new BasicStroke(1));
                gg.drawLine(esquerdo, y, w - direito, y);
                gg.setColor(CINZA);
                gg.drawString(String.format("%.0f", maior * i / 4), 8, y + 4);
            }
            int passo = largura / 7;
            int barra = Math.min(44, Math.max(13, passo / 2));
            for (int i = 0; i < 7; i++) {
                double valor = padaria.getFaturamentoDoDia(i + 1);
                int alturaBarra = (int) (valor / maior * (altura - 5));
                int x = esquerdo + passo * i + (passo - barra) / 2;
                int y = topo + altura - alturaBarra;
                gg.setColor(AZUL_GRAFICO);
                if (alturaBarra > 0) {
                    gg.fillRoundRect(x, y, barra, alturaBarra, 10, 10);
                }
                gg.setColor(TEXTO);
                gg.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics m = gg.getFontMetrics();
                String dia = Padaria.DIAS[i];
                gg.drawString(dia, x + (barra - m.stringWidth(dia)) / 2, topo + altura + 24);
            }
            gg.dispose();
        }
    }

    /** Gráfico azul do mês, em outra página, dividido em cinco períodos. */
    private final class PainelGraficoMensal extends JPanel {
        private PainelGraficoMensal() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int larguraTela = getWidth();
            int alturaTela = getHeight();
            int margemEsquerda = 64;
            int margemDireita = 25;
            int topo = 55;
            int rodape = 60;
            int largura = Math.max(1, larguraTela - margemEsquerda - margemDireita);
            int altura = Math.max(1, alturaTela - topo - rodape);

            double maior = 0;
            for (int periodo = 1; periodo <= 5; periodo++) {
                maior = Math.max(maior, padaria.getFaturamentoDoPeriodoDoMes(periodo));
            }
            maior = Math.max(100, maior);

            // Linhas e valores de referência para comparar os períodos.
            gg.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i <= 4; i++) {
                int y = topo + altura - altura * i / 4;
                gg.setColor(LINHA);
                gg.drawLine(margemEsquerda, y, larguraTela - margemDireita, y);
                gg.setColor(CINZA);
                gg.drawString(String.format(Locale.forLanguageTag("pt-BR"), "%.0f", maior * i / 4), 12, y + 4);
            }

            String[] rotulos = {"1–7", "8–14", "15–21", "22–28", "29–fim"};
            int passo = largura / 5;
            int barra = Math.min(78, Math.max(20, passo / 2));
            for (int i = 0; i < 5; i++) {
                double valor = padaria.getFaturamentoDoPeriodoDoMes(i + 1);
                int alturaBarra = (int) (valor / maior * (altura - 8));
                int x = margemEsquerda + i * passo + (passo - barra) / 2;
                int y = topo + altura - alturaBarra;
                gg.setColor(AZUL_GRAFICO);
                if (alturaBarra > 0) {
                    gg.fillRoundRect(x, y, barra, alturaBarra, 11, 11);
                }

                // Apresenta o faturamento sobre cada coluna preenchida.
                gg.setFont(new Font("SansSerif", Font.BOLD, 12));
                gg.setColor(ROXO_ESCURO);
                String valorTexto = valor > 0 ? moeda(valor) : "R$ 0";
                FontMetrics fonte = gg.getFontMetrics();
                gg.drawString(valorTexto, x + (barra - fonte.stringWidth(valorTexto)) / 2,
                        Math.max(24, y - 9));

                gg.setColor(TEXTO);
                gg.setFont(new Font("SansSerif", Font.BOLD, 13));
                fonte = gg.getFontMetrics();
                gg.drawString(rotulos[i], x + (barra - fonte.stringWidth(rotulos[i])) / 2,
                        topo + altura + 25);
            }
            gg.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorado) {
                // O programa também funciona com o visual padrão do Java.
            }
            new TelaGrafica().setVisible(true);
        });
    }
}
