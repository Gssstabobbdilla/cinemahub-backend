resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${local.env}-rds-sg"
  description = "Grupo de seguridad para la base de datos RDS de CINEMAHUB"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-rds-sg"
  })
}

output "rds_security_group_id" {
  description = "ID del grupo de seguridad de RDS"
  value       = aws_security_group.rds.id
}
