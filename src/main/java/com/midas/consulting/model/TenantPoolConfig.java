package com.midas.consulting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class TenantPoolConfig {
    private int maxPoolSize = 50;
    private int minPoolSize = 10;
    private int maxWaitTimeMs = 3000;
    private int maxConnectionIdleTimeMs = 60000;
    private int maxConnectionLifeTimeMs = 1800000;
}