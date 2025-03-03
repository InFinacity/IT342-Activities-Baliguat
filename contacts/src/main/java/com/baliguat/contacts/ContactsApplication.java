package com.baliguat.contacts;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ContactsApplication {
	private static final String APPLICATION_NAME = "Google People API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Arrays.asList(PeopleServiceScopes.CONTACTS_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
			throws IOException {
		// Load client secrets.
		InputStream in = ContactsApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void getPersonInfo(Person person) {
		// Get names
		List<Name> names = person.getNames();
		if (names != null && !names.isEmpty()) {
			for (Name personName : names) {
				System.out.println("Name: " + personName.getDisplayName());
			}
		} else {
			System.out.println("No names found.");
		}

		// Get email addresses
		List<EmailAddress> emails = person.getEmailAddresses();
		if (emails != null && !emails.isEmpty()) {
			for (EmailAddress personEmail : emails) {
				System.out.println("Email: " + personEmail.getValue());
			}
		} else {
			System.out.println("No email addresses found.");
		}

		// Get phone numbers
		List<PhoneNumber> phones = person.getPhoneNumbers();
		if (phones != null && !phones.isEmpty()) {
			for (PhoneNumber personPhone : phones) {
				System.out.println("Phone number: " + personPhone.getValue());
			}
		} else {
			System.out.println("No phone numbers found.");
		}
	}

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			PeopleService service =
					new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
							.setApplicationName(APPLICATION_NAME)
							.build();

			// Request 120 connections.
			ListConnectionsResponse response = service.people().connections()
					.list("people/me")
					.setPageSize(120)
					// specify fields to be returned
					.setRequestMaskIncludeField("person.names,person.emailAddresses,person.phoneNumbers")
					.execute();

			// Display information about a person.
			List<Person> connections = response.getConnections();
			if (connections != null && !connections.isEmpty()) {
				for (Person person : connections) {
					getPersonInfo(person);
				}
			} else {
				System.out.println("No connections found.");
			}

			SpringApplication.run(ContactsApplication.class, args);

		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			System.out.println("Error occurred: " + e.getMessage());
		}
	}
}
