package exception;

public class ResponseException extends Exception{
    private final int statusCode;

    public ResponseException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
