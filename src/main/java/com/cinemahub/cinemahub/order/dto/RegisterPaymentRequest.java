package com.cinemahub.cinemahub.order.dto;

// approved simula el resultado de la pasarela de pago. Cuando se integre una pasarela real
// (Stripe, Culqi, etc.), este campo lo determina la respuesta de esa pasarela, no el cliente.
public record RegisterPaymentRequest(String paymentMethod, String transactionCode, boolean approved) {
}