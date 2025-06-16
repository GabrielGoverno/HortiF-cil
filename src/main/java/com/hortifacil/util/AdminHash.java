package com.hortifacil.util;

import org.mindrot.jbcrypt.BCrypt;

public class AdminHash {
    public static void main(String[] args) {
        String senha = "admin123"; // você pode trocar por qualquer outra
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(10));
        System.out.println("Senha: " + senha);
        System.out.println("Hash gerado: " + hash);
    }
}
