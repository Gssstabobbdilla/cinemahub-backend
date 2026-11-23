# CinemaHub Lambdas

`consultar_cartelera` is an independent Java 21 Lambda. It reads `movies` and `classifications`, retrieves credentials from Secrets Manager, and connects to PostgreSQL through RDS Proxy with TLS.

Build before a local Terraform plan:

```powershell
cd backend
.\mvnw.cmd -B -Paws-lambda -DskipTests package
cd ../iac
terraform plan -var-file=dev.tfvars.example
```

GitHub Actions builds the package before Terraform validation and planning. The other Lambda functions remain Node.js stubs until their operations are implemented.
