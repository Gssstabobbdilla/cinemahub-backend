resource "aws_vpc_security_group_egress_rule" "lambda_to_rds" {
  security_group_id            = aws_security_group.lambda.id
  referenced_security_group_id = aws_security_group.rds_proxy.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Conexion Lambda a PostgreSQL mediante RDS Proxy"
  tags                         = merge(local.common_tags, { Name = "${var.project_name}-${local.env}-lambda-to-rds-proxy" })
}
