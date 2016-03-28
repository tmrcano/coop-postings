package io.github.rhildred;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.*;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.io.File;




import javax.servlet.http.*;

public class Oauth2 {

	private WebClient conn = null;
	private String sKey = null, sSecretToken = null, sRedirect = null;

	public Oauth2() {
		this.conn = new WebClient();

		this.sKey = "Will be replaced from json";
		this.sSecretToken = "Will be replaced from json";
		try{
			ObjectMapper mapper = new ObjectMapper();

			// read JSON from a file
			Map<String, Object> map = mapper.readValue(
					new File(System.getProperty("user.dir") + "/../data/creds/google.json"),
					new TypeReference<Map<String, Object>>() {
					});

			this.sKey = (String) map.get("ClientID");
			this.sSecretToken = (String) map.get("ClientSecret");
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public void redirect(String sReturnUrl, HttpServletResponse res) throws IOException {
		this.sRedirect = sReturnUrl;
		if (!this.sRedirect.contains("localhost")) {
			String sPattern = ":\\d\\d*";
			this.sRedirect = this.sRedirect.replaceAll(sPattern, "");
		}
		String sAuthUrl = "https://accounts.google.com/o/oauth2/auth?redirect_uri=%s&client_id=%s&scope=https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email&response_type=code&max_auth_age=0";
		String sRedirectToGoogle = String.format(sAuthUrl, this.sRedirect,
				this.sKey);
		res.sendRedirect(sRedirectToGoogle);

	}

	public JSONObject handleCode(String sCode) throws IOException, ParseException
    {
		//step 5
		// then google has redirected to us so build up query for 2nd phase of authentication
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
		nameValuePairs.add(new BasicNameValuePair("client_id", this.sKey));
		nameValuePairs.add(new BasicNameValuePair("client_secret", this.sSecretToken));
		nameValuePairs.add(new BasicNameValuePair("code", sCode));
		nameValuePairs.add(new BasicNameValuePair("redirect_uri", this.sRedirect));

		JSONObject oResult = (JSONObject) this.conn.downloadJson("https://accounts.google.com/o/oauth2/token", nameValuePairs);
		String sAccessToken = (String)oResult.get("access_token");

		// step 7
		// now we can get the user info
		String sUserInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=%s";
		JSONObject oInfo = (JSONObject) conn.downloadJson(String.format(sUserInfoUrl, sAccessToken));
		return oInfo;
    }

	public JSONObject getCreds(HttpSession sess)
	{
		return (JSONObject)sess.getAttribute("creds");
	}

	public void close() {
		this.conn.dispose();
	}

}
