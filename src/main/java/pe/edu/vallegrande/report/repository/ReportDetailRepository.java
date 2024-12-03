package pe.edu.vallegrande.report.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.report.model.ReportDetail;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReportDetailRepository extends ReactiveCrudRepository<ReportDetail, Long> {
    Flux<ReportDetail> findByReportId(Long reportId);
}
