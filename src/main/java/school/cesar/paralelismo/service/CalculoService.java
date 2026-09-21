package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

/**
 * Operação computacionalmente custosa. Mantida idêntica em todas as
 * versões para garantir comparação justa de desempenho.
 */
@Service
public class CalculoService {

    private static final int ITERACOES = 1000;

    public double calcular(double valor) {
        double resultado = valor;
        for (int i = 0; i < ITERACOES; i++) {
            resultado += Math.sin(valor + i)
                    * Math.cos(valor - i)
                    * Math.sqrt(Math.abs(valor) + 1);
        }
        return resultado;
    }
}
