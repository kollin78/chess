package client;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import exception.ResponseException;


public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String connectionString;

    public ServerFacade(String connectionString) {
        this.connectionString = connectionString;
    }

    public AuthData register(UserData user) throws ResponseException {
        var request = buildRequest("POST", "/user", user, null);
        var response = sendRequest(request);

        return handleResponse(response, AuthData.class);
    }

    public AuthData login(LoginRequest request) throws ResponseException {
        var req = buildRequest("POST", "/session", request, null);
        var response = sendRequest(req);

        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);

        handleResponse(response, null);
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);

        return handleResponse(response, ListGamesResult.class);
    }

    public int createGame(CreateGameRequest gameRequest, String authToken) throws ResponseException {
        var request = buildRequest("POST", "/game", gameRequest, authToken);
        var response = sendRequest(request);

        return handleResponse(response, CreateGameResult.class).gameID();
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws ResponseException {
        var request = buildRequest("PUT", "/game", joinGameRequest, authToken);
        var response = sendRequest(request);

        handleResponse(response, null);
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

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if(!isSuccessful(status)) {
            var body = response.body();
            throw new ResponseException(status, "Error: " + body);
        }

        if ((responseClass != null) && (response.body() != null) && (!response.body().isEmpty())) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
