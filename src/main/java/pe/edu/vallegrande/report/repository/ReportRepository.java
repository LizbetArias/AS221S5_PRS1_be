package pe.edu.vallegrande.report.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.report.model.Report;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends ReactiveCrudRepository<Report, Long> {
}
