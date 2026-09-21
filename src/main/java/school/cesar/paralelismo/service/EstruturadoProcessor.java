package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/**
 * V3 - Paralelismo estruturado com StructuredTaskScope (JEP 453/480).
 * A diferença essencial: o escopo garante que todas as subtarefas
 * terminam antes de sair do bloco try-with-resources (estrutura explícita).
 */
@Service
@SuppressWarnings("preview")
public class EstruturadoProcessor {

    private final CalculoService calculoService;

    public EstruturadoProcessor(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public double processar(double[][] matriz, int numeroDeTarefas) throws InterruptedException {
        List<int[]> faixas = Particionador.particionar(matriz.length, numeroDeTarefas);

        try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<Double>> subtarefas = new ArrayList<>(faixas.size());

            for (int[] faixa : faixas) {
                subtarefas.add(escopo.fork(() -> processarFaixa(matriz, faixa[0], faixa[1])));
            }

            escopo.join();
            escopo.throwIfFailed(RuntimeException::new);

            double resultado = 0.0;
            for (StructuredTaskScope.Subtask<Double> subtarefa : subtarefas) {
                resultado += subtarefa.get();
            }
            return resultado;
        }
    }

    private double processarFaixa(double[][] matriz, int inicio, int fim) {
        double soma = 0.0;
        for (int i = inicio; i < fim; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                soma += calculoService.calcular(matriz[i][j]);
            }
        }
        return soma;
    }
}
