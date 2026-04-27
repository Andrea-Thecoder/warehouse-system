package it.warehouse.transport.api;

import it.warehouse.transport.dto.PagedResultDTO;
import it.warehouse.transport.dto.city.BaseDetailCityDTO;
import it.warehouse.transport.dto.search.CitySearchRequest;
import it.warehouse.transport.dto.search.LocationSearchRequest;
import it.warehouse.transport.dto.search.SearchRequest;
import it.warehouse.transport.service.CityService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "API Cities")
@Path("cities")
@Slf4j
public class CityResource {


    @Inject
    CityService cityService;

    @GET
    @Operation(
            summary = "Find Cities.",
            description = "API find all Cities on database. Can apply advanced filter by region ID and Cities name or istat code."
    )
    public PagedResultDTO<BaseDetailCityDTO> findAllCities (
            @BeanParam @Valid CitySearchRequest request
    ){
        log.info("CityResource - findAllCities");
        return cityService.findCities(request);
    }





}
