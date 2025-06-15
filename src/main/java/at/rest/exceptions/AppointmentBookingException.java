package at.rest.exceptions;

public class AppointmentBookingException extends RuntimeException {
    public AppointmentBookingException(String message) {
        super(message);
    }
}
