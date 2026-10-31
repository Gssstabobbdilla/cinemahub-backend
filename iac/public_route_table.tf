resource "aws_route_table" "public" {
  vpc_id = aws_vpc.cinemahub.id

  tags = {
    Name        = "${var.project_name}-${local.env}-public-route-table"
    Environment = local.env
  }
}

resource "aws_route" "public_internet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.cinemahub.id
}