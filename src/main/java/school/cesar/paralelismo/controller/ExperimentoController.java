package school.cesar.paralelismo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.cesar.paralelismo.dto.LinhaExperimento;
import school.cesar.paralelismo.service.ExperimentoService;

import java.util.List;

/**
 * Executa bateria de experimentos de desempenho.
 * GET /api/experimento                 -> completa (500..2000)
 * GET /api/experimento?tamanho=500     -> apenas um tamanho (mais rápido)
 */
@RestController
@RequestMapping("/api/experimento")
public class ExperimentoController {

    private final ExperimentoService experimentoService;

    public ExperimentoController(ExperimentoService experimentoService) {
        this.experimentoService = experimentoService;
    }

    @GetMapping
    public List<LinhaExperimento> executar(@RequestParam(required = false) Integer tamanho) throws InterruptedException {
        if (tamanho != null) {
            return experimentoService.executarParaTamanho(tamanho);
        }
        return experimentoService.executarTudo();
    }
}
