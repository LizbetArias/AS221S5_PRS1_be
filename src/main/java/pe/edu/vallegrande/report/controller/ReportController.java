package pe.edu.vallegrande.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.edu.vallegrande.report.model.Report;
import pe.edu.vallegrande.report.model.ReportRequest;
import pe.edu.vallegrande.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Listar todos los reportes con sus detalles, con un filtro opcional por estado (A o I)
    @GetMapping
    public Flux<ReportRequest> getAllReports(@RequestParam(value = "active", required = false) String active) {
        // Validamos si el parámetro 'active' es válido
        if (active != null && !active.equals("A") && !active.equals("I")) {
            return Flux.error(new IllegalArgumentException("Invalid 'active' value. Expected 'A' or 'I'."));
        }

        // Si no se pasa el parámetro 'active', por defecto filtramos solo los activos ("A")
        if (active == null) {
            active = "A"; // Valor por defecto si no se especifica
        }

        // Llamamos al servicio para obtener los reportes filtrados
        return reportService.getAllReports(active);
    }

    // Listar un reporte por ID con sus detalles
    @GetMapping("/{id}")
    public Mono<ReportRequest> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id);
    }

    // Insertar Report con detalles
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Report> createReport(@RequestBody ReportRequest request) {
        return reportService.createReport(request.getReport(), Flux.fromIterable(request.getReportDetails()));
    }

    // Actualizar Report y sus detalles
    @PutMapping("/{id}")
    public Mono<Report> updateReport(@PathVariable Long id, @RequestBody ReportRequest updatedRequest) {
        return reportService.updateReport(id, updatedRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // Para ver más detalles en la consola
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    //Delete Logic Report
    @DeleteMapping("/{id}")
    public Mono<Report> deleteReport(@PathVariable Long id) {
        return reportService.deleteReport(id);
    }

    //Restore Report
    @PutMapping("/restore/{id}")
    public Mono<Report> restoreReport(@PathVariable Long id) {
        return reportService.restoreReport(id);
    }

}
