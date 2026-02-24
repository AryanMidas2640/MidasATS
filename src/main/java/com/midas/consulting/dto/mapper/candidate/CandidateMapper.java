package com.midas.consulting.dto.mapper.candidate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequestMidas;
import com.midas.consulting.model.candidate.CandidateMidas;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class CandidateMapper {
    public static CandidateMidas toCandidate(CreateCandidateRequestMidas createCandidateRequestMidas) {
        return new CandidateMidas()
                .setId(createCandidateRequestMidas.getId())
                .setCertifications(createCandidateRequestMidas.getCertifications())
                .setCurrentCTC(createCandidateRequestMidas.getCurrentCTC())
                .setName(createCandidateRequestMidas.getName())
                .setDegree(createCandidateRequestMidas.getDegree())
//                .setFullText(createCandidateRequestMidas.getFullText())

//                .setChangeLog(createCandidateRequestMidas.getChangeLog())
                .setDesignation(Collections.singletonList(createCandidateRequestMidas.getDesignation()))
                .setEmail(createCandidateRequestMidas.getEmail())
                .setGender(createCandidateRequestMidas.getGender())
                .setDate_added(createCandidateRequestMidas.getDate_added())
                .setLicenses(createCandidateRequestMidas.getLicenses())
                .setMunicipality(createCandidateRequestMidas.getMunicipality())
                .setPhone(createCandidateRequestMidas.getPhone())
                .setSkills(createCandidateRequestMidas.getSkills())
                .setCompaniesWorkedAt(createCandidateRequestMidas.getCompaniesWorkedAt())
                .setDateOfBirth(createCandidateRequestMidas.getDateOfBirth())
                .setRegions(createCandidateRequestMidas.getRegions())
                .setFileHandle(createCandidateRequestMidas.getFileHandle())
                .setDesiredShifts(createCandidateRequestMidas.getDesiredShifts())
                .setLast_updated(createCandidateRequestMidas.getLast_updated())
                .setTotalExp(createCandidateRequestMidas.getTotalExp())
                .setProfession(createCandidateRequestMidas.getProfession())
                .setPrimarySpeciality(createCandidateRequestMidas.getPrimarySpeciality())
                .setUniversity(createCandidateRequestMidas.getUniversity())
                .setEducation(createCandidateRequestMidas.getEducation())
                .setExperience(createCandidateRequestMidas.getExperience())
                .setWorkAuthorization(createCandidateRequestMidas.getWorkAuthorization())
                .setOtherPhone(createCandidateRequestMidas.getOtherPhone())
                .setAdditionalProperties(createCandidateRequestMidas.getAdditionalProperties());

    }

    public static CandidateMidas mapToCandidate(JsonNode parsedJson) {
        ObjectMapper mapper = new ObjectMapper();
        CandidateMidas candidate = new CandidateMidas();
        Map<String, Object> additionalProperties = new LinkedHashMap<>();
        parsedJson= parsedJson.get("parsed");
        parsedJson.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            switch (key) {
                case "languages":
                    candidate.setLanguages(mapper.convertValue(value, List.class));
                    break;
                case "linkedin":
                    candidate.setLinkedin(value.asText());
                    break;
                case "education":
                    candidate.setEducation(mapper.convertValue(value, List.class));
                    break;
                case "experience":
                    candidate.setExperience(mapper.convertValue(value, List.class));
                    break;
                case "resume_segments":
                    candidate.setResumeSegments(mapper.convertValue(value, Map.class));
                    break;
                case "summary":
                    candidate.setSummary(value.asText());
                    break;
                case "website":
                    candidate.setWebsite(value.asText());
                    break;
                case "designition":
                    candidate.setDesignation(mapper.convertValue(value, List.class));
                    break;


                case "email":
                    candidate.setEmail(value.asText());
                    break;
                case "phone":
                    candidate.setPhone(value.asText());
                    break;
                case "name":
                    candidate.setName(value.asText());
                    break;
                case "city":
                    candidate.setCity(value.asText());
                    break;
                case "state":
                    candidate.setState(value.asText());
                    break;
                case "zip":
                    candidate.setZip(value.asText());
                    break;
                case "skills":
                    candidate.setSkills(mapper.convertValue(value, List.class));
                    break;
                case "degree":
                    candidate.setDegree(mapper.convertValue(value, List.class));
                    break;
                case "certifications":
                    candidate.setCertifications(mapper.convertValue(value, List.class));
                    break;
                case "companies_worked_at":
                    candidate.setCompaniesWorkedAt(mapper.convertValue(value, List.class));
                    break;
                case "university":
                    candidate.setUniversity(mapper.convertValue(value, List.class));

                    break;
                case "full_text":
                    candidate.setFullText(value.asText());
                    break;
                case "total_experience":
                    candidate.setTotalExp(String.valueOf(value.asInt()));
                    break;
                case "address":
                    JsonNode address = value;
                    if (address.has("city")) candidate.setCity(address.get("city").asText());
                    if (address.has("state")) candidate.setState(address.get("state").asText());
                    if (address.has("zip")) candidate.setZip(address.get("zip").asText());
                    break;
                default:
                    additionalProperties.put(key, mapper.convertValue(value, Object.class));
            }
        });

        candidate.setAdditionalProperties(additionalProperties);
        candidate.setDate_added(new Date());
        return candidate;
    }

    public static CreateCandidateRequestMidas toCandidateRequest(CandidateMidas createCandidateRequestMidas) {
        return new CreateCandidateRequestMidas()
                .setId(createCandidateRequestMidas.getId())
                .setCertifications(createCandidateRequestMidas.getCertifications())
                .setCurrentCTC(createCandidateRequestMidas.getCurrentCTC())
                .setName(createCandidateRequestMidas.getName())
                .setDegree(createCandidateRequestMidas.getDegree())
//                .setChangeLog(createCandidateRequestMidas.getChangeLog())
//                .setDesignation(createCandidateRequestMidas.getDesignation())
                .setEmail(createCandidateRequestMidas.getEmail())
                .setGender(createCandidateRequestMidas.getGender())
                .setDate_added(createCandidateRequestMidas.getDate_added())
                .setLicenses(createCandidateRequestMidas.getLicenses())
                .setMunicipality(createCandidateRequestMidas.getMunicipality())
                .setPhone(createCandidateRequestMidas.getPhone())
                .setSkills(createCandidateRequestMidas.getSkills())
                .setCompaniesWorkedAt(createCandidateRequestMidas.getCompaniesWorkedAt())
                .setDateOfBirth(createCandidateRequestMidas.getDateOfBirth())
                .setRegions(createCandidateRequestMidas.getRegions())
                .setFileHandle(createCandidateRequestMidas.getFileHandle())
                .setDesiredShifts(createCandidateRequestMidas.getDesiredShifts())
                .setLast_updated(createCandidateRequestMidas.getLast_updated())
                .setTotalExp(createCandidateRequestMidas.getTotalExp())
                .setProfession(createCandidateRequestMidas.getProfession())
                .setPrimarySpeciality(createCandidateRequestMidas.getPrimarySpeciality())
                .setUniversity(createCandidateRequestMidas.getUniversity())
                .setWorkAuthorization(createCandidateRequestMidas.getWorkAuthorization())
                .setOtherPhone(createCandidateRequestMidas.getOtherPhone())
                .setAdditionalProperties(createCandidateRequestMidas.getAdditionalProperties());

    }
}