package it.io.openliberty.guides.query;

// imports for a JAXRS client to simplify the code
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
// logger imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// testcontainers imports
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import jakarta.ws.rs.client.ClientBuilder;
// simple import to build a URI/URL
import jakarta.ws.rs.core.UriBuilder;

public class LibertyContainer extends GenericContainer<LibertyContainer> {

    static final Logger LOGGER = LoggerFactory.getLogger(LibertyContainer.class);

    private String baseURL;

    public static String getProtocol() {
        return System.getProperty("test.protocol", "http");
    }

    public static boolean testHttps() {
        return getProtocol().equalsIgnoreCase("http");
    }

    public LibertyContainer(final String dockerImageName) {
        super(dockerImageName);
        // wait for smarter planet message by default
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));
        init();
    }

    public <T> T createRestClient(Class<T> clazz) {
        String urlPath = getBaseURL();
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(clazz);
    }

    public String getBaseURL() throws IllegalStateException {
        if (baseURL != null) {
            return baseURL;
        }
        if (!this.isRunning()) {
            throw new IllegalStateException(
                "Container must be running to determine hostname and port");
        }
        baseURL =  "http://" + this.getContainerIpAddress()
            + ":" + this.getFirstMappedPort();
        System.out.println("TEST: " + baseURL);
        return baseURL;
    }

    private void init() {
        this.addExposedPorts(9084);
        return;
    }
}