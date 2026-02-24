package com.midas.consulting.controller.v1.response.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Reference{
    public String name;
    public String position;
    public String company;
    public String contact_info;
}
