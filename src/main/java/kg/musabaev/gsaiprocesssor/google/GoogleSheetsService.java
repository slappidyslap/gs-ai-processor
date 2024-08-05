package kg.musabaev.seogooglesheetshelper.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
public class GoogleSheetsService {
	private static final String APPLICATION_NAME = "SEO Google Sheets Helper";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final Path APP_PATH = Path.of(System.getProperty("user.home"), ".seo-google-sheets-helper");
	private static final Path TOKENS_DIRECTORY_PATH = APP_PATH.resolve("tokens");
	private static final String CREDENTIALS_FILE_PATH = "cred.json";

	/*
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS);

	private final NetHttpTransport HTTP_TRANSPORT;

	public GoogleSheetsService(MainFormModel mainFormModel) throws GeneralSecurityException, IOException {
		this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the cred.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
			throws IOException {
		// Load client secrets.
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);

        GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIRECTORY_PATH.toFile()))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(-1).build();
		return new AuthorizationCodeInstalledAppByBrowser(flow, receiver).authorize("user");// TODO dialog browser

	}

	public Sheets getSheets() throws IOException {
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public void updateValues(Sheets sheets, String spreadsheetsId, String range, List<List<Object>> values) throws IOException {
		ValueRange body = new ValueRange().setValues(values);
		sheets.spreadsheets().values()
				.update(spreadsheetsId, range, body)
				.setValueInputOption("RAW")
				.execute();
	}
}
