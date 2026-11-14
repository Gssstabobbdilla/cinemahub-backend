resource "aws_vpc" "cinemahub" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name        = "${var.project_name}-${local.env}-vpc"
    Environment = local.env
  }
}

resource "aws_default_security_group" "default" {
  vpc_id = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-default-sg-restricted"
  })
}