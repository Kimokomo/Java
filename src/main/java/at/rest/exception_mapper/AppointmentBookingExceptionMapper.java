package at.rest.exception_mapper;

import at.rest.exceptions.AppointmentBookingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class AppointmentBookingExceptionMapper implements ExceptionMapper<AppointmentBookingException> {

    @Override
    public Response toResponse(AppointmentBookingException e) {
        Map<String, String> response = Map.of("error", e.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(response)
                .build();
    }
}

