package com.hortifacil.model;

import org.mindrot.jbcrypt.BCrypt;

public class Criptografia {
    public static String hashSenha(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    public static boolean verificarSenha(String senhaPlain, String hash) {
        if (senhaPlain == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(senhaPlain, hash);
    }
}