package com.midas.consulting.controller.v1.request.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Certification{
    public String name;
    public String issuer;
    public String date_issued;
    public String expiration_date;
}