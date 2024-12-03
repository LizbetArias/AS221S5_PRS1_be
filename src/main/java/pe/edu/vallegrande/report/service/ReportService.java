package pe.edu.vallegrande.report.service;

import pe.edu.vallegrande.report.model.Report;
import pe.edu.vallegrande.report.model.ReportDetail;
import pe.edu.vallegrande.report.model.ReportRequest;
import pe.edu.vallegrande.report.repository.ReportRepository;
import pe.edu.vallegrande.report.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    public Flux<ReportRequest> getAllReports(String status) {
        // Filtrar por estado (A = Activo, I = Inactivo)
        return reportRepository.findAll()
                .filter(report -> report.getActive().equals(status))  // Filtro de estado
                .flatMap(report ->
                        reportDetailRepository.findByReportId(report.getId())
                                .collectList() // Convertimos los detalles a una lista
                                .map(details -> new ReportRequest(report, details)) // Combinamos Report y detalles
                );
    }

    public Mono<ReportRequest> getReportById(Long id) {
        return reportRepository.findById(id)
                .flatMap(report ->
                        reportDetailRepository.findByReportId(report.getId())
                                .collectList() // Convertimos los detalles a una lista
                                .map(details -> new ReportRequest(report, details)) // Combinamos Report y detalles
                );
    }


    // Insertar un Report con sus detalles
    public Mono<Report> createReport(Report report, Flux<ReportDetail> reportDetails) {
        return reportRepository.save(report) // Guardamos el Report
                .flatMap(savedReport ->
                        // Asignamos el ID del report recién creado a los detalles
                        reportDetails
                                .map(detail -> {
                                    detail.setReportId(savedReport.getId());
                                    return detail;
                                })
                                .collectList()
                                .flatMap(details ->
                                        reportDetailRepository.saveAll(details) // Guardamos los detalles asociados
                                                .collectList()
                                                .thenReturn(savedReport)
                                )
                );
    }

    public Mono<Report> updateReport(Long reportId, ReportRequest updatedRequest) {
        return reportRepository.findById(reportId)
                .flatMap(existingReport -> {
                    // Actualizamos los campos del Report
                    existingReport.setYear(updatedRequest.getReport().getYear());
                    existingReport.setTrimester(updatedRequest.getReport().getTrimester());
                    existingReport.setDescription(updatedRequest.getReport().getDescription());
                    existingReport.setSchedule(updatedRequest.getReport().getSchedule());

                    return reportRepository.save(existingReport); // Guardamos el Report actualizado
                })
                .flatMap(report -> {
                    // Obtenemos los detalles existentes
                    return reportDetailRepository.findByReportId(report.getId())
                            .collectList() // Convertimos Flux a List<ReportDetail>
                            .flatMap(existingDetails -> {
                                // Creamos un conjunto de ID de los detalles existentes
                                Set<Long> existingDetailIds = existingDetails.stream()
                                        .map(ReportDetail::getId)
                                        .collect(Collectors.toSet());

                                // Filtramos los detalles nuevos
                                List<ReportDetail> newDetails = updatedRequest.getReportDetails().stream()
                                        .filter(detail -> detail.getId() == null || !existingDetailIds.contains(detail.getId()))
                                        .map(detail -> {
                                            detail.setReportId(report.getId());
                                            return detail;
                                        })
                                        .collect(Collectors.toList());

                                // Actualizamos los detalles existentes
                                List<Mono<ReportDetail>> updateMonos = updatedRequest.getReportDetails().stream()
                                        .filter(detail -> detail.getId() != null && existingDetailIds.contains(detail.getId()))
                                        .map(detail -> {
                                            return existingDetails.stream()
                                                    .filter(ed -> ed.getId().equals(detail.getId()))
                                                    .findFirst()
                                                    .map(existingDetail -> {
                                                        existingDetail.setWorkshop(detail.getWorkshop());
                                                        existingDetail.setDescription(detail.getDescription());
                                                        existingDetail.setImg(detail.getImg());
                                                        return reportDetailRepository.save(existingDetail); // Retorna un Mono<ReportDetail>
                                                    })
                                                    .orElse(null); // Retorna null si no se encuentra
                                        })
                                        .filter(Objects::nonNull) // Filtramos los nulos
                                        .collect(Collectors.toList());
                                // Guardamos los nuevos detalles y eliminamos los que no están en la nueva lista
                                return Flux.merge(updateMonos)
                                        .collectList()
                                        .thenMany(reportDetailRepository.deleteAll(existingDetails.stream()
                                                .filter(ed -> !updatedRequest.getReportDetails().stream()
                                                        .anyMatch(newDetail -> newDetail.getId() != null && newDetail.getId().equals(ed.getId())))
                                                .collect(Collectors.toList())))
                                        .thenMany(reportDetailRepository.saveAll(newDetails))
                                        .then(Mono.just(report)); // Retornamos el report como Mono
                            });
                });
    }

    //Delete Logic Report
    public Mono<Report> deleteReport(Long reportId) {
        return reportRepository.findById(reportId)
                .flatMap(report -> {
                    report.setActive("I"); // Cambia el estado a inactivo
                    return reportRepository.save(report); // Guarda el report actualizado
                });
    }

    //Restore Report
    public Mono<Report> restoreReport(Long reportId) {
        return reportRepository.findById(reportId)
                .flatMap(report -> {
                    report.setActive("A");
                    return reportRepository.save(report);
                });
    }

}
