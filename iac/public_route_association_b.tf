resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public[1].id
  route_table_id = aws_route_table.public.id
}
