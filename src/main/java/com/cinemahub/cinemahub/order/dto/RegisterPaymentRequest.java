package com.cinemahub.cinemahub.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// approved simula el resultado de la pasarela de pago. Cuando se integre una pasarela real
// (Stripe, Culqi, etc.), este campo lo determina la respuesta de esa pasarela, no el cliente.
public record RegisterPaymentRequest(
        @NotBlank @Size(max = 30) String paymentMethod,
        @Size(max = 100) String transactionCode,
        boolean approved
) {
}