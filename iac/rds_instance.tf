resource "aws_db_instance" "cinemahub" {
  identifier          = "${var.project_name}-${local.env}-postgres"
  engine              = var.db_engine
  engine_version      = var.db_engine_version
  instance_class      = var.db_instance_class
  allocated_storage   = var.db_allocated_storage_gb
  multi_az            = var.db_multi_az
  publicly_accessible = var.db_publicly_accessible

  db_name                     = var.db_name
  username                    = var.db_username
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.cinemahub.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  auto_minor_version_upgrade = true
  copy_tags_to_snapshot = true
  storage_encrypted   = true
  deletion_protection = false
  skip_final_snapshot = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-postgres"
  })
}