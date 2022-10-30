package dcapture.reports.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.Serial;

public class MessageException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 89467000L;
    private final HttpStatus httpStatus;
    private final Object[] arguments;
    private MediaType mediaType;

    public MessageException(String messageCode, Object... args) {
        this(HttpStatus.BAD_REQUEST, messageCode, args);
    }

    public MessageException(HttpStatus httpStatus, String messageCode, Object... args) {
        super(messageCode);
        this.httpStatus = httpStatus;
        this.arguments = args;
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    public MessageException(HttpStatus httpStatus, String messageCode, MediaType mediaType, Object[] args) {
        super(messageCode);
        this.httpStatus = httpStatus;
        this.arguments = args;
        this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
