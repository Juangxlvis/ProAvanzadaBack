package org.uniquindio.edu.co.gpsanjuan_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.uniquindio.edu.co.gpsanjuan_backend.entities.Examen;

import java.util.List;

public interface ExamenRepository extends JpaRepository<Examen, Long> {
    @Query(value = "SELECT ID_TEMA, TITULO FROM TEMA", nativeQuery = true)
    List<Object[]> obtenerCursos();
}
