package indexcalculator.exception;

import org.springframework.http.HttpStatusCode;

public class IndexRuntimeException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public IndexRuntimeException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return super.getMessage();
    }
}
