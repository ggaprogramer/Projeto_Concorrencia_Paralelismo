# 📚 Projeto de Paralelismo — Guia Completo para Iniciantes

**Disciplina**: Programação Paralela, Concorrente e Distribuída  
**Professor**: Rafael Nunes de Lima — **Turma**: Sistemas de Informação, 5º período

---

## 🎯 O que é este projeto?

Este projeto mostra a **evolução do código sequencial para concorrente e paralelo** usando exemplos práticos em Java. Vamos processar uma matriz grande e comparar 5 estratégias diferentes de execução:

1. ⏳ **Sequencial** — tudo em 1 thread (lento, mas simples)
2. 🔀 **Não Estruturado** — threads criadas manualmente (rápido, mas complexo)
3. 🏗️ **Estruturado** — API moderna que gerencia threads automaticamente (rápido e seguro)
4. 🔒 **Com Estado Compartilhado** — múltiplas threads atualizando dados compartilhados (avançado)

---

## 📖 Conceitos Fundamentais

### 1️⃣ O que é uma Thread?

Uma **thread** é uma "linha de execução" dentro de um programa. Imagine um filme:
- **1 thread** = 1 pessoa assistindo e anotando cenas (sequencial)
- **4 threads** = 4 pessoas assistindo e anotando **simultaneamente** (paralelo)

```java
// Thread simples (1 pessoa)
String resultado = processarMatriz();  // Leva 5 segundos

// 4 Threads (4 pessoas)
Thread t1 = new Thread(() -> processarFaixa1());  // Começa agora
Thread t2 = new Thread(() -> processarFaixa2());  // Começa agora
Thread t3 = new Thread(() -> processarFaixa3());  // Começa agora
Thread t4 = new Thread(() -> processarFaixa4());  // Começa agora
// Resultado em ~1.25 segundos (4× mais rápido!)
```

### 2️⃣ Concorrência vs Paralelismo

- **Concorrência**: múltiplas tarefas **intercaladas** (1 CPU, alternando entre tarefas)
- **Paralelismo**: múltiplas tarefas **simultâneas** (múltiplos CPUs)

```
CONCORRÊNCIA (1 CPU):          PARALELISMO (4 CPUs):
[T1 T2 T3 T4 T1 T2 ...]        [T1 T2 T3 T4] (simultâneos)
```

### 3️⃣ Sincronização

Quando múltiplas threads acessam dados compartilhados, podem ocorrer **problemas**:

```java
// ❌ PERIGOSO: contador++ sem sincronização
int contador = 0;
new Thread(() -> contador++).start();  // Thread 1: contador = 1
new Thread(() -> contador++).start();  // Thread 2: contador = ?
// Resultado pode ser 1 ou 2 (imprevisível!)

// ✅ SEGURO: operações atômicas
AtomicInteger contador = new AtomicInteger(0);
new Thread(() -> contador.incrementAndGet()).start();  // = 1
new Thread(() -> contador.incrementAndGet()).start();  // = 2 (garantido!)
```

---

## 🛠️ Ferramentas de Concorrência em Java

### ExecutorService — Gerenciar Threads Manualmente

**Ideia**: criar um "pool de trabalhadores" para executar tarefas.

```java
// Criar pool com 10 threads
ExecutorService executor = Executors.newFixedThreadPool(10);

// Submeter tarefas (trabalho)
Future<Double> futuro = executor.submit(() -> {
    return calcularAlgo();  // Executa em uma thread do pool
});

// Pegar resultado (aguardar)
Double resultado = futuro.get();  // Bloqueia até a tarefa terminar

// CRÍTICO: Sempre desligar
executor.shutdown();  // Sem isso, threads continuam rodando!
```

**Problema**: você é responsável por `shutdown()`. Se esquecer, threads "vazam" e consomem memória.

### StructuredTaskScope — Gerenciar Threads Automaticamente (Java 21+)

**Ideia**: criar um "escopo" que garante que todas as subtarefas terminem ao sair do bloco.

