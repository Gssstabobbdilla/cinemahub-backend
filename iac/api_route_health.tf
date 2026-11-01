resource "aws_apigatewayv2_route" "health" {
  api_id    = aws_apigatewayv2_api.cinemahub.id
  route_key = "GET /health"

  target = "integrations/${aws_apigatewayv2_integration.consultar_cartelera.id}"
}