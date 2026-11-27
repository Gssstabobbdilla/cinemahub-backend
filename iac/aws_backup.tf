resource "aws_backup_vault" "cinemahub" {
  name = "${var.project_name}-${local.env}-backup-vault"

  tags = local.common_tags
}

resource "aws_backup_plan" "cinemahub" {
  name = "${var.project_name}-${local.env}-rds-daily-backup"

  rule {
    rule_name         = "${var.project_name}-${local.env}-rds-daily"
    target_vault_name = aws_backup_vault.cinemahub.name
    schedule          = "cron(0 8 * * ? *)"

    lifecycle {
      delete_after = 14
    }

    recovery_point_tags = merge(local.common_tags, {
      Name = "${var.project_name}-${local.env}-rds-backup"
    })
  }

  tags = local.common_tags
}

resource "aws_iam_role" "backup" {
  name = "${var.project_name}-${local.env}-backup-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "backup.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "backup" {
  role       = aws_iam_role.backup.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup"
}

resource "aws_backup_selection" "cinemahub_rds" {
  name         = "${var.project_name}-${local.env}-rds-selection"
  plan_id      = aws_backup_plan.cinemahub.id
  iam_role_arn = aws_iam_role.backup.arn
  resources    = [aws_db_instance.cinemahub.arn]

  depends_on = [aws_iam_role_policy_attachment.backup]
}

output "backup_vault_arn" {
  description = "ARN del vault de AWS Backup para CinemaHub"
  value       = aws_backup_vault.cinemahub.arn
}

output "backup_plan_id" {
  description = "Identificador del plan diario de respaldos de RDS"
  value       = aws_backup_plan.cinemahub.id
}
