package com.zubayear.groundhog.controller;

import com.zubayear.groundhog.model.Organisation;
import com.zubayear.groundhog.model.Requirement;
import com.zubayear.groundhog.model.Scholarship;
import com.zubayear.groundhog.model.ginput.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class BaseController {
    private final R2dbcEntityOperations entityOperations;

    @Autowired
    public BaseController(R2dbcEntityOperations entityOperations) {
        this.entityOperations = entityOperations;
    }

    /*Scholarship*/
    @QueryMapping
    public Flux<Scholarship> scholarships(@Argument ScholarshipInput scholarshipInput) {
        Criteria criteria = Criteria.empty();

        Sort sort = Sort.unsorted();
        if (scholarshipInput != null) {

            sort = createSortFromSortInput(scholarshipInput.sorts());

            if (StringUtils.isNotBlank(scholarshipInput.title()))
                criteria = criteria.and(Criteria.where("title").like("%" + scholarshipInput.title().toLowerCase() + "%").ignoreCase(true));

            if (scholarshipInput.total() != null && scholarshipInput.total() >= 0)
                criteria = criteria.and(Criteria.where("total").lessThanOrEquals(scholarshipInput.total()));
            if (scholarshipInput.lastDate() != null && scholarshipInput.lastDate().isAfter(LocalDate.now()))
                criteria = criteria.and(Criteria.where("last_date").lessThanOrEquals(scholarshipInput.lastDate()));
            if (scholarshipInput.daysLeft() != null && scholarshipInput.daysLeft() >= 0)
                criteria = criteria.and(Criteria.where("days_left").lessThanOrEquals(scholarshipInput.daysLeft()));
        }

        return entityOperations.select(Scholarship.class)
                .matching(Query.query(criteria).sort(sort))
                .all();
    }

    private Sort createSortFromSortInput(List<SortInput> sortInputs) {
        if (sortInputs != null && !sortInputs.isEmpty()) {
            List<Sort.Order> sortOrders = sortInputs.stream()
                    .map(sortInput -> {
                        Sort.Direction direction = Sort.Direction.ASC;
                        if (SortDirection.DESCENDING.equals(sortInput.direction())) {
                            direction = Sort.Direction.DESC;
                        }
                        return new Sort.Order(direction, sortInput.field());
                    })
                    .collect(Collectors.toList());
            return Sort.by(sortOrders);
        }
        return Sort.unsorted(); // Default: No sorting
    }

    @QueryMapping
    public Mono<Scholarship> scholarshipById(@Argument String id) {
        Criteria criteria = Criteria.empty();
        if (id != null) criteria = Criteria.where("id").is(id);
        return entityOperations.select(Scholarship.class)
                .matching(Query.query(criteria)).first();
    }

    @BatchMapping(field = "requirements", typeName = "Scholarship")
    public Mono<Map<Scholarship, List<Requirement>>> requirementsLoader(List<Scholarship> scholarships) {
        log.info("Fetching requirements for all scholarships");

        var scholarshipIds = scholarships.stream()
                .map(Scholarship::id)
                .collect(Collectors.toList());

        // Query requirements based on scholarship IDs
        return entityOperations.select(Requirement.class)
                .matching(Query.query(Criteria.where("scholarships_id").in(scholarshipIds)))
                .all()
                .collectList()
                .map(requirementList -> {
                    // Populate the response map for each scholarship
                    Map<Scholarship, List<Requirement>> res = new HashMap<>();
                    scholarships.forEach(s -> res.put(s, requirementList.stream().filter(x -> x.scholarships_id().equals(s.id())).toList()));
                    return res;
                });
    }

    @MutationMapping
    public Mono<Scholarship> createScholarship(@Argument CreateScholarshipInput input) {
        var sch = new Scholarship(null, input.title(), input.description(), input.purpose(), LocalDate.now(), input.days_left(), input.total(), input.organisations_id());
        return entityOperations.insert(sch);
    }

    @BatchMapping(field = "scholarships", typeName = "Organisation")
    public Mono<Map<Organisation, List<Scholarship>>> scholarshipsBatch(List<Organisation> organisations) {
        log.info("Fetching scholarships for all requirements");

        var orgIds = organisations.stream()
                .map(Organisation::id)
                .toList();

        // Query requirements based on scholarship IDs
        return entityOperations.select(Scholarship.class)
                .matching(Query.query(Criteria.where("organisations_id").in(orgIds)))
                .all()
                .collectList()
                .map(x -> {
                    // Populate the response map for each scholarship
                    Map<Organisation, List<Scholarship>> res = new HashMap<>();
                    organisations.forEach(o -> res.put(o, x.stream().filter(s -> s.organisations_id().equals(o.id())).toList()));
                    return res;
                });
    }

    /*Organisation*/
    @MutationMapping
    public Mono<Organisation> createOrganisation(@Argument CreateOrganisationInput input) {
        var org = new Organisation(null, input.org_name(), input.description(), input.founded(), input.origin_country());
        return entityOperations.insert(org);
    }

    @QueryMapping
    public Flux<Organisation> organisations(@Argument OrganisationInput organisationInput) {
        Criteria criteria = Criteria.empty();
        if (organisationInput != null) {
            if (StringUtils.isNotBlank(organisationInput.org_name()))
                criteria = criteria.and(Criteria.where("org_name").like("%" + organisationInput.org_name().toLowerCase() + "%").ignoreCase(true));
//                criteria = Criteria.where("org_name").like(organisationInput.getOrgName().trim());
            if (StringUtils.isNotBlank(organisationInput.origin_country()))
                criteria = criteria.and(Criteria.where("origin_country").like("%" + organisationInput.origin_country().toLowerCase() + "%").ignoreCase(true));
//                criteria = criteria.and(Criteria.where("origin_country").like(organisationInput.getOriginCountry().trim()));
            if (organisationInput.founded() != null)
                criteria = criteria.and(Criteria.where("founded").lessThanOrEquals(organisationInput.founded()));
        }

        return entityOperations.select(Organisation.class)
                .matching(Query.query(criteria))
                .all();
    }

    @QueryMapping
    public Mono<Organisation> organisationById(@Argument String id) {
        Criteria criteria = Criteria.empty();
        if (id != null) criteria = Criteria.where("id").is(id);
        return entityOperations.select(Organisation.class)
                .matching(Query.query(criteria)).first();
    }

    /*Requirement*/
    @QueryMapping
    public Mono<Requirement> requirementById(@Argument String id) {
        Criteria criteria = Criteria.empty();
        if (id != null) criteria = Criteria.where("id").is(id);
        return entityOperations.select(Requirement.class)
                .matching(Query.query(criteria)).first();
    }

    @MutationMapping
    public Mono<Requirement> createRequirement(@Argument CreateRequirementInput input) {
        var requirement = new Requirement(null, input.title(), input.description(), input.scholarships_id());
        return entityOperations.insert(requirement);
    }

    @QueryMapping
    public Flux<Requirement> requirements(@Argument RequirementInput requirementInput) {
        Criteria criteria = Criteria.empty();
        if (requirementInput != null && StringUtils.isNotBlank(requirementInput.title()))
            criteria = Criteria.where("title").like("%" + requirementInput.title().toLowerCase(Locale.ROOT) + "%").ignoreCase(true);
        return entityOperations.select(Requirement.class)
                .matching(Query.query(criteria))
                .all();
    }

}
