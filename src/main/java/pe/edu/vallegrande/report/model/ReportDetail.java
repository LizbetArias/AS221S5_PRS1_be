package pe.edu.vallegrande.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report_detail")
public class ReportDetail {
    @Id
    private Long id;
    @Column("report_id")
    private Long reportId;
    private String workshop;
    private String description;
    private String[] img;
}
