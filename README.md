# Infraestructura — Semana 9 - Apply/Destroy desde GitHub Actions

La idea de este trabajo es ver cómo podemos utilizar GitHub Actions para automatizar el ciclo de vida de nuestra infraestructura.

Se implementará workflows que permitan ejecutar las operaciones ´terraform apply´ y ´terraform destroy´ desde GitHub Actions, automatizando tanto la creación como la eliminación de los recursos de infraestructura.

Para hacer este trabajo, utilizaremos OIDC como mecanismo de autenticación entre GitHub Actions y aws. Esto nos permitirá obtener credenciales temporales sin necesidad de almacenar claves de acceso permanentes como secretos dentro de este repositorio.

## Flujo

- Push a `development`: el workflow existente ejecuta `fmt`, `init`, `validate` y `plan`.
- `Terraform Apply - Dev`: el usuario selecciona `APPLY`. El job genera un plan con el rol de lectura y luego aplica ese mismo plan con el rol de despliegue.
- `Terraform Destroy - Dev`: el usuario selecciona `DESTROY`. El job genera un plan de destrucción con el rol de lectura y luego ejecuta ese plan con el rol de despliegue.
- Ambos workflows solo permiten la rama `development`, usan OIDC, el mismo S3 y el grupo de concurrencia `terraform-dev`.

## Archivos

- `.github/workflows/terraform-apply.yml`
- `.github/workflows/terraform-destroy.yml`
- `iam/github-actions-plan-read-policy.json`
- `iam/github-actions-deploy-trust-policy.json`
- `iam/github-actions-deploy-policy.json`

## 1. Validar e instalar la política de solo lectura al rol de plan

Ejecutar desde la raíz del repositorio, con PowerShell y el perfil AWS SSO administrador:

```powershell
aws sso login --profile ManuGott
$env:AWS_PROFILE = "ManuGott"
$env:AWS_PAGER = ""
aws sts get-caller-identity

aws accessanalyzer validate-policy `
  --policy-type IDENTITY_POLICY `
  --policy-document file://iam/github-actions-plan-read-policy.json `
  --region us-east-2 `
  --no-cli-pager
```

Revisa que no haya hallazgos de tipo `ERROR` antes de continuar. 

```powershell
aws iam create-policy `
  --policy-name lab09-github-actions-plan-read-policy `
  --description "Read-only Terraform plan access for CinemaHub dev" `
  --policy-document file://iam/github-actions-plan-read-policy.json

aws iam attach-role-policy `
  --role-name lab09-github-actions-dev `
  --policy-arn arn:aws:iam::829999272931:policy/lab09-github-actions-plan-read-policy
```

Después de confirmar que la nueva política está adjunta, quitar la política anterior que contenía permisos de escritura que ya no necesita el rol de plan:

```powershell
aws iam detach-role-policy `
  --role-name lab09-github-actions-dev `
  --policy-arn arn:aws:iam::829999272931:policy/lab09-github-actions-dev-policy
```

No borrar la política anterior en esta etapa; solo queda desasociada del rol de plan.

## 2. Validar y crear el rol de despliegue

Antes de crear la política:

```powershell
aws accessanalyzer validate-policy `
  --policy-type IDENTITY_POLICY `
  --policy-document file://iam/github-actions-deploy-policy.json `
  --region us-east-2 `
  --no-cli-pager
```

Revisa que no haya hallazgos de tipo `ERROR`. Luego crea el rol y la política:

```powershell
aws iam create-policy `
  --policy-name lab09-github-actions-deploy-dev-policy `
  --description "Scoped Terraform apply/destroy access for CinemaHub dev" `
  --policy-document file://iam/github-actions-deploy-policy.json

aws iam create-role `
  --role-name lab09-github-actions-deploy-dev `
  --description "GitHub Actions OIDC role for CinemaHub dev apply and destroy" `
  --max-session-duration 3600 `
  --assume-role-policy-document file://iam/github-actions-deploy-trust-policy.json

aws iam attach-role-policy `
  --role-name lab09-github-actions-deploy-dev `
  --policy-arn arn:aws:iam::829999272931:policy/lab09-github-actions-deploy-dev-policy
```

El trust policy limita la asunción al repositorio, usando su subject OIDC inmutable y el Environment `dev`
Verificar:

```powershell
aws iam get-role --role-name lab09-github-actions-deploy-dev --query "Role.Arn" --output text
aws iam list-attached-role-policies --role-name lab09-github-actions-deploy-dev --output table
aws iam list-attached-role-policies --role-name lab09-github-actions-dev --output table
```

El ARN esperado del nuevo rol es:

`arn:aws:iam::829999272931:role/lab09-github-actions-deploy-dev`

## 3. Incorporar los workflows al repositorio

Desde la raíz del repo:

```powershell
git status
git add iam/
git commit -m "feat(iam): separate terraform plan and deploy permissions"

git add .github/workflows/terraform-apply.yml .github/workflows/terraform-destroy.yml README-SETUP.md
git commit -m "feat(ci): add manual terraform apply and destroy workflows"

git push origin development
```

Los archivos de workflow manual deben estar también en la rama predeterminada para que GitHub habilite `workflow_dispatch`. 

## 4. Ejecutar

- Apply: Actions → `Terraform Apply - Dev` → Run workflow → branch `development` → confirmation `APPLY`.
- Destroy: Actions → `Terraform Destroy - Dev` → Run workflow → branch `development` → confirmation `DESTROY`.

Los pasos muestran el plan en el resumen del workflow y, en el mismo run, aplican inmediatamente el plan guardado. La confirmación manual `APPLY` o `DESTROY` se da al iniciar el workflow. El propio run solo admite la rama `development`.
