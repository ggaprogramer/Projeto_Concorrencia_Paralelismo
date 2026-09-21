package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;
import school.cesar.paralelismo.dto.LinhaExperimento;

import java.util.ArrayList;
import java.util.List;

/**
 * Executa experimentos de desempenho: 4 tamanhos de matriz × 3 quantidades
 * de tarefas, 10 repetições cada. Calcula tempo médio, speedup e corretude.
 */
@Service
public class ExperimentoService {

    private static final int[] TAMANHOS_MATRIZ = {500, 1000, 1500, 2000};
    private static final int[] QUANTIDADES_TAREFAS = {5, 10, 100};
    private static final int REPETICOES = 10;
    private static final double TOLERANCIA_RELATIVA = 1e-6;

    private final MatrizGenerator matrizGenerator;
    private final SequencialProcessor sequencialProcessor;
    private final NaoEstruturadoProcessor naoEstruturadoProcessor;
    private final EstruturadoProcessor estruturadoProcessor;
    private final EstruturadoAtomicProcessor estruturadoAtomicProcessor;
    private final EstruturadoQueueProcessor estruturadoQueueProcessor;

    public ExperimentoService(MatrizGenerator matrizGenerator,
                               SequencialProcessor sequencialProcessor,
                               NaoEstruturadoProcessor naoEstruturadoProcessor,
                               EstruturadoProcessor estruturadoProcessor,
                               EstruturadoAtomicProcessor estruturadoAtomicProcessor,
                               EstruturadoQueueProcessor estruturadoQueueProcessor) {
        this.matrizGenerator = matrizGenerator;
        this.sequencialProcessor = sequencialProcessor;
        this.naoEstruturadoProcessor = naoEstruturadoProcessor;
        this.estruturadoProcessor = estruturadoProcessor;
        this.estruturadoAtomicProcessor = estruturadoAtomicProcessor;
        this.estruturadoQueueProcessor = estruturadoQueueProcessor;
    }

    public List<LinhaExperimento> executarTudo() throws InterruptedException {
        List<LinhaExperimento> linhas = new ArrayList<>();
        for (int tamanho : TAMANHOS_MATRIZ) {
            linhas.addAll(executarParaTamanho(tamanho));
        }
        return linhas;
    }

    public List<LinhaExperimento> executarParaTamanho(int tamanho) throws InterruptedException {
        List<LinhaExperimento> linhas = new ArrayList<>();
        double[][] matriz = matrizGenerator.gerar(tamanho, tamanho);

        Resultado baseline = medir(REPETICOES, () -> sequencialProcessor.processar(matriz));
        linhas.add(new LinhaExperimento("Sequencial", tamanho, tamanho, 1,
                baseline.tempoMs(), 1.0, true));

        for (int tarefas : QUANTIDADES_TAREFAS) {
            adicionarLinha(linhas, "Paralelismo não estruturado", tamanho, tarefas, baseline,
                    () -> naoEstruturadoProcessor.processar(matriz, tarefas));

            adicionarLinha(linhas, "Paralelismo estruturado", tamanho, tarefas, baseline,
                    envolver(() -> estruturadoProcessor.processar(matriz, tarefas)));

            adicionarLinha(linhas, "Estruturado + AtomicInteger (DoubleAdder)", tamanho, tarefas, baseline,
                    envolver(() -> estruturadoAtomicProcessor.processar(matriz, tarefas)));

            adicionarLinha(linhas, "Estruturado + ConcurrentLinkedQueue", tamanho, tarefas, baseline,
                    envolver(() -> estruturadoQueueProcessor.processar(matriz, tarefas)));
        }

        return linhas;
    }

    private void adicionarLinha(List<LinhaExperimento> linhas, String nome, int tamanho, int tarefas,
                                 Resultado baseline, Supplier<Double> executor) {
        Resultado resultado = medir(REPETICOES, executor);
        boolean correto = validarCorretude(baseline, resultado);
        double speedup = baseline.tempoMs() / resultado.tempoMs();
        linhas.add(new LinhaExperimento(nome, tamanho, tamanho, tarefas,
                resultado.tempoMs(), speedup, correto));
    }

    private boolean validarCorretude(Resultado baseline, Resultado resultado) {
        double diferenca = Math.abs(resultado.valor() - baseline.valor())
                / Math.max(1e-9, Math.abs(baseline.valor()));
        return diferenca < TOLERANCIA_RELATIVA;
    }

    private Resultado medir(int repeticoes, Supplier<Double> executor) {
        long somaTemposNs = 0;
        double ultimoResultado = 0.0;
        for (int i = 0; i < repeticoes; i++) {
            long inicio = System.nanoTime();
            ultimoResultado = executor.get();
            long fim = System.nanoTime();
            somaTemposNs += (fim - inicio);
        }
        double tempoMs = (somaTemposNs / (double) repeticoes) / 1_000_000.0;
        return new Resultado(tempoMs, ultimoResultado);
    }

    private Supplier<Double> envolver(ExecutorComExcecao executor) {
        return () -> {
            try {
                return executor.executar();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }

    @FunctionalInterface
    private interface ExecutorComExcecao {
        double executar() throws InterruptedException;
    }

    private record Resultado(double tempoMs, double valor) {
    }
}
