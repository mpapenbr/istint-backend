package de.mp.istint.server.util;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogRequestFilter extends AbstractRequestLoggingFilter {
    final String INTERNAL_STOP_WATCH = "X-InternalStopWatch";

    public LogRequestFilter() {
        this(1000);
    }

    public LogRequestFilter(int maxPayloadLength) {
        setIncludePayload(true);
        setMaxPayloadLength(maxPayloadLength);
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return log.isDebugEnabled();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        StopWatch sw = new StopWatch();
        sw.start();
        request.setAttribute(INTERNAL_STOP_WATCH, sw);
        log.debug(message);

    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        String duration = Optional.ofNullable(request.getAttribute(INTERNAL_STOP_WATCH)).map(o -> {
            StopWatch sw = (StopWatch) o;
            sw.stop();
            return sw.toString();
        }).orElse("n.a.");

        log.debug(message
                + (request.getContentLength() > getMaxPayloadLength() ? String.format("(%d of %d)", getMaxPayloadLength(), request.getContentLength()) : "")
                + " " + duration);
        request.removeAttribute(INTERNAL_STOP_WATCH);

    }

}
