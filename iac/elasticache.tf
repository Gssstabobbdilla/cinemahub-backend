resource "aws_elasticache_subnet_group" "cinemahub" {
  name        = "${var.project_name}-${local.env}-cache-subnets"
  description = "Subredes privadas para ElastiCache de CinemaHub"
  subnet_ids  = aws_subnet.private[*].id

  tags = local.common_tags
}

resource "aws_security_group" "elasticache" {
  name        = "${var.project_name}-${local.env}-elasticache-sg"
  description = "Permite el acceso de las Lambdas a ElastiCache"
  vpc_id      = aws_vpc.cinemahub.id

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-elasticache-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "elasticache_from_lambda" {
  security_group_id            = aws_security_group.elasticache.id
  referenced_security_group_id = aws_security_group.lambda.id
  from_port                    = 6379
  to_port                      = 6379
  ip_protocol                  = "tcp"
  description                  = "Permite Redis TLS desde las Lambdas"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-cache-from-lambda"
  })
}

resource "aws_elasticache_replication_group" "cinemahub" {
  replication_group_id       = "${var.project_name}-${local.env}-cache"
  description                = "Cache Redis privada de CinemaHub"
  engine                     = "redis"
  engine_version             = "7.1"
  node_type                  = "cache.t4g.micro"
  parameter_group_name       = "default.redis7"
  port                       = 6379
  num_cache_clusters         = length(aws_subnet.private)
  automatic_failover_enabled = true
  multi_az_enabled           = true
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  subnet_group_name          = aws_elasticache_subnet_group.cinemahub.name
  security_group_ids         = [aws_security_group.elasticache.id]
  snapshot_retention_limit   = 1
  apply_immediately          = false

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-elasticache"
  })
}

output "elasticache_primary_endpoint" {
  description = "Endpoint primario de ElastiCache Redis"
  value       = aws_elasticache_replication_group.cinemahub.primary_endpoint_address
}

output "elasticache_reader_endpoint" {
  description = "Endpoint de lectura de ElastiCache Redis"
  value       = aws_elasticache_replication_group.cinemahub.reader_endpoint_address
}
