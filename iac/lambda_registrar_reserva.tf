resource "aws_lambda_function" "registrar_reserva" {
  function_name= "${var.project_name}-${local.env}-registrar-reserva"
  role= aws_iam_role.lambda_execution.arn
  handler= "com.cinemahub.cinemahub.lambda.reservation.RegistrarReservaHandler::handleRequest"
  runtime= "java21"
  filename= "${path.module}/../backend/target/cinemahub-0.0.1-SNAPSHOT-aws-lambda.jar"
  source_code_hash= filebase64sha256("${path.module}/../backend/target/cinemahub-0.0.1-SNAPSHOT-aws-lambda.jar")
  memory_size= max(var.lambda_memory_size, 512)
  timeout= max(var.lambda_timeout, 30)
  kms_key_arn= aws_kms_key.logs_cmk.arn

  tracing_config {
    mode = "Active"
  }

  vpc_config {
    subnet_ids= aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  dead_letter_config {
    target_arn = aws_sqs_queue.lambda_dlq.arn
  }

  environment {
    variables = {
      DB_HOST= aws_db_proxy.cinemahub.endpoint
      DB_PORT= tostring(var.db_port)
      DB_NAME= var.db_name
      DB_SECRET_ARN = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.lambda_vpc_access,
    aws_iam_role_policy.lambda_read_rds_secret
  ]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-registrar-reserva"
  })
}
