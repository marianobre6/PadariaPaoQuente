package padaria;

/**
 * Representa um produto da padaria.
 * Os dados ficam privados para garantir que o estoque e o preço
 * só sejam alterados por operações que façam as validações necessárias.
 */
public class Produto {
    private static int proximoCodigo = 1;

    private final int codigo;
    private final String nome;
    private final String categoria;
    private double preco;
    private int quantidade;

    public Produto(String nome, String categoria, double preco, int quantidade) {
        this.codigo = proximoCodigo++;
        if (nome == null) {
            this.nome = "";
        } else {
            this.nome = nome.trim();
        }
        if (categoria == null) {
            this.categoria = "Sem categoria";
        } else {
            this.categoria = categoria.trim();
        }
        if (Double.isFinite(preco) && preco > 0) {
            this.preco = preco;
        } else {
            this.preco = 0;
        }
        if (quantidade >= 0) {
            this.quantidade = quantidade;
        } else {
            this.quantidade = 0;
        }
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public boolean setPreco(double novoPreco) {
        if (!Double.isFinite(novoPreco) || novoPreco <= 0) {
            return false;
        }
        preco = novoPreco;
        return true;
    }

    public boolean adicionarEstoque(int unidades) {
        if (unidades <= 0 || unidades > Integer.MAX_VALUE - quantidade) {
            return false;
        }
        quantidade += unidades;
        return true;
    }

    // Só permite retirar quantidades positivas e disponíveis.
    public boolean removerEstoque(int unidades) {
        if (unidades <= 0 || unidades > quantidade) {
            return false;
        }
        quantidade -= unidades;
        return true;
    }

    public double valorEmEstoque() {
        return preco * quantidade;
    }

    public boolean estoqueBaixo(int limite) {
        return quantidade < limite;
    }
}
