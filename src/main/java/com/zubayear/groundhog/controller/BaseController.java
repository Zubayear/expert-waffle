package com.zubayear.groundhog.controller;

import com.zubayear.groundhog.entity.Organisation;
import com.zubayear.groundhog.entity.Requirement;
import com.zubayear.groundhog.entity.Scholarship;
import com.zubayear.groundhog.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.SQL;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class BaseController {
    private final R2dbcEntityOperations entityOperations;

    @Autowired
    public BaseController(R2dbcEntityOperations entityOperations) {
        this.entityOperations = entityOperations;
    }

    @QueryMapping
    public Flux<Scholarship> scholarships(@Argument ScholarshipInput scholarshipInput) {
        Criteria criteria = Criteria.empty();

        Sort sort = Sort.unsorted();
        if (scholarshipInput != null) {

            sort = createSortFromSortInput(scholarshipInput.getSorts());

            if (StringUtils.isNotBlank(scholarshipInput.getTitle()))
                criteria = criteria.and(Criteria.where("title").like("%" + scholarshipInput.getTitle().toLowerCase() + "%").ignoreCase(true));

            if (scholarshipInput.getTotal() != null && scholarshipInput.getTotal() >= 0)
                criteria = criteria.and(Criteria.where("total").lessThanOrEquals(scholarshipInput.getTotal()));
            if (scholarshipInput.getLastDate() != null && scholarshipInput.getLastDate().isAfter(LocalDate.now()))
                criteria = criteria.and(Criteria.where("last_date").lessThanOrEquals(scholarshipInput.getLastDate()));
            if (scholarshipInput.getDaysLeft() != null && scholarshipInput.getDaysLeft() >= 0)
                criteria = criteria.and(Criteria.where("days_left").lessThanOrEquals(scholarshipInput.getDaysLeft()));
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
                        if (SortDirection.DESCENDING.equals(sortInput.getDirection())) {
                            direction = Sort.Direction.DESC;
                        }
                        return new Sort.Order(direction, sortInput.getField());
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
                .map(Scholarship::getId)
                .collect(Collectors.toList());

        // Query requirements based on scholarship IDs
        return entityOperations.select(Requirement.class)
                .matching(Query.query(Criteria.where("scholarships_id").in(scholarshipIds)))
                .all()
                .collectList()
                .map(requirementList -> {
                    // Populate the response map for each scholarship
                    Map<Scholarship, List<Requirement>> res = new HashMap<>();
                    scholarships.forEach(s -> res.put(s, requirementList.stream().filter(x -> x.getScholarshipsId().equals(s.getId())).toList()));
                    return res;
                });
    }

    @MutationMapping
    public Mono<Scholarship> createScholarship(@Argument CreateScholarshipInput input) {
        var sch = Scholarship.builder()
                .id(UUID.randomUUID())
                .title(input.getTitle())
                .description(input.getDescription())
                .purpose(input.getPurpose())
                .daysLeft(input.getDaysLeft())
                .total(input.getTotal())
                .lastDate(LocalDate.now())
                .organisationsId(input.getOrganisationsId())
                .build();
        return entityOperations.insert(sch);
    }

    @MutationMapping
    public Mono<Organisation> createOrganisation(@Argument CreateOrganisationInput input) {
        var org = Organisation.builder()
                .id(UUID.randomUUID())
                .description(input.getDescription())
                .founded(input.getFounded())
                .orgName(input.getOrgName())
                .originCountry(input.getOriginCountry())
                .build();
        return entityOperations.insert(org);
    }

    @MutationMapping
    public Mono<Requirement> createRequirement(@Argument CreateRequirementInput input) {
        var requirement = Requirement.builder()
                .id(UUID.randomUUID())
                .description(input.getDescription())
                .title(input.getTitle())
                .scholarshipsId(input.getScholarshipsId())
                .build();
        return entityOperations.insert(requirement);
    }

    @QueryMapping
    public Flux<Requirement> requirements(@Argument RequirementInput requirementInput) {
        Criteria criteria = Criteria.empty();
        if (requirementInput != null && StringUtils.isNotBlank(requirementInput.getTitle()))
            criteria = Criteria.where("title").like(requirementInput.getTitle());
        return entityOperations.select(Requirement.class)
                .matching(Query.query(criteria))
                .all();
    }

    @QueryMapping
    public Flux<Organisation> organisations(@Argument OrganisationInput organisationInput) {
        Criteria criteria = Criteria.empty();
        if (organisationInput != null) {
            if (StringUtils.isNotBlank(organisationInput.getOrgName()))
                criteria = criteria.and(Criteria.where("org_name").like("%" + organisationInput.getOrgName().toLowerCase() + "%").ignoreCase(true));
//                criteria = Criteria.where("org_name").like(organisationInput.getOrgName().trim());
            if (StringUtils.isNotBlank(organisationInput.getOriginCountry()))
                criteria = criteria.and(Criteria.where("origin_country").like("%" + organisationInput.getOriginCountry().toLowerCase() + "%").ignoreCase(true));
//                criteria = criteria.and(Criteria.where("origin_country").like(organisationInput.getOriginCountry().trim()));
            if (organisationInput.getFounded() != null)
                criteria = criteria.and(Criteria.where("founded").lessThanOrEquals(organisationInput.getFounded()));
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

    @QueryMapping
    public Mono<Requirement> requirementById(@Argument String id) {
        Criteria criteria = Criteria.empty();
        if (id != null) criteria = Criteria.where("id").is(id);
        return entityOperations.select(Requirement.class)
                .matching(Query.query(criteria)).first();
    }

    @BatchMapping(field = "scholarships", typeName = "Organisation")
    public Mono<Map<Organisation, List<Scholarship>>> scholarshipsBatch(List<Organisation> organisations) {
        log.info("Fetching scholarships for all requirements");

        var orgIds = organisations.stream()
                .map(Organisation::getId)
                .toList();

        // Query requirements based on scholarship IDs
        return entityOperations.select(Scholarship.class)
                .matching(Query.query(Criteria.where("organisations_id").in(orgIds)))
                .all()
                .collectList()
                .map(x -> {
                    // Populate the response map for each scholarship
                    Map<Organisation, List<Scholarship>> res = new HashMap<>();
                    organisations.forEach(o -> res.put(o, x.stream().filter(s -> s.getOrganisationsId().equals(o.getId())).toList()));
                    return res;
                });
    }
}
