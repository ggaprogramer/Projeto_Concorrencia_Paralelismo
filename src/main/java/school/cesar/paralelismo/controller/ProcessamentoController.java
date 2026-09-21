package school.cesar.paralelismo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.cesar.paralelismo.dto.ResultadoProcessamento;
import school.cesar.paralelismo.service.CalculoService;
import school.cesar.paralelismo.service.EstruturadoAtomicProcessor;
import school.cesar.paralelismo.service.EstruturadoProcessor;
import school.cesar.paralelismo.service.EstruturadoQueueProcessor;
import school.cesar.paralelismo.service.MatrizGenerator;
import school.cesar.paralelismo.service.NaoEstruturadoProcessor;
import school.cesar.paralelismo.service.SequencialProcessor;

/**
 * Endpoints para executar cada uma das versões isoladamente.
 * GET /api/processamento/{versao}?linhas=500&colunas=500&tarefas=10
 *
 * Versões: sequencial, nao-estruturado, estruturado, estruturado-atomico, estruturado-fila
 */
@RestController
@RequestMapping("/api/processamento")
public class ProcessamentoController {

    private final MatrizGenerator matrizGenerator;
    private final SequencialProcessor sequencialProcessor;
    private final NaoEstruturadoProcessor naoEstruturadoProcessor;
    private final EstruturadoProcessor estruturadoProcessor;
    private final EstruturadoAtomicProcessor estruturadoAtomicProcessor;
    private final EstruturadoQueueProcessor estruturadoQueueProcessor;

    public ProcessamentoController(MatrizGenerator matrizGenerator,
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

    @GetMapping("/sequencial")
    public ResultadoProcessamento sequencial(@RequestParam(defaultValue = "500") int linhas,
                                              @RequestParam(defaultValue = "500") int colunas) throws InterruptedException {
        double[][] matriz = matrizGenerator.gerar(linhas, colunas);
        return executarComTempo("Sequencial", linhas, colunas, 1,
                () -> sequencialProcessor.processar(matriz));
    }

    @GetMapping("/nao-estruturado")
    public ResultadoProcessamento naoEstruturado(@RequestParam(defaultValue = "500") int linhas,
                                                  @RequestParam(defaultValue = "500") int colunas,
                                                  @RequestParam(defaultValue = "10") int tarefas) throws InterruptedException {
        double[][] matriz = matrizGenerator.gerar(linhas, colunas);
        return executarComTempo("Paralelismo não estruturado", linhas, colunas, tarefas,
                () -> naoEstruturadoProcessor.processar(matriz, tarefas));
    }

    @GetMapping("/estruturado")
    public ResultadoProcessamento estruturado(@RequestParam(defaultValue = "500") int linhas,
                                               @RequestParam(defaultValue = "500") int colunas,
                                               @RequestParam(defaultValue = "10") int tarefas) throws InterruptedException {
        double[][] matriz = matrizGenerator.gerar(linhas, colunas);
        return executarComTempo("Paralelismo estruturado", linhas, colunas, tarefas,
                () -> estruturadoProcessor.processar(matriz, tarefas));
    }

    @GetMapping("/estruturado-atomico")
    public ResultadoProcessamento estruturadoAtomico(@RequestParam(defaultValue = "500") int linhas,
                                                      @RequestParam(defaultValue = "500") int colunas,
                                                      @RequestParam(defaultValue = "10") int tarefas) throws InterruptedException {
        double[][] matriz = matrizGenerator.gerar(linhas, colunas);
        return executarComTempo("Estruturado + AtomicInteger (DoubleAdder)", linhas, colunas, tarefas,
                () -> estruturadoAtomicProcessor.processar(matriz, tarefas));
    }

    @GetMapping("/estruturado-fila")
    public ResultadoProcessamento estruturadoFila(@RequestParam(defaultValue = "500") int linhas,
                                                   @RequestParam(defaultValue = "500") int colunas,
                                                   @RequestParam(defaultValue = "10") int tarefas) throws InterruptedException {
        double[][] matriz = matrizGenerator.gerar(linhas, colunas);
        return executarComTempo("Estruturado + ConcurrentLinkedQueue", linhas, colunas, tarefas,
                () -> estruturadoQueueProcessor.processar(matriz, tarefas));
    }

    private ResultadoProcessamento executarComTempo(String nome, int linhas, int colunas, int tarefas,
                                                     Executor executor) throws InterruptedException {
        long inicio = System.nanoTime();
        double resultado = executor.executar();
        long tempoMs = (System.nanoTime() - inicio) / 1_000_000;
        return new ResultadoProcessamento(nome, linhas, colunas, tarefas, resultado, tempoMs);
    }

    @FunctionalInterface
    private interface Executor {
        double executar() throws InterruptedException;
    }
}
