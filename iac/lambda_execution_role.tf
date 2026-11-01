
resource "aws_iam_role" "lambda_execution" {
  name = "${var.project_name}-${local.env}-lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-lambda-execution-role"
  })
}
