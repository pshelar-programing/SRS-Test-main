package com.srs.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.annotation.Resource;
import javax.sql.DataSource;

import com.amazonaws.services.secretsmanager.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DataSourceConfiguration {

	@Resource
	private Environment env;

	private static final Logger log = LoggerFactory.getLogger(DataSourceConfiguration.class);

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties appDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	public DataSource appDataSource() {

		String secretName = env.getProperty("spring.aws.secretsmanager.secretName");
		String endpoint = env.getProperty("spring.aws.secretsmanager.endpoint");
		String region = env.getProperty("spring.aws.secretsmanager.region");

		AWSSecretsManager client  = AWSSecretsManagerClientBuilder. standard ()
				. withRegion (region)
				. build ();


		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode secretsJson = null;

		String secret, decodedBinarySecret;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
				. withSecretId (secretName);

		GetSecretValueResult getSecretValueResponse = null;
		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

		} catch(ResourceNotFoundException e) {
			log.error("The requested secret " + secretName + " was not found");
		} catch (InvalidRequestException e) {
			log.error("The request was invalid due to: " + e.getMessage());
		} catch (InvalidParameterException e) {
			log.error("The request had invalid params: " + e.getMessage());
		}

		if(getSecretValueResponse == null) {
			return null;
		}

		// Decrypted secret using the associated KMS CMK
		// Depending on whether the secret was a string or binary, one of these fields will be populated
		 secret = getSecretValueResponse.getSecretString();
		if(secret == null) {
			log.error("The Secret String returned is null");
			return null;
		}
			try {
				secretsJson = objectMapper.readTree(secret);
			} catch (IOException e) {
				log.error("Exception while retreiving secret values: " + e.getMessage());
			}
		

		System.out.println("Secrets json - "+secretsJson);
		String host = secretsJson.get("host").textValue();
		String port = secretsJson.get("port").textValue();
		String dbname = secretsJson.get("dbname").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();
		appDataSourceProperties().setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbname);
		appDataSourceProperties().setUsername(username);
		appDataSourceProperties().setPassword(password);

        return appDataSourceProperties().initializeDataSourceBuilder().build();
    }


		/*String secretName = "dev-mysql";
		String region = "us-east-2";




		// Create a Secrets Manager client
		AWSSecretsManager client  = AWSSecretsManagerClientBuilder. standard ()
				. withRegion (region)
				. build ();

		// In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
		// See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
		// We rethrow the exception by default.

		String secret, decodedBinarySecret;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
				. withSecretId (secretName);
		GetSecretValueResult getSecretValueResult = null ;

		try {
			getSecretValueResult = client. getSecretValue (getSecretValueRequest);
		} catch (DecryptionFailureException e) {
			// Secrets Manager can't decrypt the protected secret text using the provided KMS key.
			// Deal with the exception here, and/or rethrow at your discretion.
			throw e;
		} catch (InternalServiceErrorException e) {
			// An error occurred on the server side.
			// Deal with the exception here, and/or rethrow at your discretion.
			throw e;
		} catch (InvalidParameterException e) {
			// You provided an invalid value for a parameter.
			// Deal with the exception here, and/or rethrow at your discretion.
			throw e;
		} catch (InvalidRequestException e) {
			// You provided a parameter value that is not valid for the current state of the resource.
			// Deal with the exception here, and/or rethrow at your discretion.
			throw e;
		} catch (ResourceNotFoundException e) {
			// We can't find the resource that you asked for.
			// Deal with the exception here, and/or rethrow at your discretion.
			throw e;
		}

		// Decrypts secret using the associated KMS CMK.
		// Depending on whether the secret is a string or binary, one of these fields will be populated.
		if (getSecretValueResult. getSecretString () != null ) {
			secret = getSecretValueResult. getSecretString ();
		}
		else {
			decodedBinarySecret = new String(Base64. getDecoder (). decode (getSecretValueResult. getSecretBinary ()). array ());
		}
	return null;
*/


}
