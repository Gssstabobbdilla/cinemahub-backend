locals {
  core_lambda_routes = {
    consultar_cartelera = {
      method        = "GET"
      path          = "/cartelera"
      function_name = aws_lambda_function.consultar_cartelera.function_name
      invoke_arn    = aws_lambda_function.consultar_cartelera.invoke_arn
    }
    consultar_asientos = {
      method        = "GET"
      path          = "/funciones/*/asientos"
      function_name = aws_lambda_function.consultar_asientos.function_name
      invoke_arn    = aws_lambda_function.consultar_asientos.invoke_arn
    }
    reservar_asientos = {
      method        = "POST"
      path          = "/asientos/reservar"
      function_name = aws_lambda_function.reservar_asientos.function_name
      invoke_arn    = aws_lambda_function.reservar_asientos.invoke_arn
    }
    liberar_asientos = {
      method        = "POST"
      path          = "/asientos/liberar"
      function_name = aws_lambda_function.liberar_asientos.function_name
      invoke_arn    = aws_lambda_function.liberar_asientos.invoke_arn
    }
    registrar_reserva = {
      method        = "POST"
      path          = "/reservas"
      function_name = aws_lambda_function.registrar_reserva.function_name
      invoke_arn    = aws_lambda_function.registrar_reserva.invoke_arn
    }
    consultar_reservas = {
      method        = "GET"
      path          = "/reservas"
      function_name = aws_lambda_function.consultar_reservas.function_name
      invoke_arn    = aws_lambda_function.consultar_reservas.invoke_arn
    }
    consultar_productos = {
      method        = "GET"
      path          = "/productos"
      function_name = aws_lambda_function.consultar_productos.function_name
      invoke_arn    = aws_lambda_function.consultar_productos.invoke_arn
    }
    realizar_pago = {
      method        = "POST"
      path          = "/pagos"
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

resource "aws_apigatewayv2_route" "consultar_cartelera" {
  #checkov:skip=CKV_AWS_309:Endpoint publico de consulta de cartelera para frontend web
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "GET /cartelera"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["consultar_cartelera"].id}"
}

resource "aws_apigatewayv2_route" "consultar_asientos" {
  #checkov:skip=CKV_AWS_309:Endpoint publico de consulta de disponibilidad de asientos
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "GET /funciones/{showtimeId}/asientos"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["consultar_asientos"].id}"
}

resource "aws_apigatewayv2_route" "reservar_asientos" {
  #checkov:skip=CKV_AWS_309:Autorizacion validada en capa aplicativa Lambda para laboratorio academico
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "POST /asientos/reservar"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["reservar_asientos"].id}"
}

resource "aws_apigatewayv2_route" "liberar_asientos" {
  #checkov:skip=CKV_AWS_309:Autorizacion validada en capa aplicativa Lambda para laboratorio academico
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "POST /asientos/liberar"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["liberar_asientos"].id}"
}

resource "aws_apigatewayv2_route" "registrar_reserva" {
  #checkov:skip=CKV_AWS_309:Autorizacion validada en capa aplicativa Lambda para laboratorio academico
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "POST /reservas"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["registrar_reserva"].id}"
}

resource "aws_apigatewayv2_route" "consultar_reservas" {
  #checkov:skip=CKV_AWS_309:Autorizacion validada en capa aplicativa Lambda para laboratorio academico
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "GET /reservas"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["consultar_reservas"].id}"
}

resource "aws_apigatewayv2_route" "consultar_productos" {
  #checkov:skip=CKV_AWS_309:Endpoint publico de consulta de catalogo de dulceria
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "GET /productos"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["consultar_productos"].id}"
}

resource "aws_apigatewayv2_route" "realizar_pago" {
  #checkov:skip=CKV_AWS_309:Autorizacion validada en capa aplicativa Lambda para laboratorio academico
  api_id             = aws_apigatewayv2_api.cinemahub.id
  route_key          = "POST /pagos"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.core_routes["realizar_pago"].id}"
}

resource "aws_lambda_permission" "allow_apigateway_core" {
  for_each = local.core_lambda_routes

  statement_id  = "AllowExecutionFromAPIGateway_${each.key}"
  action        = "lambda:InvokeFunction"
  function_name = each.value.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.cinemahub.execution_arn}/*/${each.value.method}${each.value.path}"
}