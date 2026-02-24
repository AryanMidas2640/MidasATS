package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class EmailInfo{
    @JsonProperty("EmailTo")
private String emailTo;
    @JsonProperty("EmailBody") 
private String emailBody;
    @JsonProperty("EmailReplyTo") 
private String emailReplyTo;
    @JsonProperty("EmailSignature") 
private String emailSignature;
    @JsonProperty("EmailFrom") 
private String emailFrom;
    @JsonProperty("EmailSubject") 
private String emailSubject;
    @JsonProperty("EmailCC") 
private String emailCC;
}
