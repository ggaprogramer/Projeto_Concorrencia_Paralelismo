package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.StructuredTaskScope;

/**
 * V4b - Estado compartilhado com ConcurrentLinkedQueue (coleção thread-safe).
 * Cada tarefa adiciona seu resultado parcial na fila sem sincronização explícita.
 */
@Service
@SuppressWarnings("preview")
public class EstruturadoQueueProcessor {

    private final CalculoService calculoService;

    public EstruturadoQueueProcessor(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public double processar(double[][] matriz, int numeroDeTarefas) throws InterruptedException {
        List<int[]> faixas = Particionador.particionar(matriz.length, numeroDeTarefas);
        ConcurrentLinkedQueue<Double> resultados = new ConcurrentLinkedQueue<>();

        try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int[] faixa : faixas) {
                escopo.fork((Callable<Void>) () -> {
                    double soma = 0.0;
                    for (int i = faixa[0]; i < faixa[1]; i++) {
                        for (int j = 0; j < matriz[i].length; j++) {
                            soma += calculoService.calcular(matriz[i][j]);
                        }
                    }
                    resultados.add(soma);
                    return null;
                });
            }

            escopo.join();
            escopo.throwIfFailed(RuntimeException::new);

            return resultados.stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
    }
}
