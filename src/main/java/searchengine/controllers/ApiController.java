package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchResults;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorMessage, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of("result", false, "error", errorMessage));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Map<String, Object>> startIndexing() {
        if (indexingService.isIndexing()) {
            return createErrorResponse("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }
        boolean success = indexingService.startIndexing();
        return success ? ResponseEntity.ok(Map.of("result", true)) : createErrorResponse("Ошибка при запуске индексации", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Map<String, Object>> stopIndexing() {
        if (!indexingService.isIndexing()) {
            return createErrorResponse("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }
        boolean success = indexingService.stopIndexing();
        return success ? ResponseEntity.ok(Map.of("result", true)) : createErrorResponse("Ошибка при остановке индексации", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Map<String, Object>> indexPage(@RequestParam String url) {
        if (!indexingService.isValidUrl(url)) {
            return createErrorResponse("Данная страница находится за пределами сайтов, указанных в конфигурационном файле", HttpStatus.BAD_REQUEST);
        }
        boolean success = indexingService.indexPage(url);
        return success ? ResponseEntity.ok(Map.of("result", true)) : createErrorResponse("Ошибка при индексации страницы", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        if (query == null || query.trim().isEmpty()) {
            return createErrorResponse("Задан пустой поисковый запрос", HttpStatus.BAD_REQUEST);
        }
        try {
            SearchResults results = searchService.search(query, site, offset, limit);
            if (results == null) {
                return createErrorResponse("Ошибка при выполнении поиска", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(Map.of("result", true, "count", results.getTotalCount(), "data", results.getData()));
        } catch (Exception e) {
            return createErrorResponse("Внутренняя ошибка сервера при поиске", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}