resource "aws_security_group" "lambda" {
  name        = "${var.project_name}-${local.env}-lambda-sg"
  description = "Grupo de seguridad para las funciones Lambda"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-lambda-sg"
  })
}
