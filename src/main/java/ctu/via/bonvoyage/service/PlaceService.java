package ctu.via.bonvoyage.service;

import com.github.dozermapper.core.Mapper;
import ctu.via.bonvoyage.interfaces.entity.PlaceEntity;
import ctu.via.bonvoyage.interfaces.repository.PlaceRepository;
import ctu.via.bonvoyage.interfaces.response.api.PlaceApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpServerErrorException;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ApiCommunicationService apiCommunication;
    private final Mapper mapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaceService.class);

    public PlaceService(@NotNull @Autowired PlaceRepository placeRepository,
                        @NotNull @Autowired ApiCommunicationService apiCommunication,
                        @NotNull @Autowired Mapper mapper) {
        this.placeRepository = placeRepository;
        this.apiCommunication = apiCommunication;
        this.mapper = mapper;
    }

    public PlaceEntity getInfoByPlaceName(@NonNull String placeName,
                                          @NotNull String latCity, @NotNull String lngCity){
        LOGGER.debug("getInfoByPlaceName {} {} {}", placeName, latCity, lngCity);
        Assert.notNull(placeName, "placeName cannot be null!");
        PlaceApiResponse placeApiResponse;

        try {
            CompletableFuture<PlaceApiResponse> completableFuture = apiCommunication
                    .callApiForPlaceInfoDiscover(placeName, latCity, lngCity);
            placeApiResponse = completableFuture.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e){
            LOGGER.debug("getInfoByPlaceNameError {} {} {}", placeName, latCity, lngCity, e);
            throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "HERE Discover API call failed!");
        }

        List<PlaceEntity> places = prepareResponse(placeApiResponse);
        if (!places.isEmpty()){
            return placeRepository.save(places.get(0));
        }

        return null;
    }

    public List<PlaceEntity> getInfoByCategory(@NotNull String category,
                                               @NotNull String latCity, @NotNull String lngCity) {
        LOGGER.debug("getInfoByCategory {} {} {}", category, latCity, lngCity);
        Assert.notNull(category, "category cannot be null!");
        Assert.notNull(latCity, "latCity cannot be null!");
        Assert.notNull(lngCity, "lngCity cannot be null!");
        PlaceApiResponse placeApiResponse;

        try {
            CompletableFuture<PlaceApiResponse> completableFuture = apiCommunication
                    .callApiForPlaceInfoBrowse(category, latCity, lngCity);
            placeApiResponse = completableFuture.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e){
            LOGGER.debug("getInfoByCategoryError {} {} {} {}", category, latCity, lngCity, e);
            throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "HERE Browse API call failed!");
        }

        List<PlaceEntity> places = prepareResponse(placeApiResponse);
        if (!places.isEmpty()){
            return placeRepository.saveAll(places);
        }

        return null;
    }

    private List<PlaceEntity> prepareResponse(@NotNull PlaceApiResponse placeApiResponse){
        List<PlaceEntity> result = new ArrayList<>();

        if (placeApiResponse != null && placeApiResponse.getItems() != null){
            for (PlaceApiResponse.Item place : placeApiResponse.getItems()){
                result.add(mapper.map(place, PlaceEntity.class));
            }
        }

        return result;
    }

    public void deletePlaces(List<PlaceEntity> placeEntities){
        LOGGER.debug("deletePlaces {}", placeEntities);

        for (PlaceEntity placeEntity : placeEntities){
            placeRepository.delete(placeEntity);
        }
    }

}
