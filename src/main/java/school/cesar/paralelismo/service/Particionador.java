package school.cesar.paralelismo.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Divide as linhas da matriz em N faixas aproximadamente equilibradas.
 * Cada tarefa processa uma faixa contígua (ex: linhas 0–99, 100–199, ...).
 */
public final class Particionador {

    private Particionador() {
    }

    public static List<int[]> particionar(int totalLinhas, int numeroDeTarefas) {
        int tarefas = Math.min(Math.max(1, numeroDeTarefas), totalLinhas);
        List<int[]> faixas = new ArrayList<>(tarefas);
        int linhasPorTarefa = totalLinhas / tarefas;

        for (int i = 0; i < tarefas; i++) {
            int inicio = i * linhasPorTarefa;
            int fim = (i == tarefas - 1) ? totalLinhas : (i + 1) * linhasPorTarefa;
            faixas.add(new int[]{inicio, fim});
        }
        return faixas;
    }
}
