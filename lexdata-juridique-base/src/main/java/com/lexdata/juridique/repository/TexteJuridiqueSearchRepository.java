package com.lexdata.juridique.repository;

import com.lexdata.juridique.models.TexteJuridiqueDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TexteJuridiqueSearchRepository extends ElasticsearchRepository<TexteJuridiqueDocument, String> {
}
