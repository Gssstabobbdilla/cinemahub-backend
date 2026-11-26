variable "frontend_domain_name" {
  description = "Dominio propio del frontend; vacio mientras no se compre/configure un dominio"
  type        = string
  default     = ""
}

data "aws_route53_zone" "frontend" {
  count        = var.frontend_domain_name == "" ? 0 : 1
  name         = "${var.frontend_domain_name}."
  private_zone = false
}

resource "aws_acm_certificate" "frontend" {
  count             = var.frontend_domain_name == "" ? 0 : 1
  provider          = aws.us_east_1
  domain_name       = var.frontend_domain_name
  validation_method = "DNS"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-${local.env}-frontend-certificate"
  })

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "certificate_validation" {
  for_each = var.frontend_domain_name == "" ? {} : {
    for option in aws_acm_certificate.frontend[0].domain_validation_options :
    option.domain_name => {
      name   = option.resource_record_name
      record = option.resource_record_value
      type   = option.resource_record_type
    }
  }

  zone_id         = data.aws_route53_zone.frontend[0].zone_id
  name            = each.value.name
  type            = each.value.type
  ttl             = 60
  records         = [each.value.record]
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "frontend" {
  count                   = var.frontend_domain_name == "" ? 0 : 1
  provider                = aws.us_east_1
  certificate_arn         = aws_acm_certificate.frontend[count.index].arn
  validation_record_fqdns = [for record in values(aws_route53_record.certificate_validation) : record.fqdn]
}

resource "aws_route53_record" "frontend_ipv4" {
  count   = var.frontend_domain_name == "" ? 0 : 1
  zone_id = data.aws_route53_zone.frontend[count.index].zone_id
  name    = var.frontend_domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "frontend_ipv6" {
  count   = var.frontend_domain_name == "" ? 0 : 1
  zone_id = data.aws_route53_zone.frontend[count.index].zone_id
  name    = var.frontend_domain_name
  type    = "AAAA"

  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}

output "frontend_route53_name_servers" {
  description = "Nameservers de la zona DNS existente en Route 53"
  value       = var.frontend_domain_name == "" ? [] : data.aws_route53_zone.frontend[0].name_servers
}
