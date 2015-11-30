package org.javameta.samples.meta;

import org.javameta.base.MetaEntity;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
@MetaEntity
public class SampleEntity {

    private String value;

    public SampleEntity() {
        this("default");
    }

    public SampleEntity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
