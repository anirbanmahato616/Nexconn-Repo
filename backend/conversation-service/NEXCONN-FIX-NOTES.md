RecoverX Conversation Service - Nexconn JSON compatibility fix

Changes:
1. Updated Jackson imports in NexconnService and NexconnWebhookController to Jackson 3
   (`tools.jackson.databind`), which is used by Spring Boot 4.
   The previous `com.fasterxml.jackson.databind.JsonNode` reference caused the
   HttpMessageConversionException observed in the application log.
2. Removed the hard-coded Nexconn App Secret from application.properties.
3. Credentials are now loaded from:
   NEXCONN_APP_KEY
   NEXCONN_APP_SECRET

Windows CMD (run in the same CMD window used to launch the service):
  set "NEXCONN_APP_KEY=your-app-key"
  set "NEXCONN_APP_SECRET=your-app-secret"
  mvnw.cmd spring-boot:run

Alternatively, configure the two variables in STS Run Configurations > Environment.
Stop the old service process and restart it after changing variables.

Token endpoint:
  POST http://localhost:8085/api/nexconn/token
  Authorization: Bearer <RecoverX JWT>
  No request body.

Note: Maven compilation could not be run in the preparation environment because Maven
was not installed and the Maven wrapper was not executable there. Import the project
into STS and allow Maven dependencies to resolve before testing.
