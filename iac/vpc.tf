resource "aws_vpc" "cinemahub" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name        = "${var.project_name}-${local.env}-vpc"
    Environment = local.env
  }
}
