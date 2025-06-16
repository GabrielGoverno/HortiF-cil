package com.hortifacil.model;

public class Endereco {
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private int clienteId;  // ID do cliente dono do endereço

    // Construtor vazio
    public Endereco() {}

    // Construtor completo (com clienteId)
    public Endereco(String rua, String numero, String complemento, String bairro, int clienteId) {
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.clienteId = clienteId;
    }

    // Getters e setters
    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    @Override
    public String toString() {
        return rua + ", " + numero + (complemento != null && !complemento.isEmpty() ? " - " + complemento : "") + ", " + bairro;
    }
}
