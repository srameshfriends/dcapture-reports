package dcapture.reports.application;

import dcapture.reports.util.LocaleMessage;
import dcapture.reports.util.MessageException;
import dcapture.reports.util.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Component
public class ReportsExceptionHandler {
    private final LocaleMessage localeMessage;

    @Autowired
    public ReportsExceptionHandler(LocaleMessage localeMessage) {
        this.localeMessage = localeMessage;
    }

    @ExceptionHandler(value = MessageException.class)
    public @ResponseBody ResponseEntity<Object> exception(MessageException exception) {
        MessageResponse message = new MessageResponse();
        message.setStatusCode(exception.getHttpStatus().value());
        message.setMessage(exception.getMessage());
        message.setTimestamp(new Date());
        message.setDescription(localeMessage.getMessage(exception.getMessage(), exception.getArguments()));
        return ResponseEntity.status(exception.getHttpStatus()).headers(httpHeaders -> httpHeaders.setContentType(exception.getMediaType())).body(message);
    }
}
