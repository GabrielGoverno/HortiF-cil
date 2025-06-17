package com.hortifacil.util;

import com.hortifacil.controller.AdicionarEstoqueController;

/**
 * Singleton para fornecer acesso global ao ControleEstoqueAdminController.
 * Deve ser inicializado uma única vez via setInstance().
 */
public class EstoqueSingleton {

    private static AdicionarEstoqueController instance;

    public static void setInstance(AdicionarEstoqueController ctrl) {
        instance = ctrl;
    }

    public static AdicionarEstoqueController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdionarEstoqueController não foi inicializado");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }
}
