resource "aws_security_group" "rds_proxy" {
  name        = "${var.project_name}-${local.env}-rds-proxy-sg"
  description = "Permite el acceso de las Lambdas al proxy de RDS"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-proxy-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "rds_proxy_from_lambda" {
  security_group_id            = aws_security_group.rds_proxy.id
  referenced_security_group_id = aws_security_group.lambda.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Permite PostgreSQL desde Lambda hacia RDS Proxy"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-proxy-from-lambda"
  })
}

resource "aws_vpc_security_group_egress_rule" "rds_proxy_to_database" {
  security_group_id            = aws_security_group.rds_proxy.id
  referenced_security_group_id = aws_security_group.rds.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Permite al proxy conectarse a RDS"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-proxy-to-rds"
  })
}

resource "aws_vpc_security_group_ingress_rule" "rds_from_proxy" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.rds_proxy.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Permite PostgreSQL desde RDS Proxy"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-from-proxy"
  })
}

resource "aws_iam_role" "rds_proxy" {
  name = "${var.project_name}-${local.env}-rds-proxy-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "rds.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy" "rds_proxy_read_secret" {
  name = "${var.project_name}-${local.env}-rds-proxy-read-secret"
  role = aws_iam_role.rds_proxy.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
    }]
  })
}

resource "aws_db_proxy" "cinemahub" {
  name                   = "${var.project_name}-${local.env}-proxy"
  debug_logging          = false
  engine_family          = "POSTGRESQL"
  idle_client_timeout    = 1800
  require_tls            = true
  role_arn               = aws_iam_role.rds_proxy.arn
  vpc_security_group_ids = [aws_security_group.rds_proxy.id]
  vpc_subnet_ids         = aws_subnet.private[*].id

  auth {
    auth_scheme = "SECRETS"
    description = "Credenciales administradas de RDS"
    iam_auth    = "DISABLED"
    secret_arn  = aws_db_instance.cinemahub.master_user_secret[0].secret_arn
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-proxy"
  })
}

resource "aws_db_proxy_default_target_group" "cinemahub" {
  db_proxy_name = aws_db_proxy.cinemahub.name

  connection_pool_config {
    max_connections_percent      = 80
    max_idle_connections_percent = 50
    connection_borrow_timeout    = 120
  }

  lifecycle {
    replace_triggered_by = [aws_db_proxy.cinemahub.id]
  }
}

resource "aws_db_proxy_target" "cinemahub" {
  db_proxy_name          = aws_db_proxy.cinemahub.name
  target_group_name      = aws_db_proxy_default_target_group.cinemahub.name
  db_instance_identifier = aws_db_instance.cinemahub.identifier

  lifecycle {
    replace_triggered_by = [aws_db_proxy.cinemahub.id]
  }
}

output "rds_proxy_endpoint" {
  description = "Endpoint privado para conexiones PostgreSQL a traves de RDS Proxy"
  value       = aws_db_proxy.cinemahub.endpoint
}

