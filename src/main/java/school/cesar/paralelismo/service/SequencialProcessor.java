package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

/**
 * V1 - Processamento sequencial (baseline).
 * Referência para medir speedup e validar corretude das demais versões.
 */
@Service
public class SequencialProcessor {

    private final CalculoService calculoService;

    public SequencialProcessor(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    public double processar(double[][] matriz) {
        double resultado = 0.0;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                resultado += calculoService.calcular(matriz[i][j]);
            }
        }
        return resultado;
    }
}
