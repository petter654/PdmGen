package br.com.aegea.geradorpdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    // ### MÉTODO DE PROCESSAMENTO DO EXCEL (LÓGICA CORRIGIDA) ###
    public void processarEAtualizarBaseExcel(InputStream inputStream) throws Exception {
        System.out.println("Iniciando processamento do arquivo Excel com a lógica correta...");
        Map<String, List<String>> templatesExtraidos = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) { rowIterator.next(); } // Pula cabeçalho

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // LÓGICA CORRETA:
                Cell classeCell = row.getCell(0);      // Coluna A para a CLASSE
                Cell descricaoCell = row.getCell(1);   // Coluna B para a DESCRIÇÃO

                if (classeCell != null && descricaoCell != null && 
                    classeCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING &&
                    descricaoCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {

                    String classe = classeCell.getStringCellValue().trim().toUpperCase();
                    String descricao = descricaoCell.getStringCellValue();

                    if (!classe.isEmpty()) {
                        // Usamos computeIfAbsent para garantir que pegamos apenas o primeiro exemplo de cada classe da Coluna A
                        templatesExtraidos.computeIfAbsent(classe, k -> extrairAtributos(descricao));
                    }
                }
            }
        }
        
        System.out.println(templatesExtraidos.size() + " classes únicas extraídas do Excel.");
        
        File arquivoJson = ResourceUtils.getFile("classpath:parametros_por_tipo.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(arquivoJson, templatesExtraidos);
        System.out.println("Arquivo JSON foi atualizado com sucesso.");

        carregarJsonParaMemoria();
    }
    
    //
    // --- O RESTANTE DA CLASSE NÃO PRECISA DE ALTERAÇÕES ---
    //
    @PostConstruct
    public void inicializarTemplates() {
        System.out.println("Iniciando carregamento de templates do JSON...");
        carregarJsonParaMemoria();
    }
    
    public List<String> sugerirClasses(String query) {
        if(query == null || query.isBlank()) return List.of();
        String upperQuery = query.toUpperCase();
        return this.templatesCarregados.keySet().stream()
                .filter(classe -> classe.contains(upperQuery))
                .sorted()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public List<String> getAtributosPorClasse(String classe) {
        return this.templatesCarregados.get(classe.toUpperCase());
    }
    
    public String gerarDescricaoLonga(String classe, Map<String, String> valores) {
        List<String> atributosOrdenados = this.templatesCarregados.get(classe.toUpperCase());
        if (atributosOrdenados == null) return "ERRO: Classe não encontrada.";
        String valoresConcatenados = atributosOrdenados.stream()
                .map(attr -> attr + ": " + valores.getOrDefault(attr, "").toUpperCase())
                .collect(Collectors.joining("; "));
        return classe.toUpperCase() + "; " + valoresConcatenados;
    }

    private void carregarJsonParaMemoria() {
        try {
            File arquivoJson = ResourceUtils.getFile("classpath:parametros_por_tipo.json");
            if (!arquivoJson.exists() || arquivoJson.length() == 0) {
                 System.out.println("AVISO: parametros_por_tipo.json está vazio ou não existe.");
                 this.templatesCarregados.clear();
                 return;
            }
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, List<String>>> typeReference = new TypeReference<>() {};
            this.templatesCarregados = mapper.readValue(arquivoJson, typeReference);
            System.out.println("SUCESSO: " + this.templatesCarregados.size() + " templates carregados na memória.");
        } catch (Exception e) {
            System.err.println("AVISO: Não foi possível carregar o arquivo JSON. Erro: " + e.getMessage());
            this.templatesCarregados.clear();
        }
    }
    
    private List<String> extrairAtributos(String textoDescricao) {
        Set<String> atributos = new LinkedHashSet<>();
        Pattern padrao = Pattern.compile("([A-Z0-9\\s/]+?):");
        Matcher matcher = padrao.matcher(textoDescricao.toUpperCase().strip());
        while (matcher.find()) {
            atributos.add(matcher.group(1).trim());
        }
        return new ArrayList<>(atributos);
    }
}