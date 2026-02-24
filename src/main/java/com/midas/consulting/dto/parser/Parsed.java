        package com.midas.consulting.dto.parser;

        import com.fasterxml.jackson.annotation.*;

        import javax.annotation.Generated;
        import java.util.LinkedHashMap;
        import java.util.List;
        import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "companies-worked-at",
        "degree",
        "designition",
        "email",
        "name",
        "phone",
        "skills",
        "total_exp",
        "university"
})
@Generated("jsonschema2pojo")
public class Parsed {

    @JsonProperty("companies-worked-at")
    private List<String> companiesWorkedAt;
    @JsonProperty("degree")
    private List<Object> degree;
    @JsonProperty("designition")
    private List<String> designition;
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("skills")
    private List<String> skills;
    @JsonProperty("total_exp")
    private Integer totalExp;
    @JsonProperty("university")
    private List<Object> university;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("companies-worked-at")
    public List<String> getCompaniesWorkedAt() {
        return companiesWorkedAt;
    }

    @JsonProperty("companies-worked-at")
    public void setCompaniesWorkedAt(List<String> companiesWorkedAt) {
        this.companiesWorkedAt = companiesWorkedAt;
    }

    @JsonProperty("degree")
    public List<Object> getDegree() {
        return degree;
    }

    @JsonProperty("degree")
    public void setDegree(List<Object> degree) {
        this.degree = degree;
    }

    @JsonProperty("designition")
    public List<String> getDesignition() {
        return designition;
    }

    @JsonProperty("designition")
    public void setDesignition(List<String> designition) {
        this.designition = designition;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonProperty("skills")
    public List<String> getSkills() {
        return skills;
    }

    @JsonProperty("skills")
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    @JsonProperty("total_exp")
    public Integer getTotalExp() {
        return totalExp;
    }

    @JsonProperty("total_exp")
    public void setTotalExp(Integer totalExp) {
        this.totalExp = totalExp;
    }

    @JsonProperty("university")
    public List<Object> getUniversity() {
        return university;
    }

    @JsonProperty("university")
    public void setUniversity(List<Object> university) {
        this.university = university;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}