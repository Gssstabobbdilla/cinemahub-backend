data "aws_availability_zones" "available" {
  state = "available"
  filter {
    name   = "zone-name"
    values = ["us-east-2a", "us-east-2b", "us-east-2c"]
  }
}

locals {
  azs = slice(data.aws_availability_zones.available.names, 0, 2)
}