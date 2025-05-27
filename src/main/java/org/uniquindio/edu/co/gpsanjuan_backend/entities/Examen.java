package org.uniquindio.edu.co.gpsanjuan_backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "examen_seq_gen")
    @SequenceGenerator(name = "examen_seq_gen", sequenceName = "examen_seq", allocationSize = 1)
    private Long id_examen;
    private  Integer tiempo_max;
    private Integer numero_preguntas;
    private Integer porcentajeCurso;
    private String nombre;
    private Integer porcentaje_aprobatorio;
    private String fecha_hora_inicio;
    private String fecha_hora_fin;
    private Integer num_preguntas_aleatorias;
    private Integer id_tema;
    private Integer id_docente;
    private Integer id_grupo;

}
