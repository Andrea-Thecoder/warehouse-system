package it.warehouse.transport.service;


import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import it.warehouse.transport.dto.PagedResultDTO;
import it.warehouse.transport.dto.city.BaseDetailCityDTO;
import it.warehouse.transport.dto.search.CitySearchRequest;
import it.warehouse.transport.dto.search.LocationSearchRequest;
import it.warehouse.transport.dto.search.SearchRequest;
import it.warehouse.transport.model.City;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CityService {

    @Inject
    Database db;


    private static final String DEFAULT_SORT = "name ASC, istat_code ASC";

    public PagedResultDTO<BaseDetailCityDTO> findCities (CitySearchRequest request){
        log.info("findCities: Starting find cities list");
        ExpressionList<City> query = db.find(City.class)
                .setLabel("FindCities")
                .where();
        request.filterBuilder(query);

        request.pagination(query,DEFAULT_SORT);

        PagedList<City> list = query.findPagedList();
        return PagedResultDTO.of(list,BaseDetailCityDTO::of);
    }
}