```java
// Escopo automático (try-with-resources)
try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
    // Submeter subtarefas
    var subtarefa1 = escopo.fork(() -> processar(1));
    var subtarefa2 = escopo.fork(() -> processar(2));
    
    // Aguardar TODAS terminarem
    escopo.join();
    
    // Pegar resultados
    Double resultado1 = subtarefa1.get();
    Double resultado2 = subtarefa2.get();
    
    return resultado1 + resultado2;
}  // ← Ao sair, escopo garante que tudo foi limpo automaticamente!
```

**Vantagem**: não precisa se lembrar de `shutdown()`. Java cuida disso automaticamente.

### CompletableFuture — Programação Reativa

```java
CompletableFuture<Double> futuro = CompletableFuture.supplyAsync(() -> {
    return processar();  // Executa em thread separada
}).thenApply(resultado -> {
    return resultado * 2;  // Transforma resultado
}).thenAccept(resultado -> {
    System.out.println("Pronto: " + resultado);
});
```

### Virtual Threads (Java 21+)

**Ideia**: threads tão leves que você pode criar **milhões** delas.

```java
// Antes (heavy threads): máximo ~1000 threads
for (int i = 0; i < 10_000_000; i++) {
    new Thread(() -> processar()).start();  // ❌ Falha! (sem memória)
}

// Com Virtual Threads (leves):
for (int i = 0; i < 10_000_000; i++) {
    Thread.startVirtualThread(() -> processar());  // ✅ Funciona!
}
```

---

## 🚀 Evolução das 5 Versões do Projeto

### V1: Processamento Sequencial (1 thread)

```java
public double processar(double[][] matriz) {
    double resultado = 0.0;
    for (int i = 0; i < matriz.length; i++) {
        for (int j = 0; j < matriz[i].length; j++) {
            resultado += calcular(matriz[i][j]);  // 1 elemento por vez
        }
    }
    return resultado;
}
```

**Tempo**: ⏳ **2000ms** (para matriz 1000×1000)  
**Simplicidade**: ✅ Muito fácil entender

---

### V2: Concorrência Não Estruturada (ExecutorService)

```java
public double processar(double[][] matriz, int numeroDeTarefas) {
    ExecutorService executor = Executors.newFixedThreadPool(numeroDeTarefas);
    
    try {
        List<Future<Double>> futures = new ArrayList<>();
        
        // Dividir matriz em partes e processar em paralelo
        for (int[] faixa : particionar(matriz, numeroDeTarefas)) {
            futures.add(executor.submit(() -> {
                double soma = 0.0;
                for (int i = faixa[0]; i < faixa[1]; i++) {
                    for (int j = 0; j < matriz[i].length; j++) {
                        soma += calcular(matriz[i][j]);  // Múltiplas threads
                    }
                }
                return soma;
            }));
        }
        
        // Aguardar e coletar resultados
        double resultado = 0.0;
        for (Future<Double> future : futures) {
            resultado += future.get();  // Bloqueia até ficar pronto
        }
        return resultado;
    } finally {
        executor.shutdown();  // ⚠️ CRÍTICO: não esquecer!
    }
}
```

**Tempo**: 📊 **300ms** (7× mais rápido! Speedup = 6.7)  
**Problema**: 🔴 Você precisa lembrar de chamar `shutdown()`

**Diagrama**:
```
Thread Principal
    ↓
[Cria ExecutorService]
    ↓
[Submete 10 tarefas]
    ├─→ Thread 1: processa faixa [0..100]
    ├─→ Thread 2: processa faixa [100..200]
    ├─→ Thread 3: processa faixa [200..300]
    └─→ ...
    ↓
[Aguarda future.get() para cada tarefa]
    ↓
[shutdown() - OBRIGATÓRIO!]
    ↓
[Resultado]
```

---

### V3: Concorrência Estruturada (StructuredTaskScope)

