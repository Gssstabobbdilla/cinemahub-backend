resource "aws_internet_gateway" "cinemahub" {
  vpc_id = aws_vpc.cinemahub.id

  tags = {
    Name = "${var.project_name}-${local.env}-internet-gateway"
  }
}