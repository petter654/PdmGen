package br.com.aegea.geradorpdm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import br.com.aegea.geradorpdm.service.PdmService;

import java.util.List;
import java.util.Map;

@RestController
public class ControllerPdm {

    @Autowired
    private PdmService pdmService;

    @GetMapping("/sugerirClasses")
    public List<String> sugerirClasses(@RequestParam("query") String query) {
        return pdmService.sugerirClasses(query);
    }

    @GetMapping("/getAtributos")
    public List<String> getAtributos(@RequestParam("classe") String classe) {
        return pdmService.getAtributosPorClasse(classe);
    }

    // ### MÉTODO CORRIGIDO AQUI ###
    @SuppressWarnings("unchecked")
    @PostMapping("/gerarDescricao")
    public Map<String, String> gerarDescricao(@RequestBody Map<String, Object> payload) {
        String classe = (String) payload.get("classe");
        // A linha abaixo é a que causa o aviso, que agora será ignorado
        Map<String, String> valores = (Map<String, String>) payload.get("valores");
        String descricaoFinal = pdmService.gerarDescricaoLonga(classe, valores);
        return Map.of("descricao", descricaoFinal);
    }

    @PostMapping("/admin/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo para enviar.");
        }
        try {
            pdmService.processarEAtualizarBaseExcel(arquivo.getInputStream());
            return ResponseEntity.ok("Arquivo processado e base de templates atualizada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Falha ao processar o arquivo: " + e.getMessage());
        }
    }
}