package at.rest.resources;

import at.rest.dtos.BuchDTO;
import at.rest.servcie.BuchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuchResource {


    @Inject
    private BuchService buchService;

    @GET
    public List<BuchDTO> getUsersListdb() {
        return buchService.getAllBooks();
    }
}




