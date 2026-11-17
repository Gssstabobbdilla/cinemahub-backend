resource "aws_security_group" "vpce_secretsmanager" {
  name        = "${var.project_name}-${local.env}-vpce-secretsmanager-sg"
  description = "Grupo de seguridad para el VPC Endpoint de Secrets Manager"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-vpce-secretsmanager-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "vpce_from_lambda" {
  security_group_id            = aws_security_group.vpce_secretsmanager.id
  referenced_security_group_id = aws_security_group.lambda.id
  from_port                    = 443
  to_port                      = 443
  ip_protocol                  = "tcp"
  description                  = "Permite trafico HTTPS desde el Security Group de Lambda hacia Secrets Manager"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-vpce-from-lambda"
  })
}

resource "aws_vpc_security_group_egress_rule" "lambda_to_vpce_secretsmanager" {
  security_group_id            = aws_security_group.lambda.id
  referenced_security_group_id = aws_security_group.vpce_secretsmanager.id
  from_port                    = 443
  to_port                      = 443
  ip_protocol                  = "tcp"
  description                  = "Permite a Lambda consultar secretos en Secrets Manager por red privada"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-lambda-to-vpce-secrets"
  })
}

resource "aws_vpc_endpoint" "secretsmanager" {
  vpc_id              = aws_vpc.cinemahub.id
  service_name        = "com.amazonaws.${var.aws_region}.secretsmanager"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.vpce_secretsmanager.id]
  private_dns_enabled = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-vpce-secretsmanager"
  })
}