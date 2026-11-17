resource "aws_cloudwatch_log_group" "api_access" {
  name              = "/aws/apigateway/${var.project_name}-${local.env}-http-api-access"
  retention_in_days = 365
  kms_key_id        = aws_kms_key.logs_cmk.arn

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-http-api-access"
  })
}