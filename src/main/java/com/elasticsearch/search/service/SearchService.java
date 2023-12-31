package com.elasticsearch.search.service;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.elasticsearch.search.api.model.Result;
import com.elasticsearch.search.domain.EsClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.*;

@Service
public class SearchService {

    private final EsClient esClient;

    public SearchService(EsClient esClient) {
        this.esClient = esClient;
    }

    public List<Result> submitQuery(String query, Integer page) {
        if(isNull(query) || query.isBlank()){
            return new ArrayList<Result>();
        }
        if(isNull(page) || page <= 0){
            page = 1;
        }

        var searchResponse = esClient.search(query, page);

        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();

        var resultsList = hits
            .stream()
            .map(
                h ->
                    new Result()
                        .abs(treatContent(h.highlight().get("content").get(0)))
                        .title(h.source().get("title").asText())
                        .url(h.source().get("url").asText())
                ).collect(Collectors.toList());
        return resultsList;
    }

    private String treatContent(String content) {
        content = content.replaceAll("</?(som|math)\\d*>", " ");
        content = content.replaceAll("[^A-Za-z\\s</>]+", " ");
        content = content.replaceAll("\\s+", " ");
        content = content.replaceAll("^\\s+", "");
        return content;
    }
}
