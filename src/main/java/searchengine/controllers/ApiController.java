package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Map<String, Object>> startIndexing() {
        if (indexingService.isIndexing()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("result", false, "error", "Индексация уже запущена"));
        }
        boolean success = indexingService.startIndexing();
        return success
                ? ResponseEntity.ok(Map.of("result", true))
                : ResponseEntity.status(500)
                .body(Map.of("result", false, "error", "Ошибка при запуске индексации"));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Map<String, Object>> stopIndexing() {
        if (!indexingService.isIndexing()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("result", false, "error", "Индексация не запущена"));
        }
        boolean success = indexingService.stopIndexing();
        return success
                ? ResponseEntity.ok(Map.of("result", true))
                : ResponseEntity.status(500)
                .body(Map.of("result", false, "error", "Ошибка при остановке индексации"));
    }
}