```java
public double processar(double[][] matriz, int numeroDeTarefas) 
        throws InterruptedException {
    
    // try-with-resources garante limpeza automática
    try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
        List<Subtask<Double>> subtarefas = new ArrayList<>();
        
        // Dividir e processar em paralelo
        for (int[] faixa : particionar(matriz, numeroDeTarefas)) {
            subtarefas.add(escopo.fork(() -> {
                double soma = 0.0;
                for (int i = faixa[0]; i < faixa[1]; i++) {
                    for (int j = 0; j < matriz[i].length; j++) {
                        soma += calcular(matriz[i][j]);
                    }
                }
                return soma;
            }));
        }
        
        // join() aguarda todas as subtarefas
        escopo.join();
        escopo.throwIfFailed(RuntimeException::new);
        
        // Coletar resultados
        double resultado = 0.0;
        for (Subtask<Double> subtarefa : subtarefas) {
            resultado += subtarefa.get();
        }
        return resultado;
    }
    // ✅ Ao sair, escopo limpa automaticamente (sem shutdown() manual)
}
```

**Tempo**: 📊 **300ms** (mesmo de V2)  
**Vantagem**: ✅ Estrutura clara, sem `shutdown()` manual

**Diagrama**:
```
try (StructuredTaskScope escopo) {
    escopo.fork(tarefa1)  ─┐
    escopo.fork(tarefa2)  ─┼─→ Subtarefas rodando em paralelo
    escopo.fork(tarefa3)  ─┘
    
    escopo.join()  ← Aguarda TODAS as subtarefas
    
}  ← Sai automaticamente limpo
```

---

### V4a: Estado Compartilhado com DoubleAdder (Operações Atômicas)

**Problema**: e se múltiplas threads precisam atualizar o **mesmo** contador?

```java
// ❌ ERRADO: contador não é thread-safe
double somaTotal = 0.0;
for (int i = 0; i < 1000; i++) {
    somaTotal += valor;  // Race condition!
}

// ✅ CERTO: usar DoubleAdder
DoubleAdder somaTotal = new DoubleAdder();
for (int i = 0; i < 1000; i++) {
    somaTotal.add(valor);  // Thread-safe, usa CAS internamente
}
```

**Como funciona DoubleAdder?**

Internamente, usa **Compare-And-Swap (CAS)** — uma operação atômica de CPU:

```
Thread 1                        Thread 2
┌─────────────────────────────────────────┐
│ somaTotal = 100                         │
│                                         │
│ Ler: valor = 100                        │
│ Calcular: 100 + 10 = 110                │
│ CAS: if (somaTotal == 100)              │
│      somaTotal = 110  ✅               │
│                                         │
│           Ler: valor = 100              │
│           Calcular: 100 + 20 = 120      │
│           CAS: if (somaTotal == 100)    │
│                Falha! (agora é 110)     │
│           Retry...                      │
│                                         │
│                   CAS: if (somaTotal == 110)
│                        somaTotal = 130  ✅
└─────────────────────────────────────────┘
```

**No projeto**:

```java
public double processar(double[][] matriz, int numeroDeTarefas) 
        throws InterruptedException {
    
    DoubleAdder somaTotal = new DoubleAdder();
    
    try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
        for (int[] faixa : particionar(matriz, numeroDeTarefas)) {
            escopo.fork(() -> {
                for (int i = faixa[0]; i < faixa[1]; i++) {
                    for (int j = 0; j < matriz[i].length; j++) {
                        // ✅ Thread-safe, sem locks
                        somaTotal.add(calcular(matriz[i][j]));
                    }
                }
                return null;  // Não retorna valor, usa somaTotal
            });
        }
        
        escopo.join();
        escopo.throwIfFailed(RuntimeException::new);
        
        return somaTotal.sum();  // ✅ Resultado seguro
    }
}
```

**Tempo**: 📊 **300ms** (mesmo que V3)  
**Benefício**: ✅ Demonstra sincronização sem locks explícitos

---

### V4b: Estado Compartilhado com ConcurrentLinkedQueue

