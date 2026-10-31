resource "aws_db_subnet_group" "cinemahub" {
  name        = "${var.project_name}-${local.env}-db-subnets"
  description = "Subredes privadas para RDS de CINEMAHUB"
  subnet_ids  = aws_subnet.private[*].id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-db-subnets"
  })
}

output "rds_subnet_group_name" {
  description = "Nombre del grupo de subredes de RDS"
  value       = aws_db_subnet_group.cinemahub.name
}
