package it.warehouse.administrator.service;


import io.ebean.Database;
import it.warehouse.administrator.model.RoleType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class LookupService {

    @Inject
    Database db;

    @Getter
    private Map<String, String> rolesMap = new HashMap<>();

    @PostConstruct
    public void init() {
        this.rolesMap = getLabelForRoles();
    }

    public List<RoleType> findRoles() {
        return db.find(RoleType.class).orderBy("label ASC").findList();
    }


    public List<RoleType> getRoleTypesForRegistration(List<String> roleTypeids) {
        return db.find(RoleType.class).where().idIn(roleTypeids).findList();
    }

    private Map<String, String> getLabelForRoles() {
        return db.find(RoleType.class)
                .orderBy("label ASC")
                .findList()
                .stream()
                .collect(Collectors.toMap(
                        RoleType::getId,
                        RoleType::getLabel
                ));
    }
}
