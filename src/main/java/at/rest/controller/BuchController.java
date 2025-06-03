package at.rest.controller;

import at.rest.dtos.BuchDTO;
import at.rest.dtos.BuchPageDTO;
import at.rest.servcie.BuchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
public class BuchController {


    @Inject
    private BuchService buchService;

    @GET
    public List<BuchDTO> getBooksList() {
        return buchService.getAllBooks();
    }

    @POST
    public void addBook(BuchDTO buch) {
        buchService.saveBook(buch);
    }

    @PUT
    @Path("/{id}")
    public Response updateBook(@PathParam("id") Long id, BuchDTO buch) {
        buchService.updateBook(id, buch);
        return Response.noContent().build();  // 204 No Content bei Erfolg
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") Long id) {
        buchService.deleteBookById(id);
        return Response.noContent().build();  // 204 No Content bei Erfolg
    }

    @GET
    @Path("/paginated/{page}/{size}")
    public BuchPageDTO getBooksPaginatedWithCount(
            @PathParam("page") int page,
            @PathParam("size") int size) {
        return buchService.getBooksPaginatedWithCount(page, size);
    }
}




