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

variable "public_subnets_cidr" {
  description = "CIDR de las subredes publicas"
  type        = list(string)
}

variable "private_subnets_cidr" {
  description = "CIDR de las subredes privadas"
  type        = list(string)
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


variable "db_username" {
  description = "Usuario administrador inicial de RDS"
  type        = string
  default     = "cinema_admin"
}

variable "db_engine_version" {
  description = "Versión de PostgreSQL"
  type        = string
  default     = "17.11"
}

variable "db_instance_class" {
  description = "Clase de la instancia RDS para el entorno de desarrollo"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage_gb" {
  description = "Almacenamiento inicial de RDS en GB"
  type        = number
  default     = 20
}

variable "lambda_runtime" {
  description = "Runtime de Node.js para las funciones Lambda sin lógica de negocio"
  type        = string
  default     = "nodejs22.x"
}

variable "lambda_memory_size" {
  description = "Memoria en MB para cada función Lambda"
  type        = number
  default     = 128
}

variable "lambda_timeout" {
  description = "Tiempo máximo de ejecución en segundos para cada función Lambda"
  type        = number
  default     = 15
}

