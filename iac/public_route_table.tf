resource "aws_route_table" "public" {
  vpc_id = aws_vpc.cinemahub.id

  tags = {
    Name        = "${var.project_name}-${local.env}-public-route-table"
    Environment = local.env
  }
}