package com.gestrh.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/health")
public class HealthResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String ok() { return "{\"status\":\"ok\"}"; }
}
