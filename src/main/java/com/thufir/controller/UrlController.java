package com.thufir.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.thufir.entity.HashUrl;
import com.thufir.repository.HashUrlRepository;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@ComponentScan
public class UrlController {

    private final Cache<String, String> cache;
    private final HashUrlRepository repository;
    private static final Integer CACHE_MAX_SIZE = 1000;
    private static final Integer CACHE_VALID_FOR = 30;
    private static final Logger LOG = getLogger(UrlController.class);

    @Autowired
    public UrlController(final HashUrlRepository repository) {
        this.repository = repository;

        cache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterWrite(CACHE_VALID_FOR, TimeUnit.MINUTES)
            .recordStats()
            .build();
        //Fetch latest items in the size of CACHE_LIMIT from DB and populate the cache
        cache.putAll(repository.findAll(new PageRequest(0, CACHE_MAX_SIZE, new Sort(new Sort.Order(Sort.Direction.DESC, "id"))))
            .getContent().stream().collect(Collectors.toMap(HashUrl::getHash, HashUrl::getUrl)));
    }

    @RequestMapping(value = "/url/{code}", method = RequestMethod.GET)
    public void redirect(@PathVariable String code, HttpServletResponse response) throws Exception {
        Optional<String> optionalUrl = Optional.ofNullable(cache.getIfPresent(code));
        if (optionalUrl.isPresent()) {
            LOG.info("Input shortUrl {} found in cache", code);
            response.sendRedirect(optionalUrl.get());
            return;
        }

        Optional<HashUrl> optionalHashUrl = Optional.ofNullable(repository.findByHash(code));
        if (optionalHashUrl.isPresent()) {
            String url = optionalHashUrl.get().getUrl();
            LOG.info("Input shortUrl {} found in db. Adding to cache", code);
            cache.put(code, url);
            response.sendRedirect(url);
        } else {
            LOG.info("Input shortUrl {} is not valid", code);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> save(@RequestBody String url, HttpServletRequest req) {
        String baseUrl = req.getRequestURL().toString();
        UrlValidator validator = new UrlValidator(new String[]{"http", "https", "ftp"});

        if (validator.isValid(url)) {
            final String hash = hasher(url);
            //Check cache first! If present and it's the same url, return fast without involving repo
            Optional<String> optionalUrl = Optional.ofNullable(cache.getIfPresent(hash));
            if (optionalUrl.isPresent() && optionalUrl.get().equals(url)) {
                LOG.info("Input url {} found in cache", url);
            } else {
                Optional<HashUrl> optionalHashUrl = Optional.ofNullable(repository.findByHash(hash));
                //If present yet we got the same hash for a different url from the repo (very unlikely yet possible)
                if (optionalHashUrl.isPresent() && !optionalHashUrl.get().equals(new HashUrl(hash, url))) {
                    //Delete the present entry in the db with the same hash. LRU philosophy, no country for old entry.
                    LOG.info("Another url {} with same hash {} found in db. Deleting! ", optionalHashUrl.get().getUrl(), hash);
                    repository.delete(optionalHashUrl.get().getId());
                }
                repository.save(new HashUrl(hash, url));
                cache.put(hash, url);
            }
            return new ResponseEntity<>(baseUrl + "url/" + hash, HttpStatus.OK);
        } else {
            LOG.info("Input url {} is not valid", url);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String hasher(String url) {
        return Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
    }
}
