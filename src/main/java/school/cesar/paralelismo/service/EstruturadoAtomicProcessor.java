package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * V4a - Estado compartilhado com DoubleAdder (variável atômica).
 * Cada tarefa acumula diretamente em somaTotal.add() sem sincronização explícita.
 * Usa Compare-And-Swap (CAS) internamente — operação lock-free.
 */
@Service
@SuppressWarnings("preview")
public class EstruturadoAtomicProcessor {

    private final CalculoService calculoService;

    public EstruturadoAtomicProcessor(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public double processar(double[][] matriz, int numeroDeTarefas) throws InterruptedException {
        List<int[]> faixas = Particionador.particionar(matriz.length, numeroDeTarefas);
        DoubleAdder somaTotal = new DoubleAdder();

        try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int[] faixa : faixas) {
                escopo.fork((Callable<Void>) () -> {
                    for (int i = faixa[0]; i < faixa[1]; i++) {
                        for (int j = 0; j < matriz[i].length; j++) {
                            somaTotal.add(calculoService.calcular(matriz[i][j]));
                        }
                    }
                    return null;
                });
            }

            escopo.join();
            escopo.throwIfFailed(RuntimeException::new);
            return somaTotal.sum();
        }
    }
}
