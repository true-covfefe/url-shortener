package com.thufir.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class TestUtils {

    public static ImmutableList<String> getSampleUrls() {
        return ImmutableList.of("https://www.google.com", "https://www.yahoo.com",
            "http://www.bbc.co.uk/science/space/solarsystem/sun_and_planets/earth", "http://berserk.wikia.com/wiki/Berserk_(2016_Anime)");
    }

    public static String hasher(String url) {
        return Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
    }
}
