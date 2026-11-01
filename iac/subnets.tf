resource "aws_subnet" "public" {
  count = length(var.public_subnets_cidr)

  vpc_id                  = aws_vpc.cinemahub.id
  cidr_block              = var.public_subnets_cidr[count.index]
  availability_zone       = local.azs[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-${local.env}-public-${count.index + 1}"
  }
}

resource "aws_subnet" "private" {
  count = length(var.private_subnets_cidr)

  vpc_id                  = aws_vpc.cinemahub.id
  cidr_block              = var.private_subnets_cidr[count.index]
  availability_zone       = local.azs[count.index]
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-${local.env}-private-${count.index + 1}"
  }
}
