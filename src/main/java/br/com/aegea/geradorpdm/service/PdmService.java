package br.com.aegea.geradorpdm.service;

// ... (todos os outros imports continuam os mesmos)
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*; // Importa tudo de ss.usermodel
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



@Service
public class PdmService {

    private Map<String, List<String>> templatesCarregados = new ConcurrentHashMap<>();

    // ### MÉTODO DE PROCESSAMENTO COM MODO DETETIVE ATIVADO ###
    public void processarEAtualizarBaseExcel(InputStream inputStream) throws Exception {
        System.out.println("--- MODO DETETIVE: Iniciando processamento do arquivo Excel ---");
        Map<String, List<String>> templatesExtraidos = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int numeroLinha = 0;

            for (Row row : sheet) {
                numeroLinha++;
                if (numeroLinha == 1) { // Pula cabeçalho
                    System.out.println("Pulando cabeçalho...");
                    continue;
                }
                
                System.out.println("\n--- Lendo Linha Nº " + numeroLinha + " ---");
                
                Cell classeCell = row.getCell(0);      // Coluna A
                Cell descricaoCell = row.getCell(2);   // Coluna B

                if (classeCell == null || descricaoCell == null) {
                    System.out.println("AVISO: Célula da Classe ou Descrição é NULA. Pulando linha.");
                    continue;
                }

                // Usando um método mais seguro para ler os valores, independente do tipo da célula
                String classe = getCellValueAsString(classeCell).trim().toUpperCase();
                String descricao = getCellValueAsString(descricaoCell);

                System.out.println("  > LIDO DA COLUNA A (CLASSE): '" + classe + "'");
                System.out.println("  > LIDO DA COLUNA B (DESCRICAO): '" + descricao.substring(0, Math.min(descricao.length(), 200)) + "...'");

                if (!classe.isEmpty()) {
                    templatesExtraidos.computeIfAbsent(classe, k -> {
                        System.out.println("  > SUCESSO: Nova classe encontrada! Extraindo atributos para '" + k + "'");
                        return extrairAtributos(descricao);
                    });
                } else {
                    System.out.println("AVISO: Classe extraída está vazia. Pulando.");
                }
            }
        }
        
        System.out.println("\n--- PROCESSAMENTO FINALIZADO ---");
        System.out.println(templatesExtraidos.size() + " classes únicas extraídas do Excel.");
        
        File arquivoJson = ResourceUtils.getFile("classpath:parametros_por_tipo.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(arquivoJson, templatesExtraidos);
        System.out.println("Arquivo JSON foi atualizado com sucesso.");

        carregarJsonParaMemoria();
    }
    
    // Novo método auxiliar para ler qualquer tipo de célula como String
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
    
    //
    // --- O RESTANTE DA CLASSE NÃO MUDA ---
    //
    @PostConstruct public void inicializarTemplates() { 
    	System.out.println("Iniciando carregamento de templates do JSON..."); carregarJsonParaMemoria(); 
    	}
    public List<String> sugerirClasses(String query) { if(query == null || query.isBlank()) return List.of(); 
    String upperQuery = query.toUpperCase(); 
    return this.templatesCarregados.keySet().stream().filter(classe -> classe.contains(upperQuery)).sorted().limit(10).collect(Collectors.toList()); }
    public List<String> getAtributosPorClasse(String classe) { 
    	return this.templatesCarregados.get(classe.toUpperCase()); 
    	}
    public String gerarDescricaoLonga(String classe, Map<String, String> valores) {
        List<String> atributosOrdenados = this.templatesCarregados.get(classe.toUpperCase());
        if (atributosOrdenados == null) {
            return "ERRO: Classe não encontrada.";
        }

        // A MÁGICA ACONTECE AQUI:
        String valoresConcatenados = atributosOrdenados.stream()
                // 1. FILTRA: Mantém apenas os atributos cujo valor não está em branco
                .filter(attr -> {
                    String valor = valores.getOrDefault(attr, "");
                    return valor != null && !valor.isBlank();
                })
                // 2. MAPEIA: Monta a string "CHAVE: VALOR" apenas para os itens que passaram no filtro
                .map(attr -> attr + ": " + valores.get(attr).trim().toUpperCase())
                // 3. JUNTA: Concatena tudo com "; "
                .collect(Collectors.joining("; "));

        return classe.toUpperCase() + "; " + valoresConcatenados;
    }
    private void carregarJsonParaMemoria() { try { File arquivoJson = ResourceUtils.getFile("classpath:parametros_por_tipo.json"); if (!arquivoJson.exists() || arquivoJson.length() == 0) { System.out.println("AVISO: parametros_por_tipo.json está vazio ou não existe."); this.templatesCarregados.clear(); return; } ObjectMapper mapper = new ObjectMapper(); TypeReference<Map<String, List<String>>> typeReference = new TypeReference<>() {}; this.templatesCarregados = mapper.readValue(arquivoJson, typeReference); System.out.println("SUCESSO: " + this.templatesCarregados.size() + " templates carregados na memória."); } catch (Exception e) { System.err.println("AVISO: Não foi possível carregar o arquivo JSON. Erro: " + e.getMessage()); this.templatesCarregados.clear(); } }
    private List<String> extrairAtributos(String textoDescricao) { Set<String> atributos = new LinkedHashSet<>(); Pattern padrao = Pattern.compile("([A-Z0-9\\s/]+?):"); Matcher matcher = padrao.matcher(textoDescricao.toUpperCase().strip()); while (matcher.find()) { atributos.add(matcher.group(1).trim()); } return new ArrayList<>(atributos); }
}