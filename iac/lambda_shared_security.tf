resource "aws_signer_signing_profile" "lambda" {
  platform_id = "AWSLambda-SHA384-ECDSA"
  tags        = local.common_tags
}

resource "aws_lambda_code_signing_config" "this" {
  allowed_publishers {
    signing_profile_version_arns = [aws_signer_signing_profile.lambda.version_arn]
  }
  policies {
    untrusted_artifact_on_deployment = "Enforce"
  }
}

resource "aws_sqs_queue" "lambda_dlq" {
  name              = "${var.project_name}-${local.env}-lambda-dlq"
  kms_master_key_id = aws_kms_key.logs_cmk.arn
  tags              = local.common_tags
}

resource "aws_iam_role_policy" "lambda_dlq_send" {
  name = "${var.project_name}-${local.env}-lambda-dlq-send"
  role = aws_iam_role.lambda_execution.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["sqs:SendMessage"]
      Resource = aws_sqs_queue.lambda_dlq.arn
    }]
  })
}