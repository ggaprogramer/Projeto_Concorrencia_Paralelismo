package school.cesar.paralelismo.service;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Gera matrizes aleatórias reproduzíveis (seed fixa).
 * Permite comparar resultados entre diferentes implementações.
 */
@Service
public class MatrizGenerator {

    private static final long SEED_FIXA = 42L;

    public double[][] gerar(int linhas, int colunas) {
        Random random = new Random(SEED_FIXA);
        double[][] matriz = new double[linhas][colunas];
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                matriz[i][j] = random.nextDouble() * 100.0;
            }
        }
        return matriz;
    }
}
