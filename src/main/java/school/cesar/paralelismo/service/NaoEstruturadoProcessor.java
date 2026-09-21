package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * V2 - Paralelismo não estruturado com ExecutorService.
 * Demonstra o problema da concorrência não estruturada:
 * o ciclo de vida do pool deve ser gerenciado manualmente (shutdown() obrigatório).
 */
@Service
public class NaoEstruturadoProcessor {

    private final CalculoService calculoService;

    public NaoEstruturadoProcessor(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public double processar(double[][] matriz, int numeroDeTarefas) {
        List<int[]> faixas = Particionador.particionar(matriz.length, numeroDeTarefas);
        ExecutorService executor = Executors.newFixedThreadPool(faixas.size());

        try {
            List<Future<Double>> futures = new ArrayList<>(faixas.size());
            for (int[] faixa : faixas) {
                futures.add(executor.submit(() -> processarFaixa(matriz, faixa[0], faixa[1])));
            }

            double resultado = 0.0;
            for (Future<Double> future : futures) {
                resultado += future.get();
            }
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento interrompido", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Falha ao processar tarefa", e.getCause());
        } finally {
            // CRÍTICO: sempre chamar shutdown() para evitar vazamento de threads
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
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
