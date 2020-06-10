package app.repository;

import app.Models.DBLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBLogRepository extends JpaRepository<DBLog,Long> {

}
