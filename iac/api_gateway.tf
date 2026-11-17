resource "aws_apigatewayv2_api" "cinemahub" {
  name          = "${var.project_name}-${local.env}-http-api"
  description   = "API HTTP de CinemaHub para integrar las funciones Lambda"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["http://localhost:5173", "http://127.0.0.1:5173"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization"]
    max_age       = 3600
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-http-api"
  })
}

output "api_gateway_id" {
  description = "ID de la API HTTP en API Gateway"
  value       = aws_apigatewayv2_api.cinemahub.id
}

output "api_gateway_endpoint" {
  description = "URL base HTTPS de la API HTTP"
  value       = aws_apigatewayv2_api.cinemahub.api_endpoint
}