package com.edcotizacion.cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String folio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "vigencia_dias", nullable = false)
    private int vigenciaDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.BORRADOR;

    /** Cliente del catálogo (puede no existir si se borró). */
    @Column(name = "cliente_id")
    private Long clienteId;

    /** Copia de los datos del cliente al momento de cotizar. */
    @Embedded
    @AttributeOverride(name = "nombre", column = @Column(name = "cliente_nombre", nullable = false))
    @AttributeOverride(name = "contacto", column = @Column(name = "cliente_contacto"))
    @AttributeOverride(name = "telefono", column = @Column(name = "cliente_telefono"))
    @AttributeOverride(name = "email", column = @Column(name = "cliente_email"))
    @AttributeOverride(name = "rfc", column = @Column(name = "cliente_rfc"))
    @AttributeOverride(name = "direccion", column = @Column(name = "cliente_direccion"))
    private DatosCliente cliente = DatosCliente.vacio();

    @Column(name = "aplica_iva", nullable = false)
    private boolean aplicaIva = true;

    @Column(name = "tasa_iva", nullable = false)
    private BigDecimal tasaIva;

    @Column(nullable = false)
    private BigDecimal envio = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal iva;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "forma_pago")
    private String formaPago;

    @Column(name = "tiempo_entrega")
    private String tiempoEntrega;

    private String garantia;

    private String observaciones;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden")
    private List<Partida> partidas = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime creada;

    @Column(nullable = false)
    private LocalDateTime modificada;

    /** Sustituye todas las partidas (las anteriores se borran) y las numera en orden. */
    public void reemplazarPartidas(List<Partida> nuevas) {
        partidas.clear();
        int orden = 1;
        for (Partida p : nuevas) {
            p.asignar(this, orden++);
            partidas.add(p);
        }
    }

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
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public int getVigenciaDias() { return vigenciaDias; }
    public void setVigenciaDias(int vigenciaDias) { this.vigenciaDias = vigenciaDias; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public DatosCliente getCliente() { return cliente; }
    public void setCliente(DatosCliente cliente) { this.cliente = cliente; }
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
    /** Solo lectura: para cambiarlas usa {@link #reemplazarPartidas(List)}. */
    public List<Partida> getPartidas() { return Collections.unmodifiableList(partidas); }
    public LocalDateTime getCreada() { return creada; }
    public void setCreada(LocalDateTime creada) { this.creada = creada; }
    public LocalDateTime getModificada() { return modificada; }
    public void setModificada(LocalDateTime modificada) { this.modificada = modificada; }
}
