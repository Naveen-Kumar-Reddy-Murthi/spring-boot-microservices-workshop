package io.javabrains.moviecatalogservice.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@EnableCircuitBreaker
public class UserRatingService {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallBackUserRating",
            threadPoolKey = "userRatingInfoPool",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "20"),
                    @HystrixProperty(name = "maxQueueSize", value = "10")
            },
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000"),
                    //timeout for actual method before turning to fallback method
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
                    //no of requests hystrix needs to see before deciding to call fallback method
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    //out of {circuitBreaker.requestVolumeThreshold} value, how many should fail for deciding to open the circuit
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")
                    //how long circuit breaker should sleep
            })
    public UserRating getForObject(String userId) {
        return restTemplate.getForObject("http://ratings-data-service/ratingsdata/user/" + userId, UserRating.class);
    }


    @HystrixCommand(fallbackMethod = "getFallBackUserRating")
    private UserRating getFallBackUserRating(String userId) {
        UserRating userRating = new UserRating();
        userRating.setUserId(userId);
        userRating.setRatings(Arrays.asList(
                new Rating("1", 0)
        ));
        return userRating;
    }
}
