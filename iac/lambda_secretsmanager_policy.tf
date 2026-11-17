resource "aws_iam_role_policy" "lambda_read_rds_secret" {
  name = "${var.project_name}-${local.env}-lambda-read-rds-secret"
  role = aws_iam_role.lambda_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "AllowReadRDSMasterSecret"
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
      }
    ]
  })
}