package client;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String connectionString;

    public ServerFacade(String connectionString) {
        this.connectionString = connectionString;
    }

    public AuthData register(UserData user) throws DataAccessException {
        var request = buildRequest("POST", "/user", user, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(connectionString + path))
                .method(method, makeRequestBody(body));

        if(body != null) {
            builder.setHeader("Content-Type", "application/json");
        }

        if(authToken != null) {
            builder.setHeader("authorization", authToken);
        }

        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if(request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

}
