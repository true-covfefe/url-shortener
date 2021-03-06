package com.thufir.repository;

import com.google.common.collect.ImmutableMap;
import com.thufir.UrlshortenerApplication;
import com.thufir.entity.HashUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.thufir.utils.TestUtils.getSampleUrls;
import static com.thufir.utils.TestUtils.hasher;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UrlshortenerApplication.class)
public class HashUrlRepositoryTest {

    @Autowired
    private HashUrlRepository repo;

    @Before
    public void before() {
        repo.deleteAll();
    }

    @Test
    public void testFindById() {
        HashUrl persisted = givenHashUrl();
        HashUrl found = repo.findOne(persisted.getId());
        assertEquals(found, persisted);
    }

    @Test
    public void testFindByHash() {
        HashUrl persisted = givenHashUrl();
        HashUrl found = repo.findByHash(persisted.getHash());
        assertEquals(found, persisted);
    }

    @Test
    public void save() throws Exception {
        assertEquals(0, repo.findAll().size());

        ImmutableMap<String, String> urlToHashMap = hashUrlMapBuilder(getSampleUrls());

        getSampleUrls().forEach(url -> {
            HashUrl hashUrl = new HashUrl(urlToHashMap.get(url), url);
            HashUrl saved = repo.save(hashUrl);

            HashUrl found = repo.findOne(saved.getId());
            assertEquals(saved.getId(), found.getId());
            assertEquals(saved, found);
            assertEquals(saved.getHash(), urlToHashMap.get(url));
        });

        assertEquals(4, repo.findAll().size());
    }

    private ImmutableMap<String, String> hashUrlMapBuilder(List<String> urls) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        urls.forEach(url -> builder.put(url, hasher(url)));
        return builder.build();
    }

    private HashUrl givenHashUrl() {
        return repo.save(new HashUrl("1q2w3e4r", "http://www.yahoo.com"));
    }
}