resource "aws_lambda_permission" "allow_apigateway_health" {
  statement_id  = "AllowExecutionFromAPIGatewayHealth"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.consultar_cartelera.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.cinemahub.execution_arn}/*/GET/health"
}