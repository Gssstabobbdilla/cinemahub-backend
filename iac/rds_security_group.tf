resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${local.env}-rds-sg"
  description = "Grupo de seguridad para la base de datos"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-sg"
  })
}
