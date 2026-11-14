resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.cinemahub.id
  name        = "$default"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_access.arn
    format = jsonencode({
      requestId = "$context.requestId"
      sourceIp= "$context.identity.sourceIp"
      requestTime= "$context.requestTime"
      httpMethod = "$context.httpMethod"
      routeKey= "$context.routeKey"
      status  = "$context.status"
      responseLength   = "$context.responseLength"
      protocol  = "$context.protocol"
      extendedRequestId  = "$context.extendedRequestId"
    })
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-api-default-stage"
  })
}

output "api_invoke_url" {
  description = "URL de invocación de la API HTTP en el stage default"
  value  = aws_apigatewayv2_stage.default.invoke_url
}