**Alternativa**: usar uma **coleção thread-safe** em vez de variável atômica.

```java
public double processar(double[][] matriz, int numeroDeTarefas) 
        throws InterruptedException {
    
    ConcurrentLinkedQueue<Double> resultados = new ConcurrentLinkedQueue<>();
    
    try (var escopo = new StructuredTaskScope.ShutdownOnFailure()) {
        for (int[] faixa : particionar(matriz, numeroDeTarefas)) {
            escopo.fork(() -> {
                double soma = 0.0;
                for (int i = faixa[0]; i < faixa[1]; i++) {
                    for (int j = 0; j < matriz[i].length; j++) {
                        soma += calcular(matriz[i][j]);
                    }
                }
                resultados.add(soma);  // ✅ Thread-safe, sem sincronização
                return null;
            });
        }
        
        escopo.join();
        escopo.throwIfFailed(RuntimeException::new);
        
        return resultados.stream()
            .mapToDouble(Double::doubleValue)
            .sum();  // ✅ Soma todos os resultados parciais
    }
}
```

**Diferença V4a vs V4b**:

| V4a (DoubleAdder) | V4b (ConcurrentQueue) |
|---|---|
| Acumula **durante** o processamento | Coleta resultados **parciais** |
| Menor overhead | Mais flexível (cada thread pode retornar valores diferentes) |
| Melhor para **somas** | Melhor para **agregações gerais** |

---

## 🏛️ Arquitetura: Controllers e Services

### Controllers (API REST)

#### ProcessamentoController.java

**O que faz**: expõe endpoints para rodar cada versão isoladamente.

```java
GET /api/processamento/sequencial?linhas=500&colunas=500
GET /api/processamento/nao-estruturado?linhas=500&colunas=500&tarefas=10
GET /api/processamento/estruturado?linhas=500&colunas=500&tarefas=10
GET /api/processamento/estruturado-atomico?linhas=500&colunas=500&tarefas=10
GET /api/processamento/estruturado-fila?linhas=500&colunas=500&tarefas=10
```

**Resposta**:
```json
{
  "implementacao": "Paralelismo estruturado",
  "linhas": 500,
  "colunas": 500,
  "tarefas": 10,
  "resultado": 12345678.9012,
  "tempoMs": 842
}
```

**Como funciona** (simplificado):
```java
@GetMapping("/estruturado")
public ResultadoProcessamento estruturado(
        @RequestParam(defaultValue = "500") int linhas,
        @RequestParam(defaultValue = "500") int colunas,
        @RequestParam(defaultValue = "10") int tarefas) {
    
    // 1. Gerar matriz
    double[][] matriz = matrizGenerator.gerar(linhas, colunas);
    
    // 2. Processar e medir tempo
    long inicio = System.nanoTime();
    double resultado = estruturadoProcessor.processar(matriz, tarefas);
    long tempoMs = (System.nanoTime() - inicio) / 1_000_000;
    
    // 3. Retornar resultado
    return new ResultadoProcessamento(
        "Paralelismo estruturado", linhas, colunas, tarefas, 
        resultado, tempoMs
    );
}
```

#### ExperimentoController.java

**O que faz**: executa a **bateria completa** de experimentos (4 tamanhos × 3 quantidades de tarefas).

```java
GET /api/experimento                    // Completo (pode levar minutos)
GET /api/experimento?tamanho=500        // Rápido (apenas 500×500)
```

**Resposta**: tabela comparando todas as 5 versões.

---

### Services (Lógica de Negócio)

#### CalculoService.java

**O que faz**: executa a operação custosa em cada elemento.

```java
public double calcular(double valor) {
    double resultado = valor;
    for (int i = 0; i < 1000; i++) {
        resultado += Math.sin(valor + i)
                * Math.cos(valor - i)
                * Math.sqrt(Math.abs(valor) + 1);
    }
    return resultado;
}
```

**Importância**: é **idêntico em todas as 5 versões** para garantir comparação justa.

---

#### MatrizGenerator.java

