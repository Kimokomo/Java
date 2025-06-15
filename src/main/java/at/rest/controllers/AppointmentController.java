package at.rest.controllers;

import at.rest.dtos.AdminAppointmentDto;
import at.rest.dtos.AppointmentDTO;
import at.rest.services.appointment.AdminAppointmentService;
import at.rest.services.appointment.AppointmentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDate;
import java.util.List;

@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppointmentController {

    @Context
    private SecurityContext securityContext;

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private AdminAppointmentService adminAppointmentService;

    @GET
    public Response getAppointments(@QueryParam("date") String dateStr) {
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        List<AppointmentDTO> appointmentDTOs = appointmentService.findAppointmentDTOsByDate(date);
        return Response.ok().entity(appointmentDTOs).build();
    }

    @POST
    public Response bookAppointment(AppointmentDTO appointmentDto) {
        String username = securityContext.getUserPrincipal().getName();
        AppointmentDTO updated = appointmentService.bookAppointment(username, appointmentDto);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/admin/appointment-overview")
    public List<AdminAppointmentDto> getAllAppointments() {
        return adminAppointmentService.getAllAppointments();
    }
}
