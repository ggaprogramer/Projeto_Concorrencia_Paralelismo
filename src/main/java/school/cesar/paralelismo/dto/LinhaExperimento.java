package school.cesar.paralelismo.dto;

/**
 * Uma linha da tabela final de comparacao exigida na Secao 10 do
 * enunciado: Implementacao | Tempo medio | Speedup | Quantidade de
 * tarefas | Resultado correto.
 */
public record LinhaExperimento(
        String implementacao,
        int linhas,
        int colunas,
        int tarefas,
        double tempoMedioMs,
        double speedup,
        boolean resultadoCorreto
) {
}
