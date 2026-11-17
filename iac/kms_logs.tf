data "aws_caller_identity" "current" {}

resource "aws_kms_key" "logs_cmk" {
  description             = "CMK para cifrado de logs CloudWatch, SQS DLQ y Performance Insights de CinemaHub"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "EnableRootAccountPermissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "AllowCloudWatchLogsAndServices"
        Effect = "Allow"
        Principal = {
          Service = [
            "logs.${var.aws_region}.amazonaws.com",
            "sqs.amazonaws.com",
            "rds.amazonaws.com"
          ]
        }
        Action = [
          "kms:Encrypt*",
          "kms:Decrypt*",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:Describe*"
        ]
        Resource = "*"
      }
    ]
  })

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-logs-cmk"
  })
}

resource "aws_kms_alias" "logs_cmk" {
  name          = "alias/${var.project_name}-${local.env}-logs-cmk"
  target_key_id = aws_kms_key.logs_cmk.key_id
}