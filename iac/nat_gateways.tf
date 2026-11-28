resource "aws_eip" "nat" {
  count  = length(aws_subnet.public)
  domain = "vpc"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-nat-eip-${count.index + 1}"
  })
}

resource "aws_nat_gateway" "public" {
  count         = length(aws_subnet.public)
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  depends_on = [aws_internet_gateway.cinemahub]

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-nat-${count.index + 1}"
  })
}
