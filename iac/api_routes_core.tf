locals {
  core_lambda_routes = {
    consultar_cartelera = {
      method        = "GET"
      path          = "/movies"
      route_key     = "GET /movies"
      function_name = aws_lambda_function.consultar_cartelera.function_name
      invoke_arn    = aws_lambda_function.consultar_cartelera.invoke_arn
    }
    consultar_funcion = {
      method        = "GET"
      path          = "/showtimes/{id}"
      route_key     = "GET /showtimes/{id}"
      function_name = aws_lambda_function.consultar_funcion.function_name
      invoke_arn    = aws_lambda_function.consultar_funcion.invoke_arn
    }
    buscar_funciones = {
      method        = "GET"
      path          = "/showtimes"
      route_key     = "GET /showtimes"
      function_name = aws_lambda_function.buscar_funciones.function_name
      invoke_arn    = aws_lambda_function.buscar_funciones.invoke_arn
    }
    consultar_asientos = {
      method        = "GET"
      path          = "/showtimes/{showtimeId}/seats"
      route_key     = "GET /showtimes/{showtimeId}/seats"
      function_name = aws_lambda_function.consultar_asientos.function_name
      invoke_arn    = aws_lambda_function.consultar_asientos.invoke_arn
    }
    registrar_reserva = {
      method        = "POST"
      path          = "/reservations"
      route_key     = "POST /reservations"
      function_name = aws_lambda_function.registrar_reserva.function_name
      invoke_arn    = aws_lambda_function.registrar_reserva.invoke_arn
    }
    consultar_reservas = {
      method        = "GET"
      path          = "/users/{userId}/reservations"
      route_key     = "GET /users/{userId}/reservations"
      function_name = aws_lambda_function.consultar_reservas.function_name
      invoke_arn    = aws_lambda_function.consultar_reservas.invoke_arn
    }
    consultar_reserva = {
      method        = "GET"
      path          = "/reservations/{id}"
      route_key     = "GET /reservations/{id}"
      function_name = aws_lambda_function.consultar_reserva.function_name
      invoke_arn    = aws_lambda_function.consultar_reserva.invoke_arn
    }
    consultar_asientos_reserva = {
      method        = "GET"
      path          = "/reservations/{id}/seats"
      route_key     = "GET /reservations/{id}/seats"
      function_name = aws_lambda_function.consultar_asientos_reserva.function_name
      invoke_arn    = aws_lambda_function.consultar_asientos_reserva.invoke_arn
    }
    cancelar_reserva = {
      method        = "POST"
      path          = "/reservations/{id}/cancel"
      route_key     = "POST /reservations/{id}/cancel"
      function_name = aws_lambda_function.liberar_asientos.function_name
      invoke_arn    = aws_lambda_function.liberar_asientos.invoke_arn
    }
    crear_orden = {
      method        = "POST"
      path          = "/reservations/{reservationId}/order"
      route_key     = "POST /reservations/{reservationId}/order"
      function_name = aws_lambda_function.crear_orden.function_name
      invoke_arn    = aws_lambda_function.crear_orden.invoke_arn
    }
    consultar_productos = {
      method        = "GET"
      path          = "/products"
      route_key     = "GET /products"
      function_name = aws_lambda_function.consultar_productos.function_name
      invoke_arn    = aws_lambda_function.consultar_productos.invoke_arn
    }
    realizar_pago = {
      method        = "POST"
      path          = "/orders/{orderId}/payments"
      route_key     = "POST /orders/{orderId}/payments"
      function_name = aws_lambda_function.realizar_pago.function_name
      invoke_arn    = aws_lambda_function.realizar_pago.invoke_arn
    }
  }
}

resource "aws_apigatewayv2_integration" "core_routes" {
  for_each = local.core_lambda_routes

  api_id                 = aws_apigatewayv2_api.cinemahub.id
  integration_type       = "AWS_PROXY"
  integration_method     = "POST"
  integration_uri        = each.value.invoke_arn
  payload_format_version = "2.0"
  description            = "Integracion HTTP API con Lambda ${each.key}"
}

resource "aws_apigatewayv2_route" "core_routes" {
  for_each           = local.core_lambda_routes
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = each.value.route_key
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes[each.key].id}"
}

resource "aws_lambda_permission" "allow_apigateway_core" {
  for_each = local.core_lambda_routes

  statement_id  = "AllowExecutionFromAPIGateway_${each.key}"
  action        = "lambda:InvokeFunction"
  function_name = each.value.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.cinemahub.execution_arn}/*/${each.value.method}${each.value.path}"
}









