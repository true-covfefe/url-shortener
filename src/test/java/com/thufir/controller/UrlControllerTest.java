package com.thufir.controller;

import com.google.common.collect.ImmutableMap;
import com.thufir.UrlshortenerApplication;
import com.thufir.entity.HashUrl;
import com.thufir.repository.HashUrlRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.thufir.utils.TestUtils.getSampleUrls;
import static com.thufir.utils.TestUtils.hasher;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UrlshortenerApplication.class)
@WebAppConfiguration
public class UrlControllerTest {
    @Autowired
    private WebApplicationContext webContext;

    @Autowired
    private HashUrlRepository repo;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webContext)
            .build();
        repo.deleteAll();
    }

    @Test
    public void postUrl() throws Exception {
        //Given
        ImmutableMap<String, String> urlToHashMap = hashUrlMapBuilder(getSampleUrls());

        urlToHashMap.keySet().forEach(k -> {
            try {
                //When
                MvcResult result = mockMvc.perform(post("/")
                    .content(k)
                    .contentType(TEXT_PLAIN))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
                //Then
                Assert.assertTrue(result.getResponse().getContentAsString().contains("/url/" + urlToHashMap.get(k)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void getWithExistingUrl() throws Exception {
        //Given
        ImmutableMap<String, String> urlToHashMap = hashUrlMapBuilder(getSampleUrls());

        urlToHashMap.keySet().forEach(url -> {
            final String hash = urlToHashMap.get(url);
            repo.save(new HashUrl(urlToHashMap.get(url), url));
            try {
                //When
                mockMvc.perform(get("/url/" + hash))
                    //Then
                    .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ImmutableMap<String, String> hashUrlMapBuilder(List<String> urls) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        urls.stream().forEach(url -> builder.put(url, hasher(url)));
        return builder.build();
    }
}
