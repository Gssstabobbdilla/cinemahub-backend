resource "aws_cognito_user_pool" "cinemahub" {
  name  = "${var.project_name}-${local.env}-users"
  username_attributes  = ["email"]
  auto_verified_attributes = ["email"]
  mfa_configuration = "OFF"

  password_policy {
    minimum_length = 12
    require_lowercase  = true
    require_numbers  = true
    require_symbols  = true
    require_uppercase = true
    temporary_password_validity_days = 7
  }

  account_recovery_setting {
    recovery_mechanism {
      name = "verified_email"
      priority = 1
    }
  }

  user_attribute_update_settings {
    attributes_require_verification_before_update = ["email"]
  }

  schema {
    attribute_data_type = "String"
    name = "cinemahub_user_id"
    required = false
    mutable = true

    string_attribute_constraints {
      min_length = 1
      max_length = 20
    }
  }

  tags = local.common_tags
}

resource "aws_cognito_user_pool_client" "cinemahub_web" {
  name = "${var.project_name}-${local.env}-web"
  user_pool_id = aws_cognito_user_pool.cinemahub.id

  generate_secret = false
  prevent_user_existence_errors = "ENABLED"
  enable_token_revocation  = true
  explicit_auth_flows = [
    "ALLOW_USER_SRP_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]

  read_attributes = [
    "email",
    "email_verified",
    "given_name",
    "family_name",
    "phone_number",
    "custom:cinemahub_user_id"
  ]

  # El ID interno de CinemaHub solo puede asignarse desde un proceso confiable.
  write_attributes = [
    "email",
    "given_name",
    "family_name",
    "phone_number"
  ]

  access_token_validity  = 60
  id_token_validity = 60
  refresh_token_validity = 30

  token_validity_units {
    access_token = "minutes"
    id_token = "minutes"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_group" "user" {
  name = "USER"
  description  = "Usuario estándar de CinemaHub"
  user_pool_id = aws_cognito_user_pool.cinemahub.id
  precedence = 10
}

resource "aws_cognito_user_group" "admin" {
  name = "ADMIN"
  description = "Administradores autorizados de CinemaHub"
  user_pool_id = aws_cognito_user_pool.cinemahub.id
  precedence = 1
}

output "cognito_user_pool_id" {
  description = "ID del User Pool de CinemaHub"
  value = aws_cognito_user_pool.cinemahub.id
}

output "cognito_user_pool_client_id" {
  description = "ID público del App Client de CinemaHub"
  value = aws_cognito_user_pool_client.cinemahub_web.id
}

output "cognito_issuer_url" {
  description = "Issuer para configurar el JWT authorizer de API Gateway"
  value       = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.cinemahub.id}"
}
