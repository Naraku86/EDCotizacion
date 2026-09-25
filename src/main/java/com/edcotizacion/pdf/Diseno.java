package com.edcotizacion.pdf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diseño del PDF: qué plantilla (acomodo) se usa, colores, textos editables y secciones
 * opcionales. Se guarda como JSON en config ('plantilla.diseno'); DisenoService lo completa
 * con los valores del diseño genérico para que nunca falte una clave.
 */
public class Diseno {

    public static final List<String> PLANTILLAS = List.of("clasica", "minimalista", "moderna", "compacta");

    private String plantilla = "clasica";
    private String papel = "carta";
    private Map<String, String> colores = new LinkedHashMap<>();
    /** Títulos, encabezados de columnas y pie: todo lo que se edita haciendo clic en la hoja. */
    private Map<String, String> textos = new LinkedHashMap<>();
    private boolean mostrarNombre = true;
    private boolean mayusculas = true;
    private boolean numeroPartida = true;
    private boolean paginas = true;
    private Seccion banco = new Seccion();
    private Seccion nota = new Seccion();
    private Seccion firma = new Seccion();

    /** Sección opcional que se agrega con "+ ..." en el editor. */
    public static class Seccion {
        private boolean activo;
        private String titulo;
        private String texto;

        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
    }

    /** Texto por clave; "" si no existe. */
    public String t(String clave) {
        String v = textos.get(clave);
        return v == null ? "" : v;
    }

    public String getPlantilla() { return PLANTILLAS.contains(plantilla) ? plantilla : "clasica"; }
    public void setPlantilla(String plantilla) { this.plantilla = plantilla; }
    public String getPapel() { return "a4".equals(papel) ? "a4" : "carta"; }
    public void setPapel(String papel) { this.papel = papel; }
    public Map<String, String> getColores() { return colores; }
    public void setColores(Map<String, String> colores) { this.colores = colores; }
    public Map<String, String> getTextos() { return textos; }
    public void setTextos(Map<String, String> textos) { this.textos = textos; }
    public boolean isMostrarNombre() { return mostrarNombre; }
    public void setMostrarNombre(boolean mostrarNombre) { this.mostrarNombre = mostrarNombre; }
    public boolean isMayusculas() { return mayusculas; }
    public void setMayusculas(boolean mayusculas) { this.mayusculas = mayusculas; }
    public boolean isNumeroPartida() { return numeroPartida; }
    public void setNumeroPartida(boolean numeroPartida) { this.numeroPartida = numeroPartida; }
    public boolean isPaginas() { return paginas; }
    public void setPaginas(boolean paginas) { this.paginas = paginas; }
    public Seccion getBanco() { return banco; }
    public void setBanco(Seccion banco) { this.banco = banco; }
    public Seccion getNota() { return nota; }
    public void setNota(Seccion nota) { this.nota = nota; }
    public Seccion getFirma() { return firma; }
    public void setFirma(Seccion firma) { this.firma = firma; }
}
