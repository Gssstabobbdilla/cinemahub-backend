resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public[0].id
  route_table_id = aws_route_table.public.id
}