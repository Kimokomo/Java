package at.rest.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(CORSFilter.class.getName());

    private static final String DEFAULT_ENV = "dev";
    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("app.env", DEFAULT_ENV);
        logger.info("CORS ENV beim Start: " + env);
        try (InputStream input = CORSFilter.class.getClassLoader().getResourceAsStream("cors-" + env + ".properties")) {
            if (input != null) {
                props.load(input);
            } else {
                throw new RuntimeException("config file cors-" + env + ".properties nicht gefunden!!!!!");
            }
        } catch (IOException e) {
            logger.warning("Fehler beim Laden der CORS-Konfiguration");
            throw new RuntimeException("Failed to load CORS config", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        String allowedOrigin = props.getProperty("allowed.origin", "*");

        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
    }
}
