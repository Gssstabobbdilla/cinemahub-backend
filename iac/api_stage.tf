resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.cinemahub.id
  name        = "$default"
  auto_deploy = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-api-default-stage"
  })
}

output "api_invoke_url" {
  description = "URL de invocación de la API HTTP en el stage default"
  value       = aws_apigatewayv2_stage.default.invoke_url
}
