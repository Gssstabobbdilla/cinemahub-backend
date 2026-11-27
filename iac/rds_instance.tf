resource "aws_db_parameter_group" "cinemahub_pg" {
  name        = "${var.project_name}-${local.env}-pg17-params"
  family      = "postgres17"
  description = "Parameter group con query logging habilitado para CinemaHub"

  parameter {
    name  = "log_statement"
    value = "all"
  }

  parameter {
    name  = "log_min_duration_statement"
    value = "1000"
  }

  parameter {
    name         = "rds.force_ssl"
    value        = "1"
    apply_method = "pending-reboot"
  }

  tags = local.common_tags
}

resource "aws_iam_role" "rds_enhanced_monitoring" {
  name = "${var.project_name}-${local.env}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "monitoring.rds.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "rds_enhanced_monitoring" {
  role       = aws_iam_role.rds_enhanced_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

resource "aws_db_instance" "cinemahub" {
  identifier          = "${var.project_name}-${local.env}-postgres"
  engine              = var.db_engine
  engine_version      = var.db_engine_version
  instance_class      = var.db_instance_class
  allocated_storage   = var.db_allocated_storage_gb
  multi_az            = true
  publicly_accessible = var.db_publicly_accessible

  db_name                     = var.db_name
  username                    = var.db_username
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.cinemahub.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  parameter_group_name   = aws_db_parameter_group.cinemahub_pg.name

  auto_minor_version_upgrade = true
  copy_tags_to_snapshot      = true
  storage_encrypted          = true
  deletion_protection        = false
  skip_final_snapshot        = true

  enabled_cloudwatch_logs_exports       = ["postgresql", "upgrade"]
  monitoring_interval                   = 60
  monitoring_role_arn                   = aws_iam_role.rds_enhanced_monitoring.arn
  performance_insights_enabled          = true
  performance_insights_retention_period = 7
  performance_insights_kms_key_id       = aws_kms_key.logs_cmk.arn

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-postgres"
  })
}

output "rds_endpoint" {
  description = "Host y puerto de la instancia RDS PostgreSQL"
  value       = aws_db_instance.cinemahub.endpoint
}

output "rds_secret_arn" {
  description = "ARN del secreto en Secrets Manager con credenciales de RDS"
  value       = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
}
