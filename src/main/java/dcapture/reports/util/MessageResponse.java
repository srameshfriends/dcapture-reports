package dcapture.reports.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

@Data
public class MessageResponse {
    @JsonProperty("status_code")
    @ToString.Include(name = "status_code")
    private int statusCode;
    private Date timestamp;
    private String message;
    private String description;

    public static ResponseEntity<MessageResponse> ok(String messageCode, String description) {
        MessageResponse jms = new MessageResponse();
        jms.setStatusCode(HttpStatus.OK.value());
        jms.setTimestamp(new Date());
        jms.setMessage(messageCode);
        jms.setDescription(description);
        return ResponseEntity.ok(jms);
    }

    public static ResponseEntity<MessageResponse> ok(LocaleMessage localeMessage, String messageCode, Object... args) {
        return ok(messageCode, localeMessage.getMessage(messageCode, args));
    }

    public static ResponseEntity<MessageResponse> badRequest(String messageCode, String description) {
        MessageResponse jms = new MessageResponse();
        jms.setStatusCode(HttpStatus.BAD_REQUEST.value());
        jms.setTimestamp(new Date());
        jms.setMessage(messageCode);
        jms.setDescription(description);
        return ResponseEntity.badRequest().body(jms);
    }

    public static ResponseEntity<MessageResponse> badRequest(LocaleMessage localeMessage, String messageCode, Object... args) {
        return badRequest(messageCode, localeMessage.getMessage(messageCode, args));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
