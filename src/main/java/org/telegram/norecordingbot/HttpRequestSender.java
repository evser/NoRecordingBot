package org.telegram.norecordingbot;

import at.mukprojects.giphy4j.dao.RequestSender;
import at.mukprojects.giphy4j.http.Request;
import at.mukprojects.giphy4j.http.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestSender implements RequestSender {

	@Override
	public Response sendRequest(Request request) throws IOException {

		HttpURLConnection connection = createConnection(request);
		Response response = send(connection);

		return response;
	}

	private HttpURLConnection createConnection(Request request) throws IOException {
		URL url = new URL(request.getUrl());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(Giphy4JConstants.TIMEOUT);
		connection.setReadTimeout(Giphy4JConstants.TIMEOUT);

		connection.setRequestMethod(Giphy4JConstants.SEND_METHOD);

		return connection;
	}

	private Response send(HttpURLConnection connection) throws IOException {
		connection.connect();

		if (connection.getResponseCode() != 200) {
			throw new IOException("Bad response! Code: " + connection.getResponseCode());
		}

		Map<String, String> headers = new HashMap<>();
		for (String key : connection.getHeaderFields().keySet()) {
			headers.put(key, connection.getHeaderFields().get(key).get(0));
		}

		String body = null;
		InputStream inputStream = null;

		try {
			inputStream = connection.getInputStream();

			String encoding = connection.getContentEncoding();
			encoding = encoding == null ? Giphy4JConstants.ENCODING : encoding;

			body = IOUtils.toString(inputStream, encoding);
		} catch (IOException e) {
			throw new IOException(e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		if (body == null) {
			throw new IOException("Unparseable response body! \n {" + body + "}");
		}

		Response response = new Response(headers, body);

		return response;
	}

}
