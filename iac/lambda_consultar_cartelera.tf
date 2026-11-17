resource "aws_lambda_function" "consultar_cartelera" {
  function_name                  = "${var.project_name}-${local.env}-consultar-cartelera"
  role                           = aws_iam_role.lambda_execution.arn
  handler                        = "index.handler"
  runtime                        = var.lambda_runtime
  filename                       = data.archive_file.lambda_empty.output_path
  source_code_hash               = data.archive_file.lambda_empty.output_base64sha256
  memory_size                    = var.lambda_memory_size
  timeout                        = var.lambda_timeout
#  reserved_concurrent_executions = 5
#  code_signing_config_arn        = aws_lambda_code_signing_config.this.arn

  tracing_config {
    mode = "Active"
  }

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  dead_letter_config {
    target_arn = aws_sqs_queue.lambda_dlq.arn
  }
  kms_key_arn = aws_kms_key.logs_cmk.arn
  environment {
    variables = {
      DB_HOST       = aws_db_instance.cinemahub.address
      DB_PORT       = tostring(var.db_port)
      DB_NAME       = var.db_name
      DB_SECRET_ARN = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
    }
  }

  depends_on = [aws_iam_role_policy_attachment.lambda_vpc_access]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-consultar-cartelera"
  })
}