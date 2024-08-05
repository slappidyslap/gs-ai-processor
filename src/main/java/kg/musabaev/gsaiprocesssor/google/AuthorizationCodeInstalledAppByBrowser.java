package kg.musabaev.seogooglesheetshelper.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import javafx.scene.text.Text;
import kg.musabaev.seogooglesheetshelper.BrowserUtil;
import kg.musabaev.seogooglesheetshelper.gui.component.SimpleHyperlink;
import kg.musabaev.seogooglesheetshelper.gui.dialog.WarningAlert;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AuthorizationCodeInstalledAppByBrowser extends AuthorizationCodeInstalledApp {

	private final AuthorizationCodeFlow flow;
	private final VerificationCodeReceiver receiver;

	public AuthorizationCodeInstalledAppByBrowser(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
		super(flow, receiver);
		this.flow = flow;
		this.receiver = receiver;
	}

	@Override
	public Credential authorize(String userId) throws IOException {
		try {
			Credential credential = flow.loadCredential(userId);
			if (credential != null
					&& (credential.getRefreshToken() != null
					|| credential.getExpiresInSeconds() == null
					|| credential.getExpiresInSeconds() > 60)) {
				return credential;
			}
			// open in browser
			String redirectUri = receiver.getRedirectUri();
			String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
			log.info("Попытка открыть браузер...");
			if (BrowserUtil.open(url))
				log.info("Браузер открыт, ждем код...");
			else {
				log.info("Не удалось открыть браузер. Вызываем диалоговое окно...");
				var alert = new WarningAlert(
						new Text("Не удалось автоматически открыть браузер. Пожалуйста перейдите на "),
						new SimpleHyperlink("страницу авторизации, ", url),
						new Text("чтобы продолжить")
				);
				alert.showAndWait();
			}
			// receive authorization code and exchange it for an access token
			String code = receiver.waitForCode();
			TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
			// store credential and return it
			return flow.createAndStoreCredential(response, userId);
		} finally {
			receiver.stop();
		}
	}
}
