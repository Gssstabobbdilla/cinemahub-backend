data "archive_file" "lambda_empty" {
  type             = "zip"
  output_path      = "${path.module}/.terraform/lambda-empty-handler.zip"
  output_file_mode = "0666"

  source {
    content  = "exports.handler = async () => ({ statusCode: 204, body: \"\" });"
    filename = "index.js"
  }
}
