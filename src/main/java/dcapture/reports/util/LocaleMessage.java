package dcapture.reports.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Locale;

@Component
public class LocaleMessage {
    private final MessageSource messageSource;

    @Autowired
    public LocaleMessage(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public String getMessage(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    public ResponseEntity<MessageResponse> badRequest(String messageCode, Object... args) {
        MessageResponse jms = new MessageResponse();
        jms.setStatusCode(HttpStatus.BAD_REQUEST.value());
        jms.setTimestamp(new Date());
        jms.setMessage(messageCode);
        jms.setDescription(getMessage(messageCode, args));
        return ResponseEntity.badRequest().body(jms);
    }

    public ResponseEntity<MessageResponse> ok(String messageCode, Object... args) {
        MessageResponse jms = new MessageResponse();
        jms.setStatusCode(HttpStatus.OK.value());
        jms.setTimestamp(new Date());
        jms.setMessage(messageCode);
        jms.setDescription(getMessage(messageCode, args));
        return ResponseEntity.ok(jms);
    }
}
