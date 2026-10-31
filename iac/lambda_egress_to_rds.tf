resource "aws_vpc_security_group_egress_rule" "lambda_to_rds" {
  security_group_id            = aws_security_group.lambda.id
  referenced_security_group_id = aws_security_group.rds.id
  from_port                    = var.db_port
  to_port                      = var.db_port
  ip_protocol                  = "tcp"
  description                  = "Permite a Lambda conectarse a PostgreSQL en RDS"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-lambda-to-rds"
  })
}
