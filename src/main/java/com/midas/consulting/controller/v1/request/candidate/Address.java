package com.midas.consulting.controller.v1.request.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Address{
    public String street;
    public String city;
    public String state;
    public String zip;
    public String country;
}








