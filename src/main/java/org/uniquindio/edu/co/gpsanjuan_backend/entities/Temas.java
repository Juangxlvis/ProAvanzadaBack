package org.uniquindio.edu.co.gpsanjuan_backend.entities;

public class Temas {
    private Integer id_tema;
    private String titulo;

    public Temas() {
    }

    public Temas(Integer id_tema, String titulo) {
        this.id_tema = id_tema;
        this.titulo = titulo;
    }

    public Integer getId_tema() {
        return id_tema;
    }

    public void setId_tema(Integer id_tema) {
        this.id_tema = id_tema;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

}