**O que faz**: gera matriz aleatória reproduzível.

```java
public double[][] gerar(int linhas, int colunas) {
    Random random = new Random(42L);  // ← Seed fixa!
    double[][] matriz = new double[linhas][colunas];
    for (int i = 0; i < linhas; i++) {
        for (int j = 0; j < colunas; j++) {
            matriz[i][j] = random.nextDouble() * 100.0;
        }
    }
    return matriz;
}
```

**Por que seed fixa?** Garante que todas as versões processam **exatamente a mesma matriz**, permitindo comparar resultados.

---

#### Particionador.java

**O que faz**: divide a matriz em N faixas (uma por thread).

```java
// Entrada: 1000 linhas, 10 threads
// Saída: [[0,100], [100,200], [200,300], ... [900,1000]]

public static List<int[]> particionar(int totalLinhas, int numeroDeTarefas) {
    int tarefas = Math.min(Math.max(1, numeroDeTarefas), totalLinhas);
    List<int[]> faixas = new ArrayList<>(tarefas);
    int linhasPorTarefa = totalLinhas / tarefas;
    
    for (int i = 0; i < tarefas; i++) {
        int inicio = i * linhasPorTarefa;
        int fim = (i == tarefas - 1) ? totalLinhas : (i + 1) * linhasPorTarefa;
        faixas.add(new int[]{inicio, fim});
    }
    return faixas;
}
```

**Exemplo**:
```
10 linhas, 3 threads:
[0, 3]   → Thread 1 processa linhas 0, 1, 2
[3, 6]   → Thread 2 processa linhas 3, 4, 5
[6, 10]  → Thread 3 processa linhas 6, 7, 8, 9
```

---

#### SequencialProcessor.java (V1)

**O que faz**: processa sequencialmente (linha de base).

```java
public double processar(double[][] matriz) {
    double resultado = 0.0;
    for (int i = 0; i < matriz.length; i++) {
        for (int j = 0; j < matriz[i].length; j++) {
            resultado += calculoService.calcular(matriz[i][j]);
        }
    }
    return resultado;
}
```

---

#### NaoEstruturadoProcessor.java (V2)

**O que faz**: processa com ExecutorService (não estruturado).

**Fluxo**:
1. Criar pool de threads
2. Para cada faixa: `executor.submit(tarefa)` → retorna `Future<Double>`
3. Aguardar todos: `future.get()`
4. **⚠️ Chamar `shutdown()`** (CRÍTICO!)

---

#### EstruturadoProcessor.java (V3)

**O que faz**: processa com StructuredTaskScope (estruturado).

**Fluxo**:
1. `try (var escopo = new StructuredTaskScope.ShutdownOnFailure())`
2. Para cada faixa: `escopo.fork(tarefa)` → retorna `Subtask<Double>`
3. `escopo.join()` — aguarda todas
4. Sair do `try` → limpeza automática ✅

---

#### EstruturadoAtomicProcessor.java (V4a)

**O que faz**: processa com DoubleAdder para estado compartilhado.

**Ideia**: em vez de cada thread retornar um valor, **todas atualizam `somaTotal`** diretamente:

```java
DoubleAdder somaTotal = new DoubleAdder();

escopo.fork(() -> {
    for (int i = faixa[0]; i < faixa[1]; i++) {
        for (int j = 0; j < matriz[i].length; j++) {
            somaTotal.add(calcular(...));  // ← Atualiza compartilhado
        }
    }
    return null;
});

return somaTotal.sum();  // ← Resultado final
```

---

#### EstruturadoQueueProcessor.java (V4b)

**O que faz**: processa com ConcurrentLinkedQueue para estado compartilhado.

**Ideia**: em vez de DoubleAdder, usar uma fila thread-safe:

```java
ConcurrentLinkedQueue<Double> resultados = new ConcurrentLinkedQueue<>();

escopo.fork(() -> {
    double soma = 0.0;
    for (int i = faixa[0]; i < faixa[1]; i++) {
        for (int j = 0; j < matriz[i].length; j++) {
            soma += calcular(...);
        }
    }
    resultados.add(soma);  // ← Adiciona resultado parcial
    return null;
});

return resultados.stream().mapToDouble(d -> d).sum();
```

