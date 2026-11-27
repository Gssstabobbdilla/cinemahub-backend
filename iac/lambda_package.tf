locals {
  java_lambda_package_path = "${path.module}/../backend/target/cinemahub-0.0.1-SNAPSHOT-aws-lambda.jar"
  java_lambda_package_hash = fileexists(local.java_lambda_package_path) ? filebase64sha256(local.java_lambda_package_path) : null
}

data "archive_file" "lambda_empty" {
  type             = "zip"
  output_path      = "${path.module}/.terraform/lambda-empty-handler.zip"
  output_file_mode = "0666"

  source {
    content  = "exports.handler = async () => ({ statusCode: 204, body: \"\" });"
    filename = "index.js"
  }
}
