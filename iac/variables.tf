variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "cinemahub"
}

locals {
  env = terraform.workspace

  common_tags = {
    Project     = var.project_name
    Environment = local.env
    ManagedBy   = "Terraform"
  }
}

variable "aws_region" {
  description = "Región AWS"
  type        = string
  default     = "us-east-2"
}

variable "vpc_cidr" {
  description = "Bloque CIDR de la VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "priv_subnet_a_cidr" {
  description = "CIDR de la subred privada de aplicación/datos en AZ lógica A"
  type        = string
  default     = "10.0.11.0/24"
}

variable "priv_subnet_b_cidr" {
  description = "CIDR de la subred privada de aplicación/datos en AZ lógica B"
  type        = string
  default     = "10.0.12.0/24"
}

variable "public_subnet_a_cidr" {
  description = "CIDR de la subred pública en AZ lógica A"
  type        = string
  default     = "10.0.1.0/24"
}

variable "public_subnet_b_cidr" {
  description = "CIDR de la subred pública en AZ lógica B"
  type        = string
  default     = "10.0.2.0/24"
}

variable "db_name" {
  description = "Nombre del RDS"
  type        = string
  default     = "cinema_db"
}

variable "db_engine" {
  description = "Motor de la bd"
  type        = string
  default     = "postgres"
}

variable "db_port" {
  description = "Puerto de PostgreSQL en RDS"
  type        = number
  default     = 5432
}

variable "db_multi_az" {
  description = "Habilita alta disponibilidad Multi-AZ para RDS"
  type        = bool
  default     = false
}

variable "db_publicly_accessible" {
  description = "Mantiene la aRDS sin acceso público"
  type        = bool
  default     = false
}

