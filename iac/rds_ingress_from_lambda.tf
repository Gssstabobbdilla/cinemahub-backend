resource "aws_vpc_security_group_ingress_rule" "rds_from_lambda" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.lambda.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Permite PostgreSQL desde las funciones Lambda"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-from-lambda"
  })
}
