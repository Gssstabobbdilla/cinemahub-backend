package com.cinemahub.cinemahub.order.entity;

import com.cinemahub.cinemahub.reservation.entity.Reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

// Se llama "orders" en BD (palabra reservada en varios dialectos SQL); en Java la clase
// se llama Order para reflejar el dominio. No confundir con java.util.Order (no existe) ni
// con javax/jakarta.persistence.criteria.Order (paquete distinto, sin conflicto real).
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Una reserva genera, como máximo, una orden (UNIQUE en reservation_id).
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "purchased_at")
    private OffsetDateTime purchasedAt;

    protected Order() {
        // requerido por JPA
    }

    public Order(Reservation reservation, BigDecimal total) {
        this.reservation = reservation;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OffsetDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(OffsetDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Order{id=%d, status=%s, total=%s}".formatted(id, status, total);
    }
}