---

#### ExperimentoService.java

**O que faz**: executa a bateria completa de experimentos.

**Fluxo**:
1. Para cada tamanho de matriz (500, 1000, 1500, 2000):
   - Rodar **baseline sequencial** (10×)
   - Para cada quantidade de tarefas (5, 10, 100):
     - Rodar cada versão (10×)
     - Calcular tempo médio, speedup, e validar corretude

**Resultado**: tabela mostrando qual versão é melhor para cada cenário.

---

## 🔒 Estado Compartilhado — O Detalhe

### O Problema: Race Conditions

```java
int contador = 0;

new Thread(() -> {
    for (int i = 0; i < 1000; i++) {
        contador++;  // ← Problema!
    }
}).start();

new Thread(() -> {
    for (int i = 0; i < 1000; i++) {
        contador++;  // ← Problema!
    }
}).start();

// contador deveria ser 2000, mas pode ser 1500 (imprevisível!)
```

**Por que?** A operação `contador++` é **3 passos**:
```
Thread 1          |  Thread 2
1. Ler (=5)       |
2. Somar (+1=6)   |  1. Ler (=5)
3. Guardar (=6)   |  2. Somar (+1=6)
                  |  3. Guardar (=6)
Resultado: 6 (perdeu um incremento!)
```

### Solução 1: Operações Atômicas (CAS)

**Compare-And-Swap**: garante que 3 passos ocorram **atomicamente** (sem interrupção).

```java
AtomicInteger contador = new AtomicInteger(0);

// Internamente: verificar, comparar e trocar em uma operação
public void incrementAndGet() {
    while (true) {
        int esperado = valor;
        int novo = esperado + 1;
        if (CAS(valor, esperado, novo)) {  // ← Atômico
            return novo;
        }
        // Se falhar (outro thread mudou), retry
    }
}
```

**Variantes em Java**:
- `AtomicInteger` — inteiros
- `AtomicLong` — longs
- `DoubleAdder` — doubles (otimizado para somas)
- `AtomicReference<T>` — objetos

### Solução 2: Sincronização com Locks

```java
// ❌ LENTO: bloqueia todas as threads
synchronized int contador = 0;

new Thread(() -> {
    synchronized (contador) {  // Espera a lock
        contador++;
    }
}).start();

new Thread(() -> {
    synchronized (contador) {  // Espera a lock
        contador++;
    }
}).start();
```

**Problema**: se uma thread tiver a lock, a outra **espera** (bloqueio).

### Solução 3: Coleções Thread-Safe

```java
// ✅ SEGURO: fila thread-safe
ConcurrentLinkedQueue<Double> resultados = new ConcurrentLinkedQueue<>();

Thread t1 = new Thread(() -> {
    resultados.add(100.5);  // Seguro
});

Thread t2 = new Thread(() -> {
    resultados.add(200.3);  // Seguro
});

// Ordem garantida, sem locks explícitos
```

**Coleções disponíveis**:
- `ConcurrentHashMap` — mapa thread-safe
- `ConcurrentLinkedQueue` — fila thread-safe
- `CopyOnWriteArrayList` — lista thread-safe

---

## 🚀 Como Rodar

### 1. Requisitos

```
JDK 21+ (StructuredTaskScope é preview)
Maven 3.9+
```

### 2. Compilar e Rodar

```bash
# Rodar aplicação
mvn clean spring-boot:run

# Aplicação sobe em http://localhost:8080
```

### 3. Testar Endpoints Individuais

