package com.thufir.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "hashurls")
public class HashUrl {

    @Id
    public String id;

    @Indexed
    private String hash;

    private String url;

    public HashUrl(String hash, String url) {
        this.hash = hash;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, "id");
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, url);
    }
}
