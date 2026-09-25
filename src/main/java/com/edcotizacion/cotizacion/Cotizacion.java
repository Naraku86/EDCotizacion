package com.edcotizacion.cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.edcotizacion.cliente.Cliente;

public class Cotizacion {

    private Long id;
    private String folio;
    private LocalDate fecha;
    private int vigenciaDias;
    private Estado estado = Estado.BORRADOR;
    /** Copia de los datos del cliente al momento de cotizar. */
    private Cliente cliente = new Cliente();
    private boolean aplicaIva = true;
    private BigDecimal tasaIva;
    private BigDecimal envio = BigDecimal.ZERO;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String formaPago;
    private String tiempoEntrega;
    private String garantia;
    private String observaciones;
    private List<Partida> partidas = new ArrayList<>();
    private LocalDateTime creada;
    private LocalDateTime modificada;

    public LocalDate getVence() {
        return fecha == null ? null : fecha.plusDays(vigenciaDias);
    }

    public boolean isVencida() {
        return estado.isAbierta() && getVence() != null && LocalDate.now().isAfter(getVence());
    }

    public BigDecimal getCostoTotal() {
        return partidas.stream()
                .filter(p -> p.getCosto() != null && p.getCantidad() != null)
                .map(p -> p.getCosto().multiply(p.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGananciaTotal() {
        return partidas.stream()
                .map(Partida::getGanancia)
                .filter(g -> g != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public int getVigenciaDias() { return vigenciaDias; }
    public void setVigenciaDias(int vigenciaDias) { this.vigenciaDias = vigenciaDias; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public boolean isAplicaIva() { return aplicaIva; }
    public void setAplicaIva(boolean aplicaIva) { this.aplicaIva = aplicaIva; }
    public BigDecimal getTasaIva() { return tasaIva; }
    public void setTasaIva(BigDecimal tasaIva) { this.tasaIva = tasaIva; }
    public BigDecimal getEnvio() { return envio; }
    public void setEnvio(BigDecimal envio) { this.envio = envio; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public String getTiempoEntrega() { return tiempoEntrega; }
    public void setTiempoEntrega(String tiempoEntrega) { this.tiempoEntrega = tiempoEntrega; }
    public String getGarantia() { return garantia; }
    public void setGarantia(String garantia) { this.garantia = garantia; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<Partida> getPartidas() { return partidas; }
    public void setPartidas(List<Partida> partidas) { this.partidas = partidas; }
    public LocalDateTime getCreada() { return creada; }
    public void setCreada(LocalDateTime creada) { this.creada = creada; }
    public LocalDateTime getModificada() { return modificada; }
    public void setModificada(LocalDateTime modificada) { this.modificada = modificada; }
}