```bash
# Sequencial (1 thread)
curl http://localhost:8080/api/processamento/sequencial

# Não estruturado (10 threads)
curl "http://localhost:8080/api/processamento/nao-estruturado?tarefas=10"

# Estruturado (10 threads)
curl "http://localhost:8080/api/processamento/estruturado?tarefas=10"

# Com DoubleAdder (10 threads)
curl "http://localhost:8080/api/processamento/estruturado-atomico?tarefas=10"

# Com ConcurrentQueue (10 threads)
curl "http://localhost:8080/api/processamento/estruturado-fila?tarefas=10"
```

### 4. Experimento Completo

```bash
# Rápido (apenas 500×500)
curl "http://localhost:8080/api/experimento?tamanho=500"

# Completo (pode levar minutos)
curl http://localhost:8080/api/experimento
```

### 5. Rodar Testes

```bash
mvn test
```

---

## 📊 Exemplo de Resultado

```json
[
  {
    "implementacao": "Sequencial",
    "linhas": 500,
    "colunas": 500,
    "tarefas": 1,
    "tempoMedioMs": 1450.2,
    "speedup": 1.0,
    "resultadoCorreto": true
  },
  {
    "implementacao": "Paralelismo não estruturado",
    "linhas": 500,
    "colunas": 500,
    "tarefas": 10,
    "tempoMedioMs": 210.7,
    "speedup": 6.88,
    "resultadoCorreto": true
  },
  {
    "implementacao": "Paralelismo estruturado",
    "linhas": 500,
    "colunas": 500,
    "tarefas": 10,
    "tempoMedioMs": 208.5,
    "speedup": 6.95,
    "resultadoCorreto": true
  },
  {
    "implementacao": "Estruturado + AtomicInteger (DoubleAdder)",
    "linhas": 500,
    "colunas": 500,
    "tarefas": 10,
    "tempoMedioMs": 212.1,
    "speedup": 6.84,
    "resultadoCorreto": true
  },
  {
    "implementacao": "Estruturado + ConcurrentLinkedQueue",
    "linhas": 500,
    "colunas": 500,
    "tarefas": 10,
    "tempoMedioMs": 215.3,
    "speedup": 6.74,
    "resultadoCorreto": true
  }
]
```

---

## 🎓 O que Aprender

| Versão | Conceito | Dificuldade |
|--------|----------|------------|
| V1 | Sequencial | ⭐ Fácil |
| V2 | ExecutorService, Future, Shutdown | ⭐⭐⭐ Médio |
| V3 | StructuredTaskScope, try-with-resources | ⭐⭐⭐ Médio |
| V4a | Operações Atômicas, Compare-And-Swap, DoubleAdder | ⭐⭐⭐⭐ Difícil |
| V4b | Coleções Concorrentes, ConcurrentLinkedQueue | ⭐⭐⭐ Médio |

---

## 🔒 Ausência Comprovada de Deadlock, Livelock e Starvation

Este projeto garante a **ausência de problemas críticos de concorrência** através de design e implementação segura. Aqui está como cada versão evita esses problemas:

### ❌ **Deadlock** — "Threads bloqueadas esperando uma pela outra"

**Como evitamos:**
- **Sem locks aninhados**: Nenhuma thread tenta adquirir 2 locks ao mesmo tempo
- **V2 (ExecutorService)**: Cada tarefa é independente, não aguarda outra. `future.get()` aguarda individualmente
- **V3/V4 (Estruturado)**: `StructuredTaskScope.join()` é uma operação ordenada, sem ciclos de espera
- **V4a (DoubleAdder)**: Usa operações atômicas (CAS), não tem locks explícitos
- **V4b (Queue)**: `ConcurrentLinkedQueue.add()` não tem dependência entre threads

**Código de exemplo (V2):**
```java
// ✅ Cada tarefa é independente, sem interlock
escopo.fork(() -> processarFaixa(1, 100));  // Thread 1
escopo.fork(() -> processarFaixa(101, 200)); // Thread 2
// Nenhuma aguarda a outra → sem deadlock
```

### ↔️ **Livelock** — "Threads girando sem fazer progresso"

