package com.hortifacil.util;

import com.hortifacil.controller.ControleEstoqueAdminController;

/**
 * Singleton para fornecer acesso global ao ControleEstoqueAdminController.
 * Deve ser inicializado uma única vez via setInstance().
 */
public class EstoqueSingleton {

    private static ControleEstoqueAdminController instance;

    public static void setInstance(ControleEstoqueAdminController ctrl) {
        instance = ctrl;
    }

    public static ControleEstoqueAdminController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ControleEstoqueAdminController não foi inicializado");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }
}
