resource "aws_apigatewayv2_integration" "consultar_cartelera" {
  api_id                 = aws_apigatewayv2_api.cinemahub.id
  integration_type       = "AWS_PROXY"
  integration_method     = "POST"
  integration_uri        = aws_lambda_function.consultar_cartelera.invoke_arn
  payload_format_version = "2.0"
  description            = "Integración HTTP API con Lambda consultar-cartelera"
}