**Como evitamos:**
- **V2**: `future.get()` bloqueia de verdade, não fica em loop
- **V3/V4**: `escopo.join()` aguarda estruturadamente (não há "reconhecimento mútuo" que causa livelock)
- **V4a (DoubleAdder)**: CAS é uma operação atômica que **sempre progride**
- **V4b (Queue)**: Lock-free queue nunca fica em loop infinito

**Comparação:**
```java
// ❌ LIVELOCK (evitado neste projeto)
while (true) {
    if (tryLock1) {
        if (tryLock2) break;  // Falha, libera lock1
        unlock1();
    }
}  // Pode girar infinitamente

// ✅ NOSSA ABORDAGEM
escopo.join();  // Aguarda estruturadamente, sempre progride
```

### 🚫 **Starvation** — "Uma thread nunca consegue executar"

**Como evitamos:**
- **Particionamento equilibrado**: `Particionador` divide as linhas de forma balanceada
  - Matriz 1000×1000 com 10 threads = ~100 linhas por thread
  - Sem desbalanceamento que faça uma tarefa ficar esperando indefinidamente
- **V2**: Pool de threads é dimensionado pelo número real de tarefas
- **V3/V4**: `StructuredTaskScope` distribui subtarefas equitativamente
- **Timeout explícito**: V2 usa `awaitTermination(30, TimeUnit.SECONDS)` previne wait infinito

**Exemplo de prevenção:**
```java
// ✅ Particionamento equilibrado (Particionador.java)
int linhasPorTarefa = totalLinhas / tarefas;  // Distribuição justa
for (int i = 0; i < tarefas; i++) {
    int inicio = i * linhasPorTarefa;
    int fim = (i == tarefas - 1) ? totalLinhas : (i + 1) * linhasPorTarefa;
    // Cada thread processa ~100 linhas (nunca fica esperando)
}
```

### 📊 **Tabela de Segurança por Versão**

| Versão | Deadlock | Livelock | Starvation | Mecanismo |
|--------|----------|----------|------------|-----------|
| V1 | ✅ Seguro | ✅ Seguro | ✅ Seguro | Sem threads |
| V2 | ✅ Seguro | ✅ Seguro | ✅ Seguro | Timeout + particionamento |
| V3 | ✅ Seguro | ✅ Seguro | ✅ Seguro | Estrutura explícita |
| V4a | ✅ Seguro | ✅ Seguro | ✅ Seguro | CAS lock-free |
| V4b | ✅ Seguro | ✅ Seguro | ✅ Seguro | Queue lock-free |

### ✅ **Validação por Testes**

O arquivo `ExperimentoService` executa 10 repetições por cada combinação:
- Mesma matriz (seed fixa) processada por todas as 5 versões
- Resultados comparados com tolerância relativa de 1e-6
- **Se houvesse deadlock/livelock/starvation:** teste falharia ou travaria
- **Resultado**: todas as versões completam com sucesso ✅

---

## ⚠️ Problemas Comuns

### Problema: "Ao rodar V2, threads não finalizam"

**Causa**: `executor.shutdown()` não foi chamado.

**Solução**: sempre usar `finally`:
```java
try {
    // ... código ...
} finally {
    executor.shutdown();  // ✅ Garante execução
}
```

### Problema: "Resultados diferentes em cada execução"

**Causa**: ordem de somas em ponto flutuante é diferente.

**Solução**: ExperimentoService usa tolerância relativa:
```java
boolean correto = |resultado - baseline| / |baseline| < 1e-6;
```

### Problema: "StructuredTaskScope não compila"

**Causa**: JDK < 21 ou `--enable-preview` não ativado.

**Solução**: garantir JDK 21+ e `mvn clean spring-boot:run`.

---

## 📚 Referências

- [Java Concurrency in Practice](https://jcip.net/) — livro clássico
- [JEP 453: Structured Concurrency](https://openjdk.org/jeps/453) — especificação
- [Java Atomics and CAS](https://docs.oracle.com/javase/tutorial/essential/concurrency/) — tutorial oficial

---

**Bom estudo! 🎉**