resource "aws_lambda_function" "registrar_reserva" {
  function_name    = "${var.project_name}-${local.env}-registrar-reserva"
  role             = aws_iam_role.lambda_execution.arn
  handler          = "index.handler"
  runtime          = var.lambda_runtime
  filename         = data.archive_file.lambda_empty.output_path
  source_code_hash = data.archive_file.lambda_empty.output_base64sha256
  memory_size      = var.lambda_memory_size
  timeout          = var.lambda_timeout

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  depends_on = [aws_iam_role_policy_attachment.lambda_vpc_access]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-registrar-reserva"
  })
}

