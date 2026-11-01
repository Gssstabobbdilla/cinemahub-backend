resource "aws_apigatewayv2_api" "cinemahub" {
  name          = "${var.project_name}-${local.env}-http-api"
  description   = "api http para integrar las funciones Lambda"
  protocol_type = "HTTP"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-http-api"
  })
}

output "api_gateway_id" {
  description = "Id de la API"
  value       = aws_apigatewayv2_api.cinemahub.id
}

output "api_gateway_endpoint" {
  description = "Endpointde la API http"
  value       = aws_apigatewayv2_api.cinemahub.api_endpoint
}
