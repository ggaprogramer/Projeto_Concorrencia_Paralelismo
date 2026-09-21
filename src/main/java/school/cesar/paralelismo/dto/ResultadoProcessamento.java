package school.cesar.paralelismo.dto;

/**
 * Resultado de uma unica execucao de uma das implementacoes,
 * retornado pelos endpoints em /api/processamento/*.
 */
public record ResultadoProcessamento(
        String implementacao,
        int linhas,
        int colunas,
        int tarefas,
        double resultado,
        long tempoMs
) {
}
